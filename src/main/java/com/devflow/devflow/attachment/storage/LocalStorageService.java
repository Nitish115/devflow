package com.devflow.devflow.attachment.storage;

import com.devflow.devflow.common.exception.AttachmentNotFoundException;
import com.devflow.devflow.common.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${app.storage.local-path:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new InvalidOperationException("Could not initialize upload storage location: " + e.getMessage());
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidOperationException("Failed to store empty file.");
        }

        String rawFilename = file.getOriginalFilename();
        String cleanFilename = rawFilename != null ? StringUtils.cleanPath(rawFilename) : "file";
        
        // Extract base filename without directories
        cleanFilename = Paths.get(cleanFilename).getFileName().toString();
        // Sanitize: allow only letters, digits, dots, hyphens, and underscores
        String sanitizedFilename = cleanFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        String storageKey = UUID.randomUUID().toString() + "_" + sanitizedFilename;

        try {
            Path destinationFile = this.rootLocation.resolve(storageKey).normalize();
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new InvalidOperationException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageKey;
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new InvalidOperationException("Cannot access file outside current directory.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new AttachmentNotFoundException("Could not read file: " + storageKey);
            }
        } catch (MalformedURLException e) {
            throw new AttachmentNotFoundException("Could not read file: " + storageKey);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey).normalize();
            if (file.startsWith(rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            // Log or ignore deletion failure
        }
    }
}
