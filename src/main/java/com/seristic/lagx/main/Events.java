package com.seristic.lagx.main;

import com.seristic.lagx.api.proto.DelayedLRProtocolResult;
import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.api.proto.Protocol;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.proto.bin.CCEntities;
import com.seristic.lagx.util.DoubleVar;
import com.seristic.lagx.util.HBZConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.WorldInitEvent;

public class Events implements Listener {
   private static final List<UUID> useLocationLagRemoval = new ArrayList<>();
   private static final List<UUID> chatDelay = new ArrayList<>();
   private static boolean canSLDRun = true;

   @EventHandler(priority = EventPriority.LOWEST)
   public void onWorldLoad(WorldInitEvent e) {
      if (HBZConfig.noSpawnChunks) {
         LagX.instance.getLogger().warning("Config `noSpawnChunks` is not supported in Folia, disabled.");
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onChat(AsyncPlayerChatEvent e) {
      Player p = e.getPlayer();
      UUID uuid = p.getUniqueId();
      if (!LagX.hasPermission(p, "lagx.nochatdelay") && HBZConfig.chatDelay > 0) {
         if (chatDelay.contains(uuid)) {
            e.setCancelled(true);
            Help.sendMsg(p, "§cPlease slow down your chat.", true);
            return;
         }

         this.chatDelayCooldown(uuid);
      }

      if (HBZConfig.doRelativeAction && !useLocationLagRemoval.contains(uuid)
            && e.getMessage().toLowerCase().contains("lag")) {
         if (canSLDRun && HBZConfig.isAIActive) {
            this.smartLagDetection();
         }

         // Schedule the entity detection and removal to run on the main thread
         // since getNearbyEntities() cannot be called from async chat thread
         p.getScheduler().run(LagX.getInstance(), task -> {
            List<Entity> nearbyEntities = p.getNearbyEntities((double) HBZConfig.localLagRadius,
                  (double) HBZConfig.localLagRadius, (double) HBZConfig.localLagRadius);
            if (nearbyEntities.size() < HBZConfig.localLagTriggered) {
               return;
            }

            this.cooldown(uuid);
            int entsLeng = (int) ((float) nearbyEntities.size() * HBZConfig.localThinPercent);
            int toRemove = nearbyEntities.size() - entsLeng;

            for (int i = 0; i < toRemove && !nearbyEntities.isEmpty(); i++) {
               nearbyEntities.remove(0);
            }

            p.sendMessage("§eEntities around you are being removed because we detected you were lagging.");
            if (HBZConfig.doOnlyItemsForRelative) {
               // Schedule item removal on main thread for Folia compatibility
               Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), removeTask -> {
                  for (Entity entity : nearbyEntities) {
                     if (entity instanceof Item) {
                        // Schedule each entity removal on its owning region
                        Bukkit.getRegionScheduler().run(LagX.getInstance(), entity.getLocation(), regionTask -> {
                           if (entity.isValid()) {
                              entity.remove();
                           }
                        });
                     }
                  }
               }, 50L, TimeUnit.MILLISECONDS);
            } else if (HBZConfig.dontDoFriendlyMobsForRelative) {
               // Schedule entity removal on main thread for Folia compatibility
               Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), removeTask -> {
                  for (Entity entity : nearbyEntities) {
                     if (entity instanceof Item || java.util.Arrays.asList(CCEntities.hostile).contains(entity.getType())) {
                        // Schedule each entity removal on its owning region
                        Bukkit.getRegionScheduler().run(LagX.getInstance(), entity.getLocation(), regionTask -> {
                           if (entity.isValid()) {
                              entity.remove();
                           }
                        });
                     }
                  }
               }, 50L, TimeUnit.MILLISECONDS);
            } else {
               // Schedule all entity removal on main thread for Folia compatibility
               Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), removeTask -> {
                  for (Entity entity : nearbyEntities) {
                     if (entity instanceof Item || 
                         java.util.Arrays.asList(CCEntities.hostile).contains(entity.getType()) ||
                         java.util.Arrays.asList(CCEntities.peaceful).contains(entity.getType())) {
                        // Schedule each entity removal on its owning region
                        Bukkit.getRegionScheduler().run(LagX.getInstance(), entity.getLocation(), regionTask -> {
                           if (entity.isValid()) {
                              entity.remove();
                           }
                        });
                     }
                  }
               }, 50L, TimeUnit.MILLISECONDS);
            }
         }, null);
      }
   }

   @EventHandler
   public void onSpawn(EntitySpawnEvent e) {
      if (HBZConfig.thinMobs && e.getLocation().getChunk().getEntities().length > HBZConfig.thinAt) {
         e.setCancelled(true);
      }
   }

   private void cooldown(UUID u) {
      useLocationLagRemoval.add(u);
      Bukkit.getAsyncScheduler()
            .runDelayed(
                  LagX.getInstance(), task -> useLocationLagRemoval.remove(u),
                  20L * (long) HBZConfig.localLagRemovalCooldown * 50L, TimeUnit.MILLISECONDS);
   }

   private void smartAIcooldown() {
      canSLDRun = false;
      Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), task -> canSLDRun = true,
            1200L * HBZConfig.smartaicooldown * 50L, TimeUnit.MILLISECONDS);
   }

   private void chatDelayCooldown(UUID uuid) {
      chatDelay.add(uuid);
      Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), task -> chatDelay.remove(uuid),
            (long) HBZConfig.chatDelay * 50L, TimeUnit.MILLISECONDS);
   }

   private void smartLagDetection() {
      this.smartAIcooldown();
      Runtime r = Runtime.getRuntime();
      long ram_used = (r.totalMemory() - r.freeMemory()) / 1024L / 1024L;
      long ram_total = r.maxMemory() / 1024L / 1024L;
      if (ram_total - ram_used < HBZConfig.ramConstant) {
         for (LRProtocol p : HBZConfig.ramProtocols.keySet()) {
            DoubleVar<Object[], Boolean> dat = HBZConfig.ramProtocols.get(p);
            if (dat.getVar2()) {
               Protocol.rund(p, dat.getVar1(), new DelayedLRProtocolResult() {
                  @Override
                  public void receive(LRProtocolResult result) {
                  }
               });
            } else {
               p.run(dat.getVar1());
            }
         }
      } else if (TickPerSecond.getTPS() < HBZConfig.lagConstant) {
         for (LRProtocol p2 : HBZConfig.tpsProtocols.keySet()) {
            DoubleVar<Object[], Boolean> dat2 = HBZConfig.tpsProtocols.get(p2);
            if (dat2.getVar2()) {
               Protocol.rund(p2, dat2.getVar1(), new DelayedLRProtocolResult() {
                  @Override
                  public void receive(LRProtocolResult result) {
                  }
               });
            } else {
               p2.run(dat2.getVar1());
            }
         }
      }
   }
}
