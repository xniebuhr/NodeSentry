package com.sentry.commands;

/**
 * Defines the contract for all interactive terminal commands
 */
public interface Command {
    /**
     * Executes the specific logic associated with the command
     * @param args the list of arguments provided by the user
     * @throws IllegalArgumentException if the provided arguments are missing or invalid
     */
    void execute(String[] args) throws IllegalArgumentException;

    /**
     * Provides a brief explanation of how to use the command
     * @return the usage instructions for the command
     */
    String getHelp();
}