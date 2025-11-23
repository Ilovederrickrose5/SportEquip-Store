package com.sportsequipment.service.impl;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.dto.SubCategoryDTO;
import com.sportsequipment.dto.ThirdCategoryDTO;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.repository.MainCategoryRepository;
import com.sportsequipment.repository.SubCategoryRepository;
import com.sportsequipment.repository.ThirdCategoryRepository;
import com.sportsequipment.service.CategoryService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private MainCategoryRepository mainCategoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private ThirdCategoryRepository thirdCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllMainCategoriesWithSubCategories() {
        return mainCategoryRepository.findAll().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MainCategory> getAllMainCategories() {
        return mainCategoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategory> getSubCategoriesByMainId(@Nonnull Long mainId) {
        return subCategoryRepository.findByMainCategoryId(mainId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdCategory> getThirdCategoriesBySubId(@Nonnull Long subId) {
        return thirdCategoryRepository.findBySubCategoryId(subId);
    }



    @Override
    @Transactional(readOnly = true)
    public MainCategory getMainCategoryById(@Nonnull Long id) {
        return mainCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Main category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategory getSubCategoryById(@Nonnull Long id) {
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ThirdCategory getThirdCategoryById(@Nonnull Long id) {
        return thirdCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Third category not found with id: " + id));
    }



    @Override
    @Transactional
    public MainCategory createMainCategory(MainCategory mainCategory) {
        return mainCategoryRepository.save(mainCategory);
    }

    @Override
    @Transactional
    public SubCategory createSubCategory(@Nonnull Long mainCategoryId, SubCategory subCategory) {
        MainCategory mainCategory = getMainCategoryById(mainCategoryId);
        subCategory.setMainCategory(mainCategory);
        return subCategoryRepository.save(subCategory);
    }

    @Override
    @Transactional
    public ThirdCategory createThirdCategory(@Nonnull Long subCategoryId, ThirdCategory thirdCategory) {
        SubCategory subCategory = getSubCategoryById(subCategoryId);
        thirdCategory.setSubCategory(subCategory);
        return thirdCategoryRepository.save(thirdCategory);
    }



    @Override
    @Transactional
    public MainCategory updateMainCategory(@Nonnull Long id, MainCategory mainCategory) {
        MainCategory existingMainCategory = getMainCategoryById(id);
        existingMainCategory.setName(mainCategory.getName());
        existingMainCategory.setDescription(mainCategory.getDescription());
        return mainCategoryRepository.save(existingMainCategory);
    }

    @Override
    @Transactional
    public SubCategory updateSubCategory(@Nonnull Long id, SubCategory subCategory) {
        SubCategory existingSubCategory = getSubCategoryById(id);
        existingSubCategory.setName(subCategory.getName());
        existingSubCategory.setDescription(subCategory.getDescription());
        return subCategoryRepository.save(existingSubCategory);
    }

    @Override
    @Transactional
    public ThirdCategory updateThirdCategory(@Nonnull Long id, ThirdCategory thirdCategory) {
        ThirdCategory existingThirdCategory = getThirdCategoryById(id);
        existingThirdCategory.setName(thirdCategory.getName());
        existingThirdCategory.setDescription(thirdCategory.getDescription());
        return thirdCategoryRepository.save(existingThirdCategory);
    }



    @Override
    @Transactional
    public void deleteMainCategory(@Nonnull Long id) {
        MainCategory mainCategory = getMainCategoryById(id);
        mainCategoryRepository.delete(mainCategory);
    }

    @Override
    @Transactional
    public void deleteSubCategory(@Nonnull Long id) {
        SubCategory subCategory = getSubCategoryById(id);
        subCategoryRepository.delete(subCategory);
    }

    @Override
    @Transactional
    public void deleteThirdCategory(@Nonnull Long id) {
        ThirdCategory thirdCategory = getThirdCategoryById(id);
        thirdCategoryRepository.delete(thirdCategory);
    }



    // 映射方法
    private CategoryDTO mapToCategoryDTO(MainCategory mainCategory) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(mainCategory.getId());
        categoryDTO.setName(mainCategory.getName());
        categoryDTO.setDescription(mainCategory.getDescription());
        
        if (mainCategory.getSubCategories() != null) {
            categoryDTO.setSubCategories(
                mainCategory.getSubCategories().stream()
                    .map(this::mapToSubCategoryDTO)
                    .collect(Collectors.toList())
            );
        }
        
        return categoryDTO;
    }

    private SubCategoryDTO mapToSubCategoryDTO(SubCategory subCategory) {
        SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
        subCategoryDTO.setId(subCategory.getId());
        subCategoryDTO.setName(subCategory.getName());
        subCategoryDTO.setDescription(subCategory.getDescription());
        
        if (subCategory.getThirdCategories() != null) {
            subCategoryDTO.setThirdCategories(
                subCategory.getThirdCategories().stream()
                    .map(this::mapToThirdCategoryDTO)
                    .collect(Collectors.toList())
            );
        }
        
        return subCategoryDTO;
    }

    private ThirdCategoryDTO mapToThirdCategoryDTO(ThirdCategory thirdCategory) {
        ThirdCategoryDTO thirdCategoryDTO = new ThirdCategoryDTO();
        thirdCategoryDTO.setId(thirdCategory.getId());
        thirdCategoryDTO.setName(thirdCategory.getName());
        thirdCategoryDTO.setDescription(thirdCategory.getDescription());
        
        return thirdCategoryDTO;
    }
}
    