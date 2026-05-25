package com.sportsequipment.mapper;

import com.sportsequipment.entity.MainCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MainCategoryMapper {

    MainCategory findById(Long id);

    List<MainCategory> findAll();

    void insert(MainCategory mainCategory);

    void update(MainCategory mainCategory);

    void deleteById(Long id);
}