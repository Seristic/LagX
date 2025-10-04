package com.seristic.lagx.util;

import com.seristic.lagx.main.LagX;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EntityStacker implements Listener {
   private final LagX plugin;
   private boolean enabled;
   private boolean debugMode;
   private double stackingRange;
   private int maxItemStackSize;
   private int maxMobStackSize;
   private int maxSpawnerStackSize;
   private int maxStacksPerChunk;
   private String displayFormat;
   private boolean singleKill;
   private boolean preventUpwardStacking;
   private final Map<UUID, Long> recentDeaths = new ConcurrentHashMap<>();
   private final Set<EntityType> stackableEntities = new HashSet<>();
   private final Set<Material> stackableItems = new HashSet<>();
   private final NamespacedKey stackSizeKey;
   private final NamespacedKey spawnTimeKey;
   private final NamespacedKey masterKey;

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isSingleKillEnabled() {
      return this.singleKill;
   }

   public void reloadConfig() {
      boolean wasEnabled = this.enabled;
      this.stackableEntities.clear();
      this.stackableItems.clear();
      this.plugin.reloadConfig();
      this.loadConfig();
      if (!wasEnabled && this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, this.plugin);
         Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task -> this.attemptStackAllEntities(), 5L, 5L,
               TimeUnit.SECONDS);
         if (this.debugMode) {
            this.plugin
                  .getLogger()
                  .info(
                        "Entity Stacker enabled - Max stack sizes: Items="
                              + this.maxItemStackSize
                              + ", Mobs="
                              + this.maxMobStackSize
                              + ", Spawners="
                              + this.maxSpawnerStackSize);
         }
      } else if (wasEnabled && !this.enabled) {
         if (this.debugMode) {
            this.plugin.getLogger().info("Entity Stacker disabled - restart required for full effect");
         }
      } else if (this.enabled && this.debugMode) {
         this.plugin
               .getLogger()
               .info(
                     "Entity Stacker reloaded - Max stack sizes: Items="
                           + this.maxItemStackSize
                           + ", Mobs="
                           + this.maxMobStackSize
                           + ", Spawners="
                           + this.maxSpawnerStackSize);
      }
   }

   public EntityStacker(LagX plugin) {
      this.plugin = plugin;
      this.stackSizeKey = new NamespacedKey(plugin, "stack_size");
      this.spawnTimeKey = new NamespacedKey(plugin, "spawn_time");
      this.masterKey = new NamespacedKey(plugin, "stack_master");
      this.loadConfig();
      if (this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, plugin);
         Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> this.attemptStackAllEntities(), 5L, 5L,
               TimeUnit.SECONDS);
         if (this.debugMode) {
            plugin.getLogger()
                  .info(
                        "Entity Stacker enabled - Max stack sizes: Items="
                              + this.maxItemStackSize
                              + ", Mobs="
                              + this.maxMobStackSize
                              + ", Spawners="
                              + this.maxSpawnerStackSize);
         }
      }
   }

   private void loadConfig() {
      ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("stacker");
      if (config == null) {
         this.enabled = false;
         if (this.debugMode) {
            this.plugin.getLogger().info("EntityStacker: No stacker config section found, disabled");
         }
      } else {
         this.enabled = config.getBoolean("enabled", true);
         this.debugMode = config.getBoolean("debug", false);
         this.stackingRange = config.getDouble("stacking_range", 5.0);
         this.maxItemStackSize = config.getInt("max_stack_size.items", 128);
         this.maxMobStackSize = config.getInt("max_stack_size.mobs", 32);
         this.maxSpawnerStackSize = config.getInt("max_stack_size.spawners", 5);
         this.maxStacksPerChunk = config.getInt("max_stacks_per_chunk", 4);
         this.displayFormat = config.getString("display_format", "<white>[x%amount%] <reset>");
         this.preventUpwardStacking = config.getBoolean("prevent_upward_stacking", true);
         ConfigurationSection killConfig = config.getConfigurationSection("kill_behavior");
         if (killConfig != null) {
            this.singleKill = killConfig.getBoolean("single_kill", true);
         } else {
            this.singleKill = true;
         }

         List<String> entityList = config.getStringList("stackable_entities");
         if (this.debugMode) {
            this.plugin.getLogger()
                  .info("EntityStacker: Loading " + entityList.size() + " stackable entity types: " + entityList);
         }

         for (String entityName : entityList) {
            try {
               EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
               this.stackableEntities.add(entityType);
               if (this.debugMode) {
                  this.plugin.getLogger().info("EntityStacker: Added stackable entity: " + entityType);
               }
            } catch (IllegalArgumentException var9) {
               if (this.debugMode) {
                  this.plugin.getLogger().warning("Invalid entity type in stacker config: " + entityName);
               }
            }
         }

         for (String itemName : config.getStringList("stackable_items")) {
            try {
               Material material = Material.valueOf(itemName.toUpperCase());
               this.stackableItems.add(material);
            } catch (IllegalArgumentException var8) {
               if (this.debugMode) {
                  this.plugin.getLogger().warning("Invalid material type in stacker config: " + itemName);
               }
            }
         }
      }
   }

   private void attemptStackAllEntities() {
      if (this.enabled) {
         for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
               Bukkit.getRegionScheduler().run(this.plugin, world, chunk.getX(), chunk.getZ(), task -> {
                  Map<EntityType, List<Entity>> entityGroups = new HashMap<>();

                  for (Entity entity : chunk.getEntities()) {
                     if (this.isStackable(entity)) {
                        entityGroups.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
                     }
                  }

                  for (List<Entity> entities : entityGroups.values()) {
                     if (entities.size() >= 2) {
                        for (int i = 0; i < entities.size(); i++) {
                           Entity entity1 = entities.get(i);
                           if (!entity1.isDead()) {
                              for (int j = i + 1; j < entities.size(); j++) {
                                 Entity entity2 = entities.get(j);
                                 if (!entity2.isDead()
                                       && entity1.getLocation().distance(entity2.getLocation()) <= this.stackingRange) {
                                    this.stack(entity1, entity2);
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               });
            }
         }
      }
   }

   public boolean isStackable(Entity entity) {
      if (entity == null || entity.isDead()) {
         return false;
      } else if (entity instanceof Item item) {
         return this.stackableItems.contains(item.getItemStack().getType());
      } else {
         return entity instanceof LivingEntity ? this.stackableEntities.contains(entity.getType()) : false;
      }
   }

   private boolean stack(Entity entity1, Entity entity2) {
      if (this.isStackable(entity1) && this.isStackable(entity2)) {
         if (entity1.getType() != entity2.getType()) {
            return false;
         } else {
            Entity master = null;
            Entity slave = null;
            if (this.isStackMaster(entity1)) {
               master = entity1;
               slave = entity2;
            } else if (this.isStackMaster(entity2)) {
               master = entity2;
               slave = entity1;
            } else {
               long entity1SpawnTime = this.getEntitySpawnTime(entity1);
               long entity2SpawnTime = this.getEntitySpawnTime(entity2);
               if (entity1SpawnTime <= entity2SpawnTime) {
                  master = entity1;
                  slave = entity2;
               } else {
                  master = entity2;
                  slave = entity1;
               }

               this.setStackMaster(master);
            }

            // DIRECTIONAL STACKING CHECK: Only allow stacking downward or horizontally
            // Prevents entities in kill zones from teleporting back up to spawners
            if (this.preventUpwardStacking) {
               double masterY = master.getLocation().getY();
               double slaveY = slave.getLocation().getY();

               // If slave is below master, don't allow stacking (prevents upward
               // teleportation)
               if (slaveY < masterY - 0.5) {
                  if (this.debugMode) {
                     this.plugin.getLogger().info("Blocked upward stacking: " + slave.getType() +
                           " at Y=" + slaveY + " would teleport up to Y=" + masterY);
                  }
                  return false;
               }
            }

            int masterStackSize = this.getStackSize(master);
            int slaveStackSize = this.getStackSize(slave);
            if (master instanceof Item && slave instanceof Item) {
               Item masterItem = (Item) master;
               Item slaveItem = (Item) slave;
               if (masterStackSize <= 0) {
                  masterStackSize = 1;
                  this.setStackSize(master, 1);
               }

               if (slaveStackSize <= 0) {
                  slaveStackSize = 1;
                  this.setStackSize(slave, 1);
               }

               int combinedStackSize = masterStackSize + slaveStackSize;
               if (combinedStackSize > this.maxItemStackSize) {
                  if (this.debugMode) {
                     this.plugin.getLogger().info(
                           "Item stack would exceed max size (" + this.maxItemStackSize + "), using split stacking");
                  }

                  this.setStackSize(master, this.maxItemStackSize);
                  int remainder = Math.min(combinedStackSize - this.maxItemStackSize, this.maxItemStackSize);
                  this.setStackSize(slave, remainder);
                  this.setStackMaster(slave);
                  return true;
               } else {
                  this.setStackSize(master, combinedStackSize);
                  if (this.debugMode) {
                     this.plugin
                           .getLogger()
                           .info(
                                 "Stacked ITEM with "
                                       + slaveStackSize
                                       + " stacks into master with "
                                       + masterStackSize
                                       + " stacks = "
                                       + combinedStackSize
                                       + " total stacks");
                  }

                  try {
                     slave.teleport(master.getLocation());
                  } catch (Exception var13) {
                  }

                  slave.remove();
                  this.updateDisplayName(master, combinedStackSize);
                  return true;
               }
            } else {
               int combinedStackSize = masterStackSize + slaveStackSize;
               int maxStackSize = master instanceof Item ? this.maxItemStackSize : this.maxMobStackSize;
               if (combinedStackSize <= maxStackSize) {
                  this.setStackSize(master, combinedStackSize);

                  try {
                     slave.teleport(master.getLocation());
                  } catch (Exception var14) {
                  }

                  slave.remove();
                  if (this.debugMode) {
                     this.plugin
                           .getLogger()
                           .info("Stacked " + slave.getType() + " (" + slaveStackSize + ") into master stack ("
                                 + masterStackSize + ") = " + combinedStackSize);
                  }

                  return true;
               } else {
                  Chunk chunk = master.getLocation().getChunk();
                  int currentStacks = this.countStacksInChunk(chunk, master.getType());
                  if (currentStacks >= this.maxStacksPerChunk) {
                     if (this.debugMode) {
                        this.plugin.getLogger().info("Cannot stack: chunk at max stacks (" + this.maxStacksPerChunk
                              + ") for " + master.getType());
                     }

                     return false;
                  } else {
                     int remainder = combinedStackSize - maxStackSize;
                     this.setStackSize(master, maxStackSize);
                     this.setStackSize(slave, remainder);

                     try {
                        slave.teleport(master.getLocation());
                     } catch (Exception var15) {
                     }

                     this.setStackMaster(slave);
                     if (this.debugMode) {
                        this.plugin
                              .getLogger()
                              .info(
                                    "Split stacking: filled master "
                                          + master.getType()
                                          + " to max ("
                                          + maxStackSize
                                          + "), remainder "
                                          + remainder
                                          + " stays separate");
                     }

                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private int countStacksInChunk(Chunk chunk, EntityType entityType) {
      int stackCount = 0;

      for (Entity entity : chunk.getEntities()) {
         if (entity.getType() == entityType && this.isStackable(entity)) {
            stackCount++;
         }
      }

      if (this.debugMode) {
         this.plugin.getLogger().info("Found " + stackCount + " actual stacks of " + entityType + " in chunk");
      }

      return stackCount;
   }

   private int getStackSize(Entity entity) {
      if (entity == null) {
         return 0;
      } else {
         try {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            if (container.has(this.stackSizeKey, PersistentDataType.INTEGER)) {
               int size = (Integer) container.get(this.stackSizeKey, PersistentDataType.INTEGER);
               if (size >= 0 && size <= 1000000) {
                  return size;
               }

               if (this.debugMode) {
                  this.plugin.getLogger().warning(
                        "Invalid stack size detected: " + size + " for " + entity.getType() + ", resetting to 1");
               }

               this.setStackSize(entity, 1);
               return 1;
            }
         } catch (Exception var5) {
            if (this.debugMode) {
               this.plugin.getLogger().warning("Error getting stack size: " + var5.getMessage());
            }

            try {
               this.setStackSize(entity, 1);
            } catch (Exception var4) {
            }
         }

         return 1;
      }
   }

   private void setStackSize(Entity entity, int size) {
      if (entity != null) {
         if (size <= 0) {
            size = 1;
         } else if (entity instanceof Item) {
            size = Math.min(size, this.maxItemStackSize);
         } else {
            size = Math.min(size, this.maxMobStackSize);
         }

         try {
            entity.getPersistentDataContainer().set(this.stackSizeKey, PersistentDataType.INTEGER, size);
            this.updateDisplayName(entity, size);
         } catch (Exception var4) {
            if (this.debugMode) {
               this.plugin.getLogger().warning("Error setting stack size: " + var4.getMessage());
            }
         }
      }
   }

   private long getEntitySpawnTime(Entity entity) {
      if (entity == null) {
         return Long.MAX_VALUE;
      } else {
         PersistentDataContainer pdc = entity.getPersistentDataContainer();
         return !pdc.has(this.spawnTimeKey, PersistentDataType.LONG) ? 0L
               : (Long) pdc.get(this.spawnTimeKey, PersistentDataType.LONG);
      }
   }

   private void setEntitySpawnTime(Entity entity, long spawnTime) {
      if (entity != null) {
         entity.getPersistentDataContainer().set(this.spawnTimeKey, PersistentDataType.LONG, spawnTime);
      }
   }

   private void setStackMaster(Entity entity) {
      if (entity != null) {
         entity.getPersistentDataContainer().set(this.masterKey, PersistentDataType.BYTE, (byte) 1);
      }
   }

   private boolean isStackMaster(Entity entity) {
      return entity == null ? false : entity.getPersistentDataContainer().has(this.masterKey, PersistentDataType.BYTE);
   }

   private void updateDisplayName(Entity entity, int size) {
      if (entity instanceof LivingEntity livingEntity) {
         String displayName = entity.getType().toString().toLowerCase().replace("_", " ");
         displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
         if (size > 1) {
            String formatted = this.displayFormat.replace("%amount%", String.valueOf(size)) + displayName;
            String miniMessage = formatted.replace("§0", "<black>")
                  .replace("§1", "<dark_blue>")
                  .replace("§2", "<dark_green>")
                  .replace("§3", "<dark_aqua>")
                  .replace("§4", "<dark_red>")
                  .replace("§5", "<dark_purple>")
                  .replace("§6", "<gold>")
                  .replace("§7", "<gray>")
                  .replace("§8", "<dark_gray>")
                  .replace("§9", "<blue>")
                  .replace("§a", "<green>")
                  .replace("§b", "<aqua>")
                  .replace("§c", "<red>")
                  .replace("§d", "<light_purple>")
                  .replace("§e", "<yellow>")
                  .replace("§f", "<white>")
                  .replace("§l", "<bold>")
                  .replace("§m", "<strikethrough>")
                  .replace("§n", "<underline>")
                  .replace("§o", "<italic>")
                  .replace("§r", "<reset>")
                  .replace("&0", "<black>")
                  .replace("&1", "<dark_blue>")
                  .replace("&2", "<dark_green>")
                  .replace("&3", "<dark_aqua>")
                  .replace("&4", "<dark_red>")
                  .replace("&5", "<dark_purple>")
                  .replace("&6", "<gold>")
                  .replace("&7", "<gray>")
                  .replace("&8", "<dark_gray>")
                  .replace("&9", "<blue>")
                  .replace("&a", "<green>")
                  .replace("&b", "<aqua>")
                  .replace("&c", "<red>")
                  .replace("&d", "<light_purple>")
                  .replace("&e", "<yellow>")
                  .replace("&f", "<white>")
                  .replace("&l", "<bold>")
                  .replace("&m", "<strikethrough>")
                  .replace("&n", "<underline>")
                  .replace("&o", "<italic>")
                  .replace("&r", "<reset>");
            Component nameComponent = MiniMessage.miniMessage().deserialize(miniMessage);
            entity.customName(nameComponent);
            livingEntity.setCustomNameVisible(true);
         } else {
            entity.customName(null);
            livingEntity.setCustomNameVisible(false);
         }
      } else if (entity instanceof Item item) {
         if (size > 1) {
            int totalItems = size * item.getItemStack().getAmount();
            String itemName = item.getItemStack().getType().toString().toLowerCase().replace("_", " ");
            itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
            String formatted = this.displayFormat.replace("%amount%", String.valueOf(totalItems)) + " " + itemName;
            String miniMessage = formatted.replace("§0", "<black>")
                  .replace("§1", "<dark_blue>")
                  .replace("§2", "<dark_green>")
                  .replace("§3", "<dark_aqua>")
                  .replace("§4", "<dark_red>")
                  .replace("§5", "<dark_purple>")
                  .replace("§6", "<gold>")
                  .replace("§7", "<gray>")
                  .replace("§8", "<dark_gray>")
                  .replace("§9", "<blue>")
                  .replace("§a", "<green>")
                  .replace("§b", "<aqua>")
                  .replace("§c", "<red>")
                  .replace("§d", "<light_purple>")
                  .replace("§e", "<yellow>")
                  .replace("§f", "<white>")
                  .replace("§l", "<bold>")
                  .replace("§m", "<strikethrough>")
                  .replace("§n", "<underline>")
                  .replace("§o", "<italic>")
                  .replace("§r", "<reset>")
                  .replace("&0", "<black>")
                  .replace("&1", "<dark_blue>")
                  .replace("&2", "<dark_green>")
                  .replace("&3", "<dark_aqua>")
                  .replace("&4", "<dark_red>")
                  .replace("&5", "<dark_purple>")
                  .replace("&6", "<gold>")
                  .replace("&7", "<gray>")
                  .replace("&8", "<dark_gray>")
                  .replace("&9", "<blue>")
                  .replace("&a", "<green>")
                  .replace("&b", "<aqua>")
                  .replace("&c", "<red>")
                  .replace("&d", "<light_purple>")
                  .replace("&e", "<yellow>")
                  .replace("&f", "<white>")
                  .replace("&l", "<bold>")
                  .replace("&m", "<strikethrough>")
                  .replace("&n", "<underline>")
                  .replace("&o", "<italic>")
                  .replace("&r", "<reset>");
            Component nameComponent = MiniMessage.miniMessage().deserialize(miniMessage);
            item.customName(nameComponent);
            item.setCustomNameVisible(true);
         } else {
            item.customName(null);
            item.setCustomNameVisible(false);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onItemSpawn(ItemSpawnEvent event) {
      if (this.enabled) {
         Item newItem = event.getEntity();
         if (this.stackableItems.contains(newItem.getItemStack().getType())) {
            this.setStackSize(newItem, 1);
            this.setEntitySpawnTime(newItem, System.currentTimeMillis());
            if (this.debugMode) {
               this.plugin
                     .getLogger()
                     .info("New item spawned: " + newItem.getItemStack().getType() + " x"
                           + newItem.getItemStack().getAmount() + ", initialized stack size to 1");
            }

            Location location = newItem.getLocation();
            Bukkit.getRegionScheduler()
                  .runDelayed(
                        this.plugin,
                        location.getWorld(),
                        location.getBlockX() >> 4,
                        location.getBlockZ() >> 4,
                        task -> {
                           if (newItem.isValid()) {
                              Item targetStack = null;
                              long oldestSpawnTime = Long.MAX_VALUE;

                              for (Entity entity : newItem.getNearbyEntities(this.stackingRange, this.stackingRange,
                                    this.stackingRange)) {
                                 if (entity instanceof Item && entity != newItem && !entity.isDead()) {
                                    Item otherItem = (Item) entity;
                                    if (otherItem.getItemStack().isSimilar(newItem.getItemStack())) {
                                       int otherStackSize = this.getStackSize(otherItem);
                                       if (otherStackSize < this.maxItemStackSize) {
                                          long otherSpawnTime = this.getEntitySpawnTime(otherItem);
                                          if (otherSpawnTime < oldestSpawnTime) {
                                             targetStack = otherItem;
                                             oldestSpawnTime = otherSpawnTime;
                                          }
                                       }
                                    }
                                 }
                              }

                              if (targetStack != null && this.stack(targetStack, newItem) && this.debugMode) {
                                 this.plugin
                                       .getLogger()
                                       .info(() -> "EntityStacker: Successfully stacked new item "
                                             + newItem.getItemStack().getType() + " into oldest existing stack");
                              }
                           }
                        },
                        5L);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onEntitySpawn(CreatureSpawnEvent event) {
      if (this.enabled) {
         Entity entity = event.getEntity();
         if (this.debugMode) {
            this.plugin
                  .getLogger()
                  .info(() -> "EntityStacker: Entity spawn event - " + entity.getType() + ", stackable: "
                        + this.stackableEntities.contains(entity.getType()));
         }

         if (this.stackableEntities.contains(entity.getType())) {
            this.setStackSize(entity, 1);
            this.setEntitySpawnTime(entity, System.currentTimeMillis());
            if (this.debugMode) {
               this.plugin.getLogger().info(() -> "EntityStacker: Set initial stack size for " + entity.getType()
                     + " at " + entity.getLocation());
            }

            Location location = entity.getLocation();
            Bukkit.getRegionScheduler()
                  .runDelayed(
                        this.plugin,
                        location.getWorld(),
                        location.getBlockX() >> 4,
                        location.getBlockZ() >> 4,
                        task -> {
                           if (entity.isValid()) {
                              if (this.debugMode) {
                                 this.plugin
                                       .getLogger()
                                       .info(
                                             () -> "EntityStacker: Attempting to stack "
                                                   + entity.getType()
                                                   + " (spawn reason: "
                                                   + event.getSpawnReason()
                                                   + ") with nearby entities");
                              }

                              Entity targetStack = null;
                              long oldestSpawnTime = Long.MAX_VALUE;

                              for (Entity nearby : entity.getNearbyEntities(this.stackingRange, this.stackingRange,
                                    this.stackingRange)) {
                                 if (nearby.getType() == entity.getType() && nearby != entity && !nearby.isDead()) {
                                    int nearbyStackSize = this.getStackSize(nearby);
                                    int maxStackSize = nearby instanceof Item ? this.maxItemStackSize
                                          : this.maxMobStackSize;
                                    if (nearbyStackSize < maxStackSize) {
                                       long nearbySpawnTime = this.getEntitySpawnTime(nearby);
                                       boolean nearbyIsMaster = this.isStackMaster(nearby);
                                       boolean targetIsMaster = targetStack != null ? this.isStackMaster(targetStack)
                                             : false;
                                       boolean shouldReplace = false;
                                       if (nearbyIsMaster && !targetIsMaster) {
                                          shouldReplace = true;
                                       } else if (nearbyIsMaster == targetIsMaster) {
                                          shouldReplace = nearbySpawnTime < oldestSpawnTime;
                                       }

                                       if (shouldReplace) {
                                          targetStack = nearby;
                                          oldestSpawnTime = nearbySpawnTime;
                                       }
                                    }
                                 }
                              }

                              if (targetStack != null) {
                                 if (this.stack(targetStack, entity)) {
                                    if (this.debugMode) {
                                       this.plugin.getLogger().info(() -> "EntityStacker: Successfully stacked new "
                                             + entity.getType() + " into existing stack");
                                    }
                                 } else {
                                    Chunk chunk = entity.getLocation().getChunk();
                                    int currentStacks = this.countStacksInChunk(chunk, entity.getType());
                                    if (currentStacks >= this.maxStacksPerChunk) {
                                       if (this.debugMode) {
                                          this.plugin
                                                .getLogger()
                                                .info(() -> "EntityStacker: Removing " + entity.getType()
                                                      + " - chunk at max stacks (" + this.maxStacksPerChunk + ")");
                                       }

                                       entity.remove();
                                    } else if (this.debugMode) {
                                       this.plugin
                                             .getLogger()
                                             .info(
                                                   () -> "EntityStacker: Keeping "
                                                         + entity.getType()
                                                         + " as new stack ("
                                                         + currentStacks
                                                         + "/"
                                                         + this.maxStacksPerChunk
                                                         + " stacks in chunk)");
                                    }
                                 }
                              } else {
                                 Chunk chunk = entity.getLocation().getChunk();
                                 int currentStacks = this.countStacksInChunk(chunk, entity.getType());
                                 if (currentStacks > this.maxStacksPerChunk) {
                                    if (this.debugMode) {
                                       this.plugin
                                             .getLogger()
                                             .info(() -> "EntityStacker: Removing " + entity.getType()
                                                   + " - chunk at max stacks (" + this.maxStacksPerChunk + ")");
                                    }

                                    entity.remove();
                                 } else if (this.debugMode) {
                                    this.plugin
                                          .getLogger()
                                          .info(() -> "EntityStacker: No nearby entities to stack with for "
                                                + entity.getType() + " - keeping as new stack");
                                 }
                              }
                           }
                        },
                        10L);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityDeath(EntityDeathEvent event) {
      if (this.enabled) {
         LivingEntity entity = event.getEntity();
         int stackSize = this.getStackSize(entity);
         long now = System.currentTimeMillis();
         Long last = this.recentDeaths.get(entity.getUniqueId());
         if (last != null && now - last < 250L) {
            if (this.debugMode) {
               this.plugin.getLogger()
                     .info(() -> "EntityStacker: Skipping duplicate death event for " + entity.getType());
            }
         } else {
            this.recentDeaths.put(entity.getUniqueId(), now);
            if (this.debugMode) {
               this.plugin
                     .getLogger()
                     .info(
                           () -> "EntityStacker: Death event for master " + entity.getType() + " with stack size "
                                 + stackSize + ", single kill: " + this.singleKill);
            }

            if (stackSize > 1) {
               if (this.singleKill) {
                  // Single kill mode: reduce stack by 1, keep the entity alive
                  int newStackSize = stackSize - 1;

                  // Cancel the death event to prevent the entity from dying
                  event.setCancelled(true);

                  // Update the stack size immediately
                  this.setStackSize(entity, newStackSize);
                  this.updateDisplayName(entity, newStackSize);

                  // Reset entity health to full
                  entity.setHealth(entity.getMaxHealth());
                  entity.setNoDamageTicks(10);
                  entity.setFireTicks(0);

                  // Drop items as if one entity died
                  List<ItemStack> drops = event.getDrops();
                  for (ItemStack drop : drops) {
                     entity.getWorld().dropItemNaturally(entity.getLocation(), drop.clone());
                  }

                  // Grant XP as if one entity died
                  int xpToDrop = event.getDroppedExp();
                  if (xpToDrop > 0 && entity.getKiller() != null) {
                     entity.getWorld().spawn(entity.getLocation(), org.bukkit.entity.ExperienceOrb.class, orb -> {
                        orb.setExperience(xpToDrop);
                     });
                  }

                  if (this.debugMode) {
                     this.plugin.getLogger().info(() -> "EntityStacker: Single kill - reduced " + entity.getType()
                           + " stack from " + stackSize + " to " + newStackSize);
                  }
                  return;
               }

               List<ItemStack> drops = event.getDrops();
               List<ItemStack> originalDrops = new ArrayList<>(drops);
               drops.clear();

               for (int i = 0; i < stackSize - 1; i++) {
                  for (ItemStack drop : originalDrops) {
                     ItemStack dropCopy = drop.clone();
                     entity.getWorld().dropItemNaturally(entity.getLocation(), dropCopy);
                  }
               }

               drops.addAll(originalDrops);
               event.setDroppedExp(event.getDroppedExp() * stackSize);
               if (this.debugMode) {
                  this.plugin
                        .getLogger()
                        .info(() -> "EntityStacker: Full stack kill - killed entire stack of " + stackSize + " "
                              + entity.getType() + " entities");
               }
            }
         }
      }
   }

   public int getStackedItemsCount() {
      int count = 0;

      for (World world : Bukkit.getWorlds()) {
         for (Entity entity : world.getEntities()) {
            if (entity instanceof Item && this.getStackSize(entity) > 1) {
               count++;
            }
         }
      }

      return count;
   }

   public int getStackedEntitiesCount() {
      int count = 0;

      for (World world : Bukkit.getWorlds()) {
         for (Entity entity : world.getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && this.getStackSize(entity) > 1) {
               count++;
            }
         }
      }

      return count;
   }

   public int stackEntitiesInRadius(Location location, int radius) {
      if (!this.enabled) {
         return 0;
      } else {
         int[] stacksCreated = new int[] { 0 };
         World world = location.getWorld();
         Collection<Entity> nearbyEntities = world.getNearbyEntities(location, (double) radius, (double) radius,
               (double) radius);
         Map<EntityType, List<Entity>> entityGroups = new HashMap<>();

         for (Entity entity : nearbyEntities) {
            if (this.isStackable(entity)) {
               entityGroups.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
            }
         }

         for (List<Entity> entities : entityGroups.values()) {
            if (entities.size() >= 2) {
               entities.sort((e1, e2) -> Integer.compare(this.getStackSize(e2), this.getStackSize(e1)));
               Entity baseEntity = entities.get(0);

               for (int i = 1; i < entities.size(); i++) {
                  if (this.stack(baseEntity, entities.get(i))) {
                     stacksCreated[0]++;
                  }
               }
            }
         }

         return stacksCreated[0];
      }
   }

   public int getMaxStacksPerChunk() {
      return this.maxStacksPerChunk;
   }

   public String getDebugInfo() {
      StringBuilder sb = new StringBuilder();
      sb.append("§6Entity Stacker Debug Information:\n");
      sb.append("§eEnabled: ").append(this.enabled).append("\n");
      sb.append("§eStacking Range: ").append(this.stackingRange).append(" blocks\n");
      sb.append("§eStackable Entities (").append(this.stackableEntities.size()).append("): ");
      if (this.stackableEntities.isEmpty()) {
         sb.append("§cNONE - This is why nothing stacks!");
      } else {
         sb.append("§a").append(this.stackableEntities.toString());
      }

      sb.append("\n§eStackable Items (").append(this.stackableItems.size()).append("): ");
      if (this.stackableItems.isEmpty()) {
         sb.append("§7None");
      } else {
         sb.append("§a").append(this.stackableItems.toString());
      }

      sb.append("\n§eMax Stack Sizes: Items=")
            .append(this.maxItemStackSize)
            .append(", Mobs=")
            .append(this.maxMobStackSize)
            .append(", Spawners=")
            .append(this.maxSpawnerStackSize);
      return sb.toString();
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      if (this.enabled) {
         Item item = event.getItem();
         int stackSize = this.getStackSize(item);
         if (stackSize <= 0) {
            stackSize = 1;
         }

         if (stackSize > 1) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            ItemStack pickedItem = item.getItemStack().clone();
            int itemAmount = pickedItem.getAmount();
            int totalItems = stackSize * itemAmount;
            int maxSingleStackSize = pickedItem.getMaxStackSize();
            int fullStacks = totalItems / maxSingleStackSize;
            int remainder = totalItems % maxSingleStackSize;
            if (this.debugMode) {
               this.plugin
                     .getLogger()
                     .info(
                           "Player "
                                 + player.getName()
                                 + " picking up stacked item: "
                                 + stackSize
                                 + " entities with "
                                 + itemAmount
                                 + " items each = "
                                 + totalItems
                                 + " total items ("
                                 + fullStacks
                                 + " full stacks + "
                                 + remainder
                                 + " remainder)");
            }

            for (int i = 0; i < fullStacks; i++) {
               ItemStack stack = pickedItem.clone();
               stack.setAmount(maxSingleStackSize);
               HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[] { stack });
               if (!leftover.isEmpty()) {
                  for (ItemStack left : leftover.values()) {
                     Item droppedItem = player.getWorld().dropItem(player.getLocation(), left);
                     this.setStackSize(droppedItem, 1);
                     droppedItem.setPickupDelay(0);
                  }
               }
            }

            if (remainder > 0) {
               ItemStack stack = pickedItem.clone();
               stack.setAmount(remainder);
               HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[] { stack });
               if (!leftover.isEmpty()) {
                  for (ItemStack left : leftover.values()) {
                     Item droppedItem = player.getWorld().dropItem(player.getLocation(), left);
                     this.setStackSize(droppedItem, 1);
                     droppedItem.setPickupDelay(0);
                  }
               }
            }

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, 1.0F);
            item.remove();
         }
      }
   }

   public void enable() {
      this.enabled = true;
   }

   public void disable() {
      this.enabled = false;
   }

   public double getStackRadius() {
      return this.stackingRange;
   }

   public int getMaxStackSize() {
      return Math.max(this.maxItemStackSize, this.maxMobStackSize);
   }

   public boolean isAutoStackEnabled() {
      return this.enabled;
   }

   public int stackEntitiesInWorld(World world) {
      if (!this.enabled || world == null) {
         return 0;
      }

      int totalStacked = 0;
      for (Chunk chunk : world.getLoadedChunks()) {
         totalStacked += stackEntitiesInChunk(chunk);
      }
      return totalStacked;
   }

   public int unstackEntitiesInWorld(World world) {
      if (!this.enabled || world == null) {
         return 0;
      }

      int totalUnstacked = 0;
      for (Chunk chunk : world.getLoadedChunks()) {
         for (Entity entity : chunk.getEntities()) {
            if (isStackable(entity) && getStackSize(entity) > 1) {
               int stackSize = getStackSize(entity);
               unstackEntity(entity);
               totalUnstacked += stackSize;
            }
         }
      }
      return totalUnstacked;
   }

   public int getTotalStacks() {
      int totalStacks = 0;
      for (World world : Bukkit.getWorlds()) {
         for (Chunk chunk : world.getLoadedChunks()) {
            for (Entity entity : chunk.getEntities()) {
               if (isStackable(entity) && getStackSize(entity) > 1) {
                  totalStacks++;
               }
            }
         }
      }
      return totalStacks;
   }

   private int stackEntitiesInChunk(Chunk chunk) {
      int stacked = 0;
      for (Entity entity : chunk.getEntities()) {
         if (isStackable(entity)) {
            stacked += tryStackEntity(entity);
         }
      }
      return stacked;
   }

   private int tryStackEntity(Entity entity) {
      if (!isStackable(entity)) {
         return 0;
      }

      Collection<Entity> nearby = entity.getLocation().getWorld()
            .getNearbyEntities(entity.getLocation(), stackingRange, stackingRange, stackingRange);

      for (Entity nearbyEntity : nearby) {
         if (nearbyEntity != entity && canStackTogether(entity, nearbyEntity)) {
            int entityStack = getStackSize(entity);
            int nearbyStack = getStackSize(nearbyEntity);
            int maxStack = entity instanceof Item ? maxItemStackSize : maxMobStackSize;

            if (entityStack + nearbyStack <= maxStack) {
               setStackSize(entity, entityStack + nearbyStack);
               nearbyEntity.remove();
               return nearbyStack;
            }
         }
      }
      return 0;
   }

   private void unstackEntity(Entity entity) {
      if (!isStackable(entity)) {
         return;
      }

      int stackSize = getStackSize(entity);
      if (stackSize <= 1) {
         return;
      }

      // Create individual entities for each stacked entity
      Location loc = entity.getLocation();
      for (int i = 1; i < stackSize; i++) {
         Entity newEntity = loc.getWorld().spawnEntity(loc, entity.getType());
         setStackSize(newEntity, 1);
      }

      setStackSize(entity, 1);
   }

   private boolean canStackTogether(Entity entity1, Entity entity2) {
      if (!isStackable(entity1) || !isStackable(entity2)) {
         return false;
      }

      if (entity1.getType() != entity2.getType()) {
         return false;
      }

      // For items, check if they are similar
      if (entity1 instanceof Item && entity2 instanceof Item) {
         Item item1 = (Item) entity1;
         Item item2 = (Item) entity2;
         return item1.getItemStack().isSimilar(item2.getItemStack());
      }

      // For living entities, they must be the same type
      return entity1 instanceof LivingEntity && entity2 instanceof LivingEntity;
   }
}
