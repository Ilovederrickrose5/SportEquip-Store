package com.sportsequipment.dto.mq;

import com.sportsequipment.dto.OrderItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 订单创建事件：用于异步清理缓存、后续可扩展发送短信/邮件/站内通知等。
 *
 * 字段选型说明：
 * - status 与旧字段 orderStatus 同时存在（setter 双向同步），兼容存量未迁移的调用方
 * - orderItems 类型直接使用业务 DTO OrderItemDTO，方便消费者拿到 productName/price 扩展使用
 * - 保留内部类 ItemLine 作为简单行结构（可选），避免引入新的破坏式变更
 */
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long userId;
    private String username;

    /** 订单状态：与 Order.status 一致（PENDING/PAID/...）。别名 orderStatus 字段通过 setter 镜像同步 */
    private String status;
    @Deprecated
    private String orderStatus;

    private BigDecimal totalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 订单项明细：直接复用 OrderItemDTO，消费者无需二次查表即可拿到商品名称/单价等信息 */
    private List<OrderItemDTO> orderItems = Collections.emptyList();

    /** 历史字段：保留以兼容未迁移的老代码序列化/反序列化 */
    @Deprecated
    private List<ItemLine> items = Collections.emptyList();

    public static class ItemLine implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long productId;
        private Integer quantity;

        public ItemLine() {
        }

        public ItemLine(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // ========== getter / setter ==========
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.orderStatus = status;
    }

    @Deprecated
    public String getOrderStatus() { return orderStatus != null ? orderStatus : status; }
    @Deprecated
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        this.status = orderStatus;
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems == null ? Collections.emptyList() : orderItems;
    }

    @Deprecated
    public List<ItemLine> getItems() { return items; }
    @Deprecated
    public void setItems(List<ItemLine> items) {
        this.items = items == null ? Collections.emptyList() : items;
    }
}
