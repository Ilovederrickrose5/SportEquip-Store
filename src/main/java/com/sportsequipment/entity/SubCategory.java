package com.sportsequipment.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubCategory {
    private Long id;

    private String name;

    private String description;

    private Long mainCategoryId;

    private MainCategory mainCategory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ThirdCategory> thirdCategories = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMainCategoryId() {
        return mainCategoryId;
    }

    public void setMainCategoryId(Long mainCategoryId) {
        this.mainCategoryId = mainCategoryId;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
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

    public List<ThirdCategory> getThirdCategories() {
        return thirdCategories;
    }

    public void setThirdCategories(List<ThirdCategory> thirdCategories) {
        this.thirdCategories = thirdCategories;
    }
}