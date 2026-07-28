package com.secondhand.note.service;

import com.secondhand.common.storage.R2ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Uploads note photos via the shared R2 object storage, compressing them first so
 * they load fast when the listing is viewed.
 */
@Service
public class NotePhotoStorageService {

    private static final String NOTE_PREFIX = "notes";

    /** Longest-side cap for stored photos — large enough for a full-screen detail view. */
    private static final int MAX_STORAGE_DIM = 1600;

    /** JPEG quality for stored photos: a good size/quality balance for listing images. */
    private static final float STORAGE_QUALITY = 0.80f;

    /**
     * Photos already within {@link #MAX_STORAGE_DIM} and under this size are stored as-is.
     * Clients compress before upload, so this makes the server a guardrail that only
     * re-encodes oversized images rather than double-compressing every upload.
     */
    private static final long SKIP_COMPRESS_UNDER_BYTES = 1_000_000L;

    private final R2ObjectStorage storage;
    private final ImageCompressor imageCompressor;

    public NotePhotoStorageService(R2ObjectStorage storage, ImageCompressor imageCompressor) {
        this.storage = storage;
        this.imageCompressor = imageCompressor;
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
            uploadedUrls.add(uploadOne(file, userId));
        }
        return uploadedUrls;
    }

    /**
     * Compresses one photo and stores it, falling back to the untouched original if
     * compression is not possible (e.g. an unsupported format).
     *
     * @param file   the uploaded image
     * @param userId the owning user's id
     * @return the public URL of the stored photo
     */
    private String uploadOne(MultipartFile file, Long userId) {
        ImageCompressor.Result compressed = readAndCompress(file);
        if (compressed != null && compressed.recompressed()) {
            String key = buildObjectKey(userId, toJpegName(file.getOriginalFilename()));
            return storage.upload(key, compressed.bytes(), "image/jpeg");
        }
        return storage.upload(buildObjectKey(userId, file.getOriginalFilename()), file);
    }

    /** Reads the file bytes and compresses them; returns null so the caller stores the original if reading fails. */
    private ImageCompressor.Result readAndCompress(MultipartFile file) {
        try {
            return imageCompressor.compressToJpeg(
                    file.getBytes(), MAX_STORAGE_DIM, STORAGE_QUALITY, SKIP_COMPRESS_UNDER_BYTES);
        } catch (IOException ex) {
            return null;
        }
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

    /** Swaps a filename's extension for ".jpg" so the stored key matches the re-encoded content. */
    private String toJpegName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "photo.jpg";
        }
        int dot = originalFilename.lastIndexOf('.');
        String base = dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
        return base + ".jpg";
    }
}
