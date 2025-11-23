package com.sportsequipment.service.impl;

import com.sportsequipment.dto.UserDTO;
import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.repository.UserRepository;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = findById(id);
        
        // 检查当前用户是否有权限查看该用户信息
        checkUserAccess(user.getId());
        
        return mapToUserDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Long userId = userDetails.getId();
        if (userId == null) {
            throw new IllegalStateException("User ID cannot be null");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return mapToUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        // 检查当前用户是否有权限更新该用户信息
        checkUserAccess(id);
        
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // 只更新非null的字段，避免覆盖现有数据
        if (userUpdateRequest.getUsername() != null) {
            existingUser.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getEmail() != null) {
            existingUser.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getPhone() != null) {
            existingUser.setPhone(userUpdateRequest.getPhone());
        } else {
            // 允许清除电话号码
            existingUser.setPhone("");
        }
        if (userUpdateRequest.getAddress() != null) {
            existingUser.setAddress(userUpdateRequest.getAddress());
        } else {
            // 允许清除地址
            existingUser.setAddress("");
        }
        // 如果提供了新的头像，则更新头像
        if (userUpdateRequest.getAvatar() != null) {
            existingUser.setAvatar(userUpdateRequest.getAvatar());
        }
        
        // 如果提供了新密码且不为空，则更新密码
        if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        return mapToUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Override
    @Transactional
    public UserDTO changeUserRole(Long id, String role) {
        // 检查当前用户是否为管理员
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (!userDetails.getRole().equals("ADMIN")) {
            throw new UnauthorizedException("只有管理员可以修改用户角色");
        }
        
        return mapToUserDTO(updateUserRole(id, role));
    }
    
    @Override
    @Transactional
    public User updateUserRole(Long id, String role) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("无效的角色。角色必须是USER或ADMIN");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setRole(role);
        return userRepository.save(user);
    }

    // 检查用户是否有权限访问资源
    private void checkUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 只有管理员或用户本人可以访问
        if (!userDetails.getRole().equals("ADMIN") && !userDetails.getId().equals(userId)) {
            throw new UnauthorizedException("您无权访问该资源");
        }
    }

    // 转换实体到DTO
    private UserDTO mapToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setAddress(user.getAddress());
        userDTO.setRole(user.getRole());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setAvatar(user.getAvatar());
        return userDTO;
    }

    @Override
    @Transactional
    public boolean changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Long userId = userDetails.getId();
        if (userId == null) {
            throw new IllegalStateException("User ID cannot be null");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 加密新密码并更新
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
    