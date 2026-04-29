package com.sentry.ui;

import com.sentry.commands.Command;
import com.sentry.core.MonitorEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsoleREPLTest {

    private MonitorEngine engine;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        engine = new MonitorEngine(HttpClient.newHttpClient(), "fake_webhook");
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        if (engine.isRunning()) {
            engine.stopMonitoring();
        }
    }

    @Test
    public void testReplRoutingAndExit() {
        Map<String, Command> fakeCommands = new HashMap<>();
        fakeCommands.put("ping", new Command() {
            @Override
            public void execute(String[] args) {
                System.out.println("PONG EXECUTED");
            }
            @Override
            public String getHelp() { return "ping help"; }
        });

        String simulatedInput = "ping\nexit\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ConsoleREPL repl = new ConsoleREPL(engine, fakeCommands);
        repl.start();

        System.out.flush();
        String output = outContent.toString();
        
        assertTrue(output.contains("Interactive NodeSentry Monitor v1.0")); 
        assertTrue(output.contains("PONG EXECUTED")); 
        assertTrue(output.contains("Shutting down NodeSentry")); 
    }
    
    @Test
    public void testReplHandlesUnknownCommand() {
         Map<String, Command> emptyCommands = new HashMap<>();
         
         String simulatedInput = "fakecommand\nexit\n";
         System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
         
         ConsoleREPL repl = new ConsoleREPL(engine, emptyCommands);
         repl.start();
         
         System.out.flush();
         String output = outContent.toString();
         
         assertTrue(output.contains("Command not found: 'fakecommand'"));
    }
}