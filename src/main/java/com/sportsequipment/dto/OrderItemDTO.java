package com.sportsequipment.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单项数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
    