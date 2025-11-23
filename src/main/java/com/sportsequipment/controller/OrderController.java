package com.sportsequipment.controller;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器，处理订单的CRUD操作和状态管理
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 获取当前用户的所有订单
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getCurrentUserOrders() {
        return ResponseEntity.ok(orderService.getCurrentUserOrders());
    }

    // 管理员接口：获取所有用户的订单
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 获取订单详情
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // 创建新订单
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    // 管理员接口：更新订单状态
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    // 管理员接口：删除订单
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "订单删除成功");
        response.put("orderId", id.toString());
        return ResponseEntity.ok(response);
    }
}
    