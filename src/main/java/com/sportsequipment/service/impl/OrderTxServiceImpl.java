package com.sportsequipment.service.impl;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.OrderItemDTO;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.OrderItemMapper;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.service.OrderTxService;
import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单事务服务实现：只做事务内的纯写操作，不碰分布式锁。
 * —— 调用方有义务保证进入本方法前，所有涉及商品的分布式锁都已拿齐。
 */
@Service
public class OrderTxServiceImpl implements OrderTxService {

    private static final Logger log = LoggerFactory.getLogger(OrderTxServiceImpl.class);

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    public OrderTxServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                              ProductMapper productMapper, RedisUtil redisUtil) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
    }

    @Override
    @Transactional
    public Order doCreateOrderInTx(OrderDTO orderDTO, List<OrderItemDTO> dtoItems,
                                   BigDecimal totalAmount, User user) {

        Long userId = user.getId();

        // ================ 1. 生成订单主表（必须先拿 order_id 给订单项外键用）================
        Order order = new Order();
        order.setUser(user);
        order.setUserId(userId);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        // 有支付方式 = PAID（当前前端带 paymentMethod 走这个分支），否则 = PENDING
        if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().isEmpty()) {
            order.setStatus("PAID");
        } else {
            order.setStatus("PENDING");
        }
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPhone(orderDTO.getPhone());
        order.setRecipientName(orderDTO.getRecipientName() != null ? orderDTO.getRecipientName() : user.getUsername());
        order.setRemark(orderDTO.getRemark());
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setUpdatedAt(java.time.LocalDateTime.now());

        orderMapper.insert(order);
        if (order.getId() == null) {
            throw new IllegalStateException("订单创建失败：orderMapper.insert 执行后 order.getId() 为 null，" +
                    "请确认 OrderMapper.xml <insert> 配置了 useGeneratedKeys=\"true\" keyProperty=\"id\"，且 order 表 id 为 AUTO_INCREMENT");
        }
        log.debug("[OrderTxService] 订单主表插入成功 orderId={}", order.getId());

        // ================ 2. 二次库存检查 + 扣库存 + 生成订单项（锁已在外面拿齐，这里不再加锁）================
        for (OrderItemDTO itemDTO : dtoItems) {
            Long productId = itemDTO.getProductId();

            // 2.1 二次库存检查（拿锁之后再查一遍，防止预检→拿锁间隙被别人扣完，防超卖核心）
            Product product = productMapper.findById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("库存不足：" + product.getName() + "（剩余：" + product.getStock() + "）");
            }

            // 2.2 扣库存（原子 SQL 配合锁外 MySQL 行锁，双重保险）
            product.setStock(product.getStock() - itemDTO.getQuantity());
            product.setUpdatedAt(java.time.LocalDateTime.now());
            productMapper.update(product);

            // 2.3 同步删商品详情缓存，保证一致性（锁内+事务内执行，不会出现"缓存删了、db 还没写"的空窗）
            redisUtil.delete("product:detail::" + productId);

            // 2.4 生成订单项，order_id 用上面回写的自增主键
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setOrderId(order.getId());
            orderItem.setProduct(product);
            orderItem.setProductId(productId);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItemMapper.insert(orderItem);
        }

        return order;
    }
}
