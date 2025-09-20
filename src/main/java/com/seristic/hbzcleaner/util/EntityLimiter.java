package com.seristic.lagx.util;

import com.seristic.lagx.main.HBZCleaner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntityLimiter implements Listener {
   private final HBZCleaner plugin;
   private boolean enabled;
   private Map<String, Integer> worldLimits;
   private int defaultWorldLimit;
   private int totalPerChunk;
   private int hostilePerChunk;
   private int passivePerChunk;
   private int itemPerChunk;
   private String overflowAction;
   private int checkInterval;
   private int chunkBuffer;
   private int worldBuffer;

   public EntityLimiter(HBZCleaner plugin) {
      this.plugin = plugin;
      this.loadConfig();
      if (this.enabled && this.checkInterval > 0) {
         long intervalMs = (long)this.checkInterval * 50L;
         Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> this.checkAndEnforceLimits(), intervalMs / 50L, intervalMs / 50L);
      }
   }

   private void loadConfig() {
      ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("entity_limiter");
      if (config == null) {
         this.enabled = false;
      } else {
         this.enabled = config.getBoolean("enabled", true);
         String presetMode = config.getString("preset_mode", "advanced");
         String var3 = presetMode.toLowerCase();
         switch (var3) {
            case "basic":
               this.loadBasicPreset(config);
               break;
            case "custom":
               this.loadCustomPreset(config);
               break;
            case "advanced":
            default:
               this.loadAdvancedPreset(config);
         }

         this.checkInterval = config.getInt("check_interval", 100);
         this.chunkBuffer = config.getInt("chunk_buffer", 5);
         this.worldBuffer = config.getInt("world_buffer", 50);
      }
   }

   private void loadBasicPreset(ConfigurationSection config) {
      ConfigurationSection basicConfig = config.getConfigurationSection("basic_preset");
      if (basicConfig == null) {
         this.totalPerChunk = 30;
         this.defaultWorldLimit = 1500;
         this.overflowAction = "remove_oldest";
      } else {
         this.totalPerChunk = basicConfig.getInt("total_entities_per_chunk", 30);
         this.defaultWorldLimit = basicConfig.getInt("total_entities_per_world", 1500);
         this.overflowAction = basicConfig.getString("overflow_action", "remove_oldest");
      }

      this.hostilePerChunk = this.totalPerChunk;
      this.passivePerChunk = this.totalPerChunk;
      this.itemPerChunk = this.totalPerChunk;
      this.worldLimits = new HashMap<>();
      this.plugin.getLogger().info("EntityLimiter loaded with BASIC preset: " + this.totalPerChunk + " entities per chunk max");
   }

   private void loadAdvancedPreset(ConfigurationSection config) {
      ConfigurationSection advancedConfig = config.getConfigurationSection("advanced_preset");
      if (advancedConfig == null) {
         advancedConfig = config;
      }

      this.worldLimits = new HashMap<>();
      ConfigurationSection worldLimitsSection = advancedConfig.getConfigurationSection("world_limits");
      if (worldLimitsSection != null) {
         this.defaultWorldLimit = worldLimitsSection.getInt("default", 2000);

         for (String key : worldLimitsSection.getKeys(false)) {
            if (!key.equals("default")) {
               this.worldLimits.put(key, worldLimitsSection.getInt(key));
            }
         }
      }

      ConfigurationSection chunkLimits = advancedConfig.getConfigurationSection("chunk_limits");
      if (chunkLimits != null) {
         this.totalPerChunk = chunkLimits.getInt("total_per_chunk", 50);
         this.hostilePerChunk = chunkLimits.getInt("hostile_per_chunk", 15);
         this.passivePerChunk = chunkLimits.getInt("passive_per_chunk", 20);
         this.itemPerChunk = chunkLimits.getInt("item_per_chunk", 30);
      }

      this.overflowAction = advancedConfig.getString("overflow_action", "prevent_spawn");
      this.plugin
         .getLogger()
         .info(
            "EntityLimiter loaded with ADVANCED preset: Total="
               + this.totalPerChunk
               + ", Hostile="
               + this.hostilePerChunk
               + ", Passive="
               + this.passivePerChunk
         );
   }

   private void loadCustomPreset(ConfigurationSection config) {
      ConfigurationSection customConfig = config.getConfigurationSection("custom_config");
      if (customConfig == null) {
         this.plugin.getLogger().warning("Custom preset selected but no custom_config found, falling back to advanced preset");
         this.loadAdvancedPreset(config);
      } else {
         this.worldLimits = new HashMap<>();
         ConfigurationSection worldLimitsSection = customConfig.getConfigurationSection("world_limits");
         if (worldLimitsSection != null) {
            this.defaultWorldLimit = worldLimitsSection.getInt("default", 3000);

            for (String key : worldLimitsSection.getKeys(false)) {
               if (!key.equals("default")) {
                  this.worldLimits.put(key, worldLimitsSection.getInt(key));
               }
            }
         }

         ConfigurationSection chunkLimits = customConfig.getConfigurationSection("chunk_limits");
         if (chunkLimits != null) {
            this.totalPerChunk = chunkLimits.getInt("total_per_chunk", 75);
            this.hostilePerChunk = chunkLimits.getInt("hostile_per_chunk", 25);
            this.passivePerChunk = chunkLimits.getInt("passive_per_chunk", 30);
            this.itemPerChunk = chunkLimits.getInt("item_per_chunk", 40);
         }

         this.overflowAction = customConfig.getString("overflow_action", "remove_random");
         this.plugin
            .getLogger()
            .info(
               "EntityLimiter loaded with CUSTOM preset: Total="
                  + this.totalPerChunk
                  + ", Hostile="
                  + this.hostilePerChunk
                  + ", Passive="
                  + this.passivePerChunk
            );
      }
   }

   @EventHandler
   public void onEntitySpawn(EntitySpawnEvent event) {
      if (this.enabled) {
         Entity entity = event.getEntity();
         if (entity instanceof Item item && item.getTicksLived() < 5) {
            return;
         }

         Chunk chunk = entity.getLocation().getChunk();
         World world = entity.getWorld();
         if (!this.isWithinWorldLimit(world)) {
            event.setCancelled(true);
         } else {
            if (!this.isWithinChunkLimit(chunk, entity) && "prevent_spawn".equals(this.overflowAction)) {
               event.setCancelled(true);
            }
         }
      }
   }

   private boolean isWithinWorldLimit(World world) {
      int worldLimit = this.worldLimits.getOrDefault(world.getName(), this.defaultWorldLimit);
      return worldLimit <= 0 ? true : world.getEntityCount() < worldLimit;
   }

   private boolean isWithinChunkLimit(Chunk chunk, Entity newEntity) {
      Entity[] entities = chunk.getEntities();
      int totalCount = 0;

      for (Entity entity : entities) {
         if (!(entity instanceof ItemFrame) && !(entity instanceof Player)) {
            totalCount++;
         }
      }

      if (totalCount >= this.totalPerChunk) {
         return false;
      } else if (this.hostilePerChunk == this.passivePerChunk && this.passivePerChunk == this.itemPerChunk && this.itemPerChunk == this.totalPerChunk) {
         return true;
      } else {
         int hostileCount = 0;
         int passiveCount = 0;
         int itemCount = 0;

         for (Entity entityx : entities) {
            if (!(entityx instanceof ItemFrame) && !(entityx instanceof Player)) {
               if (entityx instanceof Item) {
                  itemCount++;
               } else if (entityx instanceof Monster) {
                  hostileCount++;
               } else if (entityx instanceof LivingEntity) {
                  passiveCount++;
               }
            }
         }

         if (newEntity instanceof Item && itemCount >= this.itemPerChunk) {
            return false;
         } else {
            return newEntity instanceof Monster && hostileCount >= this.hostilePerChunk
               ? false
               : !(newEntity instanceof LivingEntity) || newEntity instanceof Monster || passiveCount < this.passivePerChunk;
         }
      }
   }

   private void checkAndEnforceLimits() {
      for (World world : Bukkit.getWorlds()) {
         for (Chunk chunk : world.getLoadedChunks()) {
            Bukkit.getRegionScheduler().run(this.plugin, world, chunk.getX(), chunk.getZ(), task -> this.enforceChunkLimits(chunk));
         }
      }
   }

   private void enforceChunkLimits(Chunk chunk) {
      Entity[] entities = chunk.getEntities();
      List<Entity> candidatesAll = new ArrayList<>();
      List<Entity> candidatesItems = new ArrayList<>();
      List<Entity> candidatesHostile = new ArrayList<>();
      List<Entity> candidatesPassive = new ArrayList<>();

      for (Entity e : entities) {
         if (e != null && !(e instanceof Player) && !(e instanceof ItemFrame)) {
            candidatesAll.add(e);
            if (e instanceof Item) {
               candidatesItems.add(e);
            } else if (e instanceof Monster) {
               candidatesHostile.add(e);
            } else if (e instanceof LivingEntity) {
               candidatesPassive.add(e);
            }
         }
      }

      int totalCount = candidatesAll.size();
      int hostileCount = candidatesHostile.size();
      int passiveCount = candidatesPassive.size();
      int itemCount = candidatesItems.size();
      if (totalCount > this.totalPerChunk) {
         int targetTotal = Math.max(0, this.totalPerChunk - this.chunkBuffer);
         int targetHostile = Math.max(0, this.hostilePerChunk - this.chunkBuffer);
         int targetPassive = Math.max(0, this.passivePerChunk - this.chunkBuffer);
         int targetItem = Math.max(0, this.itemPerChunk - this.chunkBuffer);
         int overItem = Math.max(0, itemCount - targetItem);
         int overHostile = Math.max(0, hostileCount - targetHostile);
         int overPassive = Math.max(0, passiveCount - targetPassive);
         if (overItem > 0) {
            this.removeFromList(candidatesItems, overItem);
         }

         if (overHostile > 0) {
            this.removeFromList(candidatesHostile, overHostile);
         }

         if (overPassive > 0) {
            this.removeFromList(candidatesPassive, overPassive);
         }

         totalCount = 0;

         for (Entity ex : chunk.getEntities()) {
            if (ex != null && !(ex instanceof Player) && !(ex instanceof ItemFrame)) {
               totalCount++;
            }
         }

         if (totalCount > targetTotal) {
            int stillToRemove = totalCount - targetTotal;
            List<Entity> current = new ArrayList<>();

            for (Entity exx : chunk.getEntities()) {
               if (exx != null && !(exx instanceof Player) && !(exx instanceof ItemFrame)) {
                  current.add(exx);
               }
            }

            this.removeFromList(current, stillToRemove);
         }
      }
   }

   private void removeOldestEntities(List<Entity> list, int toRemove) {
      List<EntityLimiter.EntityData> entityDataList = new ArrayList<>();

      for (Entity entity : list) {
         if (entity != null && !(entity instanceof Player) && !(entity instanceof ItemFrame) && this.canRemoveEntity(entity)) {
            long ticks;
            try {
               ticks = (long)entity.getTicksLived();
            } catch (Exception var10) {
               ticks = System.currentTimeMillis();
            }

            entityDataList.add(new EntityLimiter.EntityData(entity, ticks));
         }
      }

      entityDataList.sort((a, b) -> Long.compare(a.ticksLived, b.ticksLived));
      int removed = 0;

      for (EntityLimiter.EntityData entityData : entityDataList) {
         if (removed >= toRemove) {
            break;
         }

         try {
            entityData.entity.remove();
            removed++;
         } catch (Exception var9) {
         }
      }
   }

   private boolean canRemoveEntity(Entity entity) {
      TownyIntegration towny = HBZCleaner.getTownyIntegration();
      return towny == null || !towny.isEntityProtected(entity);
   }

   private void removeRandomEntities(List<Entity> list, int toRemove) {
      List<Entity> shuffled = new ArrayList<>();

      for (Entity e : list) {
         if (e != null && !(e instanceof Player) && !(e instanceof ItemFrame) && this.canRemoveEntity(e)) {
            shuffled.add(e);
         }
      }

      Collections.shuffle(shuffled, (Random)ThreadLocalRandom.current());
      int removed = 0;

      for (Entity ex : shuffled) {
         if (removed >= toRemove) {
            break;
         }

         try {
            ex.remove();
            removed++;
         } catch (Exception var8) {
         }
      }
   }

   private void removeFromList(List<Entity> list, int toRemove) {
      if (toRemove > 0 && !list.isEmpty()) {
         String var3 = this.overflowAction;
         switch (var3) {
            case "remove_oldest":
               this.removeOldestEntities(list, toRemove);
               break;
            case "remove_random":
            default:
               this.removeRandomEntities(list, toRemove);
         }
      }
   }

   public void reload() {
      this.loadConfig();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public String getStatus() {
      if (!this.enabled) {
         return "§cDisabled";
      } else {
         StringBuilder status = new StringBuilder("§aEnabled\n");
         status.append("§7World Limits: §e").append(this.worldLimits.size()).append(" configured\n");
         status.append("§7Chunk Limit: §e").append(this.totalPerChunk).append(" entities\n");
         status.append("§7Overflow Action: §e").append(this.overflowAction).append("\n");
         status.append("§7Buffers: §eWorld(-").append(this.worldBuffer).append(") Chunk(-").append(this.chunkBuffer).append(")");
         return status.toString();
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
}
