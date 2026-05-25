package com.sportsequipment.mapper;

import com.sportsequipment.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {

    Review findById(Long id);

    List<Review> findAll();

    void insert(Review review);

    void update(Review review);

    void deleteById(Long id);

    List<Review> findByProductId(Long productId);

    List<Review> findByUserId(Long userId);

    Integer existsByUserIdAndProductId(Long userId, Long productId);
}