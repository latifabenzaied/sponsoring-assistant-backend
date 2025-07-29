package com.cercinaai.campaignservice.service;


import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String uploadDir = "uploads/";

    private String saveImageToLocal(MultipartFile file) throws IOException {
        String uploadsDir = "uploads/";
        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(uploadsDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadsDir + fileName;
    }

}
