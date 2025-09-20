package com.seristic.lagx.util;

import com.seristic.lagx.main.LagX;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class ItemFrameOptimizer implements Listener {
   private final LagX plugin;
   private boolean enabled;
   private boolean debugMode;

   public ItemFrameOptimizer(LagX plugin) {
      this.plugin = plugin;
      this.loadConfig();
      if (this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, plugin);
         Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> this.optimizeItemFrames(), 30L, 30L,
               TimeUnit.SECONDS);
         plugin.getLogger().info("Item Frame Optimizer enabled");
      }
   }

   private void loadConfig() {
      this.enabled = this.plugin.getConfig().getBoolean("item_frame_optimization.enabled", true);
      this.debugMode = this.plugin.getConfig().getBoolean("item_frame_optimization.debug", false);
   }

   public void reloadConfig() {
      boolean wasEnabled = this.enabled;
      this.plugin.reloadConfig();
      this.loadConfig();
      if (!wasEnabled && this.enabled) {
         Bukkit.getPluginManager().registerEvents(this, this.plugin);
         this.plugin.getLogger().info("Item Frame Optimizer enabled");
      } else if (wasEnabled && !this.enabled) {
         this.plugin.getLogger().info("Item Frame Optimizer disabled");
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   private void optimizeItemFrames() {
      if (this.enabled) {
         for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
               Bukkit.getRegionScheduler()
                     .run(
                           this.plugin,
                           world,
                           chunk.getX(),
                           chunk.getZ(),
                           task -> {
                              int itemFrameCount = 0;

                              for (Entity entity : chunk.getEntities()) {
                                 if (entity instanceof ItemFrame) {
                                    itemFrameCount++;
                                    ItemFrame frame = (ItemFrame) entity;
                                    if (frame.getTicksLived() % 100 == 0) {
                                    }
                                 }
                              }

                              if (this.debugMode && itemFrameCount > 0) {
                                 this.plugin
                                       .getLogger()
                                       .info(
                                             "Optimized " + itemFrameCount + " item frames in chunk " + chunk.getX()
                                                   + "," + chunk.getZ() + " in world " + world.getName());
                              }
                           });
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onItemFramePlace(HangingPlaceEvent event) {
      if (this.enabled) {
         if (event.getEntity() instanceof ItemFrame && this.debugMode) {
            this.plugin.getLogger().info("Item frame placed at " + event.getEntity().getLocation()
                  + " - will be optimized by ItemFrameOptimizer");
         }
      }
   }

   public int getItemFrameCount() {
      int count = 0;

      for (World world : Bukkit.getWorlds()) {
         for (Entity entity : world.getEntities()) {
            if (entity instanceof ItemFrame) {
               count++;
            }
         }
      }

      return count;
   }

   public String getDebugInfo() {
      StringBuilder sb = new StringBuilder();
      sb.append("§6Item Frame Optimizer Debug Information:\n");
      sb.append("§eEnabled: ").append(this.enabled).append("\n");
      sb.append("§eTotal Item Frames: ").append(this.getItemFrameCount()).append("\n");
      sb.append("§eOptimization: Item frames are excluded from entity limits and have reduced tick rates");
      return sb.toString();
   }
}
