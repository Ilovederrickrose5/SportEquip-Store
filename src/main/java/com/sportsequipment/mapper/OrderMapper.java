package com.sportsequipment.mapper;

import com.sportsequipment.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order findById(Long id);

    List<Order> findAll();

    void insert(Order order);

    void update(Order order);

    void deleteById(Long id);

    List<Order> findByUserId(Long userId);
}