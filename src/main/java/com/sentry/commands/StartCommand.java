package com.sentry.commands;

import com.sentry.core.MonitorEngine;

/**
 * Handles user input to commence the background monitoring thread
 */
public class StartCommand implements Command {
    private final MonitorEngine engine;
    
    /**
     * @param engine the monitor engine to be controlled
     */
    public StartCommand(MonitorEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void execute(String[] args) throws IllegalArgumentException, IllegalStateException {
        // No interval given
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing interval. Usage: " + getHelp());
        }
        
        try {
            int interval = Integer.parseInt(args[1]);
            engine.startMonitoring(interval);
            System.out.println("[INFO] Background monitoring engine started. Pinging targets every " + interval + " seconds...");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Interval must be a valid number. Usage: " + getHelp());
        }
    }

    @Override
    public String getHelp() {
        return "start <intervalSeconds> - Starts the background monitoring at the specified interval";
    }
}