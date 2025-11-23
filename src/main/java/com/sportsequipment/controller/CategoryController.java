package com.sportsequipment.controller;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.dto.SubCategoryDTO;
import com.sportsequipment.dto.ThirdCategoryDTO;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import com.sportsequipment.service.CategoryService;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类别控制器，处理商品类别的CRUD操作
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 获取所有一级类别及其子类别
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllMainCategoriesWithSubCategories());
    }

    // 获取所有一级类别
    @GetMapping("/main")
    public ResponseEntity<List<CategoryDTO>> getAllMainCategories() {
        return ResponseEntity.ok(categoryService.getAllMainCategories().stream()
                .map(mainCategory -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(mainCategory.getId());
                    dto.setName(mainCategory.getName());
                    dto.setDescription(mainCategory.getDescription());
                    return dto;
                })
                .collect(Collectors.toList()));
    }

    // 获取指定一级类别下的所有二级类别
    @GetMapping("/main/{mainId}/sub")
    public ResponseEntity<List<SubCategoryDTO>> getSubCategoriesByMainId(@PathVariable Long mainId) {
        return ResponseEntity.ok(categoryService.getSubCategoriesByMainId(mainId).stream()
                .map(subCategory -> {
                    SubCategoryDTO dto = new SubCategoryDTO();
                    dto.setId(subCategory.getId());
                    dto.setName(subCategory.getName());
                    dto.setDescription(subCategory.getDescription());
                    return dto;
                })
                .collect(Collectors.toList()));
    }

    // 获取指定二级类别下的所有三级类别
    @GetMapping("/sub/{subId}/third")
    public ResponseEntity<List<ThirdCategoryDTO>> getThirdCategoriesBySubId(@PathVariable Long subId) {
        return ResponseEntity.ok(categoryService.getThirdCategoriesBySubId(subId).stream()
                .map(thirdCategory -> {
                    ThirdCategoryDTO dto = new ThirdCategoryDTO();
                    dto.setId(thirdCategory.getId());
                    dto.setName(thirdCategory.getName());
                    dto.setDescription(thirdCategory.getDescription());
                    return dto;
                })
                .collect(Collectors.toList()));
    }



    // 管理员接口：创建一级类别
    @PostMapping("/main")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createMainCategory(@RequestBody MainCategory mainCategory) {
        MainCategory created = categoryService.createMainCategory(mainCategory);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(created.getId());
        dto.setName(created.getName());
        dto.setDescription(created.getDescription());
        return ResponseEntity.ok(dto);
    }

    // 管理员接口：创建二级类别
    @PostMapping("/main/{mainId}/sub")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryDTO> createSubCategory(
            @PathVariable Long mainId,
            @RequestBody SubCategory subCategory) {
        SubCategory created = categoryService.createSubCategory(mainId, subCategory);
        SubCategoryDTO dto = new SubCategoryDTO();
        dto.setId(created.getId());
        dto.setName(created.getName());
        dto.setDescription(created.getDescription());
        return ResponseEntity.ok(dto);
    }

    // 管理员接口：创建三级类别
    @PostMapping("/sub/{subId}/third")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThirdCategoryDTO> createThirdCategory(
            @PathVariable Long subId,
            @RequestBody ThirdCategory thirdCategory) {
        ThirdCategory created = categoryService.createThirdCategory(subId, thirdCategory);
        ThirdCategoryDTO dto = new ThirdCategoryDTO();
        dto.setId(created.getId());
        dto.setName(created.getName());
        dto.setDescription(created.getDescription());
        return ResponseEntity.ok(dto);
    }



    // 管理员接口：更新类别
    @PutMapping("/main/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateMainCategory(
            @PathVariable Long id,
            @RequestBody MainCategory mainCategory) {
        MainCategory updated = categoryService.updateMainCategory(id, mainCategory);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(updated.getId());
        dto.setName(updated.getName());
        dto.setDescription(updated.getDescription());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/sub/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryDTO> updateSubCategory(
            @PathVariable Long id,
            @RequestBody SubCategory subCategory) {
        SubCategory updated = categoryService.updateSubCategory(id, subCategory);
        SubCategoryDTO dto = new SubCategoryDTO();
        dto.setId(updated.getId());
        dto.setName(updated.getName());
        dto.setDescription(updated.getDescription());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/third/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThirdCategoryDTO> updateThirdCategory(
            @PathVariable Long id,
            @RequestBody ThirdCategory thirdCategory) {
        ThirdCategory updated = categoryService.updateThirdCategory(id, thirdCategory);
        ThirdCategoryDTO dto = new ThirdCategoryDTO();
        dto.setId(updated.getId());
        dto.setName(updated.getName());
        dto.setDescription(updated.getDescription());
        return ResponseEntity.ok(dto);
    }



    // 管理员接口：删除类别
    @DeleteMapping("/main/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMainCategory(@PathVariable Long id) {
        categoryService.deleteMainCategory(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "一级类别删除成功");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sub/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteSubCategory(@PathVariable Long id) {
        categoryService.deleteSubCategory(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "二级类别删除成功");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/third/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteThirdCategory(@PathVariable Long id) {
        categoryService.deleteThirdCategory(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "三级类别删除成功");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }


}
    