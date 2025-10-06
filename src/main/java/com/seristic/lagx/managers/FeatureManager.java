package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.util.*;
// import com.seristic.lagx.utils.MapArtManager; // DISABLED - Feature not ready, causes invisible maps

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all performance optimization features
 * Handles initialization, configuration, and lifecycle of features
 */
public class FeatureManager {

    private final LagX plugin;
    private final ConfigurationManager configManager;

    // Feature instances
    private EntityLimiter entityLimiter;
    private VillagerOptimizer villagerOptimizer;
    private EntityStacker entityStacker;
    private ItemFrameOptimizer itemFrameOptimizer;
    private PlayerDeathTracker playerDeathTracker;
    // private MapArtManager mapArtManager; // DISABLED - Feature not ready, causes
    // invisible maps

    // Feature registry for dynamic management
    private final Map<String, Object> features = new HashMap<>();
    private boolean initialized = false;

    public FeatureManager(LagX plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        if (initialized) {
            plugin.getLogger().warning("FeatureManager is already initialized");
            return;
        }

        plugin.getLogger().info("Initializing performance features...");

        try {
            // Initialize features based on configuration
            initializeEntityLimiter();
            initializeVillagerOptimizer();
            initializeEntityStacker();
            initializeItemFrameOptimizer();
            initializePlayerDeathTracker();
            // initializeMapArtManager(); // DISABLED - Feature not ready, causes invisible
            // maps

            initialized = true;
            plugin.getLogger().info("Initialized " + features.size() + " performance features");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize features: " + e.getMessage());
            throw new RuntimeException("Feature initialization failed", e);
        }
    }

    private void initializeEntityLimiter() {
        if (configManager.isFeatureEnabled("entity-limiter")) {
            try {
                entityLimiter = new EntityLimiter(plugin);
                features.put("entity-limiter", entityLimiter);
                plugin.getLogger().info("Entity Limiter enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize EntityLimiter: " + e.getMessage());
            }
        }
    }

    private void initializeVillagerOptimizer() {
        if (configManager.isFeatureEnabled("villager-optimizer")) {
            try {
                villagerOptimizer = new VillagerOptimizer(plugin);
                features.put("villager-optimizer", villagerOptimizer);
                plugin.getLogger().info("Villager Optimizer enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize VillagerOptimizer: " + e.getMessage());
            }
        }
    }

    private void initializeEntityStacker() {
        if (configManager.isFeatureEnabled("entity-stacker")) {
            try {
                entityStacker = new EntityStacker(plugin);
                features.put("entity-stacker", entityStacker);
                plugin.getLogger().info("Entity Stacker enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize EntityStacker: " + e.getMessage());
            }
        }
    }

    private void initializeItemFrameOptimizer() {
        if (configManager.isFeatureEnabled("itemframe-optimizer")) {
            try {
                itemFrameOptimizer = new ItemFrameOptimizer(plugin);
                features.put("itemframe-optimizer", itemFrameOptimizer);
                plugin.getLogger().info("ItemFrame Optimizer enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize ItemFrameOptimizer: " + e.getMessage());
            }
        }
    }

    private void initializePlayerDeathTracker() {
        if (configManager.isFeatureEnabled("death-tracker")) {
            try {
                playerDeathTracker = new PlayerDeathTracker(plugin);
                features.put("death-tracker", playerDeathTracker);
                plugin.getLogger().info("Player Death Tracker enabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize PlayerDeathTracker: " + e.getMessage());
            }
        }
    }

    // DISABLED - Map protection feature not ready, causes invisible maps
    // private void initializeMapArtManager() {
    // if (configManager.isFeatureEnabled("map-protection")) {
    // try {
    // mapArtManager = new MapArtManager(plugin);
    // features.put("map-protection", mapArtManager);
    // plugin.getLogger().info("Map Art Protection enabled");
    // } catch (Exception e) {
    // plugin.getLogger().warning("Failed to initialize MapArtManager: " +
    // e.getMessage());
    // }
    // }
    // }

    public void shutdown() {
        if (!initialized) {
            return;
        }

        plugin.getLogger().info("Shutting down performance features...");

        // DISABLED - Map art manager shutdown (feature not ready)
        // if (mapArtManager != null) {
        // mapArtManager.shutdown();
        // }

        // Clean up other features
        entityLimiter = null;
        villagerOptimizer = null;
        entityStacker = null;
        itemFrameOptimizer = null;
        playerDeathTracker = null;
        // mapArtManager = null; // DISABLED - Feature not ready

        features.clear();
        initialized = false;
    }

    public void reload() {
        shutdown();
        initialize();
    }

    // Feature getters
    public EntityLimiter getEntityLimiter() {
        return entityLimiter;
    }

    public VillagerOptimizer getVillagerOptimizer() {
        return villagerOptimizer;
    }

    public EntityStacker getEntityStacker() {
        return entityStacker;
    }

    public ItemFrameOptimizer getItemFrameOptimizer() {
        return itemFrameOptimizer;
    }

    public PlayerDeathTracker getPlayerDeathTracker() {
        return playerDeathTracker;
    }
    // public MapArtManager getMapArtManager() { return mapArtManager; } // DISABLED
    // - Feature not ready

    // Feature registry access
    @SuppressWarnings("unchecked")
    public <T> T getFeature(String name, Class<T> type) {
        Object feature = features.get(name);
        if (type.isInstance(feature)) {
            return (T) feature;
        }
        return null;
    }

    public boolean isFeatureEnabled(String name) {
        return features.containsKey(name);
    }

    public Map<String, Object> getAllFeatures() {
        return new HashMap<>(features);
    }
}