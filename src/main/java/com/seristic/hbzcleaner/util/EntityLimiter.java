package com.seristic.hbzcleaner.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import com.seristic.hbzcleaner.main.LaggRemover;

public class EntityLimiter implements Listener {
    
    private final LaggRemover plugin;
    private boolean enabled;
    private Map<String, Integer> worldLimits;
    private int defaultWorldLimit;
    private int totalPerChunk;
    private int hostilePerChunk;
    private int passivePerChunk;
    private int itemPerChunk;
    private String overflowAction;
    private int checkInterval;
    private int chunkBuffer;      // Buffer for chunk limits
    private int worldBuffer;      // Buffer for world limits
    
    public EntityLimiter(LaggRemover plugin) {
        this.plugin = plugin;
        loadConfig();
        
        if (enabled && checkInterval > 0) {
            // Start the periodic cleanup task using GlobalRegionScheduler for Folia compatibility
            // Convert ticks to milliseconds (checkInterval is in ticks, 50ms per tick)
            long intervalMs = checkInterval * 50L;
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
                checkAndEnforceLimits();
            }, intervalMs / 50L, intervalMs / 50L); // Convert back to ticks for scheduler
        }
    }
    
    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("entity_limiter");
        if (config == null) {
            enabled = false;
            return;
        }
        
        enabled = config.getBoolean("enabled", true);
        
        // Load preset mode
        String presetMode = config.getString("preset_mode", "advanced");
        
        switch (presetMode.toLowerCase()) {
            case "basic":
                loadBasicPreset(config);
                break;
            case "custom":
                loadCustomPreset(config);
                break;
            case "advanced":
            default:
                loadAdvancedPreset(config);
                break;
        }
        
        checkInterval = config.getInt("check_interval", 100);
        
        // Load buffer settings (how many entities below the limit to target when cleaning)
        chunkBuffer = config.getInt("chunk_buffer", 5);
        worldBuffer = config.getInt("world_buffer", 50);
    }
    
    private void loadBasicPreset(ConfigurationSection config) {
        ConfigurationSection basicConfig = config.getConfigurationSection("basic_preset");
        if (basicConfig == null) {
            // Fallback to default basic values
            totalPerChunk = 30;
            defaultWorldLimit = 1500;
            overflowAction = "remove_oldest";
        } else {
            totalPerChunk = basicConfig.getInt("total_entities_per_chunk", 30);
            defaultWorldLimit = basicConfig.getInt("total_entities_per_world", 1500);
            overflowAction = basicConfig.getString("overflow_action", "remove_oldest");
        }
        
        // For basic preset, all entity types share the same limit
        hostilePerChunk = totalPerChunk;
        passivePerChunk = totalPerChunk;
        itemPerChunk = totalPerChunk;
        
        worldLimits = new HashMap<>();
        plugin.getLogger().info("EntityLimiter loaded with BASIC preset: " + totalPerChunk + " entities per chunk max");
    }
    
    private void loadAdvancedPreset(ConfigurationSection config) {
        ConfigurationSection advancedConfig = config.getConfigurationSection("advanced_preset");
        if (advancedConfig == null) {
            // Use main config as fallback for backward compatibility
            advancedConfig = config;
        }
        
        // Load world limits
        worldLimits = new HashMap<>();
        ConfigurationSection worldLimitsSection = advancedConfig.getConfigurationSection("world_limits");
        if (worldLimitsSection != null) {
            defaultWorldLimit = worldLimitsSection.getInt("default", 2000);
            for (String key : worldLimitsSection.getKeys(false)) {
                if (!key.equals("default")) {
                    worldLimits.put(key, worldLimitsSection.getInt(key));
                }
            }
        }
        
        // Load chunk limits
        ConfigurationSection chunkLimits = advancedConfig.getConfigurationSection("chunk_limits");
        if (chunkLimits != null) {
            totalPerChunk = chunkLimits.getInt("total_per_chunk", 50);
            hostilePerChunk = chunkLimits.getInt("hostile_per_chunk", 15);
            passivePerChunk = chunkLimits.getInt("passive_per_chunk", 20);
            itemPerChunk = chunkLimits.getInt("item_per_chunk", 30);
        }
        
        overflowAction = advancedConfig.getString("overflow_action", "prevent_spawn");
        plugin.getLogger().info("EntityLimiter loaded with ADVANCED preset: Total=" + totalPerChunk + ", Hostile=" + hostilePerChunk + ", Passive=" + passivePerChunk);
    }
    
    private void loadCustomPreset(ConfigurationSection config) {
        ConfigurationSection customConfig = config.getConfigurationSection("custom_config");
        if (customConfig == null) {
            plugin.getLogger().warning("Custom preset selected but no custom_config found, falling back to advanced preset");
            loadAdvancedPreset(config);
            return;
        }
        
        // Load world limits
        worldLimits = new HashMap<>();
        ConfigurationSection worldLimitsSection = customConfig.getConfigurationSection("world_limits");
        if (worldLimitsSection != null) {
            defaultWorldLimit = worldLimitsSection.getInt("default", 3000);
            for (String key : worldLimitsSection.getKeys(false)) {
                if (!key.equals("default")) {
                    worldLimits.put(key, worldLimitsSection.getInt(key));
                }
            }
        }
        
        // Load chunk limits
        ConfigurationSection chunkLimits = customConfig.getConfigurationSection("chunk_limits");
        if (chunkLimits != null) {
            totalPerChunk = chunkLimits.getInt("total_per_chunk", 75);
            hostilePerChunk = chunkLimits.getInt("hostile_per_chunk", 25);
            passivePerChunk = chunkLimits.getInt("passive_per_chunk", 30);
            itemPerChunk = chunkLimits.getInt("item_per_chunk", 40);
        }
        
        overflowAction = customConfig.getString("overflow_action", "remove_random");
        plugin.getLogger().info("EntityLimiter loaded with CUSTOM preset: Total=" + totalPerChunk + ", Hostile=" + hostilePerChunk + ", Passive=" + passivePerChunk);
    }
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!enabled) return;
        
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        World world = entity.getWorld();
        
        // Check world limits
        if (!isWithinWorldLimit(world)) {
            event.setCancelled(true);
            return;
        }
        
        // Check chunk limits
        if (!isWithinChunkLimit(chunk, entity)) {
            if ("prevent_spawn".equals(overflowAction)) {
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isWithinWorldLimit(World world) {
        int worldLimit = worldLimits.getOrDefault(world.getName(), defaultWorldLimit);
        if (worldLimit <= 0) return true; // No limit
        
        return world.getEntityCount() < worldLimit;
    }
    
    private boolean isWithinChunkLimit(Chunk chunk, Entity newEntity) {
        Entity[] entities = chunk.getEntities();
        
        // Count entities by type
        int totalCount = entities.length;
        
        // Check total limit first
        if (totalCount >= totalPerChunk) return false;
        
        // If this is basic mode (all limits are the same), only check total
        if (hostilePerChunk == passivePerChunk && passivePerChunk == itemPerChunk && itemPerChunk == totalPerChunk) {
            return true; // Already checked total above
        }
        
        // Advanced mode - check individual type limits
        int hostileCount = 0;
        int passiveCount = 0;
        int itemCount = 0;
        
        for (Entity entity : entities) {
            if (entity instanceof Item) {
                itemCount++;
            } else if (entity instanceof Monster) {
                hostileCount++;
            } else if (entity instanceof LivingEntity) {
                passiveCount++;
            }
        }
        
        // Check specific type limits for advanced mode
        if (newEntity instanceof Item && itemCount >= itemPerChunk) return false;
        if (newEntity instanceof Monster && hostileCount >= hostilePerChunk) return false;
        if (newEntity instanceof LivingEntity && !(newEntity instanceof Monster) && passiveCount >= passivePerChunk) return false;
        
        return true;
    }
    
    private void checkAndEnforceLimits() {
        // For Folia compatibility, schedule checks per chunk to respect region threading
        // Note: World limits are disabled in Folia mode due to cross-region access limitations
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                final Chunk finalChunk = chunk;
                Bukkit.getRegionScheduler().run(plugin, world, finalChunk.getX(), finalChunk.getZ(), task -> {
                    // Only check chunk limits since world limits require cross-region access
                    enforceChunkLimits(finalChunk);
                });
            }
        }
    }
    
    
    private void enforceChunkLimits(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        
        if (entities.length <= totalPerChunk) return;
        
        // Remove excess entities down to a buffer below the limit
        // This prevents constant triggering when at the exact limit
        int targetCount = totalPerChunk - chunkBuffer;
        int toRemove = entities.length - targetCount;
        
        if (toRemove > 0) {
            switch (overflowAction) {
                case "remove_oldest":
                    removeOldestEntities(entities, toRemove);
                    break;
                case "remove_random":
                    removeRandomEntities(entities, toRemove);
                    break;
            }
        }
    }
    
    private void removeOldestEntities(Entity[] entities, int toRemove) {
        // Create a list with UUID and ticks lived to avoid threading issues
        java.util.List<EntityData> entityDataList = new java.util.ArrayList<>();
        
        for (Entity entity : entities) {
            if (entity != null && !(entity instanceof org.bukkit.entity.Player) && canRemoveEntity(entity)) {
                try {
                    // Safely get ticks lived - this might fail in threading environment
                    long ticksLived = entity.getTicksLived();
                    entityDataList.add(new EntityData(entity, ticksLived));
                } catch (Exception e) {
                    // If we can't get ticks lived due to threading, use random order
                    entityDataList.add(new EntityData(entity, System.currentTimeMillis()));
                }
            }
        }
        
        // Sort by ticks lived (older entities first)
        entityDataList.sort((a, b) -> Long.compare(a.ticksLived, b.ticksLived));
        
        int removed = 0;
        for (EntityData entityData : entityDataList) {
            if (removed >= toRemove) break;
            try {
                entityData.entity.remove();
                removed++;
            } catch (Exception e) {
                // Entity might have been removed or moved to different region
                continue;
            }
        }
    }
    
    private static class EntityData {
        final Entity entity;
        final long ticksLived;
        
        EntityData(Entity entity, long ticksLived) {
            this.entity = entity;
            this.ticksLived = ticksLived;
        }
    }
    
    /**
     * Check if an entity can be removed (not protected by Towny)
     */
    private boolean canRemoveEntity(Entity entity) {
        TownyIntegration towny = LaggRemover.getTownyIntegration();
        if (towny != null && towny.isEntityProtected(entity)) {
            return false; // Protected by Towny
        }
        return true;
    }
    
    private void removeRandomEntities(Entity[] entities, int toRemove) {
        // Shuffle array and remove first N entities (excluding players)
        Entity[] shuffled = entities.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            Entity temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }
        
        int removed = 0;
        for (Entity entity : shuffled) {
            if (removed >= toRemove) break;
            if (entity != null && !(entity instanceof org.bukkit.entity.Player) && canRemoveEntity(entity)) {
                entity.remove();
                removed++;
            }
        }
    }
    
    public void reload() {
        loadConfig();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getStatus() {
        if (!enabled) return "§cDisabled";
        
        StringBuilder status = new StringBuilder("§aEnabled\n");
        status.append("§7World Limits: §e").append(worldLimits.size()).append(" configured\n");
        status.append("§7Chunk Limit: §e").append(totalPerChunk).append(" entities\n");
        status.append("§7Overflow Action: §e").append(overflowAction).append("\n");
        status.append("§7Buffers: §eWorld(-").append(worldBuffer).append(") Chunk(-").append(chunkBuffer).append(")");
        
        return status.toString();
    }
}
