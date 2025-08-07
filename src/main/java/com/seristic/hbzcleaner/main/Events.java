package com.seristic.hbzcleaner.main;

import com.seristic.hbzcleaner.api.proto.DelayedLRProtocolResult;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.inf.Help;
import com.seristic.hbzcleaner.proto.bin.CCEntities;
import com.seristic.hbzcleaner.util.DoubleVar;
import com.seristic.hbzcleaner.util.LRConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.WorldInitEvent;

/* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/main/Events.class */
public class Events implements Listener {
    private static final List<UUID> useLocationLagRemoval = new ArrayList<>();
    private static final List<UUID> chatDelay = new ArrayList<>();
    private static boolean canSLDRun = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldInitEvent e) {
        if (LRConfig.noSpawnChunks) {
            LaggRemover.instance.getLogger().warning("Config `noSpawnChunks` is not supported in Folia, disabled.");
            // FIXME: e.getWorld().setKeepSpawnInMemory(false);
        }
    }

    // TODO: use AsyncChatEvent instead
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!p.hasPermission("hbzlag.nochatdelay") && LRConfig.chatDelay > 0) {
            if (chatDelay.contains(uuid)) {
                e.setCancelled(true);
                Help.sendMsg(p, "§cPlease slow down your chat.", true);
                return;
            }
            chatDelayCooldown(uuid);
        }
        if (LRConfig.doRelativeAction && !useLocationLagRemoval.contains(uuid) && e.getMessage().toLowerCase().contains("lag")) {
            if (canSLDRun && LRConfig.isAIActive) {
                smartLagDetection();
            }
            final List<Entity> nearbyEntities = p.getNearbyEntities(LRConfig.localLagRadius, LRConfig.localLagRadius, LRConfig.localLagRadius);
            if (nearbyEntities.size() < LRConfig.localLagTriggered) {
                return;
            }
            cooldown(uuid);
            int entsLeng = (int) (nearbyEntities.size() * LRConfig.localThinPercent);
            int toRemove = nearbyEntities.size() - entsLeng;
            for (int i = 0; i < toRemove && !nearbyEntities.isEmpty(); i++) {
                nearbyEntities.remove(0);
            }
            p.sendMessage("§eEntities around you are being removed because we detected you were lagging.");
            if (LRConfig.doOnlyItemsForRelative) {
                Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
                    for (Entity entity : nearbyEntities) {
                        if (entity.getType().equals(EntityType.ITEM)) {
                            entity.remove();
                        }
                    }
                }, 50L, TimeUnit.MILLISECONDS); // 1 tick = 50ms
            } else if (LRConfig.dontDoFriendlyMobsForRelative) {
                Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
                    CCEntities.clearEntities(nearbyEntities, false, CCEntities.hostile);
                    for (Entity entity : nearbyEntities) {
                        if (entity.getType().equals(EntityType.ITEM)) {
                            entity.remove();
                        }
                    }
                }, 50L, TimeUnit.MILLISECONDS); // 1 tick = 50ms
            } else {
                Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
                    CCEntities.clearEntities(nearbyEntities, false, CCEntities.hostile);
                    CCEntities.clearEntities(nearbyEntities, false, CCEntities.peaceful);
                    for (Entity entity : nearbyEntities) {
                        if (entity.getType().equals(EntityType.ITEM)) {
                            entity.remove();
                        }
                    }
                }, 50L, TimeUnit.MILLISECONDS); // 1 tick = 50ms
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (!LRConfig.thinMobs || e.getLocation().getChunk().getEntities().length <= LRConfig.thinAt) {
            return;
        }
        e.setCancelled(true);
    }

    private void cooldown(final UUID u) {
        useLocationLagRemoval.add(u);
        Bukkit.getAsyncScheduler().runDelayed(
            LaggRemover.getInstance(),
            task -> Events.useLocationLagRemoval.remove(u),
            20L * LRConfig.localLagRemovalCooldown * 50L, TimeUnit.MILLISECONDS
        );
    }

    private void smartAIcooldown() {
        canSLDRun = false;
        Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
            Events.canSLDRun = true;
        }, 1200 * LRConfig.smartaicooldown * 50L, TimeUnit.MILLISECONDS);
    }

    private void chatDelayCooldown(final UUID uuid) {
        chatDelay.add(uuid);
        Bukkit.getAsyncScheduler().runDelayed(
            LaggRemover.getInstance(),
            task -> Events.chatDelay.remove(uuid),
            LRConfig.chatDelay * 50L, TimeUnit.MILLISECONDS
        );
    }

    private void smartLagDetection() {
        smartAIcooldown();
        Runtime r = Runtime.getRuntime();
        long ram_used = ((r.totalMemory() - r.freeMemory()) / LaggRemover.MEMORY_MBYTE_SIZE) / LaggRemover.MEMORY_MBYTE_SIZE;
        long ram_total = (r.maxMemory() / LaggRemover.MEMORY_MBYTE_SIZE) / LaggRemover.MEMORY_MBYTE_SIZE;
        if (ram_total - ram_used < LRConfig.ramConstant) {
            for (LRProtocol p : LRConfig.ramProtocols.keySet()) {
                DoubleVar<Object[], Boolean> dat = LRConfig.ramProtocols.get(p);
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
        } else if (TickPerSecond.getTPS() < LRConfig.lagConstant) {
            for (LRProtocol p2 : LRConfig.tpsProtocols.keySet()) {
                DoubleVar<Object[], Boolean> dat2 = LRConfig.tpsProtocols.get(p2);
                if (dat2.getVar2()) {
                    Protocol.rund(p2, dat2.getVar1(), new DelayedLRProtocolResult() { // from class: drew6017.lr.main.Events.8
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
