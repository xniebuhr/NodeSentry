package com.sentry.commands;

import com.sentry.core.MonitorEngine;

/**
 * Handles user input for removing existing targets from the engine
 */
public class RemoveCommand implements Command {
    private final MonitorEngine engine;
    
    /**
     * @param engine the monitor engine to be modified
     */
    public RemoveCommand(MonitorEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void execute(String[] args) throws IllegalArgumentException {

    }

    @Override
    public String getHelp() {
        return "remove <url> - Removes a service from the monitoring registry";
    }
}