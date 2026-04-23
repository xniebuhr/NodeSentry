package com.sentry.commands;

import com.sentry.core.MonitorEngine;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Retrieves and displays the current health status of all monitored targets
 * Supports a static printout and a live dashboard
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
        // Route to the correct method based on the subcommand
        if (args.length > 1 && args[1].equalsIgnoreCase("live")) {
            executeLive();
        } else {
            executeStatic();
        }
    }

    /**
     * Prints out the static dashboard
     */
    private void executeStatic() {
        var targets = engine.getLatestStatus();
        
        // No targets
        if (targets == null || targets.isEmpty()) {
            System.out.println("[INFO] No targets currently being monitored. Type 'add <url>' to add one.");
            return;
        }

        System.out.println("+------------------------------------------------------+---------+---------+-------------------+");
        System.out.println("| TARGET URL                                           | STATUS  | LATENCY | SERVER SIGNATURE  |");
        System.out.println("+------------------------------------------------------+---------+---------+-------------------+");

        for (var target : targets.values()) {
            // Format the raw data into clean strings
            String statusStr;
            if (target.getLastCheck() == null) {
                statusStr = "PENDING";
            } else if (target.isUp()) {
                statusStr = target.getLastStatusCode() + " OK";
            } else {
                // -1 is hard crash/timeout, therwise print the real HTTP code
                statusStr = target.getLastStatusCode() == -1 ? "OFFLINE" : target.getLastStatusCode() + " ERR";
            }
            String latencyStr = target.getLastLatencyMs() > 0 ? target.getLastLatencyMs() + "ms" : "---";
            String sigStr = target.getServerSignature() == null ? "Unknown" : target.getServerSignature();

            // Print the formatted row using printf for column alignment
            System.out.printf("| %-52s | %-7s | %-7s | %-17s |%n", 
                    truncate(target.getUrl(), 52), 
                    statusStr, 
                    latencyStr, 
                    truncate(sigStr, 17));
        }
        System.out.println("+------------------------------------------------------+---------+---------+-------------------+");
    }

    /** 
     * Runs the live dashboard
     */
    private void executeLive() {
        // Make sure it's running first, no point in a live dashboard when nothing is pinged
        if (!engine.isRunning()) {
            System.out.println("[ERROR] Cannot start live dashboard: Monitor engine is not running. Type 'start <interval>' first.");
            return;
        }
        
        var targets = engine.getLatestStatus();

        // No targets
        if (targets == null || targets.isEmpty()) {
            System.out.println("[INFO] No targets currently being monitored. Type 'add <url>' first.");
            return;
        }

        AtomicBoolean drawing = new AtomicBoolean(true);

        Thread uiThread = new Thread(() -> {
            while (drawing.get()) {
                clearScreen();

                System.out.println("================================================================================================");
                System.out.println("                 LIVE SENTRY DASHBOARD (Press ENTER to return to REPL)                          ");
                System.out.println("================================================================================================");
                System.out.println("+------------------------------------------------------+---------+---------+-------------------+");
                System.out.println("| TARGET URL                                           | STATUS  | LATENCY | SERVER SIGNATURE  |");
                System.out.println("+------------------------------------------------------+---------+---------+-------------------+");

                for (var target : engine.getLatestStatus().values()) {
                    // Format the raw data into clean strings
                    String statusStr;
                    if (target.getLastCheck() == null) {
                        statusStr = "PENDING";
                    } else if (target.isUp()) {
                        statusStr = target.getLastStatusCode() + " OK";
                    } else {
                        // -1 is hard crash/timeout, therwise print the real HTTP code
                        statusStr = target.getLastStatusCode() == -1 ? "OFFLINE" : target.getLastStatusCode() + " ERR";
                    }
                    String latencyStr = target.getLastLatencyMs() > 0 ? target.getLastLatencyMs() + "ms" : "---";
                    String sigStr = target.getServerSignature() == null ? "Unknown" : target.getServerSignature();

                    System.out.printf("| %-52s | %-7s | %-7s | %-17s |%n",
                            truncate(target.getUrl(), 52),
                            statusStr,
                            latencyStr,
                            truncate(sigStr, 17));
                }
                System.out.println("+------------------------------------------------------+---------+---------+-------------------+");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        uiThread.start();

        Scanner localScanner = new Scanner(System.in);
        localScanner.nextLine();
        
        drawing.set(false);

        try {
            uiThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n[INFO] Exited Live Dashboard.");
    }

    /**
     * Clear the screen using built-in OS commands
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception ex) {
            // Fallback if the OS command fails for some reason
            // Using ANSI escape code to clear the screen 
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    /**
     * Helper method to ensure long strings don't break our table borders.
     * @param value the string to be truncated
     * @param length the maximum allowed length of the string
     * @return the original string if it fits, or a truncated version ending with "..."
     */
    private String truncate(String value, int length) {
        if (value.length() <= length) return value;
        return value.substring(0, length - 3) + "...";
    }

    @Override
    public String getHelp() {
        return "status [live] - Displays a formatted table of monitored services. Add 'live' for a real-time dashboard";
    }
}