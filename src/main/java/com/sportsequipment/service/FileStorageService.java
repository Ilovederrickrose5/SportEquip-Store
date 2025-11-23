package com.sportsequipment.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.sportsequipment.exception.FileStorageException;

public interface FileStorageService {
    
    String storeFile(MultipartFile file, String prefix) throws FileStorageException;
    
    String storeFile(MultipartFile file, String prefix, String customName) throws FileStorageException;
    
    Resource loadFileAsResource(String fileName) throws FileStorageException;
    
    String generateFileUrl(String fileName);
}