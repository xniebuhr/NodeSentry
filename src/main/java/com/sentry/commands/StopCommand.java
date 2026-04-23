package com.sentry.commands;

import com.sentry.core.MonitorEngine;

/**
 * Handles user input to halt the background monitoring thread
 */
public class StopCommand implements Command {
    private final MonitorEngine engine;
    
    /**
     * @param engine the monitor engine to be controlled
     */
    public StopCommand(MonitorEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void execute(String[] args) throws IllegalStateException {
        engine.stopMonitoring();
        System.out.println("[INFO] Background monitoring engine halted.");
    }

    @Override
    public String getHelp() {
        return "stop - Halts all active background monitoring";
    }
}