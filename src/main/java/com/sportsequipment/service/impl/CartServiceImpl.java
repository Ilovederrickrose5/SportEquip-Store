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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    public CartServiceImpl(CartMapper cartMapper, CartItemMapper cartItemMapper,
            UserMapper userMapper, ProductMapper productMapper,
            RedisUtil redisUtil) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
    }

    // 购物车缓存键前缀
    private static final String CART_CACHE_KEY_PREFIX = "cart:user:";

    // 缓存过期时间配置（防止雪崩）
    // 基础过期时间24小时 + 随机偏移量4小时 = 20~28小时随机过期
    private static final int CACHE_BASE_EXPIRE_HOURS = 24;
    private static final int CACHE_RANDOM_OFFSET_HOURS = 4;

    // 计算随机过期时间（20~28小时）
    private int getRandomCacheExpireHours() {
        return CACHE_BASE_EXPIRE_HOURS + (int) (Math.random() * CACHE_RANDOM_OFFSET_HOURS);
    }

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
            cart.setUserId(user.getId());
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

        // 先清除旧缓存，确保获取最新数据
        redisUtil.delete(cacheKey);

        Cart cart = getOrCreateCart();
        CartDTO cartDTO = mapToCartDTO(cart);
        redisUtil.set(cacheKey, cartDTO, getRandomCacheExpireHours(), TimeUnit.HOURS);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = getCurrentUser();
        // 细粒度锁，锁类型：用户 + 商品 ID 复合细粒度锁，用于用户添加商品到购物车时的并发操作。
        String lockKey = "cart:lock:" + user.getId() + ":product:" + productId;

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
                cartItem.setCartId(cart.getId());
                cartItem.setProduct(product);
                cartItem.setProductId(productId);
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
            redisUtil.set(cacheKey, cartDTO, getRandomCacheExpireHours(), TimeUnit.HOURS);

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
        // 细粒度锁，锁类型：用户 + 购物项 ID 复合细粒度锁，用于用户更新购物车项时的并发操作。
        String lockKey = "cart:lock:" + user.getId() + ":item:" + cartItemId;

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

            // 直接使用 cartId 字段，避免 getCart() 为 null 的问题
            Long cartItemCartId = cartItem.getCartId();
            Long currentCartId = cart.getId();
            if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
                throw new SecurityException("Access denied");
            }

            // 查询商品信息
            Product product = productMapper.findById(cartItem.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + cartItem.getProductId());
            }
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemMapper.update(cartItem);

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            CartDTO cartDTO = mapToCartDTO(cart);
            redisUtil.set(cacheKey, cartDTO, getRandomCacheExpireHours(), TimeUnit.HOURS);

            return cartDTO;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public CartDTO removeFromCart(Long cartItemId) {
        User user = getCurrentUser();
        // 细粒度锁，锁类型：用户 + 购物项 ID 复合细粒度锁，用于用户从购物车中移除商品时的并发操作。
        String lockKey = "cart:lock:" + user.getId() + ":item:" + cartItemId;

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

            // 直接使用 cartId 字段，避免 getCart() 为 null 的问题
            Long cartItemCartId = cartItem.getCartId();
            Long currentCartId = cart.getId();
            if (cartItemCartId == null || currentCartId == null || !cartItemCartId.equals(currentCartId)) {
                throw new SecurityException("Access denied");
            }

            cartItemMapper.deleteById(cartItemId);

            String cacheKey = CART_CACHE_KEY_PREFIX + user.getId();
            CartDTO cartDTO = mapToCartDTO(cart);
            redisUtil.set(cacheKey, cartDTO, getRandomCacheExpireHours(), TimeUnit.HOURS);

            return cartDTO;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        // 粗粒度锁，锁类型：用户 ID，用于用户清除购物车时的并发操作。
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
        // 直接使用 userId 字段，避免 getUser() 为 null 的问题
        cartDTO.setUserId(cart.getUserId());
        // 从当前用户获取 username，避免依赖 cart.getUser()
        cartDTO.setUsername(getCurrentUser().getUsername());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setUpdatedAt(cart.getUpdatedAt());

        // 手动从数据库查询 cartItems
        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());
        List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());

        cartDTO.setCartItems(cartItemDTOs);

        return cartDTO;
    }

    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        // 直接使用 productId 字段，避免 getProduct() 为 null 的问题
        cartItemDTO.setProductId(cartItem.getProductId());

        // 查询商品信息
        Product product = productMapper.findById(cartItem.getProductId());
        if (product != null) {
            cartItemDTO.setProductName(product.getName());
            cartItemDTO.setImageUrl(product.getImageUrl());
        } else {
            cartItemDTO.setProductName("未知商品");
            cartItemDTO.setImageUrl(null);
        }

        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getPrice());
        // 计算单项总价
        if (cartItem.getPrice() != null && cartItem.getQuantity() != null) {
            cartItemDTO.setItemTotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        return cartItemDTO;
    }
}
