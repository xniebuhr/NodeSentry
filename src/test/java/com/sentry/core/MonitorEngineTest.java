package com.sentry.core;

import com.sentry.model.ServiceTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

public class MonitorEngineTest {

    private MonitorEngine engine;
    private final String testUrl = "https://google.com";

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
    }

    @AfterEach
    public void tearDown() {
        if (engine != null && engine.isRunning()) {
            try {
                engine.stopMonitoring();
            } catch (IllegalStateException ignored) { /* Do nothing */ }
        }
    }

    @Test
    public void testAddTargetSuccess() {
        engine.addTarget(testUrl);
        assertTrue(engine.getLatestStatus().containsKey(testUrl));
    }

    @Test
    public void testAddSecuredTargetSuccess() {
        engine.addTarget(testUrl, "secret_token");
        ServiceTarget target = engine.getLatestStatus().get(testUrl);
        assertEquals("secret_token", target.getAuthToken());
    }

    @Test
    public void testAddInvalidTargetThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> engine.addTarget(""));
        assertThrows(IllegalArgumentException.class, () -> engine.addTarget("google.com")); 
    }

    @Test
    public void testRemoveTarget() {
        engine.addTarget(testUrl);
        assertTrue(engine.removeTarget(testUrl)); 
        assertFalse(engine.removeTarget("https://not-here.com")); 
    }

    @Test
    public void testStartMonitoringTwiceThrowsException() {
        engine.startMonitoring(10);
        assertThrows(IllegalStateException.class, () -> engine.startMonitoring(10));
    }

    @Test
    public void testStopMonitoringWhileStoppedThrowsException() {
        assertThrows(IllegalStateException.class, () -> engine.stopMonitoring());
    }
}