package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

public class AddCommandTest {

    private MonitorEngine engine;
    private AddCommand command;

    @BeforeEach
    public void setUp() {
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
        command = new AddCommand(engine);
    }

    @Test
    public void testMissingArgumentsThrowsException() {
        String[] args = {"add"};
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testInvalidUrlProtocolThrowsException() {
        String[] args = {"add", "ftp://google.com"};
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testInvalidUrlCharactersThrowsException() {
        String[] args = {"add", "https://inavlid^site.com"}; 
        assertThrows(IllegalArgumentException.class, () -> command.execute(args));
    }

    @Test
    public void testValidPublicUrlPassesToEngine() {
        String[] args = {"add", "https://google.com"};
        command.execute(args);
        assertTrue(engine.getLatestStatus().containsKey("https://google.com"));
    }

    @Test
    public void testValidSecuredUrlPassesToEngine() {
        String[] args = {"add", "https://api.github.com", "my_token"};
        command.execute(args);
        assertTrue(engine.getLatestStatus().containsKey("https://api.github.com"));
        assertEquals("my_token", engine.getLatestStatus().get("https://api.github.com").getAuthToken());
    }
}