package com.sentry.core;

import com.sentry.model.ServiceTarget;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles the asynchronous monitoring of web services using HttpClient
 */
public class MonitorEngine {
    private final HttpClient client;
    private final Map<String, ServiceTarget> targets;
    private ScheduledExecutorService scheduler;
    private final String webhookUrl;

    /**
     * Initializes the engine with required networking components
     * @param client the HttpClient instance for network requests
     * @param webhookUrl the destination URL for failure notifications (e.g. slack or discord)
     */
    public MonitorEngine(HttpClient client, String webhookUrl) {
        this.client = client;
        this.webhookUrl = webhookUrl;
        this.targets = new ConcurrentHashMap<>();
    }

    /**
     * Registers a new public URL in the monitoring pool
     * @param url the web service address to add
     * @throws IllegalArgumentException if the URL format is invalid
     */
    public void addTarget(String url) throws IllegalArgumentException {
        // Simply calls the overloaded addTarget function with no auth token
        addTarget(url, null);
    }

    /**
     * Registers a new secured URL in the monitoring pool requiring authentication
     * @param url the web service address to add
     * @param authToken the bearer token or API key for the Authorization header
     * @throws IllegalArgumentException if the URL format is invalid
     */
    public void addTarget(String url, String authToken) throws IllegalArgumentException {
        if (url == null || url.trim().isEmpty() || !url.startsWith("http")) {
            throw new IllegalArgumentException("Invalid URL. Must start with http:// or https://");
        }
        
        ServiceTarget target = new ServiceTarget();
        target.setUrl(url);
        target.setAuthToken(authToken);
        
        // Put it in the map map using the URL as the key
        targets.put(url, target);
    }

    /**
     * Removes a URL from the monitoring pool
     * @param url the web service address to remove
     */
    public boolean removeTarget(String url) {
        return targets.remove(url) != null;
    }

    /**
     * Commences background monitoring at a fixed interval
     * @param intervalSeconds the frequency of health checks
     * @throws IllegalStateException if the monitor is already running
     */
    public void startMonitoring(int intervalSeconds) throws IllegalStateException {
        if (scheduler != null && !scheduler.isShutdown()) {
            throw new IllegalStateException("Monitor is already running. Type 'stop' first.");
        }
        
        // Create a single background thread to schedule pings
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Tell the thread to loop through all targets and call performCheck every X seconds
        scheduler.scheduleAtFixedRate(() -> {
            for (ServiceTarget target : targets.values()) {
                performCheck(target);
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Ceases all background monitoring tasks
     * @throws IllegalStateException if the monitor is not currently running
     */
    public void stopMonitoring() throws IllegalStateException {
        if (scheduler == null || scheduler.isShutdown()) {
            throw new IllegalStateException("Monitor is not currently running.");
        }
        scheduler.shutdownNow();
    }

    /**
     * Retrieves the current state of all monitored services
     * @return a map of URLs to their respective status data
     */
    public Map<String, ServiceTarget> getLatestStatus() {
        return targets;
    }
    
    /**
     * Executes an asynchronous health check for a single target
     * @param target the service to verify
     */
    private void performCheck(ServiceTarget target) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(target.getUrl()))
                    .timeout(Duration.ofSeconds(5))
                    // Fake headers so program doesn't get recognized as a bot and blocked
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET();

            // Add auth token
            if (target.getAuthToken() != null && !target.getAuthToken().isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + target.getAuthToken());
            }

            HttpRequest request = requestBuilder.build();
            long startTime = System.currentTimeMillis();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        long latency = System.currentTimeMillis() - startTime;
                        boolean isCurrentlyUp = response.statusCode() >= 200 && response.statusCode() < 400;

                        target.setLastStatusCode(response.statusCode());
                        target.setLastLatencyMs(latency);
                        target.setUp(isCurrentlyUp);
                        target.setServerSignature(response.headers().firstValue("Server").orElse("Unknown"));
                        target.setLastCheck(LocalDateTime.now());

                        // Only allow one alert to be sent about the url going down otherwise it will ping at the specified interval
                        // Resets if the url becomes available again
                        if (!isCurrentlyUp && !target.isAlertSent()) {
                            sendAlert(target);
                            target.setAlertSent(true);
                        } else if (isCurrentlyUp && target.isAlertSent()) {
                            target.setAlertSent(false);
                        }
                    })
                    .exceptionally(ex -> {
                        handleFailure(target);
                        return null;
                    });
        } catch (Exception e) {
            // Catches URI creation errors so the thread doesn't die
            handleFailure(target);
        }
    }

    /**
     * Handles service check failures by updating internal status and alerting
     * Prevents network exceptions from killing monitoring thread
     * @param target the service that either failed the health check or encountered an error
     */
    private void handleFailure(ServiceTarget target) {
        target.setUp(false);
        target.setLastStatusCode(-1);
        target.setLastCheck(LocalDateTime.now());
        if (!target.isAlertSent()) {
            sendAlert(target);
            target.setAlertSent(true);
        }
    }

    /**
     * Checks if the background monitoring is active
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    /**
     * Dispatches an alert to the configured webhook upon service failure
     * @param target the service that failed the check
     */
    private void sendAlert(ServiceTarget target) {
        // Don't send if ther is no webhook URL
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("WEBHOOK_URL")) {
            return;
        }

        // Build the message
        String message = String.format("**NODE DOWN ALERT** \\n**URL:** %s\\n**Status Code:** %d\\n**Time:** %s",
                target.getUrl(), 
                target.getLastStatusCode(), 
                target.getLastCheck().toString());

        // Wrap in JSON 
        // Discord uses "content", slack uses "text" so change it if using slack :)
        String jsonPayload = "{\"content\": \"" + message + "\"}";

        // Build the POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Send async
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    System.err.println("\n[ERROR] Failed to send webhook alert for " + target.getUrl() + ": " + ex.getMessage());
                    // Reprint the prompt so the terminal doesn't look broken
                    System.out.print("Sentry> ");
                    return null;
                });
    }
}