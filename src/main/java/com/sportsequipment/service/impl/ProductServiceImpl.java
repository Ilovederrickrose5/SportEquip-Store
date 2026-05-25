package com.sportsequipment.service.impl;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Product;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productMapper.findAll().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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
    public List<ProductDTO> getProductsByPage(int page, int size) {
        // MyBatis 分页需要额外配置，这里简化处理
        return productMapper.findAll().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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
    public void deleteProduct(Long id) {
        // 验证ID是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productMapper.deleteById(id);
    }

    // 转换实体到DTO
    private ProductDTO mapToProductDTO(Product product) {
        // 验证参数是否为空
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

        // 设置三级分类信息（品牌）
        if (product.getThirdCategory() != null) {
            productDTO.setThirdCategoryId(product.getThirdCategory().getId());
            productDTO.setThirdCategoryName(product.getThirdCategory().getName());

            // 设置二级分类信息
            if (product.getThirdCategory().getSubCategory() != null) {
                productDTO.setSubCategoryId(product.getThirdCategory().getSubCategory().getId());
                productDTO.setSubCategoryName(product.getThirdCategory().getSubCategory().getName());

                // 设置一级分类信息（主分类）
                if (product.getThirdCategory().getSubCategory().getMainCategory() != null) {
                    productDTO.setMainCategoryId(product.getThirdCategory().getSubCategory().getMainCategory().getId());
                    productDTO.setMainCategoryName(
                            product.getThirdCategory().getSubCategory().getMainCategory().getName());
                }
            }
        }

        return productDTO;
    }
}
