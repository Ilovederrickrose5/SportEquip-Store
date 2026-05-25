package com.sportsequipment.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项实体类
 */
@Getter
@Setter
public class CartItem {
    private Long id;

    private Long cartId;

    private Cart cart;

    private Long productId;

    private Product product;

    private Integer quantity;

    private BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.price = product.getPrice();
        }
    }

    public BigDecimal getItemTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}