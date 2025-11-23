package com.sportsequipment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    // 添加address字段以匹配前端使用的字段名
    private String address;
    private String phone;
    // 添加paymentMethod字段以显示支付方式
    private String paymentMethod;
    // 添加recipientName字段以显示收货人姓名
    private String recipientName;
    // 添加订单备注字段
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> orderItems;
}
    