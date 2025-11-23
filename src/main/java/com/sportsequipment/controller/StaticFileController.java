package com.sportsequipment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsequipment.service.FileStorageService;

/**
 * 静态文件访问控制器，用于直接访问上传的文件
 */
@RestController
@RequestMapping("/api/upload")
public class StaticFileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 直接访问上传的文件
     * 支持的路径格式: /api/upload/{directory}/{filename}
     */
    @GetMapping("/{directory}/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String directory, 
            @PathVariable String fileName) {
        try {
            // 构建完整的文件路径（包含目录）
            String fullPath = directory + "/" + fileName;
            Resource resource = fileStorageService.loadFileAsResource(fullPath);
            
            // 根据文件扩展名确定MIME类型
            String contentType = getContentType(fileName);
            
            // 确保contentType非空
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据文件名确定MIME类型
     */
    private String getContentType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}