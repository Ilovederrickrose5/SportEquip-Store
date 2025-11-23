package com.sportsequipment.controller;

import com.sportsequipment.dto.UserDTO;
import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.service.UserService;
import com.sportsequipment.util.PasswordValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器，处理用户相关的CRUD操作和权限管理
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 管理员接口：获取所有用户
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 获取当前登录用户信息
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // 根据ID获取用户（管理员可获取任何用户，普通用户只能获取自己）
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 更新用户信息（管理员可更新任何用户，普通用户只能更新自己）
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateRequest));
    }

    // 管理员接口：删除用户
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 管理员接口：更改用户角色
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        return ResponseEntity.ok(userService.changeUserRole(id, role));
    }

    // 修改密码（需要验证旧密码）
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        // 获取当前用户角色
        UserDTO currentUser = userService.getCurrentUser();
        String role = currentUser.getRole();
        
        // 验证新密码强度（根据用户角色进行不同的验证）
        if (!PasswordValidator.isValid(newPassword, role)) {
            return ResponseEntity.badRequest().body(PasswordValidator.getValidationMessage());
        }
        
        boolean success = userService.changePassword(oldPassword, newPassword);
        if (success) {
            return ResponseEntity.ok().body("Password changed successfully");
        } else {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }
    }
}
    