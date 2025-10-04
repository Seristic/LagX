package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A command handler system that uses a more maintainable approach to command
 * management.
 * Commands are registered with their aliases in a map structure, making it easy
 * to add,
 * remove, or modify commands without complex nested if-else logic.
 */
public class CommandHandler {
    private final LagX plugin;
    private final Map<String, CommandInfo> commands = new HashMap<>();

    /**
     * Creates a new CommandHandler instance
     *
     * @param plugin The LagX plugin instance
     */
    public CommandHandler(LagX plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a new command with aliases and permission
     *
     * @param name       The primary name of the command
     * @param permission The permission required to execute the command
     * @param handler    The function that handles the command execution
     * @param aliases    Optional aliases for the command
     */
    public void registerCommand(String name, String permission, BiFunction<CommandSender, String[], Boolean> handler,
            String... aliases) {
        CommandInfo info = new CommandInfo(name, permission, handler);
        commands.put(name.toLowerCase(), info);

        // Register aliases
        for (String alias : aliases) {
            commands.put(alias.toLowerCase(), info);
        }
    }

    /**
     * Handle a command execution
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return true if the command was handled, false otherwise
     */
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (hasPermission(sender, "lagx.help")) {
                Help.send(sender instanceof Player ? (Player) sender : null, 1);
                return true;
            } else {
                Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
                return true;
            }
        }

        String subCommand = args[0].toLowerCase();
        CommandInfo info = commands.get(subCommand);

        if (info != null) {
            if (hasPermission(sender, info.permission)) {
                return info.handler.apply(sender, args);
            } else {
                Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
                return true;
            }
        }

        // No matching command found
        Help.sendMsg(sender, "§cCommand not found! Use /lagx help for a list of commands.", true);
        return true;
    }

    /**
     * Check if a player has the specified permission
     *
     * @param sender     The command sender
     * @param permission The permission to check
     * @return true if the player has the permission, false otherwise
     */
    private boolean hasPermission(CommandSender sender, String permission) {
        return !(sender instanceof Player) || sender.hasPermission(permission) || sender.hasPermission("lagx.*")
                || sender.isOp();
    }

    /**
     * A class to store command information
     */
    private static class CommandInfo {
        final String name;
        final String permission;
        final BiFunction<CommandSender, String[], Boolean> handler;

        CommandInfo(String name, String permission, BiFunction<CommandSender, String[], Boolean> handler) {
            this.name = name;
            this.permission = permission;
            this.handler = handler;
        }
    }
}