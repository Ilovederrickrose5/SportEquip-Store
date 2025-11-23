package com.sportsequipment.service;

import com.sportsequipment.dto.UserDTO;
import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.entity.User;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    User findById(Long id); // 添加此方法，直接返回User实体
    UserDTO getCurrentUser();
    UserDTO updateUser(Long id, UserUpdateRequest userUpdateRequest);
    void deleteUser(Long id);
    UserDTO changeUserRole(Long id, String role);
    User updateUserRole(Long id, String role); // 添加此方法，用于管理员控制器
    boolean changePassword(String oldPassword, String newPassword);
    User findByUsername(String username);
}
    