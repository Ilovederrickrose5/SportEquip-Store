package com.sportsequipment.service;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllMainCategoriesWithSubCategories();
    List<MainCategory> getAllMainCategories();
    List<SubCategory> getSubCategoriesByMainId(@Nonnull Long mainId);
    List<ThirdCategory> getThirdCategoriesBySubId(@Nonnull Long subId);

    MainCategory getMainCategoryById(@Nonnull Long id);
    SubCategory getSubCategoryById(@Nonnull Long id);
    ThirdCategory getThirdCategoryById(@Nonnull Long id);

    MainCategory createMainCategory(MainCategory mainCategory);
    SubCategory createSubCategory(@Nonnull Long mainCategoryId, SubCategory subCategory);
    ThirdCategory createThirdCategory(@Nonnull Long subCategoryId, ThirdCategory thirdCategory);

    MainCategory updateMainCategory(@Nonnull Long id, MainCategory mainCategory);
    SubCategory updateSubCategory(@Nonnull Long id, SubCategory subCategory);
    ThirdCategory updateThirdCategory(@Nonnull Long id, ThirdCategory thirdCategory);

    void deleteMainCategory(@Nonnull Long id);
    void deleteSubCategory(@Nonnull Long id);
    void deleteThirdCategory(@Nonnull Long id);
}
    