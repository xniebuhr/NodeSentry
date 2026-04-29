package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveCommandTest {

    private MonitorEngine engine;
    private RemoveCommand command;

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
        command = new RemoveCommand(engine);
    }

    @Test
    public void testMissingArgumentsThrowsException() {
        String[] args = {"remove"};
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testValidRemovalPassesToEngine() {
        engine.addTarget("https://google.com");
        String[] args = {"remove", "https://google.com"};
        
        command.execute(args);
        
        assertFalse(engine.getLatestStatus().containsKey("https://google.com"));
    }
}