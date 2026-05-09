package org.example.tourism.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    
    @Value("${app.upload.dir}")
    private String uploadDir;


    public List<String> uploadHotelImages(List<MultipartFile> files) {
        return uploadFiles(files, "hotels");
    }


    public List<String> uploadRoomTypeImages(List<MultipartFile> files) {
        return uploadFiles(files, "room-types");
    }


    public List<String> uploadFiles(List<MultipartFile> files, String subFolder) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new EmptyFileException("Cannot upload empty file");
            }
            String fileUrl = uploadSingleFile(file, subFolder);
            if (fileUrl != null) {
                fileUrls.add(fileUrl);
            }
        }

        return fileUrls;
    }


    public String uploadSingleFile(MultipartFile file, String subFolder) {
        try {
            Path uploadPath;
            if (subFolder != null && !subFolder.trim().isEmpty()) {
                uploadPath = Paths.get(uploadDir, subFolder);
            } else {
                uploadPath = Paths.get(uploadDir);
            }
            if (file.isEmpty()) {
                throw new EmptyFileException("Cannot upload empty file");
            }

            Path absolutePath = uploadPath.toAbsolutePath().normalize();

            log.info("=== UPLOADING FILE ===");
            log.info("Upload directory configured: {}", uploadDir);
            log.info("Subfolder: {}", subFolder);
            log.info("Full path: {}", absolutePath);

            if (!Files.exists(absolutePath)) {
                Files.createDirectories(absolutePath);
                log.info("Created directory: {}", absolutePath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = absolutePath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved to: {}", filePath);
            log.info("File size: {} bytes", Files.size(filePath));

            String fileUrl;
            if (subFolder != null && !subFolder.trim().isEmpty()) {
                fileUrl = "/uploads/" + subFolder + "/" + filename;
            } else {
                fileUrl = "/uploads/" + filename;
            }

            log.info("Access URL: http://localhost:8080{}", fileUrl);
            log.info("======================");

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            return null;
        }
    }


    public boolean deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace("/uploads/", "");

            Path filePath = Paths.get(uploadDir, relativePath);
            Path absolutePath = filePath.toAbsolutePath().normalize();

            log.info("Attempting to delete file: {}", absolutePath);

            if (!Files.exists(absolutePath)) {
                log.warn("File does not exist: {}", absolutePath);
                return false;
            }

            boolean deleted = Files.deleteIfExists(absolutePath);

            if (deleted) {
                log.info("Successfully deleted file: {}", absolutePath);
            } else {
                log.warn("Failed to delete file: {}", absolutePath);
            }

            return deleted;

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return false;
        }
    }

}