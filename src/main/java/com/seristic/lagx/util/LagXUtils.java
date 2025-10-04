package com.seristic.lagx.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility methods for LagX plugin
 * Contains common utility functions that were previously static in the main
 * class
 */
public class LagXUtils {

    /**
     * Check if a player has permission with proper fallback hierarchy
     */
    public static boolean hasPermission(Player player, String permission) {
        if (player == null) {
            return true;
        }

        if (player.isOp()) {
            return true;
        }

        if (player.hasPermission(permission)) {
            return true;
        }

        // Check for wildcard permissions
        return player.hasPermission("lagx.*") || player.hasPermission("lagx.admin");
    }

    /**
     * Check if a command sender has permission
     */
    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender == null) {
            return true;
        }

        if (sender instanceof Player) {
            return hasPermission((Player) sender, permission);
        } else {
            // Console/command blocks have all permissions
            return true;
        }
    }

    /**
     * Broadcast a message to all online players with permission
     */
    public static void broadcast(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasPermission(player, "lagx.broadcast")) {
                player.sendMessage(message);
            }
        }

        // Also send to console
        Bukkit.getLogger().info(message);
    }

    /**
     * Broadcast a warning message
     */
    public static void broadcastWarn(String message) {
        String formattedMessage = "§c[LagX Warning] " + message;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasPermission(player, "lagx.warnings")) {
                player.sendMessage(formattedMessage);
            }
        }

        // Also send to console
        Bukkit.getLogger().warning(message);
    }

    /**
     * Format memory size in MB
     */
    public static long bytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    /**
     * Format a double value to a reasonable number of decimal places
     */
    public static String formatDecimal(double value) {
        return String.format("%.2f", value);
    }

    /**
     * Get a safe string representation of an object
     */
    public static String safeString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }
}