package com.sportsequipment.service.impl;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.Product;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.MainCategoryMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.mapper.SubCategoryMapper;
import com.sportsequipment.mapper.ThirdCategoryMapper;
import com.sportsequipment.service.ProductService;
import com.sportsequipment.util.RedisUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ThirdCategoryMapper thirdCategoryMapper;
    private final SubCategoryMapper subCategoryMapper;
    private final MainCategoryMapper mainCategoryMapper;
    private final RedisUtil redisUtil;

    public ProductServiceImpl(ProductMapper productMapper, ThirdCategoryMapper thirdCategoryMapper,
            SubCategoryMapper subCategoryMapper, MainCategoryMapper mainCategoryMapper,
            RedisUtil redisUtil) {
        this.productMapper = productMapper;
        this.thirdCategoryMapper = thirdCategoryMapper;
        this.subCategoryMapper = subCategoryMapper;
        this.mainCategoryMapper = mainCategoryMapper;
        this.redisUtil = redisUtil;
    }

    // 搜索结果缓存键前缀（防止缓存穿透）
    private static final String SEARCH_CACHE_KEY_PREFIX = "product:search:";
    // 空值缓存基础过期时间（4分钟） + 随机偏移（0~2分钟） = 4~6分钟随机过期，防止雪崩
    private static final int EMPTY_CACHE_BASE_MINUTES = 4;
    private static final int EMPTY_CACHE_RANDOM_MINUTES = 2;
    // 搜索结果有值时缓存基础过期时间（25分钟） + 随机偏移（0~10分钟） = 25~35分钟随机过期，防止雪崩
    private static final int SEARCH_CACHE_BASE_MINUTES = 25;
    private static final int SEARCH_CACHE_RANDOM_MINUTES = 10;
    // 热门商品缓存键前缀
    private static final String HOT_PRODUCT_CACHE_PREFIX = "product:hot:";
    // 热门商品缓存基础过期时间（30分钟） + 随机偏移（0~30分钟） = 30~60分钟随机过期，防止雪崩
    private static final int HOT_PRODUCT_CACHE_BASE_MINUTES = 30;
    private static final int HOT_PRODUCT_CACHE_RANDOM_MINUTES = 30;
    // 随机推荐商品缓存键前缀
    private static final String RANDOM_PRODUCT_CACHE_PREFIX = "product:random:";
    // 随机推荐商品缓存基础过期时间（30分钟） + 随机偏移（0~30分钟） = 30~60分钟随机过期，防止雪崩
    private static final int RANDOM_PRODUCT_CACHE_BASE_MINUTES = 30;
    private static final int RANDOM_PRODUCT_CACHE_RANDOM_MINUTES = 30;
    // 商品分布式锁键前缀（防止缓存击穿）
    private static final String PRODUCT_LOCK_KEY_PREFIX = "lock:product:";

    /**
     * 生成随机过期时间（单位：分钟），用于防止缓存雪崩
     *
     * @param baseMinutes   基础过期时间
     * @param randomMinutes 随机偏移范围
     * @return 最终过期时间
     */
    private int getRandomExpireMinutes(int baseMinutes, int randomMinutes) {
        return baseMinutes + (int) (Math.random() * randomMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product:list", key = "'all'")
    public List<ProductDTO> getAllProducts() {
        return productMapper.findAll().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    // 没有设置过期时间，永不过期，用于缓存商品详情
    @Cacheable(value = "product:detail", key = "#id")
    public ProductDTO getProductById(Long id) {
        // 验证参数是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        return mapToProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product:list", key = "#page + '-' + #size")
    public List<ProductDTO> getProductsByPage(int page, int size) {
        // MyBatis 分页需要额外配置，这里简化处理
        return productMapper.findAll().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "product:list", "product:detail" }, allEntries = true)
    public ProductDTO createProduct(Product product) {
        // 验证参数是否为空
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // 分布式锁，防止缓存击穿
        String lockKey = PRODUCT_LOCK_KEY_PREFIX + "create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            product.setCreatedAt(java.time.LocalDateTime.now());
            product.setUpdatedAt(java.time.LocalDateTime.now());
            productMapper.insert(product);

            // 清除热门商品和随机推荐缓存，保证数据一致性
            clearProductRecommendationCache();

            return mapToProductDTO(product);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "product:list", "product:detail" }, allEntries = true)
    public ProductDTO updateProduct(Long id, Product product) {
        // 验证参数是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // 分布式锁，防止缓存击穿（锁商品ID）
        String lockKey = PRODUCT_LOCK_KEY_PREFIX + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            Product existingProduct = productMapper.findById(id);
            if (existingProduct == null) {
                throw new ResourceNotFoundException("Product not found with id: " + id);
            }

            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setImageUrl(product.getImageUrl());
            existingProduct.setUpdatedAt(java.time.LocalDateTime.now());

            productMapper.update(existingProduct);

            // 清除热门商品和随机推荐缓存，保证数据一致性
            clearProductRecommendationCache();

            return mapToProductDTO(existingProduct);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "product:list", "product:detail" }, allEntries = true)
    public void deleteProduct(Long id) {
        // 验证 ID 是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        // 分布式锁，防止缓存击穿（锁商品ID）
        String lockKey = PRODUCT_LOCK_KEY_PREFIX + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            Product product = productMapper.findById(id);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + id);
            }
            productMapper.deleteById(id);

            // 清除热门商品和随机推荐缓存，保证数据一致性
            clearProductRecommendationCache();
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    /**
     * 清除商品推荐相关缓存（热门商品、随机推荐）
     * 在商品创建/更新/删除时调用，保证缓存与数据库一致
     */
    private void clearProductRecommendationCache() {
        redisUtil.deletePattern(HOT_PRODUCT_CACHE_PREFIX + "*");
        redisUtil.deletePattern(RANDOM_PRODUCT_CACHE_PREFIX + "*");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        // 生成缓存key
        String cacheKey = SEARCH_CACHE_KEY_PREFIX + keyword.trim().toLowerCase();

        // 先从缓存读取
        @SuppressWarnings("unchecked")
        List<ProductDTO> cached = redisUtil.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        // 优先使用 MySQL 全文索引搜索，性能更优
        // 如果全文索引无结果（如关键词过短），回退到 LIKE 模糊查询
        List<Product> products;
        try {
            products = productMapper.searchByNameFullText(keyword.trim());
            // 自然语言模式下，如果关键词过短可能返回空，此时回退到 LIKE
            if (products == null || products.isEmpty()) {
                products = productMapper.search(keyword.trim());
            }
        } catch (Exception e) {
            // 全文索引异常时（如索引未创建），使用 LIKE 作为兜底方案
            products = productMapper.search(keyword.trim());
        }

        List<ProductDTO> result = products.stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        // 缓存搜索结果，防止缓存穿透和雪崩
        if (result.isEmpty()) {
            // 搜索结果为空，缓存空值标记，4~6分钟随机过期
            int emptyExpireMinutes = getRandomExpireMinutes(EMPTY_CACHE_BASE_MINUTES, EMPTY_CACHE_RANDOM_MINUTES);
            redisUtil.set(cacheKey + ":empty", "true", emptyExpireMinutes, TimeUnit.MINUTES);
            redisUtil.set(cacheKey, Collections.emptyList(), emptyExpireMinutes, TimeUnit.MINUTES);
        } else {
            // 有结果，25~35分钟随机过期，避免大量缓存同时失效
            int searchExpireMinutes = getRandomExpireMinutes(SEARCH_CACHE_BASE_MINUTES, SEARCH_CACHE_RANDOM_MINUTES);
            redisUtil.set(cacheKey, result, searchExpireMinutes, TimeUnit.MINUTES);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryAndPrice(Long thirdCategoryId, BigDecimal minPrice,
            BigDecimal maxPrice) {
        // 参数校验
        if (thirdCategoryId == null) {
            throw new IllegalArgumentException("Third category ID cannot be null");
        }
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice == null) {
            maxPrice = new BigDecimal("999999999");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        // 使用联合索引 idx_product_category_price 加速查询
        return productMapper.findByCategoryAndPrice(thirdCategoryId, minPrice, maxPrice).stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getRandomProducts(int limit) {
        if (limit <= 0) {
            limit = 8;
        }

        String cacheKey = RANDOM_PRODUCT_CACHE_PREFIX + limit;

        // 先从缓存读取
        @SuppressWarnings("unchecked")
        List<ProductDTO> cached = redisUtil.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        // 使用分布式锁防止缓存击穿
        String lockKey = PRODUCT_LOCK_KEY_PREFIX + "random:" + limit;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 双重检查，防止锁等待期间其他线程已加载缓存
            cached = redisUtil.get(cacheKey, List.class);
            if (cached != null) {
                return cached;
            }

            List<ProductDTO> result = productMapper.selectRandomProducts(limit).stream()
                    .map(this::mapToProductDTO)
                    .collect(Collectors.toList());

            // 30~60分钟随机过期，防止缓存雪崩
            int expireMinutes = getRandomExpireMinutes(RANDOM_PRODUCT_CACHE_BASE_MINUTES,
                    RANDOM_PRODUCT_CACHE_RANDOM_MINUTES);
            redisUtil.set(cacheKey, result, expireMinutes, TimeUnit.MINUTES);

            return result;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getHotProducts(int limit) {
        if (limit <= 0) {
            limit = 10;
        }

        String cacheKey = HOT_PRODUCT_CACHE_PREFIX + limit;

        // 先从缓存读取
        @SuppressWarnings("unchecked")
        List<ProductDTO> cached = redisUtil.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        // 使用分布式锁防止缓存击穿
        String lockKey = PRODUCT_LOCK_KEY_PREFIX + "hot:" + limit;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 双重检查
            cached = redisUtil.get(cacheKey, List.class);
            if (cached != null) {
                return cached;
            }

            List<ProductDTO> result = productMapper.findHotProducts(limit).stream()
                    .map(this::mapToProductDTO)
                    .collect(Collectors.toList());

            // 30~60分钟随机过期，防止缓存雪崩
            int expireMinutes = getRandomExpireMinutes(HOT_PRODUCT_CACHE_BASE_MINUTES,
                    HOT_PRODUCT_CACHE_RANDOM_MINUTES);
            redisUtil.set(cacheKey, result, expireMinutes, TimeUnit.MINUTES);

            return result;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    // 转换实体到DTO
    private ProductDTO mapToProductDTO(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setStock(product.getStock());
        productDTO.setDescription(product.getDescription());
        productDTO.setImageUrl(product.getImageUrl());
        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());

        // 设置三级分类信息（品牌）- 手动查询，避免MyBatis关联对象为null
        if (product.getThirdCategoryId() != null) {
            ThirdCategory thirdCategory = thirdCategoryMapper.findById(product.getThirdCategoryId());
            if (thirdCategory != null) {
                productDTO.setThirdCategoryId(thirdCategory.getId());
                productDTO.setThirdCategoryName(thirdCategory.getName());

                // 设置二级分类信息
                if (thirdCategory.getSubCategoryId() != null) {
                    SubCategory subCategory = subCategoryMapper.findById(thirdCategory.getSubCategoryId());
                    if (subCategory != null) {
                        productDTO.setSubCategoryId(subCategory.getId());
                        productDTO.setSubCategoryName(subCategory.getName());

                        // 设置一级分类信息（主分类）
                        if (subCategory.getMainCategoryId() != null) {
                            MainCategory mainCategory = mainCategoryMapper.findById(subCategory.getMainCategoryId());
                            if (mainCategory != null) {
                                productDTO.setMainCategoryId(mainCategory.getId());
                                productDTO.setMainCategoryName(mainCategory.getName());
                            }
                        }
                    }
                }
            }
        }

        return productDTO;
    }
}
