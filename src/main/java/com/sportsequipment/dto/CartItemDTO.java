package com.sportsequipment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车项数据传输对象
 */
@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal itemTotal;
}