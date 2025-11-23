package com.sportsequipment.controller;

import com.sportsequipment.dto.ReviewDTO;
import com.sportsequipment.entity.Review;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评价控制器，处理用户对商品的评价操作
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 提交评价
    @PostMapping
    public ResponseEntity<ReviewDTO> submitReview(@RequestBody Review review) {
        ReviewDTO savedReview = reviewService.submitReview(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    // 获取商品评价列表
    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProductId(@PathVariable Long productId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // 获取当前用户的评价列表
    @GetMapping("/user")
    public ResponseEntity<List<ReviewDTO>> getCurrentUserReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }

    // 获取商品平均评分
    @GetMapping("/products/{productId}/average")
    public ResponseEntity<Map<String, Double>> getAverageRatingByProductId(@PathVariable Long productId) {
        double averageRating = reviewService.getAverageRatingByProductId(productId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating));
    }

    // 检查当前用户是否已评价商品
    @GetMapping("/products/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIfUserReviewedProduct(@PathVariable Long productId) {
        boolean hasReviewed = reviewService.hasUserReviewedProduct(productId);
        return ResponseEntity.ok(Map.of("hasReviewed", hasReviewed));
    }
}