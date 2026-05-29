package com.sportsequipment.service.impl;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.OrderItemDTO;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.mapper.UserMapper;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

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

        orderMapper.insert(order);

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

            // 减少库存
            product.setStock(product.getStock() - itemDTO.getQuantity());
            product.setUpdatedAt(java.time.LocalDateTime.now());
            productMapper.update(product);

            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());

            // 累加总金额
            totalAmountWrapper[0] = totalAmountWrapper[0]
                    .add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        order.setTotalAmount(totalAmountWrapper[0]);
        orderMapper.update(order);

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
        orderItemDTO.setOrderId(orderItem.getOrder().getId());
        orderItemDTO.setProductId(orderItem.getProduct().getId());
        orderItemDTO.setProductName(orderItem.getProduct().getName());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        return orderItemDTO;
    }
}
