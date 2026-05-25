package com.sportsequipment.mapper;

import com.sportsequipment.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartItemMapper {

    CartItem findById(Long id);

    List<CartItem> findAll();

    void insert(CartItem cartItem);

    void update(CartItem cartItem);

    void deleteById(Long id);

    void deleteByCartId(Long cartId);

    CartItem findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByCartId(Long cartId);
}