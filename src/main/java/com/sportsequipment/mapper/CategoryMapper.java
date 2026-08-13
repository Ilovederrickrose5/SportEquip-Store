package com.sportsequipment.mapper;

import com.sportsequipment.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Category findById(@Param("id") Long id);

    List<Category> findAll();

    List<Category> findByLevel(@Param("level") Integer level);

    List<Category> findByParentId(@Param("parentId") Long parentId);

    List<Category> findSubtreeByAncestorId(@Param("ancestorId") Long ancestorId);

    List<Category> findAncestorsByDescendantId(@Param("descendantId") Long descendantId);

    List<Category> findWithDirectChildrenByLevel(@Param("level") Integer level);

    void insert(Category category);

    void update(Category category);

    void deleteById(@Param("id") Long id);

    void deleteSubtreeByAncestorId(@Param("ancestorId") Long ancestorId);

    void insertClosure(@Param("ancestorId") Long ancestorId,
                      @Param("descendantId") Long descendantId,
                      @Param("depth") Integer depth);

    void deleteClosureByDescendantId(@Param("descendantId") Long descendantId);
}
