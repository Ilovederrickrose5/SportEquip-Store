package com.sportsequipment.service.impl;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.OrderItemDTO;
import com.sportsequipment.dto.PageResponse;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.mapper.OrderItemMapper;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.mapper.UserMapper;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.OrderService;
import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
            UserMapper userMapper, ProductMapper productMapper,
            RedisUtil redisUtil) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
    }

    // 订单列表缓存键前缀
    private static final String ORDER_LIST_CACHE_PREFIX = "order:list:";
    // 订单列表缓存基础过期时间（8分钟） + 随机偏移（0~4分钟） = 8~12分钟随机过期，防止缓存雪崩
    private static final int ORDER_LIST_CACHE_BASE_MINUTES = 8;
    private static final int ORDER_LIST_CACHE_RANDOM_MINUTES = 4;

    /**
     * 生成随机过期时间（单位：分钟），用于防止缓存雪崩
     *
     * @param baseMinutes   基础过期时间
     * @param randomMinutes 随机偏移范围
     * @return 最终过期时间
     */
    private int getRandomExpireMinutes(int baseMinutes, int randomMinutes) {
        return baseMinutes + (int) (Math.random() * randomMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderMapper.findAll().stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getCurrentUserOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return orderMapper.findByUserId(userDetails.getId()).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        // 检查权限
        if (order.getUserId() == null) {
            throw new IllegalStateException("Order user cannot be null");
        }
        checkOrderAccess(order.getUserId());

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Long userId = userDetails.getId();
        if (userId == null) {
            throw new IllegalStateException("User ID cannot be null");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setUserId(userId);
        // 设置支付方式
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        // 根据是否选择了支付方式设置订单状态：选择了支付方式则为已支付(PAID)，否则为待支付(PENDING)
        if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().isEmpty()) {
            order.setStatus("PAID");
        } else {
            order.setStatus("PENDING");
        }
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPhone(orderDTO.getPhone());
        // 设置收货人姓名，如果DTO中没有提供，则使用用户名
        order.setRecipientName(orderDTO.getRecipientName() != null ? orderDTO.getRecipientName() : user.getUsername());
        // 设置订单备注
        order.setRemark(orderDTO.getRemark());
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setUpdatedAt(java.time.LocalDateTime.now());

        // 使用数组来存储可变的BigDecimal值
        BigDecimal[] totalAmountWrapper = { BigDecimal.ZERO };

        // 创建订单项
        List<OrderItemDTO> dtoItems = orderDTO.getOrderItems();
        if (dtoItems == null || dtoItems.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one order item");
        }

        // 先计算总金额并验证库存
        for (OrderItemDTO itemDTO : dtoItems) {
            Long productId = itemDTO.getProductId();
            if (productId == null) {
                throw new IllegalStateException("Product ID cannot be null");
            }
            Product product = productMapper.findById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }

            // 检查库存
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // 累加总金额
            totalAmountWrapper[0] = totalAmountWrapper[0]
                    .add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // 设置总金额后再插入订单
        order.setTotalAmount(totalAmountWrapper[0]);
        orderMapper.insert(order);

        // 再插入订单项并更新库存（使用分布式锁防止并发超卖）
        for (OrderItemDTO itemDTO : dtoItems) {
            Long productId = itemDTO.getProductId();

            // 分布式锁，防止并发扣库存导致超卖
            String lockKey = "lock:product:" + productId;
            try {
                boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new RuntimeException("系统繁忙，请稍后再试");
                }

                // 再次检查库存（双重检查）
                Product product = productMapper.findById(productId);
                if (product == null) {
                    throw new ResourceNotFoundException("Product not found with id: " + productId);
                }

                if (product.getStock() < itemDTO.getQuantity()) {
                    throw new IllegalArgumentException("库存不足：" + product.getName() + "（剩余：" + product.getStock() + "）");
                }

                // 减少库存
                product.setStock(product.getStock() - itemDTO.getQuantity());
                product.setUpdatedAt(java.time.LocalDateTime.now());
                productMapper.update(product);

                // 清除商品缓存，保证缓存一致性
                redisUtil.delete("product:detail::" + productId);

                // 创建订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setOrderId(order.getId());
                orderItem.setProduct(product);
                orderItem.setProductId(productId);
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setPrice(product.getPrice());

                // 插入订单项
                orderItemMapper.insert(orderItem);

            } finally {
                redisUtil.unlock(lockKey);
            }
        }

        // 清除订单列表缓存，保证缓存一致性
        clearOrderListCache(userId);

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        // 验证参数是否为空
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        // 验证状态是否有效
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderMapper.update(order);

        // 清除订单列表缓存，保证缓存一致性
        clearOrderListCache(order.getUserId());

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        // 验证参数是否为空
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        // 如果订单不是待处理状态，则不允许删除
        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Cannot delete order with status: " + order.getStatus());
        }

        orderMapper.deleteById(id);

        // 清除订单列表缓存，保证缓存一致性
        clearOrderListCache(order.getUserId());
    }

    // 检查订单访问权限
    private void checkOrderAccess(Long orderUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 只有管理员或订单所有者可以访问
        if (!userDetails.getRole().equals("ADMIN") && !userDetails.getId().equals(orderUserId)) {
            throw new UnauthorizedException("您无权访问该订单");
        }
    }

    // 验证订单状态是否有效
    private boolean isValidStatus(String status) {
        return status.equals("PENDING") || status.equals("PAID") ||
                status.equals("SHIPPED") || status.equals("DELIVERED") ||
                status.equals("CANCELLED");
    }

    // 转换实体到DTO
    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        // 直接使用 userId 字段，避免 getUser() 为 null 的问题
        orderDTO.setUserId(order.getUserId());
        // 从当前用户获取 username，避免依赖 order.getUser()
        orderDTO.setUsername(getCurrentUsername());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setShippingAddress(order.getShippingAddress());
        // 设置address字段，使其值与shippingAddress相同，以匹配前端使用的字段名
        orderDTO.setAddress(order.getShippingAddress());
        orderDTO.setPhone(order.getPhone());
        // 从订单实体中获取支付方式
        orderDTO.setPaymentMethod(order.getPaymentMethod());
        // 从订单实体中获取收货人姓名，如果为null则使用当前用户名
        orderDTO.setRecipientName(
                order.getRecipientName() != null ? order.getRecipientName() : getCurrentUsername());
        // 从订单实体中获取订单备注
        orderDTO.setRemark(order.getRemark());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setUpdatedAt(order.getUpdatedAt());

        orderDTO.setOrderItems(
                order.getOrderItems().stream()
                        .map(this::mapToOrderItemDTO)
                        .collect(Collectors.toList()));

        return orderDTO;
    }

    // 获取当前用户的用户名
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    private OrderItemDTO mapToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(orderItem.getId());
        // 直接使用 orderId 和 productId 字段，避免关联对象为 null 的问题
        orderItemDTO.setOrderId(orderItem.getOrderId());
        orderItemDTO.setProductId(orderItem.getProductId());

        // 查询商品名称
        Product product = productMapper.findById(orderItem.getProductId());
        orderItemDTO.setProductName(product != null ? product.getName() : "未知商品");

        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        return orderItemDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getCurrentUserOrdersWithPagination(String status, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 构建缓存key
        String cacheKey = ORDER_LIST_CACHE_PREFIX + "user:" + userId + ":status:" + (status != null ? status : "all")
                + ":page:" + page + ":size:" + size;

        // 先从缓存读取
        @SuppressWarnings("unchecked")
        PageResponse<OrderDTO> cached = redisUtil.get(cacheKey, PageResponse.class);
        if (cached != null) {
            logger.debug("从缓存获取订单列表，userId={}, page={}", userId, page);
            return cached;
        }

        // 计算偏移量
        int offset = page * size;

        // 查询订单列表（分页，不含订单项）
        List<Order> orders = orderMapper.findByUserIdWithPagination(userId, status, offset, size);

        // 查询订单总数
        int totalElements = orderMapper.countByUserId(userId, status);

        // 转换为DTO（列表页不需要订单项详情）
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::mapToOrderDTOWithoutItems)
                .collect(Collectors.toList());

        // 构建分页响应
        PageResponse<OrderDTO> pageResponse = new PageResponse<>(orderDTOs, page, size, totalElements);

        // 缓存结果，8~12分钟随机过期，防止缓存雪崩
        int expireMinutes = getRandomExpireMinutes(ORDER_LIST_CACHE_BASE_MINUTES, ORDER_LIST_CACHE_RANDOM_MINUTES);
        redisUtil.set(cacheKey, pageResponse, expireMinutes, TimeUnit.MINUTES);

        logger.debug("从数据库查询订单列表，userId={}, page={}, total={}", userId, page, totalElements);
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getAllOrdersWithPagination(String status, int page, int size) {
        // 构建缓存key
        String cacheKey = ORDER_LIST_CACHE_PREFIX + "admin:status:" + (status != null ? status : "all")
                + ":page:" + page + ":size:" + size;

        // 先从缓存读取
        @SuppressWarnings("unchecked")
        PageResponse<OrderDTO> cached = redisUtil.get(cacheKey, PageResponse.class);
        if (cached != null) {
            logger.debug("从缓存获取管理员订单列表，page={}", page);
            return cached;
        }

        // 计算偏移量
        int offset = page * size;

        // 查询订单列表（分页，不含订单项）
        List<Order> orders = orderMapper.findAllWithPagination(status, offset, size);

        // 查询订单总数
        int totalElements = orderMapper.countAll(status);

        // 转换为DTO（列表页不需要订单项详情）
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::mapToOrderDTOWithoutItems)
                .collect(Collectors.toList());

        // 构建分页响应
        PageResponse<OrderDTO> pageResponse = new PageResponse<>(orderDTOs, page, size, totalElements);

        // 缓存结果，8~12分钟随机过期，防止缓存雪崩
        int expireMinutes = getRandomExpireMinutes(ORDER_LIST_CACHE_BASE_MINUTES, ORDER_LIST_CACHE_RANDOM_MINUTES);
        redisUtil.set(cacheKey, pageResponse, expireMinutes, TimeUnit.MINUTES);

        logger.debug("从数据库查询管理员订单列表，page={}, total={}", page, totalElements);
        return pageResponse;
    }

    /**
     * 将订单实体转换为DTO（不含订单项，用于列表页）
     * 列表页只需要订单基本信息，不需要订单项详情
     * 避免N+1查询问题，提升列表页性能
     */
    private OrderDTO mapToOrderDTOWithoutItems(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setUsername(getCurrentUsername());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setShippingAddress(order.getShippingAddress());
        orderDTO.setAddress(order.getShippingAddress());
        orderDTO.setPhone(order.getPhone());
        orderDTO.setPaymentMethod(order.getPaymentMethod());
        orderDTO.setRecipientName(order.getRecipientName() != null ? order.getRecipientName() : getCurrentUsername());
        orderDTO.setRemark(order.getRemark());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setUpdatedAt(order.getUpdatedAt());

        // 列表页不设置订单项，避免N+1查询
        orderDTO.setOrderItems(Collections.emptyList());

        return orderDTO;
    }

    /**
     * 清除订单列表缓存（订单创建/更新/删除时调用）
     */
    private void clearOrderListCache(Long userId) {
        // 清除用户订单列表缓存
        redisUtil.deletePattern(ORDER_LIST_CACHE_PREFIX + "user:" + userId + ":*");
        // 清除管理员订单列表缓存
        redisUtil.deletePattern(ORDER_LIST_CACHE_PREFIX + "admin:*");
    }
}
