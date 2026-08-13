package com.sportsequipment.dto.mq;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 未支付订单延迟检查事件：
 * 投递到 order.delay.queue，TTL 到期后路由到 DLX，order.cancel.queue 的消费者
 * 取出该消息后检查订单状态，若仍为 PENDING 则自动取消并归还库存。
 */
public class OrderPendingTimeoutEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long userId;
    /** 下单时刻，便于日志核对延迟窗口 */
    private LocalDateTime createdAt;
    /** TTL 毫秒（调试/日志用） */
    private long ttlMs;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getTtlMs() { return ttlMs; }
    public void setTtlMs(long ttlMs) { this.ttlMs = ttlMs; }
}
