package com.sportsequipment.entity;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    private String name;

    /**
     * 关联统一分类表 Category.id（新标准，过渡期双写）
     * 对应数据库 product.category_id
     */
    private Long categoryId;

    private Category category;

    /**
     * 旧字段：三级分类ID（对应已废弃的 third_category 表）
     * 
     * @deprecated 过渡期保留双写，一周后切到 categoryId 再清理
     */
    @Deprecated
    private Long thirdCategoryId;

    /**
     * @deprecated 过渡期保留，一周后切到 category 字段
     */
    @Deprecated
    private ThirdCategory thirdCategory;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * @deprecated 过渡期保留，使用 getCategoryId() 替代
     */
    @Deprecated
    public Long getThirdCategoryId() {
        return thirdCategoryId;
    }

    /**
     * @deprecated 过渡期保留，使用 setCategoryId(Long) 替代
     */
    @Deprecated
    public void setThirdCategoryId(Long thirdCategoryId) {
        this.thirdCategoryId = thirdCategoryId;
    }

    /**
     * @deprecated 过渡期保留，使用 getCategory() 替代
     */
    @Deprecated
    public ThirdCategory getThirdCategory() {
        return thirdCategory;
    }

    /**
     * @deprecated 过渡期保留，使用 setCategory(Category) 替代
     */
    @Deprecated
    public void setThirdCategory(ThirdCategory thirdCategory) {
        this.thirdCategory = thirdCategory;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}