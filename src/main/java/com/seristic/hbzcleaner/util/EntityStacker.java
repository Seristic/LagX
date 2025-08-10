package com.seristic.hbzcleaner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.seristic.hbzcleaner.main.LaggRemover;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class EntityStacker implements Listener {
    
    private final LaggRemover plugin;
    private boolean enabled;
    private boolean debugMode;
    private double stackingRange;
    private int maxItemStackSize;
    private int maxMobStackSize;
    private int maxSpawnerStackSize;
    private int maxStacksPerChunk;
    private String displayFormat;
    private boolean spawnerEnabled;
    private int spawnerSpawnCount;
    private boolean spawnerAutoStack;
    
    /**
     * Check if the stacker is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Reload configuration
     */
    public void reloadConfig() {
        // Store previous enabled state
        boolean wasEnabled = enabled;
        
        // Clear existing data
        stackableEntities.clear();
        stackableItems.clear();
        
        // Force reload config from disk
        plugin.reloadConfig();
        
        // Reload config
        loadConfig();
        
        // Handle state changes
        if (!wasEnabled && enabled) {
            // Was disabled, now enabled - register events and start tasks
            Bukkit.getPluginManager().registerEvents(this, plugin);
            
            // Start periodic stacking task (runs every 5 seconds)
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
                attemptStackAllEntities();
            }, 5, 5, TimeUnit.SECONDS);
            
            plugin.getLogger().info("Entity Stacker enabled - Max stack sizes: Items=" + 
                maxItemStackSize + ", Mobs=" + maxMobStackSize + ", Spawners=" + maxSpawnerStackSize);
        } else if (wasEnabled && !enabled) {
            // Was enabled, now disabled - note that we can't easily unregister events
            // but the event handlers will check the enabled flag
            plugin.getLogger().info("Entity Stacker disabled - restart required for full effect");
        } else if (enabled) {
            // Still enabled, just reloaded settings
            plugin.getLogger().info("Entity Stacker reloaded - Max stack sizes: Items=" + 
                maxItemStackSize + ", Mobs=" + maxMobStackSize + ", Spawners=" + maxSpawnerStackSize);
        }
    }
    
    private final Set<EntityType> stackableEntities = new HashSet<>();
    private final Set<Material> stackableItems = new HashSet<>();
    
    // NamespacedKeys for persistent data
    private final NamespacedKey stackSizeKey;
    
    public EntityStacker(LaggRemover plugin) {
        this.plugin = plugin;
        this.stackSizeKey = new NamespacedKey(plugin, "stack_size");
        
        loadConfig();
        
        if (enabled) {
            // Register events
            Bukkit.getPluginManager().registerEvents(this, plugin);
            
            // Start periodic stacking task (runs every 5 seconds)
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
                attemptStackAllEntities();
            }, 5, 5, TimeUnit.SECONDS);
            
            plugin.getLogger().info("Entity Stacker enabled - Max stack sizes: Items=" + 
                maxItemStackSize + ", Mobs=" + maxMobStackSize + ", Spawners=" + maxSpawnerStackSize);
        }
    }
    
    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("stacker");
        if (config == null) {
            enabled = false;
            plugin.getLogger().info("EntityStacker: No stacker config section found, disabled");
            return;
        }

        enabled = config.getBoolean("enabled", true);
        debugMode = config.getBoolean("debug", false);
        stackingRange = config.getDouble("stacking_range", 5.0);
        maxItemStackSize = config.getInt("max_stack_size.items", 128);
        maxMobStackSize = config.getInt("max_stack_size.mobs", 32);
        maxSpawnerStackSize = config.getInt("max_stack_size.spawners", 5);
        maxStacksPerChunk = config.getInt("max_stacks_per_chunk", 4);
        displayFormat = config.getString("display_format", "<white>[x%amount%] <reset>");

        // Load spawner settings
        ConfigurationSection spawnerConfig = config.getConfigurationSection("spawner");
        if (spawnerConfig != null) {
            spawnerEnabled = spawnerConfig.getBoolean("enabled", true);
            spawnerSpawnCount = spawnerConfig.getInt("spawn_count", 2);
            spawnerAutoStack = spawnerConfig.getBoolean("auto_stack", true);
        } else {
            spawnerEnabled = true;
            spawnerSpawnCount = 2;
            spawnerAutoStack = true;
        }

        // Load stackable entities
        List<String> entityList = config.getStringList("stackable_entities");
        plugin.getLogger().info("EntityStacker: Loading " + entityList.size() + " stackable entity types: " + entityList);
        for (String entityName : entityList) {
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                stackableEntities.add(entityType);
                plugin.getLogger().info("EntityStacker: Added stackable entity: " + entityType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in stacker config: " + entityName);
            }
        }        // Load stackable items
        List<String> itemList = config.getStringList("stackable_items");
        for (String itemName : itemList) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                stackableItems.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material type in stacker config: " + itemName);
            }
        }
    }
    
    /**
     * Attempt to stack all eligible entities in all worlds
     */
    private void attemptStackAllEntities() {
        if (!enabled) return;
        
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            // Process in chunks to avoid overwhelming the region scheduler
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                Bukkit.getRegionScheduler().run(plugin, world, chunk.getX(), chunk.getZ(), task -> {
                    // Group entities by type within the chunk
                    Map<EntityType, List<Entity>> entityGroups = new HashMap<>();
                    
                    for (Entity entity : chunk.getEntities()) {
                        if (isStackable(entity)) {
                            entityGroups.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
                        }
                    }
                    
                    // Attempt to stack each group
                    for (List<Entity> entities : entityGroups.values()) {
                        if (entities.size() < 2) continue;
                        
                        // Try to stack entities with each other
                        for (int i = 0; i < entities.size(); i++) {
                            Entity entity1 = entities.get(i);
                            if (entity1.isDead()) continue;
                            
                            for (int j = i + 1; j < entities.size(); j++) {
                                Entity entity2 = entities.get(j);
                                if (entity2.isDead()) continue;
                                
                                // If they're close enough, stack them
                                if (entity1.getLocation().distance(entity2.getLocation()) <= stackingRange) {
                                    stack(entity1, entity2);
                                    break; // Once stacked, move to the next entity
                                }
                            }
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Check if an entity is stackable
     */
    public boolean isStackable(Entity entity) {
        if (entity == null || entity.isDead()) return false;
        
        // Check if this is a supported entity type
        if (entity instanceof Item) {
            Item item = (Item) entity;
            return stackableItems.contains(item.getItemStack().getType());
        } else if (entity instanceof LivingEntity) {
            return stackableEntities.contains(entity.getType());
        }
        
        return false;
    }
    
    /**
     * Stack two entities together
     * @return true if stacking was successful
     */
    private boolean stack(Entity entity1, Entity entity2) {
        if (!isStackable(entity1) || !isStackable(entity2)) return false;
        if (entity1.getType() != entity2.getType()) return false;
        
        // Determine which entity will be the base and which will be removed
        Entity baseEntity, stackEntity;
        
        int stack1 = getStackSize(entity1);
        int stack2 = getStackSize(entity2);
        
        // Choose the entity with the larger stack as the base
        if (stack1 >= stack2) {
            baseEntity = entity1;
            stackEntity = entity2;
        } else {
            baseEntity = entity2;
            stackEntity = entity1;
            // Swap the stack sizes too
            int temp = stack1;
            stack1 = stack2;
            stack2 = temp;
        }
        
        // Calculate new stack size and get max stack size
        int combinedStackSize = stack1 + stack2;
        int maxStackSize = (baseEntity instanceof Item) ? maxItemStackSize : maxMobStackSize;
        
        // Check if we can fit the entities into the existing stack
        if (combinedStackSize <= maxStackSize) {
            // Normal stacking - everything fits in one stack
            setStackSize(baseEntity, combinedStackSize);
            
            // If it's an item, update the item count
            if (baseEntity instanceof Item && stackEntity instanceof Item) {
                Item baseItem = (Item) baseEntity;
                Item stackItem = (Item) stackEntity;
                
                ItemStack itemStack = baseItem.getItemStack();
                ItemStack otherStack = stackItem.getItemStack();
                
                if (itemStack.isSimilar(otherStack)) {
                    int amount = Math.min(itemStack.getAmount() + otherStack.getAmount(), itemStack.getMaxStackSize());
                    itemStack.setAmount(amount);
                    baseItem.setItemStack(itemStack);
                }
            }
            
            // Remove the stacked entity
            stackEntity.remove();
            
            if (debugMode) {
                plugin.getLogger().info("Stacked " + stackEntity.getType() + " (" + stack2 + ") into existing stack (" + stack1 + ") = " + combinedStackSize);
            }
            return true;
        } else {
            // The combined size would exceed max stack size
            // Check if we can create a new stack in this chunk
            org.bukkit.Chunk chunk = baseEntity.getLocation().getChunk();
            int currentStacks = countStacksInChunk(chunk, baseEntity.getType());
            
            if (currentStacks >= maxStacksPerChunk) {
                // Cannot create new stack, chunk is at max stacks
                if (debugMode) {
                    plugin.getLogger().info("Cannot stack: chunk at max stacks (" + maxStacksPerChunk + ") for " + baseEntity.getType());
                }
                return false;
            }
            
            // Fill the base stack to max size and create a new stack with remainder
            int remainder = combinedStackSize - maxStackSize;
            setStackSize(baseEntity, maxStackSize);
            setStackSize(stackEntity, remainder);
            
            if (debugMode) {
                plugin.getLogger().info("Split stacking: filled " + baseEntity.getType() + " to max (" + maxStackSize + "), created new stack with " + remainder + " entities");
            }
            return true;
        }
    }
    
    /**
     * Count the number of stacks for a specific entity type in a chunk
     */
    private int countStacksInChunk(org.bukkit.Chunk chunk, EntityType entityType) {
        int stackCount = 0;
        
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == entityType && isStackable(entity)) {
                // Only count entities that have a stack size (are part of stacks)
                if (getStackSize(entity) > 0) {
                    stackCount++;
                }
            }
        }
        
        return stackCount;
    }
    
    /**
     * Get the current stack size of an entity
     */
    private int getStackSize(Entity entity) {
        if (entity == null) return 0;
        
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (container.has(stackSizeKey, PersistentDataType.INTEGER)) {
            return container.get(stackSizeKey, PersistentDataType.INTEGER);
        }
        
        // Default stack size is 1
        return 1;
    }
    
    /**
     * Set the stack size of an entity and update its display name
     */
    private void setStackSize(Entity entity, int size) {
        if (entity == null) return;
        
        // Store the stack size in persistent data
        entity.getPersistentDataContainer().set(stackSizeKey, PersistentDataType.INTEGER, size);
        
        // Update display name for living entities
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            String displayName = entity.getType().toString().toLowerCase().replace("_", " ");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            if (size > 1) {
                // Use Adventure API properly - convert legacy formatting to MiniMessage format first
                String formatted = displayFormat.replace("%amount%", String.valueOf(size)) + displayName;
                // Convert legacy § codes to MiniMessage format (<color:white>, etc.)
                String miniMessage = formatted
                    .replace("§0", "<black>")
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
                    // Also convert & codes
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
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!enabled) return;
        
        Item newItem = event.getEntity();
        if (!stackableItems.contains(newItem.getItemStack().getType())) return;
        
        // Set initial stack size
        setStackSize(newItem, 1);
        
        // Try to stack with nearby items immediately
        Location location = newItem.getLocation();
        Bukkit.getRegionScheduler().runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task -> {
            if (newItem.isValid()) {
                for (Entity entity : newItem.getNearbyEntities(stackingRange, stackingRange, stackingRange)) {
                    if (entity instanceof Item && entity != newItem && !entity.isDead()) {
                        Item otherItem = (Item) entity;
                        
                        if (otherItem.getItemStack().isSimilar(newItem.getItemStack())) {
                            if (stack(newItem, otherItem)) {
                                break; // Successfully stacked
                            }
                        }
                    }
                }
            }
        }, 5L); // Slight delay to let the item settle
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (!enabled) return;

        Entity entity = event.getEntity();
        plugin.getLogger().info("EntityStacker: Entity spawn event - " + entity.getType() + 
            ", stackable: " + stackableEntities.contains(entity.getType()));
        
        if (!stackableEntities.contains(entity.getType())) return;

        // Set initial stack size
        setStackSize(entity, 1);
        plugin.getLogger().info("EntityStacker: Set initial stack size for " + entity.getType() + " at " + entity.getLocation());

        // If this is a natural spawn, try to stack with nearby entities
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER || spawnerAutoStack) {
            // Use Folia's region-based scheduler
            Location location = entity.getLocation();
            Bukkit.getRegionScheduler().runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task -> {
                if (entity.isValid()) {
                    plugin.getLogger().info("EntityStacker: Attempting to stack " + entity.getType() + " with nearby entities");
                    int stacked = 0;
                    for (Entity nearby : entity.getNearbyEntities(stackingRange, stackingRange, stackingRange)) {
                        if (nearby.getType() == entity.getType() && nearby != entity && !nearby.isDead()) {
                            if (stack(entity, nearby)) {
                                stacked++;
                                plugin.getLogger().info("EntityStacker: Successfully stacked " + entity.getType() + " (total stacked: " + stacked + ")");
                                break; // Successfully stacked
                            }
                        }
                    }
                    if (stacked == 0) {
                        plugin.getLogger().info("EntityStacker: No nearby entities to stack with for " + entity.getType());
                    }
                }
            }, 10L); // Slight delay to let the entity settle
        }
    }    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!enabled || !spawnerEnabled) return;
        
        Entity entity = event.getEntity();
        
        // Apply stack size for spawner-spawned entities if they're stackable
        if (stackableEntities.contains(entity.getType())) {
            // Set initial stack size based on spawner settings
            setStackSize(entity, Math.min(spawnerSpawnCount, maxSpawnerStackSize));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!enabled) return;
        
        LivingEntity entity = event.getEntity();
        int stackSize = getStackSize(entity);
        
        // If this is a stacked entity, spawn multiple drops
        if (stackSize > 1) {
            List<ItemStack> drops = event.getDrops();
            List<ItemStack> originalDrops = new ArrayList<>(drops);
            
            // Clear the original drops
            drops.clear();
            
            // Add drops for each entity in the stack (except the first one which is handled normally)
            for (int i = 0; i < stackSize - 1; i++) {
                for (ItemStack drop : originalDrops) {
                    // Create a copy of the drop
                    ItemStack dropCopy = drop.clone();
                    
                    // Add the drop to the world
                    entity.getWorld().dropItemNaturally(entity.getLocation(), dropCopy);
                }
            }
            
            // Add back the original drops for the first entity
            drops.addAll(originalDrops);
            
            // Also give experience for each entity in the stack
            event.setDroppedExp(event.getDroppedExp() * stackSize);
        }
    }
    

    
    /**
     * Get count of stacked items in all worlds
     * @return Number of stacked items
     */
    public int getStackedItemsCount() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item && getStackSize(entity) > 1) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Get count of stacked entities in all worlds
     * @return Number of stacked entities
     */
    public int getStackedEntitiesCount() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity && !(entity instanceof Player) && getStackSize(entity) > 1) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Stack entities within a specific radius around a location
     * @param location The center location
     * @param radius The radius to check
     * @return Number of stacks created
     */
    public int stackEntitiesInRadius(Location location, int radius) {
        if (!enabled) return 0;
        
        final int[] stacksCreated = {0};
        World world = location.getWorld();
        
        // Get all entities in the radius
        Collection<Entity> nearbyEntities = world.getNearbyEntities(location, radius, radius, radius);
        
        // Group them by type
        Map<EntityType, List<Entity>> entityGroups = new HashMap<>();
        for (Entity entity : nearbyEntities) {
            if (isStackable(entity)) {
                entityGroups.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
            }
        }
        
        // Process each group
        for (List<Entity> entities : entityGroups.values()) {
            if (entities.size() < 2) continue;
            
            // Sort entities by stack size (largest first)
            entities.sort((e1, e2) -> Integer.compare(getStackSize(e2), getStackSize(e1)));
            
            // Stack entities from the beginning of the list (merge smaller stacks into larger ones)
            Entity baseEntity = entities.get(0);
            for (int i = 1; i < entities.size(); i++) {
                if (stack(baseEntity, entities.get(i))) {
                    stacksCreated[0]++;
                }
            }
        }
        
        return stacksCreated[0];
    }
    
    /**
     * Get the maximum stacks per chunk setting
     */
    public int getMaxStacksPerChunk() {
        return maxStacksPerChunk;
    }
    
    /**
     * Get debug information about the stacker
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6Entity Stacker Debug Information:\n");
        sb.append("§eEnabled: ").append(enabled).append("\n");
        sb.append("§eStacking Range: ").append(stackingRange).append(" blocks\n");
        sb.append("§eStackable Entities (").append(stackableEntities.size()).append("): ");
        if (stackableEntities.isEmpty()) {
            sb.append("§cNONE - This is why nothing stacks!");
        } else {
            sb.append("§a").append(stackableEntities.toString());
        }
        sb.append("\n§eStackable Items (").append(stackableItems.size()).append("): ");
        if (stackableItems.isEmpty()) {
            sb.append("§7None");
        } else {
            sb.append("§a").append(stackableItems.toString());
        }
        sb.append("\n§eMax Stack Sizes: Items=").append(maxItemStackSize)
          .append(", Mobs=").append(maxMobStackSize)
          .append(", Spawners=").append(maxSpawnerStackSize);
        
        return sb.toString();
    }
}
