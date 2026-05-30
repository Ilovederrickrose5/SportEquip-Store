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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        product.setCreatedAt(java.time.LocalDateTime.now());
        product.setUpdatedAt(java.time.LocalDateTime.now());
        productMapper.insert(product);
        return mapToProductDTO(product);
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
    }

    @Override
    @Transactional
    @CacheEvict(value = { "product:list", "product:detail" }, allEntries = true)
    public void deleteProduct(Long id) {
        // 验证 ID 是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productMapper.search(keyword.trim()).stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
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
