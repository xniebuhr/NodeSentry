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
        // Print banner
        System.out.println("==================================================");
        System.out.println("   Interactive NodeSentry Monitor v1.0");
        System.out.println("==================================================");
        System.out.println("Type 'help' to see a list of available commands.\n");

        // Main loop
        running = true;
        while (running) {
            System.out.print("Sentry> ");
            String input = scanner.nextLine().trim();

            // No input
            if (input.isEmpty()) {
                continue;
            }

            // Quit / exit
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Shutting down NodeSentry. Goodbye!");
                running = false;
                
                try {
                    engine.stopMonitoring(); 
                } catch (IllegalStateException e) {
                    // Ignore this, it just means the engine wasn't monitoring when the program was stopped so nothing actually went wrong
                }
                
                break;
            }

            // Any other input handled by handleInput function
            handleInput(input);
        }
    }

    /**
     * Parses raw user input, handles argument errors, and delegates to the appropriate command
     * @param input the full string entered by the user
     */
    private void handleInput(String input) {
        // Split by one or more spaces
        String[] args = input.split("\\s+");
        String commandName = args[0].toLowerCase();

        Command command = commandMap.get(commandName);

        if (command == null) {
            System.out.println("[ERROR] Command not found: '" + commandName + "'. Type 'help' for available commands.");
            return;
        }

        try {
            command.execute(args);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Handle expected errors
            System.out.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            System.out.println("[FATAL] An unexpected error occurred: " + e.getMessage());
        }
    }
}