package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

public class StopCommandTest {

    private MonitorEngine engine;
    private StopCommand command;

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
        command = new StopCommand(engine);
    }

    @Test
    public void testValidStopPassesToEngine() {
        engine.startMonitoring(10);
        assertTrue(engine.isRunning());
        
        String[] args = {"stop"};
        command.execute(args);
        
        assertFalse(engine.isRunning());
    }
}