package com.secondhand.user.service;

import com.secondhand.common.exception.ApiException;
import com.secondhand.common.storage.R2ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Uploads user avatar images via the shared R2 object storage.
 */
@Service
public class UserPhotoUploadService {

    private static final String AVATAR_PREFIX = "avatars";

    private final R2ObjectStorage storage;

    public UserPhotoUploadService(R2ObjectStorage storage) {
        this.storage = storage;
    }

    /**
     * Stores one avatar image for the current user.
     *
     * @param file   the uploaded image
     * @param userId the owning user's id
     * @return the public URL of the stored avatar
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("请选择图片文件", 400);
        }
        String key = AVATAR_PREFIX + "/" + userId + "/" + UUID.randomUUID() + "-"
                + storage.sanitizeFilename(file.getOriginalFilename());
        return storage.upload(key, file);
    }
}
