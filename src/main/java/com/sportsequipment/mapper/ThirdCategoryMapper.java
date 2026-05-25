package com.sportsequipment.mapper;

import com.sportsequipment.entity.ThirdCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ThirdCategoryMapper {

    ThirdCategory findById(Long id);

    List<ThirdCategory> findAll();

    void insert(ThirdCategory thirdCategory);

    void update(ThirdCategory thirdCategory);

    void deleteById(Long id);

    List<ThirdCategory> findBySubCategoryId(Long subCategoryId);
}