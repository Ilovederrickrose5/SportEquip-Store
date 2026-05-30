package com.sportsequipment.mapper;

import com.sportsequipment.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(Long id);

    List<Product> findAll();

    void insert(Product product);

    void update(Product product);

    void deleteById(Long id);

    List<Product> findByThirdCategoryId(Long thirdCategoryId);

    List<Product> searchByName(String keyword);

    List<Product> search(String keyword);
}