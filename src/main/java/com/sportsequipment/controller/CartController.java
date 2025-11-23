package com.sportsequipment.controller;

import com.sportsequipment.dto.CartDTO;
import com.sportsequipment.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 获取当前用户的购物车
     */
    @GetMapping
    public ResponseEntity<CartDTO> getCurrentUserCart() {
        return ResponseEntity.ok(cartService.getCurrentUserCart());
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addToCart(productId, quantity));
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(cartItemId, quantity));
    }

    /**
     * 从购物车中移除商品
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeFromCart(cartItemId));
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取购物车商品总数
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        return ResponseEntity.ok(cartService.getCartItemCount());
    }
}