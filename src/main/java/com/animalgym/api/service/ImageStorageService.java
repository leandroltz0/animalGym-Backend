package com.animalgym.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String uploadImage(MultipartFile file);
    void deleteImage(String imageUrl);
}
