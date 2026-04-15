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
        
    }

    @Override
    public String getHelp() {
        return "help - Displays this list of available commands and their descriptions";
    }
}