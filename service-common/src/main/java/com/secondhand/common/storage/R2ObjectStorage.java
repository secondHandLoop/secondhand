package com.secondhand.common.storage;

import com.secondhand.common.exception.ApiException;
import org.springframework.beans.factory.DisposableBean;
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
import java.util.List;

/**
 * Shared Cloudflare R2 (S3-compatible) object storage.
 *
 * <p>Owns a single lazily-built {@link S3Client} (reused across requests instead
 * of one per call) and the previously-duplicated URL/key/sanitize logic. Callers
 * build their own object key (avatars, notes/date-path, …) and delegate the
 * upload/delete here.
 */
@Service
public class R2ObjectStorage implements DisposableBean {

    private final R2StorageProperties properties;
    private volatile S3Client s3Client;

    public R2ObjectStorage(R2StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Uploads one object under the given key and returns its public URL.
     *
     * @param objectKey the fully built object key
     * @param file      the multipart file to store
     * @return the public URL of the stored object
     */
    public String upload(String objectKey, MultipartFile file) {
        try {
            return upload(objectKey, file.getBytes(), file.getContentType());
        } catch (IOException ex) {
            throw new ApiException("上传失败: " + ex.getMessage(), 500);
        }
    }

    /**
     * Uploads raw bytes under the given key and returns its public URL. Lets callers
     * store content they produced in memory (e.g. a compressed image) rather than a
     * {@link MultipartFile}.
     *
     * @param objectKey   the fully built object key
     * @param bytes       the object content
     * @param contentType the object's content type, e.g. "image/jpeg"
     * @return the public URL of the stored object
     */
    public String upload(String objectKey, byte[] bytes, String contentType) {
        validateConfigured();
        s3Client().putObject(
                PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(objectKey)
                        .contentType(contentType)
                        // Keys are UUID-unique and never rewritten; cache forever.
                        .cacheControl("public, max-age=31536000, immutable")
                        .build(),
                RequestBody.fromBytes(bytes));
        return buildPublicUrl(objectKey);
    }

    /**
     * Deletes previously uploaded objects whose URLs map back to this bucket.
     *
     * @param urls the public URLs to delete
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
     * Replaces unsafe filename characters before it is embedded in an object key.
     *
     * @param originalFilename the client-supplied filename
     * @return a filesystem/URL-safe name
     */
    public String sanitizeFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "photo";
        }
        return originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
    }

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

    @Override
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
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
