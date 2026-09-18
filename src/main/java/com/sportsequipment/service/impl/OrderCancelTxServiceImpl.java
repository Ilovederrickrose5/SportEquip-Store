package com.sportsequipment.service.impl;

import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
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
 * 库存归还走 DB 原子自增（stock = stock + ?），即使外层锁提前过期也不会丢失更新。
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
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) {
                log.warn("[cancelTx] 订单项 orderItemId={} 数量异常 qty={}，跳过库存归还", item.getId(), qty);
                continue;
            }
            // 原子自增：UPDATE product SET stock = stock + ? WHERE id = ?
            // 避免读-改-写在并发取消/归还下的丢失更新；affected=0 说明商品已被删除
            int affected = productMapper.addStock(productId, qty);
            if (affected == 0) {
                log.warn("[cancelTx] 订单项 productId={} 已不存在，跳过库存归还", productId);
            }
        }

        // 2) 订单状态 → CANCELLED
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);
        // ↑ 方法返回后 Spring 切面才 commit —— 调用方此时才能进 finally 释放锁
    }
}
