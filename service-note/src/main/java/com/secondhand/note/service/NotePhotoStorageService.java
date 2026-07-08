package com.secondhand.note.service;

import com.secondhand.common.storage.R2ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Uploads note photos via the shared R2 object storage.
 */
@Service
public class NotePhotoStorageService {

    private static final String NOTE_PREFIX = "notes";

    private final R2ObjectStorage storage;

    public NotePhotoStorageService(R2ObjectStorage storage) {
        this.storage = storage;
    }

    /**
     * Uploads the provided note photos and returns their public URLs.
     *
     * @param files  the uploaded images
     * @param userId the owning user's id
     * @return the public URLs of the stored photos
     */
    public List<String> uploadPhotos(List<MultipartFile> files, Long userId) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            uploadedUrls.add(storage.upload(buildObjectKey(userId, file.getOriginalFilename()), file));
        }
        return uploadedUrls;
    }

    /**
     * Deletes previously uploaded note photos by their public URLs.
     *
     * @param urls the URLs to delete
     */
    public void deleteByUrls(List<String> urls) {
        storage.deleteByUrls(urls);
    }

    private String buildObjectKey(Long userId, String originalFilename) {
        LocalDate now = LocalDate.now();
        return NOTE_PREFIX + "/"
                + userId + "/"
                + now.getYear() + "/"
                + now.getMonthValue() + "/"
                + now.getDayOfMonth() + "/"
                + UUID.randomUUID() + "-"
                + storage.sanitizeFilename(originalFilename);
    }
}
