package com.sportsequipment.repository;

import com.sportsequipment.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 注意：使用spring-data提供的@NonNull注解（org.springframework.lang.NonNull），与父接口保持一致
    @Override
    @NonNull
    Optional<Product> findById(@NonNull Long id);

    @Override
    @NonNull
    Page<Product> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    List<Product> findAll();

    @Query("SELECT p FROM Product p JOIN FETCH p.thirdCategory tc JOIN FETCH tc.subCategory sc JOIN FETCH sc.mainCategory mc")
    @NonNull
    List<Product> findAllWithCategories();

    @Query("SELECT p FROM Product p JOIN FETCH p.thirdCategory tc JOIN FETCH tc.subCategory sc JOIN FETCH sc.mainCategory mc")
    @NonNull
    Page<Product> findAllWithCategories(@NonNull Pageable pageable);
}