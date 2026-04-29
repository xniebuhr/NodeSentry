package com.sentry.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpCommandTest {

    private HelpCommand helpCommand;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));

        Map<String, Command> fakeCommands = new HashMap<>();
        fakeCommands.put("testcmd", new Command() {
            @Override
            public void execute(String[] args) {} 

            @Override
            public String getHelp() {
                return "testcmd - This is a dummy command for testing";
            }
        });

        helpCommand = new HelpCommand(fakeCommands);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testExecutePrintsHelpStrings() {
        String[] args = {"help"};
        helpCommand.execute(args);

        // Flush the buffer before reading to fix weird memory bug
        System.out.flush();
        String output = outContent.toString();
        
        assertTrue(output.contains("Available Commands:"));
        assertTrue(output.contains("testcmd - This is a dummy command for testing"));
        assertTrue(output.contains("quit / exit - Shuts down the application")); 
    }
}