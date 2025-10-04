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

        // Save default config if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        // Load configuration
        config = YamlConfiguration.loadConfiguration(configFile);

        // Update config version and add missing defaults
        updateConfigDefaults();

        plugin.getLogger().info("Configuration loaded successfully");
    }

    private void updateConfigDefaults() {
        boolean needsSave = false;

        // Check and update config version
        if (!CONFIG_VERSION.equals(config.getString("config-version"))) {
            config.set("config-version", CONFIG_VERSION);
            needsSave = true;
        }

        // Feature toggles
        needsSave |= addDefaultIfMissing("features.entity-limiter.enabled", true);
        needsSave |= addDefaultIfMissing("features.villager-optimizer.enabled", true);
        needsSave |= addDefaultIfMissing("features.entity-stacker.enabled", true);
        needsSave |= addDefaultIfMissing("features.itemframe-optimizer.enabled", true);
        needsSave |= addDefaultIfMissing("features.death-tracker.enabled", true);
        needsSave |= addDefaultIfMissing("features.map-protection.enabled", true);

        // Task intervals (in minutes)
        needsSave |= addDefaultIfMissing("tasks.chunk-unload.enabled", true);
        needsSave |= addDefaultIfMissing("tasks.chunk-unload.interval", 10);
        needsSave |= addDefaultIfMissing("tasks.lag-removal.enabled", true);
        needsSave |= addDefaultIfMissing("tasks.lag-removal.interval", 5);
        needsSave |= addDefaultIfMissing("tasks.entity-cleanup.enabled", true);
        needsSave |= addDefaultIfMissing("tasks.entity-cleanup.interval", 15);

        // Map protection settings
        needsSave |= addDefaultIfMissing("map-protection.auto-scan.enabled", true);
        needsSave |= addDefaultIfMissing("map-protection.auto-scan.interval", 5);
        needsSave |= addDefaultIfMissing("map-protection.require-permission", true);

        // Integration settings
        needsSave |= addDefaultIfMissing("integrations.towny.enabled", true);
        needsSave |= addDefaultIfMissing("integrations.copyright-plugin.enabled", true);
        needsSave |= addDefaultIfMissing("integrations.copyright-plugin.override-protection", false);

        // Command settings
        needsSave |= addDefaultIfMissing("commands.require-permission", true);
        needsSave |= addDefaultIfMissing("commands.tab-completion", true);

        // Warning settings
        needsSave |= addDefaultIfMissing("warnings.enabled", true);
        needsSave |= addDefaultIfMissing("warnings.tps-threshold", 18.0);
        needsSave |= addDefaultIfMissing("warnings.memory-threshold", 80);

        if (needsSave) {
            saveConfig();
            plugin.getLogger().info("Configuration updated with new defaults");
        }
    }

    private boolean addDefaultIfMissing(String path, Object defaultValue) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            return true;
        }
        return false;
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
        return config.getBoolean("tasks." + taskName + ".enabled", false);
    }

    public int getTaskInterval(String taskName) {
        return config.getInt("tasks." + taskName + ".interval", 10);
    }

    // Integration settings
    public boolean isIntegrationEnabled(String integrationName) {
        return config.getBoolean("integrations." + integrationName + ".enabled", false);
    }

    // Map protection specific settings
    public boolean isMapProtectionAutoScanEnabled() {
        return config.getBoolean("map-protection.auto-scan.enabled", true);
    }

    public int getMapProtectionScanInterval() {
        return config.getInt("map-protection.auto-scan.interval", 5);
    }

    public boolean requiresMapProtectionPermission() {
        return config.getBoolean("map-protection.require-permission", true);
    }

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
        return config.getBoolean("warnings.enabled", true);
    }

    public void setWarningsEnabled(boolean enabled) {
        config.set("warnings.enabled", enabled);
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