package com.sportsequipment.mapper;

import com.sportsequipment.entity.SubCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubCategoryMapper {

    SubCategory findById(Long id);

    List<SubCategory> findAll();

    void insert(SubCategory subCategory);

    void update(SubCategory subCategory);

    void deleteById(Long id);

    List<SubCategory> findByMainCategoryId(Long mainCategoryId);
}