package com.sportsequipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsequipment.entity.Review;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}