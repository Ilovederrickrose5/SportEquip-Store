package com.sportsequipment.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品评价数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class ReviewDTO {
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}