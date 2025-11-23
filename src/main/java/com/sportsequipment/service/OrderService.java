package com.sportsequipment.service;

import com.sportsequipment.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getCurrentUserOrders();
    OrderDTO getOrderById(Long id);
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrderStatus(Long id, String status);
    void deleteOrder(Long id);
}
    