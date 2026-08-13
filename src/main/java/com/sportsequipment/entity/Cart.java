package com.sportsequipment.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车实体类
 */
@Getter
@Setter
public class Cart {
    private Long id;

    private Long userId;

    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<CartItem> cartItems = new ArrayList<>();

    @SuppressWarnings("null")
    public BigDecimal getTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cartItems.stream()
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}