package com.sportsequipment.service;

import com.sportsequipment.dto.CartDTO;

/**
 * 购物车服务接口
 */
public interface CartService {
    
    /**
     * 获取当前用户的购物车
     */
    CartDTO getCurrentUserCart();
    
    /**
     * 添加商品到购物车
     * @param productId 商品ID
     * @param quantity 数量
     */
    CartDTO addToCart(Long productId, Integer quantity);
    
    /**
     * 更新购物车商品数量
     * @param cartItemId 购物车项ID
     * @param quantity 新数量
     */
    CartDTO updateCartItem(Long cartItemId, Integer quantity);
    
    /**
     * 从购物车中移除商品
     * @param cartItemId 购物车项ID
     */
    CartDTO removeFromCart(Long cartItemId);
    
    /**
     * 清空购物车
     */
    void clearCart();
    
    /**
     * 获取购物车商品总数
     */
    Integer getCartItemCount();
}