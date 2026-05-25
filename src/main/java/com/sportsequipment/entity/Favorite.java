package com.sportsequipment.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 收藏夹实体类
 * @author sports-equipment-team
 */
@Getter
@Setter
public class Favorite {
    private Long id;

    private Long userId;

    private Long productId;

    private LocalDateTime createdAt;

    private User user;

    private Product product;

    public Favorite() {
    }

    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
        if (user != null) {
            this.userId = user.getId();
        }
        if (product != null) {
            this.productId = product.getId();
        }
    }
}