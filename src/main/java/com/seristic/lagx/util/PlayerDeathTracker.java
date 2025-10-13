package com.seristic.lagx.util;

import com.seristic.lagx.main.LagX;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Chunk-based player death item protection system
 * Tracks chunks where players died and protects items in those chunks for a
 * configurable duration
 * This prevents items from being cleared by automated cleanup systems
 */
public class PlayerDeathTracker implements Listener {
   private final LagX plugin;

   // Track protected chunks with their expiration times
   private final Map<ChunkKey, ProtectionData> protectedChunks = new ConcurrentHashMap<>();

   // Track player deaths for debugging and statistics
   private final Map<UUID, DeathRecord> recentDeaths = new ConcurrentHashMap<>();

   // Configuration
   private boolean enabled;
   private long protectionDurationMillis;
   private int chunkRadius; // How many chunks around death location to protect
   private boolean protectDroppedItems; // Protect items that land in different chunks

   public PlayerDeathTracker(LagX plugin) {
      this.plugin = plugin;
      this.loadConfig();

      if (this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, plugin);

         // Schedule cleanup task to remove expired protections
         Bukkit.getAsyncScheduler().runAtFixedRate(
               plugin,
               task -> this.cleanupExpiredProtections(),
               30L, // Initial delay
               30L, // Period - check every 30 seconds
               TimeUnit.SECONDS);

         plugin.getLogger().info(
               "Player Death Tracker enabled (Chunk-based) - " +
                     "Protection duration: " + (protectionDurationMillis / 1000L) + " seconds, " +
                     "Chunk radius: " + chunkRadius);
      }
   }

   private void loadConfig() {
      this.enabled = plugin.getConfig().getBoolean("player_death_protection.enabled", true);
      this.protectionDurationMillis = plugin.getConfig().getLong("player_death_protection.duration_seconds", 120L)
            * 1000L;
      this.chunkRadius = plugin.getConfig().getInt("player_death_protection.chunk_radius", 1);
      this.protectDroppedItems = plugin.getConfig().getBoolean("player_death_protection.protect_dropped_items", true);
   }

   public void reloadConfig() {
      boolean wasEnabled = this.enabled;
      plugin.reloadConfig();
      this.loadConfig();

      if (!wasEnabled && this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, plugin);
         plugin.getLogger().info("Player Death Tracker enabled (Chunk-based)");
      } else if (wasEnabled && !this.enabled) {
         plugin.getLogger().info("Player Death Tracker disabled");
         // Clear all protections when disabled
         protectedChunks.clear();
         recentDeaths.clear();
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   /**
    * Handle player death - protect chunks around death location
    * CRITICAL: Uses LOWEST priority to apply protection BEFORE any other plugins
    * This ensures items are protected immediately when they drop
    */
   @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
   public void onPlayerDeath(PlayerDeathEvent event) {
      if (!enabled) {
         return;
      }

      Player player = event.getEntity();
      UUID playerId = player.getUniqueId();
      Location deathLocation = player.getLocation();
      Chunk deathChunk = deathLocation.getChunk();

      long currentTime = System.currentTimeMillis();
      long expirationTime = currentTime + protectionDurationMillis;

      // Track this death
      Set<ChunkKey> protectedChunkKeys = new HashSet<>();
      DeathRecord deathRecord = new DeathRecord(playerId, player.getName(), deathLocation, currentTime, expirationTime);

      // Protect the death chunk and surrounding chunks
      // This happens BEFORE items drop to ensure immediate protection
      World world = deathLocation.getWorld();
      int centerX = deathChunk.getX();
      int centerZ = deathChunk.getZ();

      for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
         for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
            ChunkKey chunkKey = new ChunkKey(world.getName(), centerX + dx, centerZ + dz);
            protectedChunkKeys.add(chunkKey);

            // Add or update protection
            protectedChunks.compute(chunkKey, (key, existing) -> {
               if (existing == null) {
                  return new ProtectionData(expirationTime, playerId, player.getName());
               } else {
                  // Extend protection if this death expires later
                  if (expirationTime > existing.expirationTime) {
                     existing.expirationTime = expirationTime;
                     existing.lastPlayerName = player.getName();
                  }
                  existing.deathCount++;
                  return existing;
               }
            });
         }
      }

      deathRecord.protectedChunks = protectedChunkKeys;
      recentDeaths.put(playerId, deathRecord);

      // Count items in death drops for verification
      int itemCount = event.getDrops().size();

      plugin.getLogger().info(
            "Player " + player.getName() + " died at " +
                  formatLocation(deathLocation) + " - protected " +
                  protectedChunkKeys.size() + " chunks for " +
                  (protectionDurationMillis / 1000L) + " seconds" +
                  " (Death drops: " + itemCount + " items)");
   }

   /**
    * Handle item spawn - dynamically protect chunks where death items land
    * CRITICAL: This ensures items that bounce/spread to other chunks are still
    * protected
    * Uses MONITOR priority to track items AFTER they spawn
    */
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onItemSpawn(ItemSpawnEvent event) {
      if (!enabled || !protectDroppedItems) {
         return;
      }

      Item item = event.getEntity();
      Location spawnLocation = item.getLocation();

      // Check if there are any recent deaths nearby (within last 10 seconds)
      // This catches death items that spawn after the death event
      long currentTime = System.currentTimeMillis();
      long recentDeathWindow = 10000L; // 10 seconds

      for (DeathRecord death : recentDeaths.values()) {
         // Only check deaths from the same world
         if (!death.location.getWorld().equals(spawnLocation.getWorld())) {
            continue;
         }

         // Only check recent deaths
         if (currentTime - death.deathTime > recentDeathWindow) {
            continue;
         }

         // Check if item is within reasonable distance from death location
         // (items can spread up to ~10 blocks from death point)
         double distance = death.location.distance(spawnLocation);
         if (distance <= 20.0) { // 20 block radius to be safe
            // Protect the chunk where this item landed
            Chunk itemChunk = spawnLocation.getChunk();
            ChunkKey chunkKey = new ChunkKey(
                  spawnLocation.getWorld().getName(),
                  itemChunk.getX(),
                  itemChunk.getZ());

            // Add protection for this chunk if not already protected
            protectedChunks.computeIfAbsent(chunkKey, k -> {
               plugin.getLogger().fine(
                     "Auto-protecting chunk " + chunkKey +
                           " due to death item from " + death.playerName);
               return new ProtectionData(death.expirationTime, death.playerId, death.playerName);
            });

            break; // Found the death this item belongs to
         }
      }
   }

   /**
    * Check if an item is protected by death chunk protection
    */
   public boolean isItemProtected(Item item) {
      if (!enabled) {
         return false;
      }

      Location location = item.getLocation();
      ChunkKey chunkKey = new ChunkKey(
            location.getWorld().getName(),
            location.getChunk().getX(),
            location.getChunk().getZ());

      ProtectionData protection = protectedChunks.get(chunkKey);
      if (protection == null) {
         return false;
      }

      long currentTime = System.currentTimeMillis();
      if (currentTime > protection.expirationTime) {
         // Protection expired
         protectedChunks.remove(chunkKey);
         return false;
      }

      return true;
   }

   /**
    * Check if any entity in the collection is protected
    */
   public boolean hasProtectedItems(Iterable<Entity> entities) {
      if (!enabled) {
         return false;
      }

      for (Entity entity : entities) {
         if (entity instanceof Item && isItemProtected((Item) entity)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Remove protected items from the list and return count of protected items
    */
   public int removeProtectedItems(List<Entity> entities) {
      if (!enabled) {
         return 0;
      }

      int protectedCount = 0;
      Iterator<Entity> iterator = entities.iterator();

      while (iterator.hasNext()) {
         Entity entity = iterator.next();
         if (entity instanceof Item && isItemProtected((Item) entity)) {
            iterator.remove();
            protectedCount++;
         }
      }

      return protectedCount;
   }

   /**
    * Check if a specific chunk is currently protected
    */
   public boolean isChunkProtected(World world, int chunkX, int chunkZ) {
      if (!enabled) {
         return false;
      }

      ChunkKey chunkKey = new ChunkKey(world.getName(), chunkX, chunkZ);
      ProtectionData protection = protectedChunks.get(chunkKey);

      if (protection == null) {
         return false;
      }

      long currentTime = System.currentTimeMillis();
      if (currentTime > protection.expirationTime) {
         protectedChunks.remove(chunkKey);
         return false;
      }

      return true;
   }

   /**
    * Check if a chunk is protected (using Chunk object)
    */
   public boolean isChunkProtected(Chunk chunk) {
      return isChunkProtected(chunk.getWorld(), chunk.getX(), chunk.getZ());
   }

   /**
    * Get remaining protection time for a chunk in seconds (0 if not protected)
    */
   public long getRemainingProtectionTime(World world, int chunkX, int chunkZ) {
      if (!enabled) {
         return 0;
      }

      ChunkKey chunkKey = new ChunkKey(world.getName(), chunkX, chunkZ);
      ProtectionData protection = protectedChunks.get(chunkKey);

      if (protection == null) {
         return 0;
      }

      long currentTime = System.currentTimeMillis();
      long remaining = (protection.expirationTime - currentTime) / 1000L;

      return Math.max(0, remaining);
   }

   /**
    * Check if a location is near a recent death (within protection radius)
    * This is useful for protocols/plugins to respect death grace periods
    * CRITICAL: Use this for InventoryDropCondenser and delayed drop compatibility
    * 
    * @param location The location to check
    * @return true if there's a recent death within 20 blocks, false otherwise
    */
   public boolean isRecentlyDeadNear(Location location) {
      if (!enabled) {
         return false;
      }

      long currentTime = System.currentTimeMillis();
      double protectionRadius = 20.0; // 20 block radius for delayed drops

      for (DeathRecord death : recentDeaths.values()) {
         // Check same world
         if (!death.location.getWorld().equals(location.getWorld())) {
            continue;
         }

         // Check if death is still within protection window
         if (currentTime > death.expirationTime) {
            continue;
         }

         // Check distance
         double distance = death.location.distance(location);
         if (distance <= protectionRadius) {
            return true;
         }
      }

      return false;
   }

   /**
    * Check if a location is near a recent death with custom radius
    * 
    * @param location The location to check
    * @param radius   Custom radius in blocks
    * @return true if there's a recent death within radius, false otherwise
    */
   public boolean isRecentlyDeadNear(Location location, double radius) {
      if (!enabled) {
         return false;
      }

      long currentTime = System.currentTimeMillis();

      for (DeathRecord death : recentDeaths.values()) {
         // Check same world
         if (!death.location.getWorld().equals(location.getWorld())) {
            continue;
         }

         // Check if death is still within protection window
         if (currentTime > death.expirationTime) {
            continue;
         }

         // Check distance
         double distance = death.location.distance(location);
         if (distance <= radius) {
            return true;
         }
      }

      return false;
   }

   /**
    * Get info about nearby recent deaths (for debugging/logging)
    * Returns null if no recent deaths nearby
    */
   public DeathInfo getNearbyDeathInfo(Location location, double radius) {
      if (!enabled) {
         return null;
      }

      long currentTime = System.currentTimeMillis();
      DeathRecord closestDeath = null;
      double closestDistance = Double.MAX_VALUE;

      for (DeathRecord death : recentDeaths.values()) {
         // Check same world
         if (!death.location.getWorld().equals(location.getWorld())) {
            continue;
         }

         // Check if death is still within protection window
         if (currentTime > death.expirationTime) {
            continue;
         }

         // Find closest death within radius
         double distance = death.location.distance(location);
         if (distance <= radius && distance < closestDistance) {
            closestDeath = death;
            closestDistance = distance;
         }
      }

      if (closestDeath != null) {
         long timeSinceDeath = (currentTime - closestDeath.deathTime) / 1000L;
         long timeRemaining = (closestDeath.expirationTime - currentTime) / 1000L;
         return new DeathInfo(
               closestDeath.playerName,
               closestDistance,
               timeSinceDeath,
               timeRemaining);
      }

      return null;
   }

   /**
    * Cleanup expired protections
    */
   private void cleanupExpiredProtections() {
      long currentTime = System.currentTimeMillis();
      int removedChunks = 0;
      int removedDeaths = 0;

      // Remove expired chunk protections
      Iterator<Map.Entry<ChunkKey, ProtectionData>> chunkIterator = protectedChunks.entrySet().iterator();
      while (chunkIterator.hasNext()) {
         Map.Entry<ChunkKey, ProtectionData> entry = chunkIterator.next();
         if (currentTime > entry.getValue().expirationTime) {
            chunkIterator.remove();
            removedChunks++;
         }
      }

      // Remove expired death records
      Iterator<Map.Entry<UUID, DeathRecord>> deathIterator = recentDeaths.entrySet().iterator();
      while (deathIterator.hasNext()) {
         Map.Entry<UUID, DeathRecord> entry = deathIterator.next();
         if (currentTime > entry.getValue().expirationTime) {
            deathIterator.remove();
            removedDeaths++;
         }
      }

      if (removedChunks > 0 || removedDeaths > 0) {
         plugin.getLogger().fine(
               "Cleaned up " + removedChunks + " expired chunk protections and " +
                     removedDeaths + " death records");
      }
   }

   /**
    * Get count of currently protected chunks
    */
   public int getProtectedChunkCount() {
      cleanupExpiredProtections();
      return protectedChunks.size();
   }

   /**
    * Get count of tracked recent deaths
    */
   public int getProtectedDeathCount() {
      cleanupExpiredProtections();
      return recentDeaths.size();
   }

   /**
    * Get debug information
    */
   public String getDebugInfo() {
      cleanupExpiredProtections();

      StringBuilder sb = new StringBuilder();
      sb.append("§6Player Death Tracker Debug Information:\n");
      sb.append("§eEnabled: ").append(enabled).append("\n");
      sb.append("§eProtection Mode: §bChunk-based\n");
      sb.append("§eProtection Duration: ").append(protectionDurationMillis / 1000L).append(" seconds\n");
      sb.append("§eChunk Radius: ").append(chunkRadius).append(" chunks\n");
      sb.append("§eProtect Dropped Items: ").append(protectDroppedItems).append("\n");
      sb.append("§eCurrently Protected Deaths: ").append(recentDeaths.size()).append("\n");
      sb.append("§eCurrently Protected Chunks: ").append(protectedChunks.size());

      return sb.toString();
   }

   /**
    * Get detailed debug information including chunk list
    */
   public String getDetailedDebugInfo() {
      cleanupExpiredProtections();

      StringBuilder sb = new StringBuilder();
      sb.append(getDebugInfo()).append("\n\n");

      if (!protectedChunks.isEmpty()) {
         sb.append("§6Protected Chunks:\n");
         protectedChunks.forEach((key, data) -> {
            long remaining = (data.expirationTime - System.currentTimeMillis()) / 1000L;
            sb.append("§e- ").append(key.world).append(" [")
                  .append(key.chunkX).append(", ").append(key.chunkZ).append("]")
                  .append(" - ").append(remaining).append("s remaining")
                  .append(" (").append(data.lastPlayerName).append(")\n");
         });
      }

      return sb.toString();
   }

   /**
    * Force cleanup of all protections
    */
   public void clearAllProtections() {
      int chunks = protectedChunks.size();
      int deaths = recentDeaths.size();

      protectedChunks.clear();
      recentDeaths.clear();

      plugin.getLogger().info("Cleared " + chunks + " chunk protections and " + deaths + " death records");
   }

   // Helper method to format location
   private String formatLocation(Location loc) {
      return String.format("%s (%.1f, %.1f, %.1f)",
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ());
   }

   /**
    * Chunk identifier class for efficient lookup
    */
   private static class ChunkKey {
      final String world;
      final int chunkX;
      final int chunkZ;
      private final int hashCode;

      ChunkKey(String world, int chunkX, int chunkZ) {
         this.world = world;
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         // Pre-calculate hash for performance
         this.hashCode = Objects.hash(world, chunkX, chunkZ);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o)
            return true;
         if (!(o instanceof ChunkKey))
            return false;
         ChunkKey chunkKey = (ChunkKey) o;
         return chunkX == chunkKey.chunkX &&
               chunkZ == chunkKey.chunkZ &&
               world.equals(chunkKey.world);
      }

      @Override
      public int hashCode() {
         return hashCode;
      }

      @Override
      public String toString() {
         return world + "[" + chunkX + "," + chunkZ + "]";
      }
   }

   /**
    * Protection data for a chunk
    */
   private static class ProtectionData {
      long expirationTime;
      final UUID firstPlayerId;
      String lastPlayerName;
      int deathCount;

      ProtectionData(long expirationTime, UUID playerId, String playerName) {
         this.expirationTime = expirationTime;
         this.firstPlayerId = playerId;
         this.lastPlayerName = playerName;
         this.deathCount = 1;
      }
   }

   /**
    * Death record for tracking player deaths
    */
   private static class DeathRecord {
      final UUID playerId;
      final String playerName;
      final Location location;
      final long deathTime;
      final long expirationTime;
      Set<ChunkKey> protectedChunks;

      DeathRecord(UUID playerId, String playerName, Location location, long deathTime, long expirationTime) {
         this.playerId = playerId;
         this.playerName = playerName;
         this.location = location;
         this.deathTime = deathTime;
         this.expirationTime = expirationTime;
      }
   }

   /**
    * Information about a nearby death (for logging/debugging)
    */
   public static class DeathInfo {
      public final String playerName;
      public final double distance;
      public final long secondsSinceDeath;
      public final long secondsRemaining;

      public DeathInfo(String playerName, double distance, long secondsSinceDeath, long secondsRemaining) {
         this.playerName = playerName;
         this.distance = distance;
         this.secondsSinceDeath = secondsSinceDeath;
         this.secondsRemaining = secondsRemaining;
      }

      @Override
      public String toString() {
         return String.format("%s (%.1f blocks away, %ds ago, %ds remaining)",
               playerName, distance, secondsSinceDeath, secondsRemaining);
      }
   }
}
