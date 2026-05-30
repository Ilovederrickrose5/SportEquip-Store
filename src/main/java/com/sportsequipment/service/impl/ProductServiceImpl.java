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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ThirdCategoryMapper thirdCategoryMapper;

    @Autowired
    private SubCategoryMapper subCategoryMapper;

    @Autowired
    private MainCategoryMapper mainCategoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    // 搜索结果缓存键前缀（防止缓存穿透）
    private static final String SEARCH_CACHE_KEY_PREFIX = "product:search:";
    // 空值缓存的过期时间（5分钟）
    private static final int EMPTY_CACHE_EXPIRE_MINUTES = 5;
    // 商品分布式锁键前缀（防止缓存击穿）
    private static final String PRODUCT_LOCK_KEY_PREFIX = "lock:product:";

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
        } finally {
            redisUtil.unlock(lockKey);
        }
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

        // 查询数据库
        List<ProductDTO> result = productMapper.search(keyword.trim()).stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        // 缓存搜索结果，防止缓存穿透
        if (result.isEmpty()) {
            // 搜索结果为空，缓存空值标记，5分钟后过期
            redisUtil.set(cacheKey + ":empty", "true", EMPTY_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            redisUtil.set(cacheKey, Collections.emptyList(), EMPTY_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            // 有结果，正常缓存30分钟
            redisUtil.set(cacheKey, result, 30, TimeUnit.MINUTES);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getRandomProducts(int limit) {
        if (limit <= 0) {
            limit = 8;
        }
        return productMapper.selectRandomProducts(limit).stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
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
