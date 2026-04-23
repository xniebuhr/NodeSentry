package com.sentry.core;

import com.sentry.core.MonitorEngine;
import com.sentry.model.ServiceTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.Map;

public class MonitorEngineTest {

    @Test
    public void testAddAndRemoveTarget() {
        // Set up a fresh engine with dummy network variables
        HttpClient dummyClient = HttpClient.newHttpClient();
        MonitorEngine engine = new MonitorEngine(dummyClient, "fake_webhook");
        
        // Add the target
        String testUrl = "https://google.com";
        engine.addTarget(testUrl);

        // Verify it was actually added to the map
        Map<String, ServiceTarget> currentStatus = engine.getLatestStatus();
        Assertions.assertTrue(currentStatus.containsKey(testUrl), "The engine should contain the added URL");
        Assertions.assertEquals(testUrl, currentStatus.get(testUrl).getUrl(), "The stored URL should match the input");

        // Remove the target and verify it's gone
        boolean wasRemoved = engine.removeTarget(testUrl);
        Assertions.assertTrue(wasRemoved, "removeTarget should return true for an existing URL");
        Assertions.assertFalse(engine.getLatestStatus().containsKey(testUrl), "The URL should no longer be in the registry");
    }
}