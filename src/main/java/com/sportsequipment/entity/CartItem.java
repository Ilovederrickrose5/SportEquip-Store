package com.sportsequipment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车项实体类
 */
@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    // 当产品变更时更新价格
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.price = product.getPrice();
        }
    }

    // 计算商品总价
    public BigDecimal getItemTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}