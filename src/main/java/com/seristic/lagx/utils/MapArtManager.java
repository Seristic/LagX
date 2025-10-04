package com.seristic.lagx.utils;

import com.seristic.lagx.main.LagX;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages map art protection system with automatic protection capabilities
 */
public class MapArtManager {

    private final LagX plugin;
    private final File configFile;
    private FileConfiguration config;
    private final Map<Integer, String> protectedMaps = new HashMap<>();
    private ScheduledTask autoProtectionTask;

    // Configuration defaults
    private boolean autoProtectionEnabled = true;
    private int scanIntervalMinutes = 5;
    private boolean protectAllMaps = false;
    private boolean requirePlayerPermission = true;

    public MapArtManager(LagX plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "protected-maps.yml");
        loadConfig();
        startAutoProtectionTask();
    }

    /**
     * Load the configuration file
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                configFile.createNewFile();
                // Create default configuration
                config = YamlConfiguration.loadConfiguration(configFile);
                setDefaultConfig();
                saveConfig();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create protected-maps.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadSettings();
        loadProtectedMaps();
    }

    /**
     * Set default configuration values
     */
    private void setDefaultConfig() {
        config.addDefault("auto-protection.enabled", autoProtectionEnabled);
        config.addDefault("auto-protection.scan-interval-minutes", scanIntervalMinutes);
        config.addDefault("auto-protection.protect-all-maps", protectAllMaps);
        config.addDefault("auto-protection.require-player-permission", requirePlayerPermission);
        config.options().copyDefaults(true);
    }

    /**
     * Load settings from config
     */
    private void loadSettings() {
        autoProtectionEnabled = config.getBoolean("auto-protection.enabled", true);
        scanIntervalMinutes = config.getInt("auto-protection.scan-interval-minutes", 5);
        protectAllMaps = config.getBoolean("auto-protection.protect-all-maps", false);
        requirePlayerPermission = config.getBoolean("auto-protection.require-player-permission", true);
    }

    /**
     * Load protected maps from config
     */
    private void loadProtectedMaps() {
        protectedMaps.clear();
        if (config.contains("protected-maps")) {
            for (String mapIdStr : config.getConfigurationSection("protected-maps").getKeys(false)) {
                try {
                    int mapId = Integer.parseInt(mapIdStr);
                    String owner = config.getString("protected-maps." + mapIdStr + ".owner", "Unknown");
                    protectedMaps.put(mapId, owner);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid map ID in config: " + mapIdStr);
                }
            }
        }

        plugin.getLogger().info("Loaded " + protectedMaps.size() + " protected maps");
    }

    /**
     * Save the configuration
     */
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save protected-maps.yml: " + e.getMessage());
        }
    }

    /**
     * Protect a map
     */
    public void protectMap(int mapId, String owner) {
        protectedMaps.put(mapId, owner);
        config.set("protected-maps." + mapId + ".owner", owner);
        config.set("protected-maps." + mapId + ".protected-at", System.currentTimeMillis());
        saveConfig();
    }

    /**
     * Unprotect a map
     */
    public void unprotectMap(int mapId) {
        protectedMaps.remove(mapId);
        config.set("protected-maps." + mapId, null);
        saveConfig();
    }

    /**
     * Check if a map is protected
     */
    public boolean isMapProtected(int mapId) {
        return protectedMaps.containsKey(mapId);
    }

    /**
     * Get the owner of a protected map
     */
    public String getMapOwner(int mapId) {
        return protectedMaps.get(mapId);
    }

    /**
     * Get all protected maps
     */
    public Map<Integer, String> getProtectedMaps() {
        return new HashMap<>(protectedMaps);
    }

    /**
     * Get protected map IDs
     */
    public Set<Integer> getProtectedMapIds() {
        return protectedMaps.keySet();
    }

    /**
     * Reload the manager
     */
    public void reload() {
        loadConfig();
        restartAutoProtectionTask();
    }

    /**
     * Start the automatic protection task
     */
    private void startAutoProtectionTask() {
        if (autoProtectionTask != null) {
            autoProtectionTask.cancel();
        }

        if (autoProtectionEnabled && scanIntervalMinutes > 0) {
            long intervalSeconds = scanIntervalMinutes * 60L; // Convert minutes to seconds

            autoProtectionTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                    (task) -> scanAndProtectMaps(),
                    intervalSeconds * 20L, intervalSeconds * 20L); // Convert to ticks for global scheduler

            plugin.getLogger()
                    .info("Started automatic map protection task (interval: " + scanIntervalMinutes + " minutes)");
        }
    }

    /**
     * Stop the automatic protection task
     */
    public void stopAutoProtectionTask() {
        if (autoProtectionTask != null) {
            autoProtectionTask.cancel();
            autoProtectionTask = null;
            plugin.getLogger().info("Stopped automatic map protection task");
        }
    }

    /**
     * Restart the automatic protection task
     */
    private void restartAutoProtectionTask() {
        stopAutoProtectionTask();
        startAutoProtectionTask();
    }

    /**
     * Scan for new maps and protect them automatically
     */
    private void scanAndProtectMaps() {
        if (!autoProtectionEnabled) {
            return;
        }

        int newlyProtected = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Player player : world.getPlayers()) {
                // Check if player has permission (if required)
                if (requirePlayerPermission && !player.hasPermission("lagx.mapart.autoprotect")) {
                    continue;
                }

                // Check all items in player's inventory
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof MapMeta) {
                        MapMeta mapMeta = (MapMeta) item.getItemMeta();
                        MapView mapView = mapMeta.getMapView();

                        if (mapView != null) {
                            int mapId = mapView.getId();

                            // Only protect if not already protected
                            if (!isMapProtected(mapId)) {
                                if (protectAllMaps || shouldAutoProtectMap(mapView)) {
                                    protectMap(mapId, player.getName());
                                    newlyProtected++;

                                    // Optional: Notify the player
                                    if (player.hasPermission("lagx.mapart.notify")) {
                                        player.sendMessage(
                                                "§aYour map art (ID: " + mapId + ") has been automatically protected!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (newlyProtected > 0) {
            plugin.getLogger().info("Auto-protected " + newlyProtected + " new maps");
        }
    }

    /**
     * Determine if a map should be automatically protected
     * This can be customized based on your criteria
     */
    private boolean shouldAutoProtectMap(MapView mapView) {
        // For now, we'll protect all filled maps
        // You can add more sophisticated logic here:
        // - Check if map has custom artwork
        // - Check map size/complexity
        // - Check if it's in a specific area
        return mapView != null;
    }

    /**
     * Enable or disable automatic protection
     */
    public void setAutoProtectionEnabled(boolean enabled) {
        this.autoProtectionEnabled = enabled;
        config.set("auto-protection.enabled", enabled);
        saveConfig();

        if (enabled) {
            startAutoProtectionTask();
            plugin.getLogger().info("Automatic map protection enabled");
        } else {
            stopAutoProtectionTask();
            plugin.getLogger().info("Automatic map protection disabled");
        }
    }

    /**
     * Check if automatic protection is enabled
     */
    public boolean isAutoProtectionEnabled() {
        return autoProtectionEnabled;
    }

    /**
     * Set the scan interval in minutes
     */
    public void setScanInterval(int minutes) {
        if (minutes < 1) {
            throw new IllegalArgumentException("Scan interval must be at least 1 minute");
        }

        this.scanIntervalMinutes = minutes;
        config.set("auto-protection.scan-interval-minutes", minutes);
        saveConfig();
        restartAutoProtectionTask();
        plugin.getLogger().info("Map protection scan interval set to " + minutes + " minutes");
    }

    /**
     * Get the current scan interval
     */
    public int getScanInterval() {
        return scanIntervalMinutes;
    }

    /**
     * Set whether to protect all maps or just qualifying ones
     */
    public void setProtectAllMaps(boolean protectAll) {
        this.protectAllMaps = protectAll;
        config.set("auto-protection.protect-all-maps", protectAll);
        saveConfig();
    }

    /**
     * Check if protecting all maps
     */
    public boolean isProtectingAllMaps() {
        return protectAllMaps;
    }

    /**
     * Reload the configuration from file
     */
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Map art manager configuration reloaded");
    }

    /**
     * Shutdown the manager
     */
    public void shutdown() {
        stopAutoProtectionTask();
    }
}