package com.sentry.commands;

import java.net.URI;
import java.net.URISyntaxException;

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
        // args[0] is "add", args[1] is the URL, args[2] is the optional token
        
        // No URL given
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing URL. Usage: " + getHelp());
        }
        
        String url = args[1];

        // Not valid URL
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Malformed URL: '" + url + "'. Ensure it starts with http:// or https://, and contains a valid domain format.");
        }
        
        // Add auth header
        if (args.length == 3) {
            String token = args[2];
            engine.addTarget(url, token);
            System.out.println("[SUCCESS] Added secured target: " + url);
        } else {
            engine.addTarget(url);
            System.out.println("[SUCCESS] Added public target: " + url);
        }
    }

    /**
     * Validates that a string is a structurally sound URL according to RFC standards.
     * @param urlString the raw string provided by the user
     * @return true if the URL can theoretically exist, false if it contains illegal characters
     */
    private boolean isValidUrl(String urlString) {
        try {
            // Must use a valid web protocol
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                return false;
            }

            // Must be parsable by Java's internal URI engine
            URI uri = new URI(urlString);

            // Must successfully extract a host
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }

            // Cannot start or end with a hyphen
            if (host.startsWith("-") || host.endsWith("-")) {
                return false;
            }

            // Must only contain letters, numbers, periods, and hyphens
            if (!host.matches("^[a-zA-Z0-9.-]+$")) {
                return false;
            }

            return true;

        } catch (URISyntaxException e) {
            // Bad URL
            return false;
        }
    }

    @Override
    public String getHelp() {
        return "add <url> - Adds a new service to the monitoring registry";
    }
}