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
        // No URL
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing URL. Usage: " + getHelp());
        }
        
        String url = args[1];
        boolean wasRemoved = engine.removeTarget(url);
        
        if (wasRemoved) {
            System.out.println("[SUCCESS] Removed target: " + url);
        } else {
            System.out.println("[WARNING] Target not found in registry: " + url);
        }
    }

    @Override
    public String getHelp() {
        return "remove <url> - Removes a service from the monitoring registry";
    }
}