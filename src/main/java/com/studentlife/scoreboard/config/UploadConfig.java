package com.studentlife.scoreboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuration for static file uploads.
 * Maps the /uploads/** URL path to the configured upload directory on the filesystem.
 * Allows uploaded images to be served as static resources.
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {
    
    // Upload directory path from application properties
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    /**
     * Registers a resource handler to serve uploaded files.
     * Maps /uploads/** URLs to the file system upload directory.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
