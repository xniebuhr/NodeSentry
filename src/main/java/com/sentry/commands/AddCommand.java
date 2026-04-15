package com.sentry.commands;

import com.sentry.core.MonitorEngine;

/**
 * Handles user input for adding new targets to the engine
 */
public class AddCommand implements Command {
    private final MonitorEngine engine;
    
    /**
     * @param engine the monitor engine to be modified
     */
    public AddCommand(MonitorEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void execute(String[] args) throws IllegalArgumentException {

    }

    @Override
    public String getHelp() {
        return "add <url> - Adds a new service to the monitoring registry";
    }
}