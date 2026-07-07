package com.secondhand.note.service;

import com.secondhand.common.exception.ApiException;
import jakarta.annotation.PreDestroy;
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

    /** Lazily built once and reused across uploads instead of per request. */
    private volatile S3Client s3Client;

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
        S3Client client = s3Client();
        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String key = buildObjectKey(userId, file.getOriginalFilename());
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .contentType(file.getContentType())
                        // Keys are UUID-unique and never rewritten, so browsers and the
                        // CDN can cache the object forever.
                        .cacheControl("public, max-age=31536000, immutable")
                        .build();

                client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
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

        S3Client client = s3Client();
        for (String url : urls) {
            String key = extractObjectKey(url);
            if (!StringUtils.hasText(key)) {
                continue;
            }
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build());
        }
    }

    /**
     * Returns the shared S3 client, building it lazily on first use so a client
     * (and its connection pool) is created once per service, not per request.
     *
     * @return the reused S3 client
     */
    private S3Client s3Client() {
        S3Client client = this.s3Client;
        if (client == null) {
            synchronized (this) {
                client = this.s3Client;
                if (client == null) {
                    client = buildClient();
                    this.s3Client = client;
                }
            }
        }
        return client;
    }

    /**
     * Closes the shared S3 client on shutdown.
     */
    @PreDestroy
    void closeClient() {
        if (s3Client != null) {
            s3Client.close();
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
