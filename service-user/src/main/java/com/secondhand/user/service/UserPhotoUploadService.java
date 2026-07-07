package com.secondhand.user.service;

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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Uploads user avatar images to object storage and returns their public URL.
 */
@Service
public class UserPhotoUploadService {

    private final R2StorageProperties properties;

    /** Lazily built once and reused across uploads instead of per request. */
    private volatile S3Client s3Client;

    public UserPhotoUploadService(R2StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Stores one avatar image for the current user.
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("请选择图片文件", 400);
        }
        if (!properties.isEnabled()) {
            throw new ApiException("图片存储未启用", 500);
        }

        String key = properties.getAvatarPrefix() + "/" + userId + "/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

        try {
            s3Client().putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .contentType(file.getContentType())
                            // UUID keys are never rewritten; cache forever.
                            .cacheControl("public, max-age=31536000, immutable")
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException ex) {
            throw new ApiException("上传失败: " + ex.getMessage(), 500);
        }

        return buildUrl(key);
    }

    /**
     * Returns the shared S3 client, building it lazily on first use so a client
     * (and its connection pool) is created once per service, not per upload.
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

    /**
     * Creates an S3-compatible client configured for the current R2 bucket.
     */
    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .forcePathStyle(true)
                .build();
    }

    /**
     * Converts an object key into the public URL exposed to clients.
     */
    private String buildUrl(String key) {
        String base = StringUtils.hasText(properties.getPublicBaseUrl())
                ? properties.getPublicBaseUrl().replaceAll("/$", "")
                : properties.getEndpoint().replaceAll("/$", "") + "/" + properties.getBucket();
        return base + "/" + key;
    }

    /**
     * Replaces unsafe filename characters before storing user uploads.
     */
    private String sanitize(String name) {
        if (!StringUtils.hasText(name)) return "photo";
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
