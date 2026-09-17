package com.sportsequipment.service.impl;

import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.service.OrderCancelTxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单取消事务服务实现：只做事务内的纯 DB 写操作，不碰分布式锁、不碰 Redis 缓存。
 * —— 调用方有义务保证进入本方法前，所有涉及商品的分布式锁都已拿齐。
 */
@Service
public class OrderCancelTxServiceImpl implements OrderCancelTxService {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelTxServiceImpl.class);

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderCancelTxServiceImpl(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void restoreStockAndMarkCancelledInTx(Order order, List<OrderItem> items) {
        // 1) 归还每个订单项的库存（DB 写，在事务内）
        for (OrderItem item : items) {
            Long productId = item.getProductId();
            Product product = productMapper.findById(productId);
            if (product == null) {
                log.warn("[cancelTx] 订单项 productId={} 已不存在，跳过库存归还", productId);
                continue;
            }
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            product.setStock(product.getStock() + qty);
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.update(product);
        }

        // 2) 订单状态 → CANCELLED
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);
        // ↑ 方法返回后 Spring 切面才 commit —— 调用方此时才能进 finally 释放锁
    }
}
