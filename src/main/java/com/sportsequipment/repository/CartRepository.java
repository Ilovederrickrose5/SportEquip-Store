package com.sportsequipment.repository;

import com.sportsequipment.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 购物车仓库接口
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * 根据用户ID查找购物车，使用JOIN FETCH一次性加载所有相关数据
     */
    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product WHERE c.user.id = :userId")
    Cart findByUserIdWithItemsAndProducts(Long userId);
    
    /**
     * 根据用户ID查找购物车（不加载关联数据）
     */
    Cart findByUserId(Long userId);
}