package com.sportsequipment.repository;

import com.sportsequipment.entity.ThirdCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThirdCategoryRepository extends JpaRepository<ThirdCategory, Long> {
    List<ThirdCategory> findBySubCategoryId(Long subCategoryId);
}
    