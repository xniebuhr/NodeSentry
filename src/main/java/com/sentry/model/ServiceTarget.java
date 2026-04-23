package com.sentry.model;

import java.time.LocalDateTime;

/**
 * Represents a specific web service endpoint being monitored by the system
 * Consists of only data, getters, and setters, all logic handled by engine
 */
public class ServiceTarget {
    private String url;
    private String authToken;
    private int lastStatusCode;
    private long lastLatencyMs;
    private LocalDateTime lastCheck;
    private boolean isUp;
    private String serverSignature;
    private boolean alertSent = false;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public int getLastStatusCode() { return lastStatusCode; }
    public void setLastStatusCode(int lastStatusCode) { this.lastStatusCode = lastStatusCode; }
    public long getLastLatencyMs() { return lastLatencyMs; }
    public void setLastLatencyMs(long lastLatencyMs) { this.lastLatencyMs = lastLatencyMs; }
    public LocalDateTime getLastCheck() { return lastCheck; }
    public void setLastCheck(LocalDateTime lastCheck) { this.lastCheck = lastCheck; }
    public boolean isUp() { return isUp; }
    public void setUp(boolean up) { isUp = up; }
    public String getServerSignature() { return serverSignature; }
    public void setServerSignature(String serverSignature) { this.serverSignature = serverSignature; }
    public boolean isAlertSent() { return alertSent; }
    public void setAlertSent(boolean alertSent) { this.alertSent = alertSent; }
}