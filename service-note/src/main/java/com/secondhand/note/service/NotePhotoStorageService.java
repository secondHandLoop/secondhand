package com.secondhand.note.service;

import com.secondhand.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Uploads note photos to object storage and translates stored URLs back to object keys.
 */
@Service
public class NotePhotoStorageService {

    private final R2StorageProperties properties;

    public NotePhotoStorageService(R2StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Uploads the provided note photos and returns their public URLs.
     */
    public List<String> uploadPhotos(List<MultipartFile> files, Long userId) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        validateConfigured();

        List<String> uploadedUrls = new ArrayList<>();
        try (S3Client s3Client = buildClient()) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String key = buildObjectKey(userId, file.getOriginalFilename());
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .contentType(file.getContentType())
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
                uploadedUrls.add(buildPublicUrl(key));
            }
        } catch (IOException ex) {
            throw new ApiException("上传图片失败: " + ex.getMessage(), 500);
        }

        return uploadedUrls;
    }

    /**
     * Deletes previously uploaded note photos when their URLs map back to this bucket.
     */
    public void deleteByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty() || !properties.isEnabled()) {
            return;
        }

        try (S3Client s3Client = buildClient()) {
            for (String url : urls) {
                String key = extractObjectKey(url);
                if (!StringUtils.hasText(key)) {
                    continue;
                }
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .build());
            }
        }
    }

    private S3Client buildClient() {
        validateConfigured();
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .forcePathStyle(true)
                .build();
    }

    private void validateConfigured() {
        if (!properties.isEnabled()) {
            throw new ApiException("R2 存储未启用，请先配置 storage.r2", 500);
        }
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getBucket())
                || !StringUtils.hasText(properties.getAccessKey())
                || !StringUtils.hasText(properties.getSecretKey())) {
            throw new ApiException("R2 配置不完整，请补全 endpoint/bucket/accessKey/secretKey", 500);
        }
    }

    private String buildObjectKey(Long userId, String originalFilename) {
        LocalDate now = LocalDate.now();
        String safeFilename = sanitizeFilename(originalFilename);
        return properties.getNotePrefix() + "/"
                + userId + "/"
                + now.getYear() + "/"
                + now.getMonthValue() + "/"
                + now.getDayOfMonth() + "/"
                + UUID.randomUUID() + "-"
                + safeFilename;
    }

    private String sanitizeFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "photo";
        }
        return originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String buildPublicUrl(String key) {
        String baseUrl = StringUtils.hasText(properties.getPublicBaseUrl())
                ? trimTrailingSlash(properties.getPublicBaseUrl())
                : trimTrailingSlash(properties.getEndpoint()) + "/" + properties.getBucket();
        return baseUrl + "/" + key;
    }

    private String extractObjectKey(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        String publicPrefix = trimTrailingSlash(properties.getPublicBaseUrl());
        if (StringUtils.hasText(publicPrefix) && url.startsWith(publicPrefix + "/")) {
            return url.substring((publicPrefix + "/").length());
        }

        String endpointPrefix = trimTrailingSlash(properties.getEndpoint()) + "/" + properties.getBucket() + "/";
        if (url.startsWith(endpointPrefix)) {
            return url.substring(endpointPrefix.length());
        }

        return null;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
