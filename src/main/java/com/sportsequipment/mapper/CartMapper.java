package com.sportsequipment.mapper;

import com.sportsequipment.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartMapper {

    Cart findById(Long id);

    List<Cart> findAll();

    void insert(Cart cart);

    void update(Cart cart);

    void deleteById(Long id);

    Cart findByUserId(Long userId);
}