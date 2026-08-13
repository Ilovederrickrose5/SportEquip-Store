package com.sportsequipment.service;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    List<ProductDTO> getProductsByPage(int page, int size);

    ProductDTO createProduct(Product product);

    ProductDTO updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    List<ProductDTO> searchProducts(String keyword);

    /**
     * 根据三级分类ID和价格范围筛选商品
     *
     * @param thirdCategoryId 三级分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 符合条件的商品列表
     */
    List<ProductDTO> getProductsByCategoryAndPrice(Long thirdCategoryId, BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDTO> getRandomProducts(int limit);

    /**
     * 获取热门商品（按销量排序）
     *
     * @param limit 返回商品数量
     * @return 热门商品列表
     */
    List<ProductDTO> getHotProducts(int limit);
}
