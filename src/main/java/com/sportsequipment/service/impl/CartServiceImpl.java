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
import com.sportsequipment.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisUtil redisUtil;

    private static final String CART_CACHE_KEY_PREFIX = "cart:user:";

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

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

    private Cart getOrCreateCart() {
        User user = getCurrentUser();
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
        User user = getCurrentUser();
        String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();

        Object cachedCart = redisUtil.get(cacheKey);
        if (cachedCart != null) {
            return (CartDTO) cachedCart;
        }

        Cart cart = getOrCreateCart();
        CartDTO cartDTO = mapToCartDTO(cart);
        redisUtil.set(cacheKey, cartDTO, 24, TimeUnit.HOURS);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = getCurrentUser();
        String lockKey = "cart:lock:" + user.getId();

        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("购物车操作频繁，请稍后再试");
            }

            Cart cart = getOrCreateCart();
            Product product = productMapper.findById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }

            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            Long cartId = cart.getId();
            if (cartId == null) {
                throw new IllegalStateException("Cart ID cannot be null");
            }
            CartItem existingCartItem = cartItemMapper.findByCartIdAndProductId(cartId, productId);

            if (existingCartItem != null) {
                int newQuantity = existingCartItem.getQuantity() + quantity;
                if (product.getStock() < newQuantity) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
                existingCartItem.setQuantity(newQuantity);
                existingCartItem.setUpdatedAt(java.time.LocalDateTime.now());
                cartItemMapper.update(existingCartItem);
            } else {
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

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            CartDTO cartDTO = mapToCartDTO(cart);
            redisUtil.set(cacheKey, cartDTO, 24, TimeUnit.HOURS);

            return cartDTO;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = getCurrentUser();
        String lockKey = "cart:lock:" + user.getId();

        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("购物车操作频繁，请稍后再试");
            }

            Cart cart = getOrCreateCart();
            CartItem cartItem = cartItemMapper.findById(cartItemId);
            if (cartItem == null) {
                throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
            }

            Long cartItemCartId = cartItem.getCart().getId();
            Long currentCartId = cart.getId();
            if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
                throw new SecurityException("Access denied");
            }

            Product product = cartItem.getProduct();
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemMapper.update(cartItem);

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            CartDTO cartDTO = mapToCartDTO(cart);
            redisUtil.set(cacheKey, cartDTO, 24, TimeUnit.HOURS);

            return cartDTO;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public CartDTO removeFromCart(Long cartItemId) {
        User user = getCurrentUser();
        String lockKey = "cart:lock:" + user.getId();

        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("购物车操作频繁，请稍后再试");
            }

            Cart cart = getOrCreateCart();
            CartItem cartItem = cartItemMapper.findById(cartItemId);
            if (cartItem == null) {
                throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
            }

            Long cartItemCartId = cartItem.getCart().getId();
            Long currentCartId = cart.getId();
            if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
                throw new SecurityException("Access denied");
            }

            cartItemMapper.deleteById(cartItemId);

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            CartDTO cartDTO = mapToCartDTO(cart);
            redisUtil.set(cacheKey, cartDTO, 24, TimeUnit.HOURS);

            return cartDTO;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        String lockKey = "cart:lock:" + user.getId();

        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("购物车操作频繁，请稍后再试");
            }

            Cart cart = getOrCreateCart();
            cartItemMapper.deleteByCartId(cart.getId());
            cart.setUpdatedAt(java.time.LocalDateTime.now());
            cartMapper.update(cart);

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            redisUtil.delete(cacheKey);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount() {
        Cart cart = getOrCreateCart();
        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

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

    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setProductName(cartItem.getProduct().getName());
        cartItemDTO.setImageUrl(cartItem.getProduct().getImageUrl());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getPrice());

        return cartItemDTO;
    }
}
