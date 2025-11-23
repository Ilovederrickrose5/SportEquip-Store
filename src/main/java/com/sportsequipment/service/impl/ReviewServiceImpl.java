package com.sportsequipment.service.impl;

import com.sportsequipment.dto.ReviewDTO;
import com.sportsequipment.dto.UserDTO;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Review;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.exception.ValidationException;
import com.sportsequipment.repository.OrderItemRepository;
import com.sportsequipment.repository.OrderRepository;
import com.sportsequipment.repository.ProductRepository;
import com.sportsequipment.repository.ReviewRepository;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.ReviewService;
import com.sportsequipment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public ReviewDTO submitReview(Review review) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 验证评分是否在1-5分范围内
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ValidationException("评分必须在1-5分之间");
        }

        // 验证评论内容是否超过500个字符
        if (review.getComment() != null && review.getComment().length() > 500) {
            throw new ValidationException("评论内容不能超过500个字符");
        }

        // 检查产品ID是否为空
        Long productId = review.getProductId();
        if (productId == null) {
            throw new ValidationException("商品ID不能为空");
        }
        
        // 检查产品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));

        // 检查用户是否已购买该商品
        boolean hasPurchased = hasUserPurchasedProduct(userId, review.getProductId());
        if (!hasPurchased) {
            throw new UnauthorizedException("您只能评价已购买的商品");
        }

        // 检查用户是否已评价该商品
        boolean hasReviewed = reviewRepository.existsByUserIdAndProductId(userId, review.getProductId());
        if (hasReviewed) {
            throw new ValidationException("您已经评价过该商品");
        }

        // 设置用户ID和创建时间
        review.setUserId(userId);
        review.setCreatedAt(LocalDateTime.now());

        // 保存评价
        Review savedReview = reviewRepository.save(review);

        // 转换为DTO并返回
        return mapToReviewDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        // 检查产品ID是否为空
        if (productId == null) {
            throw new ValidationException("商品ID不能为空");
        }
        
        // 检查产品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));

        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::mapToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::mapToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRatingByProductId(Long productId) {
        // 检查产品ID是否为空
        if (productId == null) {
            throw new ValidationException("商品ID不能为空");
        }
        
        // 检查产品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));

        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        // 使用BigDecimal进行精确计算
        BigDecimal sum = BigDecimal.ZERO;
        for (Review review : reviews) {
            sum = sum.add(BigDecimal.valueOf(review.getRating()));
        }
        
        // 计算平均值并保留2位小数
        BigDecimal average = sum.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        return average.doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    // 检查用户是否已购买该商品
    private boolean hasUserPurchasedProduct(Long userId, Long productId) {
        List<Order> userOrders = orderRepository.findByUserId(userId);
        for (Order order : userOrders) {
            // 订单状态必须是已支付或已发货或已完成
            if (order.getStatus().equals("PAID") || order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED")) {
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                for (OrderItem item : orderItems) {
                    if (item.getProduct().getId().equals(productId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 转换实体到DTO
    private ReviewDTO mapToReviewDTO(Review review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setProductId(review.getProductId());
        reviewDTO.setUserId(review.getUserId());
        reviewDTO.setRating(review.getRating());
        reviewDTO.setComment(review.getComment());
        reviewDTO.setCreatedAt(review.getCreatedAt());
        
        // 设置用户名
        // 通过review.getUserId()直接获取User实体的用户名
        UserDTO userDTO = userService.getUserById(review.getUserId());
        reviewDTO.setUsername(userDTO.getUsername());
        
        return reviewDTO;
    }
}