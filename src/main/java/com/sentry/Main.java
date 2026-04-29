package com.sentry;

import com.sentry.commands.*;
import com.sentry.core.MonitorEngine;
import com.sentry.ui.ConsoleREPL;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The main entry point for the app
 */
public class Main {
    
    /**
     * Initializes all dependencies and starts the interactive terminal loop
     * @param args command line arguments (not currently used)
     */
    public static void main(String[] args) {
        // Initialize the core HTTP client
        HttpClient client = HttpClient.newHttpClient();
        
        // Set up webhook url
        String webhookUrl = "WEBHOOK_URL";
        
        // Instantiate the engine
        MonitorEngine engine = new MonitorEngine(client, webhookUrl);
        
        // Map out all available commands
        Map<String, Command> commands = new LinkedHashMap<>();
        commands.put("add", new AddCommand(engine));
        commands.put("remove", new RemoveCommand(engine));
        commands.put("start", new StartCommand(engine));
        commands.put("stop", new StopCommand(engine));
        commands.put("status", new StatusCommand(engine));
        commands.put("help", new HelpCommand(commands));
        
        // Boot up the user interface and start
        ConsoleREPL ui = new ConsoleREPL(engine, commands);
        ui.start();
    }
}