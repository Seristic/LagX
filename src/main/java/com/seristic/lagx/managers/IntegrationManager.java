package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.util.TownyIntegration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages integrations with other plugins
 * Handles soft dependencies and provides unified integration interface
 */
public class IntegrationManager {

    private final LagX plugin;
    private final ConfigurationManager configManager;

    // Integration instances
    private TownyIntegration townyIntegration;
    private CopyrightPluginIntegration copyrightIntegration;

    // Integration registry
    private final Map<String, Object> integrations = new HashMap<>();

    public IntegrationManager(LagX plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        plugin.getLogger().info("Initializing plugin integrations...");

        // Initialize Towny integration
        initializeTownyIntegration();

        // Initialize Copyright plugin integration
        initializeCopyrightIntegration();

        plugin.getLogger().info("Initialized " + integrations.size() + " plugin integrations");
    }

    private void initializeTownyIntegration() {
        if (!configManager.isIntegrationEnabled("towny")) {
            return;
        }

        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            try {
                townyIntegration = new TownyIntegration(plugin);
                integrations.put("towny", townyIntegration);
                plugin.getLogger().info("Towny integration enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize Towny integration: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("Towny not found, integration disabled");
        }
    }

    private void initializeCopyrightIntegration() {
        if (!configManager.isIntegrationEnabled("copyright-plugin")) {
            return;
        }

        // Look for common copyright plugin names
        String[] copyrightPluginNames = {
                "CopyrightPlugin", "MapCopyright", "MapProtection",
                "AntiMapCopy", "MapSecurity", "MapGuard"
        };

        Plugin copyrightPlugin = null;
        String pluginName = null;

        for (String name : copyrightPluginNames) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin != null && plugin.isEnabled()) {
                copyrightPlugin = plugin;
                pluginName = name;
                break;
            }
        }

        if (copyrightPlugin != null) {
            try {
                copyrightIntegration = new CopyrightPluginIntegration(plugin, copyrightPlugin, configManager);
                integrations.put("copyright-plugin", copyrightIntegration);
                this.plugin.getLogger().info("Copyright plugin integration enabled (" + pluginName + ")");
            } catch (Exception e) {
                this.plugin.getLogger().warning("Failed to initialize copyright plugin integration: " + e.getMessage());
            }
        } else {
            this.plugin.getLogger().info("No compatible copyright plugin found");
        }
    }

    public void reload() {
        shutdown();
        initialize();
    }

    public void shutdown() {
        // Clean shutdown of integrations
        if (copyrightIntegration != null) {
            copyrightIntegration.shutdown();
        }

        integrations.clear();
        townyIntegration = null;
        copyrightIntegration = null;
    }

    // Getters
    public TownyIntegration getTownyIntegration() {
        return townyIntegration;
    }

    public CopyrightPluginIntegration getCopyrightIntegration() {
        return copyrightIntegration;
    }

    public boolean isTownyAvailable() {
        return townyIntegration != null;
    }

    public boolean isCopyrightPluginAvailable() {
        return copyrightIntegration != null;
    }

    // Generic integration access
    @SuppressWarnings("unchecked")
    public <T> T getIntegration(String name, Class<T> type) {
        Object integration = integrations.get(name);
        if (type.isInstance(integration)) {
            return (T) integration;
        }
        return null;
    }

    /**
     * Copyright Plugin Integration
     * Provides hooks and overrides for map protection
     */
    public static class CopyrightPluginIntegration {
        private final LagX plugin;
        private final Plugin copyrightPlugin;
        private final ConfigurationManager configManager;

        public CopyrightPluginIntegration(LagX plugin, Plugin copyrightPlugin, ConfigurationManager configManager) {
            this.plugin = plugin;
            this.copyrightPlugin = copyrightPlugin;
            this.configManager = configManager;

            // Register hooks or listeners here
            setupHooks();
        }

        private void setupHooks() {
            // Register event listener to intercept copyright plugin events
            if (configManager.isCopyrightPluginOverrideEnabled()) {
                Bukkit.getPluginManager().registerEvents(new CopyrightOverrideListener(plugin, this), plugin);
                plugin.getLogger().info("Copyright plugin override hooks registered");
            }
        }

        /**
         * Check if LagX should override copyright protection for a specific map
         */
        public boolean shouldOverrideProtection(int mapId) {
            // Logic to determine if LagX map protection should override
            // the copyright plugin's protection
            return configManager.isCopyrightPluginOverrideEnabled();
        }

        /**
         * Force update a protected map despite copyright restrictions
         */
        public boolean forceMapUpdate(int mapId) {
            if (!shouldOverrideProtection(mapId)) {
                return false;
            }

            try {
                // Implementation would depend on the specific copyright plugin API
                // This is a generic approach using reflection or events
                plugin.getLogger().info("Forcing map update for ID " + mapId + " (overriding copyright protection)");
                return true;
            } catch (Exception e) {
                plugin.getLogger()
                        .warning("Failed to override copyright protection for map " + mapId + ": " + e.getMessage());
                return false;
            }
        }

        /**
         * Get copyright plugin information
         */
        public String getCopyrightPluginInfo() {
            return copyrightPlugin.getName() + " v" + copyrightPlugin.getDescription().getVersion();
        }

        public void shutdown() {
            // Clean up any registered listeners or hooks
        }
    }
}