package com.sportsequipment.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 二级分类数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class SubCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private List<ThirdCategoryDTO> thirdCategories;
}
    