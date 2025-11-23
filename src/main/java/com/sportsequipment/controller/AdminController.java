package com.sportsequipment.controller;

import com.sportsequipment.dto.UserDTO;
import com.sportsequipment.entity.User;
import com.sportsequipment.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器，处理管理员权限相关的操作
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // 确保只有管理员可以访问
public class AdminController {

    private final UserService userService;
    
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 获取指定用户信息
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        // 验证角色值
        if (!"USER".equals(request.getRole()) && !"ADMIN".equals(request.getRole())) {
            return ResponseEntity.badRequest().body("Invalid role. Role must be either USER or ADMIN");
        }
        
        User updatedUser = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    /**
     * 角色更新请求类
     */
    @Getter
    @Setter
    public static class RoleUpdateRequest {
        private String role;
    }
}