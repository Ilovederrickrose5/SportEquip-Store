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
import com.sportsequipment.util.RedisUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final MainCategoryMapper mainCategoryMapper;
    private final SubCategoryMapper subCategoryMapper;
    private final ThirdCategoryMapper thirdCategoryMapper;
    private final RedisUtil redisUtil;

    public CategoryServiceImpl(MainCategoryMapper mainCategoryMapper,
            SubCategoryMapper subCategoryMapper,
            ThirdCategoryMapper thirdCategoryMapper,
            RedisUtil redisUtil) {
        this.mainCategoryMapper = mainCategoryMapper;
        this.subCategoryMapper = subCategoryMapper;
        this.thirdCategoryMapper = thirdCategoryMapper;
        this.redisUtil = redisUtil;
    }

    // 分类分布式锁键前缀（防止缓存击穿）
    private static final String CATEGORY_LOCK_KEY_PREFIX = "lock:category:";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:list", key = "'all'")
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
    @Cacheable(value = "category:main", key = "#id")
    public MainCategory getMainCategoryById(Long id) {
        MainCategory category = mainCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Main category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:sub", key = "#id")
    public SubCategory getSubCategoryById(Long id) {
        SubCategory category = subCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Sub category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category:third", key = "#id")
    public ThirdCategory getThirdCategoryById(Long id) {
        ThirdCategory category = thirdCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Third category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public MainCategory createMainCategory(MainCategory mainCategory) {
        // 分布式锁，防止缓存击穿
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            mainCategory.setCreatedAt(java.time.LocalDateTime.now());
            mainCategory.setUpdatedAt(java.time.LocalDateTime.now());
            mainCategoryMapper.insert(mainCategory);
            return mainCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public SubCategory createSubCategory(Long mainCategoryId, SubCategory subCategory) {
        // 分布式锁，防止缓存击穿
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            MainCategory mainCategory = getMainCategoryById(mainCategoryId);
            subCategory.setMainCategory(mainCategory);
            subCategory.setCreatedAt(java.time.LocalDateTime.now());
            subCategory.setUpdatedAt(java.time.LocalDateTime.now());
            subCategoryMapper.insert(subCategory);
            return subCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public ThirdCategory createThirdCategory(Long subCategoryId, ThirdCategory thirdCategory) {
        // 分布式锁，防止缓存击穿
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:create";
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            SubCategory subCategory = getSubCategoryById(subCategoryId);
            thirdCategory.setSubCategory(subCategory);
            thirdCategory.setCreatedAt(java.time.LocalDateTime.now());
            thirdCategory.setUpdatedAt(java.time.LocalDateTime.now());
            thirdCategoryMapper.insert(thirdCategory);
            return thirdCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public MainCategory updateMainCategory(Long id, MainCategory mainCategory) {
        // 分布式锁，防止缓存击穿（锁一级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            MainCategory existingMainCategory = getMainCategoryById(id);
            existingMainCategory.setName(mainCategory.getName());
            existingMainCategory.setDescription(mainCategory.getDescription());
            existingMainCategory.setUpdatedAt(java.time.LocalDateTime.now());
            mainCategoryMapper.update(existingMainCategory);
            return existingMainCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public SubCategory updateSubCategory(Long id, SubCategory subCategory) {
        // 分布式锁，防止缓存击穿（锁二级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            SubCategory existingSubCategory = getSubCategoryById(id);
            existingSubCategory.setName(subCategory.getName());
            existingSubCategory.setDescription(subCategory.getDescription());
            existingSubCategory.setUpdatedAt(java.time.LocalDateTime.now());
            subCategoryMapper.update(existingSubCategory);
            return existingSubCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    // 永不过期，用于缓存三级分类详情
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public ThirdCategory updateThirdCategory(Long id, ThirdCategory thirdCategory) {
        // 分布式锁，防止缓存击穿（锁三级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            ThirdCategory existingThirdCategory = getThirdCategoryById(id);
            existingThirdCategory.setName(thirdCategory.getName());
            existingThirdCategory.setDescription(thirdCategory.getDescription());
            existingThirdCategory.setUpdatedAt(java.time.LocalDateTime.now());
            thirdCategoryMapper.update(existingThirdCategory);
            return existingThirdCategory;
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteMainCategory(Long id) {
        // 分布式锁，防止缓存击穿（锁一级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "main:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            getMainCategoryById(id);
            mainCategoryMapper.deleteById(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteSubCategory(Long id) {
        // 分布式锁，防止缓存击穿（锁二级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "sub:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            getSubCategoryById(id);
            subCategoryMapper.deleteById(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "category:list", "category:main", "category:sub", "category:third" }, allEntries = true)
    public void deleteThirdCategory(Long id) {
        // 分布式锁，防止缓存击穿（锁三级分类ID）
        String lockKey = CATEGORY_LOCK_KEY_PREFIX + "third:" + id;
        try {
            boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            getThirdCategoryById(id);
            thirdCategoryMapper.deleteById(id);
        } finally {
            redisUtil.unlock(lockKey);
        }
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
