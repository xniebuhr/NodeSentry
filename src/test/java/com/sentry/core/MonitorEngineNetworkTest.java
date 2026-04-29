package com.sentry.core;

import com.sentry.core.MonitorEngine;
import com.sentry.model.ServiceTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitorEngineNetworkTest {

    @Mock
    private HttpClient mockClient;

    @Mock
    private HttpResponse<Void> mockResponse;

    private MonitorEngine engine;
    private final String testUrl = "https://httpbin.org/status/200";

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(mockClient, "https://discord.com/api/webhooks/fake");
    }

    @Test
    public void testPerformCheckSuccess() throws Exception {
        // Return fake 200
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(Map.of("Server", List.of("FakeServer")), (a, b) -> true));
        
        CompletableFuture<HttpResponse<Void>> futureResponse = CompletableFuture.completedFuture(mockResponse);
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        // Add target and tick once to send check
        engine.addTarget(testUrl);
        engine.startMonitoring(1);
        Thread.sleep(100);
        engine.stopMonitoring();

        // Verify 200 response
        ServiceTarget target = engine.getLatestStatus().get(testUrl);
        assertTrue(target.isUp(), "Target should be marked as UP for a 200 response");
        assertEquals(200, target.getLastStatusCode(), "Status code should match the mock");
        assertEquals("FakeServer", target.getServerSignature(), "Server signature should match the mock headers");
    }

    @Test
    public void testPerformCheckFailureTriggersWebhook() throws Exception {
        // Return fake 500 
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(Map.of(), (a, b) -> true));

        CompletableFuture<HttpResponse<Void>> futureResponse = CompletableFuture.completedFuture(mockResponse);
        
        // Two requests: one to target, one to webhook
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        engine.addTarget(testUrl);
        engine.startMonitoring(1);
        Thread.sleep(100); 
        engine.stopMonitoring();

        ServiceTarget target = engine.getLatestStatus().get(testUrl);
        assertFalse(target.isUp(), "Target should be marked as DOWN for a 500 response");
        assertEquals(500, target.getLastStatusCode());
        assertTrue(target.isAlertSent(), "Engine should have flagged that a webhook alert was sent");
        
        // Verify sendAsync was called twice
        verify(mockClient, times(2)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void testPerformCheckHardCrashHandlesExceptionally() throws Exception {
        // Simulate hard crash e.g. internet goes out
        CompletableFuture<HttpResponse<Void>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new java.net.ConnectException("Connection timed out"));
        
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(failedFuture);

        engine.addTarget(testUrl);
        engine.startMonitoring(1);
        Thread.sleep(100); 
        engine.stopMonitoring();

        // Should be a -1
        ServiceTarget target = engine.getLatestStatus().get(testUrl);
        assertFalse(target.isUp(), "Target should be DOWN after a hard crash");
        assertEquals(-1, target.getLastStatusCode(), "Status code should be -1 for exceptions");
        assertTrue(target.isAlertSent(), "Webhook should have been triggered for the crash");
    }
}