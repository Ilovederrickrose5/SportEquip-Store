package com.sportsequipment.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 主分类实体类
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class MainCategory {
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<SubCategory> subCategories = new ArrayList<>();
}