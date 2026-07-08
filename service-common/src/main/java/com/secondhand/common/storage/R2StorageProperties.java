package com.secondhand.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudflare R2 (S3-compatible) connection settings, shared by every service.
 *
 * <p>Object-key prefixes are supplied by each caller (avatars, notes, …) rather
 * than living here, so this holds only the connection fields.
 */
@Component
@ConfigurationProperties(prefix = "storage.r2")
public class R2StorageProperties {
    private boolean enabled;
    private String endpoint;
    private String region = "auto";
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String publicBaseUrl;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
}
