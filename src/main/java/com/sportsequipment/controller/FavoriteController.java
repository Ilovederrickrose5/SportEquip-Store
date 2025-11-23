package com.sportsequipment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Favorite;
import com.sportsequipment.service.FavoriteService;

import java.util.List;

/**
 * 收藏控制器，处理用户收藏相关操作
 * @author system
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // 添加商品到收藏
    @PostMapping("/products/{productId}")
    public ResponseEntity<Favorite> addToFavorite(@PathVariable Long productId) {
        Favorite favorite = favoriteService.addToFavorite(productId);
        return ResponseEntity.ok(favorite);
    }

    // 从收藏中移除商品
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> removeFromFavorite(@PathVariable Long productId) {
        favoriteService.removeFromFavorite(productId);
        return ResponseEntity.noContent().build();
    }

    // 获取当前用户的所有收藏商品
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getFavoriteProducts() {
        List<ProductDTO> favoriteProducts = favoriteService.getFavoriteProducts();
        return ResponseEntity.ok(favoriteProducts);
    }

    // 检查商品是否已收藏
    @GetMapping("/products/{productId}/check")
    public ResponseEntity<Boolean> checkFavorite(@PathVariable Long productId) {
        boolean isFavorite = favoriteService.isProductFavorite(productId);
        return ResponseEntity.ok(isFavorite);
    }
}