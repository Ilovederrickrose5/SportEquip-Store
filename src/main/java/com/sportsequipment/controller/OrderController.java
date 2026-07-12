package com.sportsequipment.controller;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.PageResponse;
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

    /**
     * 获取当前用户的订单列表（支持分页和状态筛选）
     * 
     * @param status 订单状态筛选（可选）：PENDING/PAID/SHIPPED/COMPLETED/CANCELLED
     * @param page 页码，从0开始（默认0）
     * @param size 每页数量（默认10）
     */
    @GetMapping
    public ResponseEntity<PageResponse<OrderDTO>> getCurrentUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getCurrentUserOrdersWithPagination(status, page, size));
    }

    /**
     * 获取当前用户的所有订单（不分页，保留原有接口兼容性）
     */
    @GetMapping("/list")
    public ResponseEntity<List<OrderDTO>> getCurrentUserOrdersList() {
        return ResponseEntity.ok(orderService.getCurrentUserOrders());
    }

    /**
     * 管理员接口：获取所有用户的订单列表（支持分页和状态筛选）
     * 
     * @param status 订单状态筛选（可选）
     * @param page 页码，从0开始（默认0）
     * @param size 每页数量（默认10）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<OrderDTO>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getAllOrdersWithPagination(status, page, size));
    }

    /**
     * 管理员接口：获取所有用户的订单（不分页，保留原有接口兼容性）
     */
    @GetMapping("/all/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrdersList() {
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
    