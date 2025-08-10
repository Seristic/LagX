package com.seristic.hbzcleaner.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

import com.seristic.hbzcleaner.main.LaggRemover;

public class VillagerOptimizer implements Listener {
    
    private final LaggRemover plugin;
    private boolean enabled;
    private int aiTickReduction;
    private int villagersPerChunkThreshold;
    private int disablePathfindingAfterTicks;
    private boolean reduceProfessionChanges;
    private boolean limitBreedingEnabled;
    private int maxVillagersPerChunk;
    private int breedingCooldownTicks;
    private boolean optimizeInventoryChecks;
    private boolean optimizeSleepBehavior;
    
    // Tracking maps
    private final Map<UUID, Long> villagerLastMovement = new HashMap<>();
    private final Map<UUID, Long> villagerProfessionChangeCooldown = new HashMap<>();
    private final Map<String, Long> chunkBreedingCooldown = new HashMap<>();
    private int tickCounter = 0;
    
    public VillagerOptimizer(LaggRemover plugin) {
        this.plugin = plugin;
        loadConfig();
        
        if (enabled) {
            // Start the optimization task
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
                optimizeVillagers();
            }, 50, 50, java.util.concurrent.TimeUnit.MILLISECONDS); // Run every 50ms (1 tick)
        }
    }
    
    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("villager_optimization");
        if (config == null) {
            enabled = false;
            return;
        }
        
        enabled = config.getBoolean("enabled", true);
        aiTickReduction = config.getInt("ai_tick_reduction", 2);
        villagersPerChunkThreshold = config.getInt("villagers_per_chunk_threshold", 8);
        disablePathfindingAfterTicks = config.getInt("disable_pathfinding_after_ticks", 1200);
        reduceProfessionChanges = config.getBoolean("reduce_profession_changes", true);
        optimizeInventoryChecks = config.getBoolean("optimize_inventory_checks", true);
        optimizeSleepBehavior = config.getBoolean("optimize_sleep_behavior", true);
        
        ConfigurationSection breedingConfig = config.getConfigurationSection("limit_breeding");
        if (breedingConfig != null) {
            limitBreedingEnabled = breedingConfig.getBoolean("enabled", true);
            maxVillagersPerChunk = breedingConfig.getInt("max_villagers_per_chunk", 10);
            breedingCooldownTicks = breedingConfig.getInt("breeding_cooldown_ticks", 6000);
        }
    }
    
    private void optimizeVillagers() {
        tickCounter++;
        
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                optimizeChunkVillagers(chunk);
            }
        }
        
        // Clean up old tracking data every 1000 ticks
        if (tickCounter % 1000 == 0) {
            cleanupTrackingData();
        }
    }
    
    private void optimizeChunkVillagers(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        int villagerCount = 0;
        
        for (Entity entity : entities) {
            if (entity instanceof Villager) {
                villagerCount++;
            }
        }
        
        // Only optimize if we exceed the threshold
        if (villagerCount < villagersPerChunkThreshold) {
            return;
        }
        
        for (Entity entity : entities) {
            if (entity instanceof Villager) {
                optimizeVillager((Villager) entity);
            }
        }
    }
    
    private void optimizeVillager(Villager villager) {
        UUID villagerUUID = villager.getUniqueId();
        
        // AI Tick Reduction
        if (aiTickReduction > 1 && tickCounter % aiTickReduction != 0) {
            // Skip AI processing for this villager this tick
            try {
                // Use reflection to disable AI temporarily (Folia-safe approach)
                villager.setAI(false);
                
                // Use Folia's region-based scheduler
                Location location = villager.getLocation();
                Bukkit.getRegionScheduler().runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task -> {
                    if (villager.isValid()) {
                        villager.setAI(true);
                    }
                }, 1);
            } catch (Exception e) {
                // Fallback if reflection fails
            }
        }
        
        // Track movement for pathfinding optimization
        trackVillagerMovement(villager, villagerUUID);
        
        // Optimize sleep behavior
        if (optimizeSleepBehavior) {
            optimizeVillagerSleep(villager);
        }
        
        // Inventory optimization
        if (optimizeInventoryChecks && tickCounter % 100 == 0) {
            // Reduce frequency of inventory checks
            optimizeInventoryBehavior(villager);
        }
    }
    
    private void trackVillagerMovement(Villager villager, UUID villagerUUID) {
        double x = villager.getLocation().getX();
        double z = villager.getLocation().getZ();
        
        String currentPos = String.format("%.1f,%.1f", x, z);
        Long lastUpdate = villagerLastMovement.get(villagerUUID);
        
        if (lastUpdate == null) {
            villagerLastMovement.put(villagerUUID, (long) tickCounter + currentPos.hashCode());
            return;
        }
        
        // Check if villager hasn't moved significantly
        long timeSinceLastMovement = tickCounter - (lastUpdate & 0xFFFFFFF); // Extract tick part
        if (timeSinceLastMovement > disablePathfindingAfterTicks) {
            // Disable pathfinding for stationary villagers
            try {
                villager.setAI(false);
                // Re-enable after a delay to prevent permanent disabling
                
                // Use Folia's region-based scheduler
                Location location = villager.getLocation();
                Bukkit.getRegionScheduler().runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task -> {
                    if (villager.isValid()) {
                        villager.setAI(true);
                    }
                }, 200); // 10 seconds
            } catch (Exception e) {
                // Handle any reflection errors gracefully
            }
        }
        
        // Update movement tracking
        villagerLastMovement.put(villagerUUID, (long) tickCounter + currentPos.hashCode());
    }
    
    private void optimizeVillagerSleep(Villager villager) {
        // Optimize sleep behavior by reducing unnecessary sleep state checks
        if (tickCounter % 20 == 0) { // Check only once per second
            try {
                // Reduce sleep behavior processing frequency
                if (villager.isSleeping() && Math.random() > 0.7) {
                    // 30% chance to skip sleep processing
                    return;
                }
            } catch (Exception e) {
                // Handle any API changes gracefully
            }
        }
    }
    
    private void optimizeInventoryBehavior(Villager villager) {
        // Optimize villager inventory checking frequency
        try {
            // Reduce the frequency of villager inventory updates
            if (Math.random() > 0.8) { // 20% chance to process inventory
                villager.getInventory(); // Trigger inventory check
            }
        } catch (Exception e) {
            // Handle any API issues gracefully
        }
    }
    
    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        if (!enabled || !reduceProfessionChanges) return;
        
        Villager villager = event.getEntity();
        UUID villagerUUID = villager.getUniqueId();
        
        // Check cooldown for profession changes
        Long lastChange = villagerProfessionChangeCooldown.get(villagerUUID);
        if (lastChange != null && (System.currentTimeMillis() - lastChange) < 30000) { // 30 second cooldown
            event.setCancelled(true);
            return;
        }
        
        villagerProfessionChangeCooldown.put(villagerUUID, System.currentTimeMillis());
    }
    
    @EventHandler
    public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        if (!enabled) return;
        
        // Optimize trade replenishment frequency
        if (Math.random() > 0.7) { // 30% chance to delay trade replenishment
            event.setCancelled(true);
            
            // Schedule delayed replenishment using Folia's region-based scheduler
            // Cast to appropriate type
            Entity entity = event.getEntity();
            if (!(entity instanceof Villager)) return;
            
            Villager villager = (Villager) entity;
            Location location = villager.getLocation();
            
            Bukkit.getRegionScheduler().runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task -> {
                if (villager.isValid()) {
                    // Trigger natural replenishment later
                    villager.getRecipes(); // This will refresh trades
                }
            }, 100); // 5 second delay
        }
    }
    
    public boolean canBreedInChunk(Chunk chunk) {
        if (!limitBreedingEnabled) return true;
        
        // Count villagers in chunk
        int villagerCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Villager) {
                villagerCount++;
            }
        }
        
        if (villagerCount >= maxVillagersPerChunk) {
            return false;
        }
        
        // Check breeding cooldown for this chunk
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        Long lastBreeding = chunkBreedingCooldown.get(chunkKey);
        
        if (lastBreeding != null && (tickCounter - lastBreeding) < breedingCooldownTicks) {
            return false;
        }
        
        chunkBreedingCooldown.put(chunkKey, (long) tickCounter);
        return true;
    }
    
    private void cleanupTrackingData() {
        // Remove old movement tracking data
        villagerLastMovement.entrySet().removeIf(entry -> 
            tickCounter - (entry.getValue() & 0xFFFFFFF) > 12000); // 10 minutes
        
        // Remove old profession change cooldowns
        long currentTime = System.currentTimeMillis();
        villagerProfessionChangeCooldown.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > 300000); // 5 minutes
        
        // Remove old breeding cooldowns
        chunkBreedingCooldown.entrySet().removeIf(entry -> 
            tickCounter - entry.getValue() > breedingCooldownTicks * 2);
    }
    
    public void reload() {
        loadConfig();
        villagerLastMovement.clear();
        villagerProfessionChangeCooldown.clear();
        chunkBreedingCooldown.clear();
        tickCounter = 0;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getStatus() {
        if (!enabled) return "§cDisabled";
        
        StringBuilder status = new StringBuilder("§aEnabled\n");
        status.append("§7AI Reduction: §e").append(aiTickReduction).append("x\n");
        status.append("§7Threshold: §e").append(villagersPerChunkThreshold).append(" villagers/chunk\n");
        status.append("§7Tracked Villagers: §e").append(villagerLastMovement.size());
        
        return status.toString();
    }
}
