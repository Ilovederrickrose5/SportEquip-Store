package com.sportsequipment.service.impl;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Product;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.repository.ProductRepository;
import com.sportsequipment.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllWithCategories().stream()
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
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAllWithCategories(pageable).getContent().stream()
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
        
        Product savedProduct = productRepository.save(product);
        return mapToProductDTO(savedProduct);
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
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setImageUrl(product.getImageUrl());
        
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToProductDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // 验证ID是否为空
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    // 转换实体到DTO
    private ProductDTO mapToProductDTO(@Nonnull Product product) {
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
                    productDTO.setMainCategoryName(product.getThirdCategory().getSubCategory().getMainCategory().getName());
                }
            }
        }
        
        return productDTO;
    }
}
    