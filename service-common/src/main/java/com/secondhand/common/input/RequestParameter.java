package com.secondhand.common.input;

import java.io.Serializable;

public class RequestParameter implements Serializable {
    public static final String DEFAULT_ENCRYPTION_VERSION = "2.0.0";
    private String service;
    private String version;
    private String time;
    private String data;
    private String salt;
    private boolean test = false;
    private String ip;
    private String token;
    private boolean inner;
    private String encryption_version;

    // ---- explicit getters/setters to unblock maven ----
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public boolean isTest() { return test; }
    public void setTest(boolean test) { this.test = test; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isInner() { return inner; }
    public void setInner(boolean inner) { this.inner = inner; }

    public String getEncryption_version() { return encryption_version; }
    public void setEncryption_version(String encryption_version) { this.encryption_version = encryption_version; }

    public boolean encryption(boolean encryption) {
        if (this.encryption_version == null || this.encryption_version.isEmpty()) {
            return false;
        }
        return DEFAULT_ENCRYPTION_VERSION.compareTo(this.encryption_version) < 0 && encryption;
    }
}
