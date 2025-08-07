package com.seristic.hbzcleaner.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.seristic.hbzcleaner.main.TickPerSecond;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for collecting and formatting server metrics
 */
public class ServerMetrics {
    
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    
    /**
     * Gets current TPS with color coding
     */
    public static String getFormattedTPS() {
        double tps = TickPerSecond.getTPS();
        String color;
        if (tps >= 19.0) {
            color = "§a"; // Green for good TPS
        } else if (tps >= 15.0) {
            color = "§e"; // Yellow for moderate TPS
        } else {
            color = "§c"; // Red for poor TPS
        }
        return color + DECIMAL_FORMAT.format(tps);
    }
    
    /**
     * Gets RAM usage information
     */
    public static RamInfo getRamInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        long maxMB = maxMemory / (1024 * 1024);
        long usedMB = usedMemory / (1024 * 1024);
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        return new RamInfo(usedMB, maxMB, usagePercent);
    }
    
    /**
     * Gets world statistics
     */
    public static WorldStats getWorldStats() {
        long totalChunks = 0;
        long totalEntities = 0;
        long totalPlayers = 0;
        int worldCount = 0;
        
        for (World world : Bukkit.getWorlds()) {
            worldCount++;
            totalChunks += world.getLoadedChunks().length;
            totalEntities += world.getEntities().size();
            totalPlayers += world.getPlayers().size();
        }
        
        return new WorldStats(worldCount, totalChunks, totalEntities, totalPlayers);
    }
    
    /**
     * Gets average player ping
     */
    public static double getAveragePing() {
        double totalPing = 0.0;
        int validPlayers = 0;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping = player.getPing();
            if (ping <= 10000) { // Filter out invalid pings
                totalPing += ping;
                validPlayers++;
            }
        }
        
        return validPlayers > 0 ? totalPing / validPlayers : 0.0;
    }
    
    /**
     * Formats numbers with thousands separators
     */
    public static String formatNumber(long number) {
        return NUMBER_FORMAT.format(number);
    }
    
    /**
     * Formats numbers with thousands separators
     */
    public static String formatNumber(double number) {
        return NUMBER_FORMAT.format(Math.round(number));
    }
    
    /**
     * Container class for RAM information
     */
    public static class RamInfo {
        public final long usedMB;
        public final long maxMB;
        public final double usagePercent;
        
        public RamInfo(long usedMB, long maxMB, double usagePercent) {
            this.usedMB = usedMB;
            this.maxMB = maxMB;
            this.usagePercent = usagePercent;
        }
        
        public String getFormattedUsage() {
            String color;
            if (usagePercent < 60) {
                color = "§a"; // Green for low usage
            } else if (usagePercent < 80) {
                color = "§e"; // Yellow for moderate usage
            } else {
                color = "§c"; // Red for high usage
            }
            
            return color + formatNumber(usedMB) + " / " + formatNumber(maxMB) + "MB " +
                   "(" + DECIMAL_FORMAT.format(usagePercent) + "%)";
        }
    }
    
    /**
     * Container class for world statistics
     */
    public static class WorldStats {
        public final int worldCount;
        public final long totalChunks;
        public final long totalEntities;
        public final long totalPlayers;
        
        public WorldStats(int worldCount, long totalChunks, long totalEntities, long totalPlayers) {
            this.worldCount = worldCount;
            this.totalChunks = totalChunks;
            this.totalEntities = totalEntities;
            this.totalPlayers = totalPlayers;
        }
    }
}
