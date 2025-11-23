package com.sportsequipment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 产品数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 分类相关字段
    private Long mainCategoryId;
    private String mainCategoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private Long thirdCategoryId;
    private String thirdCategoryName;
}
    