package com.sportsequipment.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.entity.User;
import com.sportsequipment.exception.FileStorageException;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.service.FileStorageService;
import com.sportsequipment.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器，处理文件上传和下载相关操作
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UserService userService;
    
    public FileUploadController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    // 上传用户头像 - 保留原有的/api/upload/avatar路径作为兼容
    @PostMapping({"/upload/avatar", "/avatar"})
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User user;
        // 兼容两种获取用户信息的方式
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            user = userService.findByUsername(userDetails.getUsername());
        } else {
            String username = authentication.getName();
            user = userService.findByUsername(username);
        }

        if (file.isEmpty()) {
            throw new FileStorageException("上传的文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("只能上传图片文件");
        }

        try {
            String fileName = fileStorageService.storeFile(file, "avatar");
            String fileUrl = fileStorageService.generateFileUrl(fileName);

            // 更新用户头像
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUsername(user.getUsername());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setAddress(user.getAddress());
            // 保持原有头像不变，但这里我们更新头像
            // 先在user对象上设置新头像
            user.setAvatar(fileUrl);
            // 同时在更新请求中设置新头像
            updateRequest.setAvatar(fileUrl);
            userService.updateUser(user.getId(), updateRequest);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileDownloadUri", fileUrl);
            // 兼容两种响应格式
            response.put("fileUrl", fileUrl);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            response.put("message", "头像上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new FileStorageException("文件上传失败: " + ex.getMessage(), ex);
        }
    }

    // 创建通用的响应映射
    private Map<String, String> createFileResponse(String fileName, MultipartFile file, String fileUrl, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileUrl", fileUrl);
        response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));
        if (message != null) {
            response.put("message", message);
        }
        return response;
    }

    // 通用文件上传接口
    @PostMapping({"/upload/file", "/file"})
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new FileStorageException("上传的文件不能为空");
        }

        try {
            // 使用FileStorageService存储文件
            String fileName = fileStorageService.storeFile(file, "file");
            String fileUrl = fileStorageService.generateFileUrl(fileName);

            return ResponseEntity.ok(createFileResponse(fileName, file, fileUrl, null));
        } catch (Exception ex) {
            throw new FileStorageException("文件上传失败: " + ex.getMessage(), ex);
        }
    }

    // 商品图片上传接口 - 仅管理员可访问
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/upload/product", "/product"})
    public ResponseEntity<?> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "customName", required = false) String customName) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new FileStorageException("上传的商品图片不能为空");
        }

        // 检查文件类型 - 只允许图片文件
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("只能上传图片文件");
        }

        try {
            // 使用FileStorageService存储文件，使用"product"作为前缀，如果提供了自定义名称则使用自定义名称
            String fileName = fileStorageService.storeFile(file, "product", customName);
            String fileUrl = fileStorageService.generateFileUrl(fileName);

            return ResponseEntity.ok(createFileResponse(fileName, file, fileUrl, "商品图片上传成功"));
        } catch (Exception ex) {
            throw new FileStorageException("商品图片上传失败: " + ex.getMessage(), ex);
        }
    }
    
    // 文件下载接口
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}