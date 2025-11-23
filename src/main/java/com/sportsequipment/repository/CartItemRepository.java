package com.sportsequipment.repository;

import com.sportsequipment.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 购物车项仓库接口
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 根据购物车ID删除所有购物车项
     */
    void deleteByCartId(Long cartId);
    
    /**
     * 根据购物车ID和商品ID查找购物车项
     */
    CartItem findByCartIdAndProductId(Long cartId, Long productId);
}