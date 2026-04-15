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

    }

    @Override
    public String getHelp() {
        return "start <intervalSeconds> - Starts the background monitoring at the specified interval";
    }
}