package com.animalgym.api.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnMissingBean(ImageStorageService.class)
public class DefaultImageStorageService implements ImageStorageService {

    @Override
    public String uploadImage(MultipartFile file) {
        throw new IllegalStateException("No storage implementation configured. Set app.storage.type=local or cloudinary");
    }

    @Override
    public void deleteImage(String imageUrl) {
    }
}
