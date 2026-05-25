package com.sportsequipment.mapper;

import com.sportsequipment.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    Favorite findById(Long id);

    List<Favorite> findAll();

    void insert(Favorite favorite);

    void update(Favorite favorite);

    void deleteById(Long id);

    List<Favorite> findByUserId(Long userId);

    Favorite findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    Integer existsByUserIdAndProductId(Long userId, Long productId);
}