package com.sportsequipment.service.impl;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.dto.SubCategoryDTO;
import com.sportsequipment.dto.ThirdCategoryDTO;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.MainCategoryMapper;
import com.sportsequipment.mapper.SubCategoryMapper;
import com.sportsequipment.mapper.ThirdCategoryMapper;
import com.sportsequipment.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private MainCategoryMapper mainCategoryMapper;

    @Autowired
    private SubCategoryMapper subCategoryMapper;

    @Autowired
    private ThirdCategoryMapper thirdCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllMainCategoriesWithSubCategories() {
        return mainCategoryMapper.findAll().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MainCategory> getAllMainCategories() {
        return mainCategoryMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategory> getSubCategoriesByMainId(Long mainId) {
        return subCategoryMapper.findByMainCategoryId(mainId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdCategory> getThirdCategoriesBySubId(Long subId) {
        return thirdCategoryMapper.findBySubCategoryId(subId);
    }

    @Override
    @Transactional(readOnly = true)
    public MainCategory getMainCategoryById(Long id) {
        MainCategory category = mainCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Main category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategory getSubCategoryById(Long id) {
        SubCategory category = subCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Sub category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public ThirdCategory getThirdCategoryById(Long id) {
        ThirdCategory category = thirdCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Third category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional
    public MainCategory createMainCategory(MainCategory mainCategory) {
        mainCategory.setCreatedAt(java.time.LocalDateTime.now());
        mainCategory.setUpdatedAt(java.time.LocalDateTime.now());
        mainCategoryMapper.insert(mainCategory);
        return mainCategory;
    }

    @Override
    @Transactional
    public SubCategory createSubCategory(Long mainCategoryId, SubCategory subCategory) {
        MainCategory mainCategory = getMainCategoryById(mainCategoryId);
        subCategory.setMainCategory(mainCategory);
        subCategory.setCreatedAt(java.time.LocalDateTime.now());
        subCategory.setUpdatedAt(java.time.LocalDateTime.now());
        subCategoryMapper.insert(subCategory);
        return subCategory;
    }

    @Override
    @Transactional
    public ThirdCategory createThirdCategory(Long subCategoryId, ThirdCategory thirdCategory) {
        SubCategory subCategory = getSubCategoryById(subCategoryId);
        thirdCategory.setSubCategory(subCategory);
        thirdCategory.setCreatedAt(java.time.LocalDateTime.now());
        thirdCategory.setUpdatedAt(java.time.LocalDateTime.now());
        thirdCategoryMapper.insert(thirdCategory);
        return thirdCategory;
    }

    @Override
    @Transactional
    public MainCategory updateMainCategory(Long id, MainCategory mainCategory) {
        MainCategory existingMainCategory = getMainCategoryById(id);
        existingMainCategory.setName(mainCategory.getName());
        existingMainCategory.setDescription(mainCategory.getDescription());
        existingMainCategory.setUpdatedAt(java.time.LocalDateTime.now());
        mainCategoryMapper.update(existingMainCategory);
        return existingMainCategory;
    }

    @Override
    @Transactional
    public SubCategory updateSubCategory(Long id, SubCategory subCategory) {
        SubCategory existingSubCategory = getSubCategoryById(id);
        existingSubCategory.setName(subCategory.getName());
        existingSubCategory.setDescription(subCategory.getDescription());
        existingSubCategory.setUpdatedAt(java.time.LocalDateTime.now());
        subCategoryMapper.update(existingSubCategory);
        return existingSubCategory;
    }

    @Override
    @Transactional
    public ThirdCategory updateThirdCategory(Long id, ThirdCategory thirdCategory) {
        ThirdCategory existingThirdCategory = getThirdCategoryById(id);
        existingThirdCategory.setName(thirdCategory.getName());
        existingThirdCategory.setDescription(thirdCategory.getDescription());
        existingThirdCategory.setUpdatedAt(java.time.LocalDateTime.now());
        thirdCategoryMapper.update(existingThirdCategory);
        return existingThirdCategory;
    }

    @Override
    @Transactional
    public void deleteMainCategory(Long id) {
        getMainCategoryById(id);
        mainCategoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteSubCategory(Long id) {
        getSubCategoryById(id);
        subCategoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteThirdCategory(Long id) {
        getThirdCategoryById(id);
        thirdCategoryMapper.deleteById(id);
    }

    // 映射方法
    private CategoryDTO mapToCategoryDTO(MainCategory mainCategory) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(mainCategory.getId());
        categoryDTO.setName(mainCategory.getName());
        categoryDTO.setDescription(mainCategory.getDescription());

        Optional.ofNullable(mainCategory.getSubCategories())
                .ifPresent(subCategories -> categoryDTO.setSubCategories(
                        subCategories.stream()
                                .map(this::mapToSubCategoryDTO)
                                .collect(Collectors.toList())));

        return categoryDTO;
    }

    private SubCategoryDTO mapToSubCategoryDTO(SubCategory subCategory) {
        SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
        subCategoryDTO.setId(subCategory.getId());
        subCategoryDTO.setName(subCategory.getName());
        subCategoryDTO.setDescription(subCategory.getDescription());

        Optional.ofNullable(subCategory.getThirdCategories())
                .ifPresent(thirdCategories -> subCategoryDTO.setThirdCategories(
                        thirdCategories.stream()
                                .map(this::mapToThirdCategoryDTO)
                                .collect(Collectors.toList())));

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
