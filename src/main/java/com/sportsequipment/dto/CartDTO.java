package com.sportsequipment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车数据传输对象
 */
@Getter
@Setter
public class CartDTO {
    private Long id;
    private Long userId;
    private String username;
    private List<CartItemDTO> cartItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}