package com.sportsequipment.service;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.dto.SubCategoryDTO;
import com.sportsequipment.dto.ThirdCategoryDTO;
import com.sportsequipment.entity.Category;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllMainCategoriesWithSubCategories();
    List<MainCategory> getAllMainCategories();
    List<SubCategory> getSubCategoriesByMainId(Long mainId);
    List<ThirdCategory> getThirdCategoriesBySubId(Long subId);

    Category getById(Long id);
    MainCategory getMainCategoryById(Long id);
    SubCategory getSubCategoryById(Long id);
    ThirdCategory getThirdCategoryById(Long id);

    MainCategory createMainCategory(MainCategory mainCategory);
    SubCategory createSubCategory(Long mainCategoryId, SubCategory subCategory);
    ThirdCategory createThirdCategory(Long subCategoryId, ThirdCategory thirdCategory);

    MainCategory updateMainCategory(Long id, MainCategory mainCategory);
    SubCategory updateSubCategory(Long id, SubCategory subCategory);
    ThirdCategory updateThirdCategory(Long id, ThirdCategory thirdCategory);

    void deleteMainCategory(Long id);
    void deleteSubCategory(Long id);
    void deleteThirdCategory(Long id);

    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}
