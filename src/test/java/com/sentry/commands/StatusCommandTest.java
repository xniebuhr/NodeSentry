package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusCommandTest {

    private MonitorEngine engine;
    private StatusCommand command;
    
    // Using these to redirect input and output to test live dashboard
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
        command = new StatusCommand(engine);
        
        // Redirect system.out for testing
        System.setOut(new PrintStream(outContent)); 
    }

    @AfterEach
    public void tearDown() {
        // Put the console back to normal
        System.setOut(originalOut); 
        System.setIn(originalIn); 
        
        if (engine.isRunning()) {
            engine.stopMonitoring();
        }
    }

    @Test
    public void testEmptyStatus() {
        String[] args = {"status"};
        command.execute(args);
        
        assertTrue(outContent.toString().contains("No targets currently being monitored"));
    }

    @Test
    public void testStaticStatusPrintsTarget() {
        engine.addTarget("https://google.com");
        String[] args = {"status"};
        command.execute(args);
        
        String output = outContent.toString();
        assertTrue(output.contains("https://google.com"));
        assertTrue(output.contains("PENDING")); 
    }

    @Test
    public void testLiveStatusFailsIfEngineStopped() {
        String[] args = {"status", "live"};
        command.execute(args);
        
        assertTrue(outContent.toString().contains("Monitor engine is not running"));
    }

    @Test
    public void testLiveStatusExecutesAndExits() {
        engine.addTarget("https://google.com");
        engine.startMonitoring(10);

        // Simulate pressing Enter to break live loop
        System.setIn(new ByteArrayInputStream("\n".getBytes()));

        String[] args = {"status", "live"};
        command.execute(args);

        String output = outContent.toString();
        assertTrue(output.contains("LIVE SENTRY DASHBOARD"));
        assertTrue(output.contains("Exited Live Dashboard"));
    }
}