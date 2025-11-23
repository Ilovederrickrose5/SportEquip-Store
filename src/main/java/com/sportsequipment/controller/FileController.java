package com.sportsequipment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sportsequipment.entity.User;
import com.sportsequipment.dto.UserUpdateRequest;
import com.sportsequipment.service.FileStorageService;
import com.sportsequipment.service.UserService;
import com.sportsequipment.exception.FileUploadException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (file.isEmpty()) {
            throw new FileUploadException("上传的文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("只能上传图片文件");
        }

        String fileName = fileStorageService.storeFile(file, "avatar");
        String fileUrl = fileStorageService.generateFileUrl(fileName);

        // 更新用户头像
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername(user.getUsername());
        updateRequest.setEmail(user.getEmail());
        updateRequest.setPhone(user.getPhone());
        updateRequest.setAddress(user.getAddress());
        updateRequest.setAvatar(fileUrl);
        userService.updateUser(user.getId(), updateRequest);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileDownloadUri", fileUrl);
        response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}