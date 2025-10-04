package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import org.bukkit.plugin.PluginManager;

/**
 * Handles plugin lifecycle, initialization, and shutdown
 * Responsible for coordinating all managers and ensuring proper
 * startup/shutdown order
 */
public class LagXPluginManager {

    private final LagX plugin;
    private final FeatureManager featureManager;
    private final TaskManager taskManager;
    private final EventManager eventManager;
    private final ConfigurationManager configManager;
    private final IntegrationManager integrationManager;
    private final ModuleLoader moduleLoader;

    private boolean initialized = false;

    public LagXPluginManager(LagX plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigurationManager(plugin);
        this.featureManager = new FeatureManager(plugin, configManager);
        this.taskManager = new TaskManager(plugin, configManager);
        this.eventManager = new EventManager(plugin);
        this.integrationManager = new IntegrationManager(plugin, configManager);
        this.moduleLoader = new ModuleLoader(plugin);
    }

    /**
     * Initialize all managers in the correct order
     */
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("LagXPluginManager is already initialized");
        }

        try {
            plugin.getLogger().info("Initializing LagX v" + plugin.getPluginMeta().getVersion());

            // 1. Load configuration first
            configManager.initialize();

            // 2. Load external modules
            moduleLoader.initialize();

            // 3. Initialize integrations (soft dependencies)
            integrationManager.initialize();

            // 4. Initialize features
            featureManager.initialize();

            // 5. Register event listeners
            eventManager.initialize();

            // 6. Start scheduled tasks
            taskManager.initialize();

            initialized = true;
            plugin.getLogger().info("LagX initialization completed successfully");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize LagX: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Plugin initialization failed", e);
        }
    }

    /**
     * Shutdown all managers in reverse order
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            plugin.getLogger().info("Shutting down LagX...");

            // Shutdown in reverse order
            taskManager.shutdown();
            eventManager.shutdown();
            featureManager.shutdown();
            integrationManager.shutdown();
            moduleLoader.shutdown();
            configManager.shutdown();

            initialized = false;
            plugin.getLogger().info("LagX shutdown completed");

        } catch (Exception e) {
            plugin.getLogger().severe("Error during LagX shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reload all managers
     */
    public void reload() {
        plugin.getLogger().info("Reloading LagX...");

        // Shutdown gracefully
        taskManager.shutdown();
        featureManager.shutdown();

        // Reload configuration
        configManager.reload();

        // Reinitialize
        integrationManager.reload();
        featureManager.initialize();
        taskManager.initialize();

        plugin.getLogger().info("LagX reload completed");
    }

    // Getters for managers
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public boolean isInitialized() {
        return initialized;
    }
}