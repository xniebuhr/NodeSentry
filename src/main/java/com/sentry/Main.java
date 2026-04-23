package com.sentry;

import com.sentry.commands.*;
import com.sentry.core.MonitorEngine;
import com.sentry.ui.ConsoleREPL;

import java.net.http.HttpClient;
import java.util.HashMap;
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
        // Currently using a discord webhook that I made a new server to test with, change to 'WEBHOOK_URL' to ignore or change to your own personal webhook if running
        String webhookUrl = "https://discord.com/api/webhooks/1496254747915587644/mWb-ClxdhvT_Zl9OJsfodMG-mSCFMrTFD4s3GZbyMolwnF4D4nKcgDgVDHECOYa2-c6c";
        
        // Instantiate the engine
        MonitorEngine engine = new MonitorEngine(client, webhookUrl);
        
        // Map out all available commands
        Map<String, Command> commands = new HashMap<>();
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