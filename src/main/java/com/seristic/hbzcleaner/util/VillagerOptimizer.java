package com.seristic.hbzcleaner.util;

import com.seristic.hbzcleaner.main.HBZCleaner;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

public class VillagerOptimizer implements Listener {
   private final HBZCleaner plugin;
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
   private final Map<UUID, Long> villagerLastMovement = new HashMap<>();
   private final Map<UUID, Long> villagerProfessionChangeCooldown = new HashMap<>();
   private final Map<String, Long> chunkBreedingCooldown = new HashMap<>();
   private int tickCounter = 0;

   public VillagerOptimizer(HBZCleaner plugin) {
      this.plugin = plugin;
      this.loadConfig();
      if (this.enabled) {
         Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> this.collectChunksForOptimization(), 50L, 50L, TimeUnit.MILLISECONDS);
      }
   }

   private void collectChunksForOptimization() {
      this.tickCounter++;
      if (this.tickCounter % 1000 == 0) {
         this.cleanupTrackingData();
      }

      for (World world : Bukkit.getWorlds()) {
         for (Chunk chunk : world.getLoadedChunks()) {
            Bukkit.getRegionScheduler().execute(this.plugin, world, chunk.getX(), chunk.getZ(), () -> this.optimizeChunkVillagers(chunk));
         }
      }
   }

   private void loadConfig() {
      ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("villager_optimization");
      if (config == null) {
         this.enabled = false;
      } else {
         this.enabled = config.getBoolean("enabled", true);
         this.aiTickReduction = config.getInt("ai_tick_reduction", 2);
         this.villagersPerChunkThreshold = config.getInt("villagers_per_chunk_threshold", 8);
         this.disablePathfindingAfterTicks = config.getInt("disable_pathfinding_after_ticks", 1200);
         this.reduceProfessionChanges = config.getBoolean("reduce_profession_changes", true);
         this.optimizeInventoryChecks = config.getBoolean("optimize_inventory_checks", true);
         this.optimizeSleepBehavior = config.getBoolean("optimize_sleep_behavior", true);
         ConfigurationSection breedingConfig = config.getConfigurationSection("limit_breeding");
         if (breedingConfig != null) {
            this.limitBreedingEnabled = breedingConfig.getBoolean("enabled", true);
            this.maxVillagersPerChunk = breedingConfig.getInt("max_villagers_per_chunk", 10);
            this.breedingCooldownTicks = breedingConfig.getInt("breeding_cooldown_ticks", 6000);
         }
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

      if (villagerCount >= this.villagersPerChunkThreshold) {
         for (Entity entityx : entities) {
            if (entityx instanceof Villager) {
               this.optimizeVillager((Villager)entityx);
            }
         }
      }
   }

   private void optimizeVillager(Villager villager) {
      UUID villagerUUID = villager.getUniqueId();
      this.trackVillagerMovement(villager, villagerUUID);
      if (this.optimizeSleepBehavior && this.tickCounter % 20 == 0) {
         try {
            boolean e = villager.isSleeping();
         } catch (Exception var4) {
            this.plugin.getLogger().warning("Error checking villager sleep state: " + var4.getMessage());
         }
      }
   }

   private void trackVillagerMovement(Villager villager, UUID villagerUUID) {
      try {
         double x = villager.getLocation().getX();
         double z = villager.getLocation().getZ();
         String currentPos = String.format("%.1f,%.1f", x, z);
         Long lastUpdate = this.villagerLastMovement.get(villagerUUID);
         if (lastUpdate == null) {
            this.villagerLastMovement.put(villagerUUID, (long)this.tickCounter + (long)currentPos.hashCode());
            return;
         }

         long timeSinceLastMovement = (long)this.tickCounter - (lastUpdate & 268435455L);
         this.villagerLastMovement.put(villagerUUID, (long)this.tickCounter + (long)currentPos.hashCode());
      } catch (Exception var11) {
         this.plugin.getLogger().warning("Error tracking villager movement: " + var11.getMessage());
      }
   }

   private void optimizeVillagerSleep(Villager villager) {
      if (this.tickCounter % 20 == 0) {
         try {
            if (villager.isSleeping() && Math.random() > 0.7) {
               return;
            }
         } catch (Exception var3) {
            this.plugin.getLogger().warning("Error in optimizeVillagerSleep: " + var3.getMessage());
         }
      }
   }

   private void optimizeInventoryBehavior(Villager villager) {
      try {
         if (Math.random() > 0.8) {
            villager.getInventory();
         }
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Error in optimizeInventoryBehavior: " + var3.getMessage());
      }
   }

   @EventHandler
   public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
      if (this.enabled && this.reduceProfessionChanges) {
         Villager villager = event.getEntity();
         UUID villagerUUID = villager.getUniqueId();
         Long lastChange = this.villagerProfessionChangeCooldown.get(villagerUUID);
         if (lastChange != null && System.currentTimeMillis() - lastChange < 30000L) {
            event.setCancelled(true);
         } else {
            this.villagerProfessionChangeCooldown.put(villagerUUID, System.currentTimeMillis());
         }
      }
   }

   @EventHandler
   public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
      if (this.enabled) {
         if (Math.random() > 0.7) {
            event.setCancelled(true);
         }
      }
   }

   public boolean canBreedInChunk(Chunk chunk) {
      if (!this.limitBreedingEnabled) {
         return true;
      } else {
         int villagerCount = 0;

         for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Villager) {
               villagerCount++;
            }
         }

         if (villagerCount >= this.maxVillagersPerChunk) {
            return false;
         } else {
            String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
            Long lastBreeding = this.chunkBreedingCooldown.get(chunkKey);
            if (lastBreeding != null && (long)this.tickCounter - lastBreeding < (long)this.breedingCooldownTicks) {
               return false;
            } else {
               this.chunkBreedingCooldown.put(chunkKey, (long)this.tickCounter);
               return true;
            }
         }
      }
   }

   private void cleanupTrackingData() {
      this.villagerLastMovement.entrySet().removeIf(entry -> (long)this.tickCounter - (entry.getValue() & 268435455L) > 12000L);
      long currentTime = System.currentTimeMillis();
      this.villagerProfessionChangeCooldown.entrySet().removeIf(entry -> currentTime - entry.getValue() > 300000L);
      this.chunkBreedingCooldown.entrySet().removeIf(entry -> (long)this.tickCounter - entry.getValue() > (long)(this.breedingCooldownTicks * 2));
   }

   public void reload() {
      this.loadConfig();
      this.villagerLastMovement.clear();
      this.villagerProfessionChangeCooldown.clear();
      this.chunkBreedingCooldown.clear();
      this.tickCounter = 0;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public String getStatus() {
      if (!this.enabled) {
         return "§cDisabled";
      } else {
         StringBuilder status = new StringBuilder("§aEnabled\n");
         status.append("§7AI Reduction: §e").append(this.aiTickReduction).append("x\n");
         status.append("§7Threshold: §e").append(this.villagersPerChunkThreshold).append(" villagers/chunk\n");
         status.append("§7Tracked Villagers: §e").append(this.villagerLastMovement.size());
         return status.toString();
      }
   }
}
