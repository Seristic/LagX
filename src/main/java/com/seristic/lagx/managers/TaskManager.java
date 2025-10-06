package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all scheduled tasks for LagX
 * Handles task lifecycle and provides unified task management
 */
public class TaskManager {

    private final LagX plugin;
    private final ConfigurationManager configManager;
    private final Map<String, ScheduledTask> tasks = new HashMap<>();

    public TaskManager(LagX plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        plugin.getLogger().info("Starting scheduled tasks...");

        // Start various periodic tasks
        startChunkUnloadTask();
        startLagRemovalTask();
        startEntityCleanupTask();

        plugin.getLogger().info("Started " + tasks.size() + " scheduled tasks");
    }

    private void startChunkUnloadTask() {
        if (!configManager.isTaskEnabled("chunk-unload")) {
            return;
        }

        int interval = configManager.getTaskInterval("chunk-unload");
        long intervalSeconds = interval * 60L; // Convert minutes to seconds

        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                (scheduledTask) -> performChunkUnload(),
                intervalSeconds * 20L, intervalSeconds * 20L); // Convert to ticks

        tasks.put("chunk-unload", task);
        plugin.getLogger().info("Chunk unload task started (interval: " + interval + " minutes)");
    }

    private void startLagRemovalTask() {
        if (!configManager.isTaskEnabled("lag-removal")) {
            return;
        }

        int interval = configManager.getTaskInterval("lag-removal");
        long intervalSeconds = interval * 60L;

        // Use global scheduler to trigger per-world region-based cleanup
        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                (scheduledTask) -> schedulePerWorldLagRemoval(),
                intervalSeconds * 20L, intervalSeconds * 20L);

        tasks.put("lag-removal", task);
        plugin.getLogger().info("Lag removal task started (interval: " + interval + " minutes)");
    }

    private void startEntityCleanupTask() {
        if (!configManager.isTaskEnabled("entity-cleanup")) {
            return;
        }

        int interval = configManager.getTaskInterval("entity-cleanup");
        long intervalSeconds = interval * 60L;

        // Use global scheduler to trigger per-world region-based cleanup
        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                (scheduledTask) -> schedulePerWorldEntityCleanup(),
                intervalSeconds * 20L, intervalSeconds * 20L);

        tasks.put("entity-cleanup", task);
        plugin.getLogger().info("Entity cleanup task started (interval: " + interval + " minutes)");
    }

    // Task implementations
    private void performChunkUnload() {
        try {
            // Implementation for chunk unloading - Folia compatible
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                org.bukkit.Chunk[] chunks = world.getLoadedChunks();
                for (org.bukkit.Chunk chunk : chunks) {
                    if (chunk.getPluginChunkTickets().isEmpty() && !chunk.isForceLoaded()) {
                        // Schedule chunk unload on the correct region thread
                        Bukkit.getRegionScheduler().execute(plugin, world, chunk.getX(), chunk.getZ(), () -> {
                            try {
                                if (world.unloadChunk(chunk)) {
                                    plugin.getLogger().fine("Unloaded chunk at " + chunk.getX() + "," + chunk.getZ() + " in " + world.getName());
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error unloading chunk at " + chunk.getX() + "," + chunk.getZ() + " in " + world.getName() + ": " + e.getMessage());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error during chunk unload task: " + e.getMessage());
        }
    }

    /**
     * Schedule lag removal across all worlds using region scheduler
     * This is Folia-compatible as it schedules tasks on entity regions
     */
    private void schedulePerWorldLagRemoval() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            // Execute on each entity's owning region
            Bukkit.getRegionScheduler().execute(plugin, world, 0, 0, () -> {
                try {
                    int removed = 0;
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity instanceof org.bukkit.entity.Item) {
                            org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
                            // Check if item is old enough to remove (5 minutes = 6000 ticks)
                            if (item.getTicksLived() > 6000) {
                                item.remove();
                                removed++;
                            }
                        }
                    }
                    if (removed > 0) {
                        plugin.getLogger().info("Removed " + removed + " old items in " + world.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger()
                            .warning("Error during lag removal in " + world.getName() + ": " + e.getMessage());
                }
            });
        }
    }

    /**
     * Schedule entity cleanup across all worlds using region scheduler
     * This is Folia-compatible as it schedules tasks on entity regions
     */
    private void schedulePerWorldEntityCleanup() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            Bukkit.getRegionScheduler().execute(plugin, world, 0, 0, () -> {
                try {
                    int cleaned = 0;
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (shouldCleanupEntity(entity)) {
                            entity.remove();
                            cleaned++;
                        }
                    }
                    if (cleaned > 0) {
                        plugin.getLogger().info("Cleaned up " + cleaned + " entities in " + world.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger()
                            .warning("Error during entity cleanup in " + world.getName() + ": " + e.getMessage());
                }
            });
        }
    }

    private boolean shouldCleanupEntity(org.bukkit.entity.Entity entity) {
        // Logic to determine if entity should be cleaned up
        if (entity instanceof org.bukkit.entity.ExperienceOrb) {
            return entity.getTicksLived() > 2400; // 2 minutes
        }
        if (entity instanceof org.bukkit.entity.Arrow) {
            return entity.getTicksLived() > 1200; // 1 minute
        }
        return false;
    }

    public void shutdown() {
        plugin.getLogger().info("Shutting down scheduled tasks...");

        for (Map.Entry<String, ScheduledTask> entry : tasks.entrySet()) {
            try {
                entry.getValue().cancel();
                plugin.getLogger().info("Cancelled task: " + entry.getKey());
            } catch (Exception e) {
                plugin.getLogger().warning("Error cancelling task " + entry.getKey() + ": " + e.getMessage());
            }
        }

        tasks.clear();
    }

    // Task management methods
    public void cancelTask(String taskName) {
        ScheduledTask task = tasks.get(taskName);
        if (task != null) {
            task.cancel();
            tasks.remove(taskName);
            plugin.getLogger().info("Cancelled task: " + taskName);
        }
    }

    public boolean isTaskRunning(String taskName) {
        ScheduledTask task = tasks.get(taskName);
        return task != null && !task.isCancelled();
    }

    public Map<String, ScheduledTask> getAllTasks() {
        return new HashMap<>(tasks);
    }
}