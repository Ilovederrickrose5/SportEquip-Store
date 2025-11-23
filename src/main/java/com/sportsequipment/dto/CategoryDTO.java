package com.sportsequipment.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 分类数据传输对象
 * @author System
 */
@Getter
@Setter
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    // 保留setter方法，因为getter未被使用
    @Setter
    private List<SubCategoryDTO> subCategories;

}
    