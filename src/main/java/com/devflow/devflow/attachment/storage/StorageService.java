package com.devflow.devflow.attachment.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file);

    Resource loadAsResource(String storageKey);

    void delete(String storageKey);
}
