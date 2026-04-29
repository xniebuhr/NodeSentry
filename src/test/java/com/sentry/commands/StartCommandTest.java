package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

public class StartCommandTest {

    private MonitorEngine engine;
    private StartCommand command;

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
        command = new StartCommand(engine);
    }

    @AfterEach
    public void tearDown() {
        if (engine.isRunning()) {
            engine.stopMonitoring();
        }
    }

    @Test
    public void testMissingArgumentsThrowsException() {
        String[] args = {"start"};
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testNonIntegerIntervalThrowsException() {
        String[] args = {"start", "ten"};
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testValidStartPassesToEngine() {
        String[] args = {"start", "15"};
        command.execute(args);
        assertTrue(engine.isRunning());
    }
}