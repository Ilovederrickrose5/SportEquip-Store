package com.sportsequipment.service;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.PageResponse;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getCurrentUserOrders();
    OrderDTO getOrderById(Long id);
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrderStatus(Long id, String status);
    void deleteOrder(Long id);
    
    /**
     * 分页查询当前用户订单
     * 
     * @param status 订单状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 分页订单数据
     */
    PageResponse<OrderDTO> getCurrentUserOrdersWithPagination(String status, int page, int size);
    
    /**
     * 分页查询所有订单（管理员使用）
     * 
     * @param status 订单状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 分页订单数据
     */
    PageResponse<OrderDTO> getAllOrdersWithPagination(String status, int page, int size);
}
    