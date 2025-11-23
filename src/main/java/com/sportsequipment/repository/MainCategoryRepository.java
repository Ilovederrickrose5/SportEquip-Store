package com.sportsequipment.repository;

import com.sportsequipment.entity.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
}
    