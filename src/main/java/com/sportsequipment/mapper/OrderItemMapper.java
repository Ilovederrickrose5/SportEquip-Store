package com.sportsequipment.mapper;

import com.sportsequipment.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    OrderItem findById(Long id);

    List<OrderItem> findAll();

    void insert(OrderItem orderItem);

    void update(OrderItem orderItem);

    void deleteById(Long id);

    List<OrderItem> findByOrderId(Long orderId);
}