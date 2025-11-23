package com.sportsequipment.service;

import com.sportsequipment.dto.ReviewDTO;
import com.sportsequipment.entity.Review;
import java.util.List;

public interface ReviewService {
    ReviewDTO submitReview(Review review);
    List<ReviewDTO> getReviewsByProductId(Long productId);
    List<ReviewDTO> getReviewsByUserId(Long userId);
    double getAverageRatingByProductId(Long productId);
    boolean hasUserReviewedProduct(Long productId);
}