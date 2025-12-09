package com.studentlife.scoreboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }
    
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
