package com.sentry.commands;

import java.util.Map;

/**
 * Iterates through available commands and displays their usage instructions
 */
public class HelpCommand implements Command {
    private final Map<String, Command> commandMap;

    /**
     * @param commandMap the registry of all available commands in the application
     */
    public HelpCommand(Map<String, Command> commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public void execute(String[] args) {
        System.out.println("Available Commands:");
        // Loop through every registered command and print its help string
        for (Command cmd : commandMap.values()) {
            System.out.println(cmd.getHelp());
        }
        // Tell them how to quit as well, not its own class so it lives here
        System.out.println("quit / exit - Shuts down the application");
    }

    @Override
    public String getHelp() {
        return "help - Displays this list of available commands and their descriptions";
    }
}