package com.sportsequipment.service.impl;

import com.sportsequipment.dto.CartDTO;
import com.sportsequipment.dto.CartItemDTO;
import com.sportsequipment.entity.Cart;
import com.sportsequipment.entity.CartItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.CartMapper;
import com.sportsequipment.mapper.CartItemMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.mapper.UserMapper;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 确保userDetails.getId()不为null
        Long userId = userDetails.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return user;
    }

    /**
     * 获取或创建当前用户的购物车
     */
    private Cart getOrCreateCart() {
        User user = getCurrentUser();
        // 检查购物车是否存在
        Cart cart = cartMapper.findByUserId(user.getId());

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCreatedAt(java.time.LocalDateTime.now());
            cart.setUpdatedAt(java.time.LocalDateTime.now());
            cartMapper.insert(cart);
        }

        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCurrentUserCart() {
        Cart cart = getOrCreateCart();
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart();
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        // 检查购物车中是否已存在该商品，确保cart.getId()不为null
        Long cartId = cart.getId();
        if (cartId == null) {
            throw new IllegalStateException("Cart ID cannot be null");
        }
        CartItem existingCartItem = cartItemMapper.findByCartIdAndProductId(cartId, productId);

        if (existingCartItem != null) {
            // 更新已有商品的数量
            int newQuantity = existingCartItem.getQuantity() + quantity;

            // 再次检查库存
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            existingCartItem.setQuantity(newQuantity);
            existingCartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemMapper.update(existingCartItem);
        } else {
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            cartItem.setCreatedAt(java.time.LocalDateTime.now());
            cartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemMapper.insert(cartItem);
        }

        cart.setUpdatedAt(java.time.LocalDateTime.now());
        cartMapper.update(cart);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart();
        CartItem cartItem = cartItemMapper.findById(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        // 检查购物车项是否属于当前用户的购物车，确保ID不为null
        Long cartItemCartId = cartItem.getCart().getId();
        Long currentCartId = cart.getId();
        if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
            throw new SecurityException("Access denied");
        }

        // 检查库存
        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(java.time.LocalDateTime.now());
        cartItemMapper.update(cartItem);

        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO removeFromCart(Long cartItemId) {
        Cart cart = getOrCreateCart();
        CartItem cartItem = cartItemMapper.findById(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        // 检查购物车项是否属于当前用户的购物车，确保ID不为null
        Long cartItemCartId = cartItem.getCart().getId();
        Long currentCartId = cart.getId();
        if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
            throw new SecurityException("Access denied");
        }

        cartItemMapper.deleteById(cartItemId);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cartItemMapper.deleteByCartId(cart.getId());
        cart.setUpdatedAt(java.time.LocalDateTime.now());
        cartMapper.update(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount() {
        Cart cart = getOrCreateCart();
        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * 将Cart实体转换为CartDTO
     */
    private CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());
        cartDTO.setUsername(cart.getUser().getUsername());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());

        cartDTO.setCartItems(cartItemDTOs);

        return cartDTO;
    }

    /**
     * 将CartItem实体转换为CartItemDTO
     */
    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setProductName(cartItem.getProduct().getName());
        cartItemDTO.setImageUrl(cartItem.getProduct().getImageUrl());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getPrice());
        cartItemDTO.setItemTotal(cartItem.getItemTotal());

        return cartItemDTO;
    }
}