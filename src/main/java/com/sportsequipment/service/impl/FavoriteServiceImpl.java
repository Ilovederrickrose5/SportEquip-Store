package com.sportsequipment.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Favorite;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.repository.FavoriteRepository;
import com.sportsequipment.repository.ProductRepository;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.FavoriteService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Favorite addToFavorite(Long productId) {
        // 验证参数是否为空
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // 验证产品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("Product is already in favorites");
        }

        // 获取产品对象
        Product favoriteProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                
        // 创建User对象引用（不需要完全加载用户对象）
        User user = new User();
        user.setId(userId);

        // 创建收藏记录
        Favorite favorite = new Favorite(user, favoriteProduct);
        return favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFromFavorite(Long productId) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 检查是否存在收藏记录
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Favorite not found for product id: " + productId);
        }

        // 删除收藏记录
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getFavoriteProducts() {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 获取用户的所有收藏记录
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        // 获取收藏的产品信息并转换为DTO
        return favorites.stream()
                .map(favorite -> {
                    Product product = favorite.getProduct();
                    if (product == null) {
                        throw new ResourceNotFoundException("Product not found");
                    }
                    return mapToProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductFavorite(Long productId) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 检查产品是否在收藏列表中
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    // 转换实体到DTO
    private ProductDTO mapToProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setStock(product.getStock());
        productDTO.setDescription(product.getDescription());
        productDTO.setImageUrl(product.getImageUrl());
        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());

        return productDTO;
    }
}