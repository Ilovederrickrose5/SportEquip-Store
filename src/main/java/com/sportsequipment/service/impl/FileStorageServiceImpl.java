package com.sportsequipment.service.impl;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sportsequipment.config.FileUploadConfig;
import com.sportsequipment.exception.FileStorageException;
import com.sportsequipment.service.FileStorageService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private final String baseUrl;
    private final long maxFileSize;

    public FileStorageServiceImpl(FileUploadConfig fileUploadConfig) {
        this.fileStorageLocation = Paths.get(fileUploadConfig.getUploadDir())
                .toAbsolutePath().normalize();
        
        // 从配置类中获取baseUrl
        this.baseUrl = fileUploadConfig.getServerUrl();
        
        // 从配置类中获取最大文件大小限制
        this.maxFileSize = fileUploadConfig.getMaxFileSize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            
            // Ensure avatar and product subdirectories exist
            Files.createDirectories(this.fileStorageLocation.resolve("avatar"));
            Files.createDirectories(this.fileStorageLocation.resolve("product"));
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }


    public String storeFile(MultipartFile file, String prefix) {
        // 默认使用null作为自定义名称，这样会使用UUID方式
        return storeFile(file, prefix, null);
    }
    
    /**
     * 存储文件，可以指定自定义文件名
     * @param file 上传的文件
     * @param prefix 文件前缀（avatar或product）
     * @param customName 自定义文件名（可选，不包含扩展名），如果为null则使用UUID自动生成
     * @return 存储后的文件相对路径
     */
    public String storeFile(MultipartFile file, String prefix, String customName) {
        // Normalize file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileStorageException("File name cannot be null or empty");
        }
        String originalFileName = StringUtils.cleanPath(originalFilename);
        String fileExtension = getFileExtension(originalFileName);
        
        String uniqueFileName;
        if (customName != null && !customName.trim().isEmpty()) {
            // 使用用户提供的自定义名称，清理并确保唯一性
            String cleanCustomName = StringUtils.cleanPath(customName);
            // 移除可能的扩展名
            if (cleanCustomName.contains(".")) {
                cleanCustomName = cleanCustomName.substring(0, cleanCustomName.lastIndexOf("."));
            }
            uniqueFileName = prefix + "_" + cleanCustomName + "." + fileExtension;
        } else {
            // 使用UUID自动生成唯一文件名
            uniqueFileName = prefix + "_" + UUID.randomUUID() + "." + fileExtension;
        }

        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds the maximum allowed size of " + maxFileSize + " bytes");
        }

        try {
            // 根据prefix创建对应的子目录
            Path subdirectory = this.fileStorageLocation.resolve(prefix);
            Files.createDirectories(subdirectory);
            
            // 检查文件名是否已存在，如果存在则添加数字后缀确保唯一性
            Path targetLocation = subdirectory.resolve(uniqueFileName);
            int counter = 1;
            String baseName = uniqueFileName.substring(0, uniqueFileName.lastIndexOf("."));
            while (Files.exists(targetLocation)) {
                uniqueFileName = baseName + "_" + counter + "." + fileExtension;
                targetLocation = subdirectory.resolve(uniqueFileName);
                counter++;
            }
            
            // Copy file to the target location in the subdirectory
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径，包含子目录
            return prefix + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            // 直接从fileStorageLocation解析相对路径，已包含子目录
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            URI fileUri = filePath.toUri();
            if (fileUri == null) {
                throw new FileStorageException("Could not determine file URI for " + fileName);
            }
            Resource resource = new UrlResource(fileUri);
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }

    public String generateFileUrl(String fileName) {
        // fileName已经包含子目录信息，直接拼接
        return baseUrl + "/upload/" + fileName;
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } catch (Exception e) {
            return "jpg";
        }
    }
}