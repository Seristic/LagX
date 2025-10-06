package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Handles all configuration management for LagX
 * Centralizes configuration loading, saving, and access
 */
public class ConfigurationManager {

    private final LagX plugin;
    private FileConfiguration config;
    private File configFile;

    // Configuration constants
    public static final String CONFIG_VERSION = "0.2.0";

    public ConfigurationManager(LagX plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void initialize() {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Check if config exists and if it's outdated
        boolean configExists = configFile.exists();

        if (configExists) {
            // Load existing config to check version
            FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
            String existingVersion = existingConfig.getString("version", "0.0.0");

            if (!CONFIG_VERSION.equals(existingVersion)) {
                plugin.getLogger().warning(
                        "Config version mismatch! Found: " + existingVersion + ", Expected: " + CONFIG_VERSION);
                plugin.getLogger().warning("Backing up old config and generating new one...");

                // Backup old config
                File backup = new File(plugin.getDataFolder(), "config.yml.backup-" + existingVersion);
                try {
                    if (configFile.renameTo(backup)) {
                        plugin.getLogger().info("Old config backed up to: " + backup.getName());
                        configExists = false; // Treat as if config doesn't exist
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to backup old config: " + e.getMessage());
                }
            }
        }

        // Save default config if it doesn't exist (or was backed up)
        if (!configExists) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Generated new config file with version " + CONFIG_VERSION);
        }

        // Load configuration
        config = YamlConfiguration.loadConfiguration(configFile);

        // Update config version and add missing defaults
        updateConfigDefaults();

        plugin.getLogger().info("Configuration loaded successfully");
    }

    private void updateConfigDefaults() {
        // Don't add any extra keys - just ensure version is set
        // The config.yml should be the source of truth
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration reloaded");
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save configuration: " + e.getMessage());
        }
    }

    public void shutdown() {
        saveConfig();
    }

    // Configuration access methods
    public FileConfiguration getConfig() {
        return config;
    }

    // Feature toggles
    public boolean isFeatureEnabled(String featureName) {
        return config.getBoolean("features." + featureName + ".enabled", false);
    }

    public void setFeatureEnabled(String featureName, boolean enabled) {
        config.set("features." + featureName + ".enabled", enabled);
        saveConfig();
    }

    // Task settings
    public boolean isTaskEnabled(String taskName) {
        // Map task names to actual config keys
        if ("lag-removal".equals(taskName)) {
            return config.getBoolean("auto-lag-removal.run", true);
        }
        // For other tasks, use the old structure if it exists
        return config.getBoolean("tasks." + taskName + ".enabled", false);
    }

    public int getTaskInterval(String taskName) {
        // Map task names to actual config keys
        if ("lag-removal".equals(taskName)) {
            // Check if debug mode is enabled
            if (config.getBoolean("auto-lag-removal.debug", false)) {
                return 1; // 1 minute for debug mode
            }
            return config.getInt("auto-lag-removal.every", 10);
        }
        // For other tasks, use the old structure if it exists
        return config.getInt("tasks." + taskName + ".interval", 10);
    }
    
    public boolean isDebugMode() {
        return config.getBoolean("auto-lag-removal.debug", false);
    }

    // Integration settings
    public boolean isIntegrationEnabled(String integrationName) {
        return config.getBoolean("integrations." + integrationName + ".enabled", false);
    }

    // DISABLED - Map protection feature not ready, causes invisible maps
    // public boolean isMapProtectionAutoScanEnabled() {
    // return config.getBoolean("map-protection.auto-scan.enabled", true);
    // }

    // public int getMapProtectionScanInterval() {
    // return config.getInt("map-protection.auto-scan.interval", 5);
    // }

    // public boolean requiresMapProtectionPermission() {
    // return config.getBoolean("map-protection.require-permission", true);
    // }

    // Copyright plugin integration
    public boolean isCopyrightPluginOverrideEnabled() {
        return config.getBoolean("integrations.copyright-plugin.override-protection", false);
    }

    // Command settings
    public boolean requiresCommandPermission() {
        return config.getBoolean("commands.require-permission", true);
    }

    public boolean isTabCompletionEnabled() {
        return config.getBoolean("commands.tab-completion", true);
    }

    // Warning settings
    public boolean areWarningsEnabled() {
        return config.getBoolean("protocol_warnings.enabled", true);
    }

    public void setWarningsEnabled(boolean enabled) {
        config.set("protocol_warnings.enabled", enabled);
        saveConfig();
    }

    public double getTpsWarningThreshold() {
        return config.getDouble("warnings.tps-threshold", 18.0);
    }

    public int getMemoryWarningThreshold() {
        return config.getInt("warnings.memory-threshold", 80);
    }

    // Generic getters for extensibility
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}