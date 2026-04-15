package com.sentry.commands;

import com.sentry.core.MonitorEngine;

/**
 * Retrieves and displays the current health status of all monitored targets
 */
public class StatusCommand implements Command {
    private final MonitorEngine engine;
    
    /**
     * @param engine the monitor engine containing the status data
     */
    public StatusCommand(MonitorEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void execute(String[] args) {

    }

    @Override
    public String getHelp() {
        return "status - Displays a formatted table of all monitored services and their current health";
    }
}