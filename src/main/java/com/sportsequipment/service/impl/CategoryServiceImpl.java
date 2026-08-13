package com.sportsequipment.service.impl;

import com.sportsequipment.dto.CategoryDTO;
import com.sportsequipment.dto.SubCategoryDTO;
import com.sportsequipment.dto.ThirdCategoryDTO;
import com.sportsequipment.entity.Category;
import com.sportsequipment.entity.MainCategory;
import com.sportsequipment.entity.SubCategory;
import com.sportsequipment.entity.ThirdCategory;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.CategoryMapper;
import com.sportsequipment.service.CategoryService;
import com.sportsequipment.util.RedisUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;

    public CategoryServiceImpl(CategoryMapper categoryMapper, RedisUtil redisUtil) {
        this.categoryMapper = categoryMapper;
        this.redisUtil = redisUtil;
    }

    private static final String CATEGORY_LOCK_KEY_PREFIX = "lock:category:";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:list", key = "'all'")
    public List<CategoryDTO> getAllMainCategoriesWithSubCategories() {
        List<Category> level1List = categoryMapper.findByLevel(1);
        if (level1List.isEmpty()) {
            return new ArrayList<>();
        }
        List<Category> level2List = categoryMapper.findByLevel(2);
        List<Category> level3List = categoryMapper.findByLevel(3);

        Map<Long, List<Category>> l3ByL2 = level3List.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        Map<Long, List<Category>> l2ByL1 = level2List.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        return level1List.stream().map(l1 -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(l1.getId());
            dto.setName(l1.getName());
            dto.setDescription(l1.getDescription());

            List<Category> childrenL2 = l2ByL1.getOrDefault(l1.getId(), new ArrayList<>());
            List<SubCategoryDTO> subDTOs = childrenL2.stream().map(l2 -> {
                SubCategoryDTO subDTO = new SubCategoryDTO();
                subDTO.setId(l2.getId());
                subDTO.setName(l2.getName());
                subDTO.setDescription(l2.getDescription());

                List<Category> childrenL3 = l3ByL2.getOrDefault(l2.getId(), new ArrayList<>());
                List<ThirdCategoryDTO> thirdDTOs = childrenL3.stream().map(l3 -> {
                    ThirdCategoryDTO thirdDTO = new ThirdCategoryDTO();
                    thirdDTO.setId(l3.getId());
                    thirdDTO.setName(l3.getName());
                    thirdDTO.setDescription(l3.getDescription());
                    return thirdDTO;
                }).collect(Collectors.toList());
                subDTO.setThirdCategories(thirdDTOs);
                return subDTO;
            }).collect(Collectors.toList());
            dto.setSubCategories(subDTOs);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MainCategory> getAllMainCategories() {
        return categoryMapper.findByLevel(1).stream()
                .map(this::toMainCategory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategory> getSubCategoriesByMainId(Long mainId) {
        return categoryMapper.findByParentId(mainId).stream()
                .filter(c -> Integer.valueOf(2).equals(c.getLevel()))
                .map(this::toSubCategory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdCategory> getThirdCategoriesBySubId(Long subId) {
        return categoryMapper.findByParentId(subId).stream()
                .filter(c -> Integer.valueOf(3).equals(c.getLevel()))
                .map(this::toThirdCategory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:main", key = "#id")
    public MainCategory getMainCategoryById(Long id) {
        Category c = getById(id);
        if (!Integer.valueOf(1).equals(c.getLevel())) {
            throw new ResourceNotFoundException("Main category not found with id: " + id);
        }
        return toMainCategory(c);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:sub", key = "#id")
    public SubCategory getSubCategoryById(Long id) {
        Category c = getById(id);
        if (!Integer.valueOf(2).equals(c.getLevel())) {
            throw new ResourceNotFoundException("Sub category not found with id: " + id);
        }
        return toSubCategory(c);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:third", key = "#id")
    public ThirdCategory getThirdCategoryById(Long id) {
        Category c = getById(id);
        if (!Integer.valueOf(3).equals(c.getLevel())) {
            throw new ResourceNotFoundException("Third category not found with id: " + id);
        }
        return toThirdCategory(c);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public MainCategory createMainCategory(MainCategory mainCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category c = new Category();
            c.setName(mainCategory.getName());
            c.setDescription(mainCategory.getDescription());
            c.setParentId(null);
            c.setLevel(1);
            c.setSortOrder(0);
            c.setStatus(1);
            c.setSeoDescription(mainCategory.getDescription());
            c.setCreatedAt(java.time.LocalDateTime.now());
            c.setUpdatedAt(java.time.LocalDateTime.now());
            c.setDeleted(0);
            c.setPath("");
            c.setPathName("");
            categoryMapper.insert(c);

            c.setPath(String.valueOf(c.getId()));
            c.setPathName(c.getName());
            categoryMapper.update(c);
            categoryMapper.insertClosure(c.getId(), c.getId(), 0);

            mainCategory.setId(c.getId());
            mainCategory.setCreatedAt(c.getCreatedAt());
            mainCategory.setUpdatedAt(c.getUpdatedAt());
            return mainCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public SubCategory createSubCategory(Long mainCategoryId, SubCategory subCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category parent = categoryMapper.findById(mainCategoryId);
            if (parent == null || !Integer.valueOf(1).equals(parent.getLevel())) {
                throw new ResourceNotFoundException("Main category not found with id: " + mainCategoryId);
            }
            Category c = new Category();
            c.setName(subCategory.getName());
            c.setDescription(subCategory.getDescription());
            c.setParentId(mainCategoryId);
            c.setLevel(2);
            c.setSortOrder(0);
            c.setStatus(1);
            c.setSeoDescription(subCategory.getDescription());
            c.setCreatedAt(java.time.LocalDateTime.now());
            c.setUpdatedAt(java.time.LocalDateTime.now());
            c.setDeleted(0);
            categoryMapper.insert(c);

            c.setPath((parent.getPath() == null ? "" : parent.getPath()) + "," + c.getId());
            c.setPathName((parent.getPathName() == null ? "" : parent.getPathName()) + " / " + c.getName());
            categoryMapper.update(c);

            categoryMapper.insertClosure(c.getId(), c.getId(), 0);
            categoryMapper.insertClosure(parent.getId(), c.getId(), 1);
            insertAncestorClosures(parent.getId(), c.getId(), 2);

            subCategory.setId(c.getId());
            subCategory.setMainCategoryId(mainCategoryId);
            subCategory.setCreatedAt(c.getCreatedAt());
            subCategory.setUpdatedAt(c.getUpdatedAt());
            return subCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public ThirdCategory createThirdCategory(Long subCategoryId, ThirdCategory thirdCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category parent = categoryMapper.findById(subCategoryId);
            if (parent == null || !Integer.valueOf(2).equals(parent.getLevel())) {
                throw new ResourceNotFoundException("Sub category not found with id: " + subCategoryId);
            }
            Category c = new Category();
            c.setName(thirdCategory.getName());
            c.setDescription(thirdCategory.getDescription());
            c.setParentId(subCategoryId);
            c.setLevel(3);
            c.setSortOrder(0);
            c.setStatus(1);
            c.setSeoDescription(thirdCategory.getDescription());
            c.setCreatedAt(java.time.LocalDateTime.now());
            c.setUpdatedAt(java.time.LocalDateTime.now());
            c.setDeleted(0);
            categoryMapper.insert(c);

            c.setPath((parent.getPath() == null ? "" : parent.getPath()) + "," + c.getId());
            c.setPathName((parent.getPathName() == null ? "" : parent.getPathName()) + " / " + c.getName());
            categoryMapper.update(c);

            categoryMapper.insertClosure(c.getId(), c.getId(), 0);
            categoryMapper.insertClosure(parent.getId(), c.getId(), 1);
            insertAncestorClosures(parent.getId(), c.getId(), 2);

            thirdCategory.setId(c.getId());
            thirdCategory.setSubCategoryId(subCategoryId);
            thirdCategory.setCreatedAt(c.getCreatedAt());
            thirdCategory.setUpdatedAt(c.getUpdatedAt());
            return thirdCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public MainCategory updateMainCategory(Long id, MainCategory mainCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(1).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Main category not found with id: " + id);
            }
            existing.setName(mainCategory.getName());
            existing.setSeoDescription(mainCategory.getDescription());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            categoryMapper.update(existing);

            mainCategory.setId(existing.getId());
            mainCategory.setCreatedAt(existing.getCreatedAt());
            mainCategory.setUpdatedAt(existing.getUpdatedAt());
            return mainCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public SubCategory updateSubCategory(Long id, SubCategory subCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(2).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Sub category not found with id: " + id);
            }
            existing.setName(subCategory.getName());
            existing.setSeoDescription(subCategory.getDescription());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            categoryMapper.update(existing);

            subCategory.setId(existing.getId());
            subCategory.setMainCategoryId(existing.getParentId());
            subCategory.setCreatedAt(existing.getCreatedAt());
            subCategory.setUpdatedAt(existing.getUpdatedAt());
            return subCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public ThirdCategory updateThirdCategory(Long id, ThirdCategory thirdCategory) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(3).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Third category not found with id: " + id);
            }
            existing.setName(thirdCategory.getName());
            existing.setSeoDescription(thirdCategory.getDescription());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            categoryMapper.update(existing);

            thirdCategory.setId(existing.getId());
            thirdCategory.setSubCategoryId(existing.getParentId());
            thirdCategory.setCreatedAt(existing.getCreatedAt());
            thirdCategory.setUpdatedAt(existing.getUpdatedAt());
            return thirdCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteMainCategory(Long id) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(1).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Main category not found with id: " + id);
            }
            categoryMapper.deleteSubtreeByAncestorId(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteSubCategory(Long id) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(2).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Sub category not found with id: " + id);
            }
            categoryMapper.deleteSubtreeByAncestorId(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteThirdCategory(Long id) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            if (!Integer.valueOf(3).equals(existing.getLevel())) {
                throw new ResourceNotFoundException("Third category not found with id: " + id);
            }
            categoryMapper.deleteById(id);
            categoryMapper.deleteClosureByDescendantId(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public Category createCategory(Category category) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            category.setCreatedAt(now);
            category.setUpdatedAt(now);
            if (category.getStatus() == null)
                category.setStatus(1);
            if (category.getDeleted() == null)
                category.setDeleted(0);
            if (category.getSortOrder() == null)
                category.setSortOrder(0);
            if (category.getLevel() == null) {
                category.setLevel(category.getParentId() == null ? 1 : 2);
            }

            String basePath = "";
            String basePathName = "";
            Integer baseDepth = 0;
            if (category.getParentId() != null) {
                Category parent = getById(category.getParentId());
                basePath = parent.getPath() == null ? "" : parent.getPath();
                basePathName = parent.getPathName() == null ? "" : parent.getPathName();
                category.setLevel(parent.getLevel() + 1);
                baseDepth = 1;
            }

            categoryMapper.insert(category);

            category.setPath((basePath.isEmpty() ? "" : basePath + ",") + category.getId());
            category.setPathName((basePathName.isEmpty() ? "" : basePathName + " / ") + category.getName());
            categoryMapper.update(category);

            categoryMapper.insertClosure(category.getId(), category.getId(), 0);
            if (category.getParentId() != null) {
                categoryMapper.insertClosure(category.getParentId(), category.getId(), baseDepth);
                insertAncestorClosures(category.getParentId(), category.getId(), baseDepth + 1);
            }
            return category;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public Category updateCategory(Long id, Category category) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            Category existing = getById(id);
            existing.setName(category.getName());
            if (category.getParentId() != null)
                existing.setParentId(category.getParentId());
            if (category.getLevel() != null)
                existing.setLevel(category.getLevel());
            if (category.getSortOrder() != null)
                existing.setSortOrder(category.getSortOrder());
            if (category.getStatus() != null)
                existing.setStatus(category.getStatus());
            if (category.getIconUrl() != null)
                existing.setIconUrl(category.getIconUrl());
            if (category.getSeoKeywords() != null)
                existing.setSeoKeywords(category.getSeoKeywords());
            if (category.getSeoDescription() != null)
                existing.setSeoDescription(category.getSeoDescription());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            categoryMapper.update(existing);
            return existing;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteCategory(Long id) {
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            getById(id);
            categoryMapper.deleteSubtreeByAncestorId(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    private void insertAncestorClosures(Long directParentId, Long descendantId, int startDepth) {
        List<Category> ancestors = categoryMapper.findAncestorsByDescendantId(directParentId);
        int depth = startDepth;
        for (Category ancestor : ancestors) {
            categoryMapper.insertClosure(ancestor.getId(), descendantId, depth);
            depth++;
        }
    }

    private MainCategory toMainCategory(Category c) {
        MainCategory m = new MainCategory();
        m.setId(c.getId());
        m.setName(c.getName());
        m.setDescription(c.getDescription());
        m.setCreatedAt(c.getCreatedAt());
        m.setUpdatedAt(c.getUpdatedAt());
        return m;
    }

    private SubCategory toSubCategory(Category c) {
        SubCategory s = new SubCategory();
        s.setId(c.getId());
        s.setName(c.getName());
        s.setDescription(c.getDescription());
        s.setMainCategoryId(c.getParentId());
        s.setCreatedAt(c.getCreatedAt());
        s.setUpdatedAt(c.getUpdatedAt());
        return s;
    }

    private ThirdCategory toThirdCategory(Category c) {
        ThirdCategory t = new ThirdCategory();
        t.setId(c.getId());
        t.setName(c.getName());
        t.setDescription(c.getDescription());
        t.setSubCategoryId(c.getParentId());
        t.setCreatedAt(c.getCreatedAt());
        t.setUpdatedAt(c.getUpdatedAt());
        return t;
    }
}
