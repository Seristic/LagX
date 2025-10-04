package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Collections;

/**
 * Base class for all LagX commands
 */
public abstract class LagXCommand {

    protected final LagX plugin;

    public LagXCommand(LagX plugin) {
        this.plugin = plugin;
    }

    /**
     * Execute the command
     * 
     * @param sender The command sender
     * @param args   Command arguments (including the command name at index 0)
     * @return true if command was handled
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Get the permission required for this command
     */
    public abstract String getPermission();

    /**
     * Get the command usage
     */
    public abstract String getUsage();

    /**
     * Get the command description
     */
    public abstract String getDescription();

    /**
     * Get tab completions for this command
     * Override in subclasses to provide custom completions
     * 
     * @param sender The command sender
     * @param args   Command arguments (including the command name at index 0)
     * @return List of possible completions
     */
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Check if sender has permission for this command
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        return LagX.hasPermission(sender, permission);
    }

    /**
     * Check if sender has permission for this specific command
     */
    protected boolean hasPermission(CommandSender sender) {
        return hasPermission(sender, getPermission());
    }
}