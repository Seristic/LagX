package com.seristic.hbzcleaner.util;

import com.seristic.hbzcleaner.main.HBZCleaner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathTracker implements Listener {
   private final HBZCleaner plugin;
   private final Map<UUID, Long> recentDeaths = new HashMap<>();
   private final Map<UUID, Location> deathLocations = new HashMap<>();
   private boolean enabled;
   private int protectionRadius;
   private long protectionDuration;

   public PlayerDeathTracker(HBZCleaner plugin) {
      this.plugin = plugin;
      this.loadConfig();
      if (this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, plugin);
         Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> this.cleanupOldDeaths(), 60L, 60L, TimeUnit.SECONDS);
         plugin.getLogger()
            .info(
               "Player Death Tracker enabled - Protection radius: "
                  + this.protectionRadius
                  + " blocks, duration: "
                  + this.protectionDuration / 1000L
                  + " seconds"
            );
      }
   }

   private void loadConfig() {
      this.enabled = this.plugin.getConfig().getBoolean("player_death_protection.enabled", true);
      this.protectionRadius = this.plugin.getConfig().getInt("player_death_protection.radius", 10);
      this.protectionDuration = this.plugin.getConfig().getLong("player_death_protection.duration_seconds", 120L) * 1000L;
   }

   public void reloadConfig() {
      boolean wasEnabled = this.enabled;
      this.plugin.reloadConfig();
      this.loadConfig();
      if (!wasEnabled && this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, this.plugin);
         this.plugin.getLogger().info("Player Death Tracker enabled");
      } else if (wasEnabled && !this.enabled) {
         this.plugin.getLogger().info("Player Death Tracker disabled");
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      if (this.enabled) {
         Player player = event.getEntity();
         UUID playerId = player.getUniqueId();
         Location deathLocation = player.getLocation();
         long currentTime = System.currentTimeMillis();
         this.recentDeaths.put(playerId, currentTime);
         this.deathLocations.put(playerId, deathLocation.clone());
         this.plugin
            .getLogger()
            .info("Player " + player.getName() + " died at " + deathLocation + " - items protected for " + this.protectionDuration / 1000L + " seconds");
      }
   }

   public boolean isItemProtected(Item item) {
      if (!this.enabled) {
         return false;
      } else {
         Location itemLocation = item.getLocation();
         long currentTime = System.currentTimeMillis();

         for (Entry<UUID, Long> entry : this.recentDeaths.entrySet()) {
            UUID playerId = entry.getKey();
            long deathTime = entry.getValue();
            if (currentTime - deathTime <= this.protectionDuration) {
               Location deathLocation = this.deathLocations.get(playerId);
               if (deathLocation != null && deathLocation.getWorld().equals(itemLocation.getWorld())) {
                  double distance = deathLocation.distance(itemLocation);
                  if (distance <= (double)this.protectionRadius) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public boolean hasProtectedItems(Iterable<Entity> entities) {
      if (!this.enabled) {
         return false;
      } else {
         for (Entity entity : entities) {
            if (entity instanceof Item && this.isItemProtected((Item)entity)) {
               return true;
            }
         }

         return false;
      }
   }

   public int removeProtectedItems(List<Entity> entities) {
      if (!this.enabled) {
         return 0;
      } else {
         int[] protectedCount = new int[]{0};
         entities.removeIf(entity -> {
            if (entity instanceof Item && this.isItemProtected((Item)entity)) {
               protectedCount[0]++;
               return true;
            } else {
               return false;
            }
         });
         return protectedCount[0];
      }
   }

   private void cleanupOldDeaths() {
      long currentTime = System.currentTimeMillis();
      this.recentDeaths.entrySet().removeIf(entry -> {
         boolean expired = currentTime - entry.getValue() > this.protectionDuration;
         if (expired) {
            this.deathLocations.remove(entry.getKey());
         }

         return expired;
      });
   }

   public int getProtectedDeathCount() {
      this.cleanupOldDeaths();
      return this.recentDeaths.size();
   }

   public String getDebugInfo() {
      StringBuilder sb = new StringBuilder();
      sb.append("§6Player Death Tracker Debug Information:\n");
      sb.append("§eEnabled: ").append(this.enabled).append("\n");
      sb.append("§eProtection Radius: ").append(this.protectionRadius).append(" blocks\n");
      sb.append("§eProtection Duration: ").append(this.protectionDuration / 1000L).append(" seconds\n");
      sb.append("§eCurrently Protected Deaths: ").append(this.getProtectedDeathCount());
      return sb.toString();
   }
}
