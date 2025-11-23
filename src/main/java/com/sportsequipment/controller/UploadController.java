package com.sportsequipment.controller;

import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.FileUploadException;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.FileStorageService;
import com.sportsequipment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;

    // 上传用户头像
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new FileUploadException("上传的文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("只能上传图片文件");
        }

        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            // 使用FileStorageService存储文件
            String fileName = fileStorageService.storeFile(file, "avatar");
            String fileUrl = fileStorageService.generateFileUrl(fileName);

            // 更新用户头像URL
            User user = userService.findByUsername(userDetails.getUsername());
            // 创建 UserUpdateRequest 对象，仅更新头像字段
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUsername(user.getUsername());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setAddress(user.getAddress());
            updateRequest.setAvatar(fileUrl);
            userService.updateUser(user.getId(), updateRequest);

            // 返回上传成功信息和文件URL
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            response.put("message", "头像上传成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new FileUploadException("文件上传失败: " + ex.getMessage());
        }
    }

    // 通用文件上传接口
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new FileUploadException("上传的文件不能为空");
        }

        try {
            // 使用FileStorageService存储文件
            String fileName = fileStorageService.storeFile(file, "file");
            String fileUrl = fileStorageService.generateFileUrl(fileName);

            // 返回上传成功信息和文件URL
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new FileUploadException("文件上传失败: " + ex.getMessage());
        }
    }
}