package com.sportsequipment.service;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Product;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    List<ProductDTO> getProductsByPage(int page, int size);
    ProductDTO createProduct(Product product);
    ProductDTO updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
    