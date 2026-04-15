package com.sentry.ui;

import com.sentry.commands.Command;
import com.sentry.core.MonitorEngine;
import java.util.Map;
import java.util.Scanner;

/**
 * Manages the terminal-based user interface and command processing loop
 */
public class ConsoleREPL {
    private final Map<String, Command> commandMap;
    private final MonitorEngine engine;
    private final Scanner scanner;
    private boolean running;

    /**
     * @param engine the engine to be controlled by the interface
     * @param commandMap the mapping of strings to executable commands
     */
    public ConsoleREPL(MonitorEngine engine, Map<String, Command> commandMap) {
        this.engine = engine;
        this.commandMap = commandMap;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the interactive command loop and blocks until exit
     */
    public void start() {

    }

    /**
     * Parses raw user input, handles argument errors, and delegates to the appropriate command
     * @param input the full string entered by the user
     */
    private void handleInput(String input) {
        
    }

    /**
     * Renders a formatted table of service statuses to the console
     */
    private void printStatusTable() {
        
    }
}