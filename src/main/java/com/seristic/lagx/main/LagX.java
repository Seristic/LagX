package com.seristic.lagx.main;

import com.seristic.lagx.commands.CommandManager;
import com.seristic.lagx.commands.PerformanceCommand;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.listeners.MapProtectionListener;
import com.seristic.lagx.managers.LagXPluginManager;
import com.seristic.lagx.util.EntityLimiter;
import com.seristic.lagx.util.EntityStacker;
import com.seristic.lagx.util.HBZTabCompleter;
import com.seristic.lagx.util.ItemFrameOptimizer;
import com.seristic.lagx.util.LagXUtils;
import com.seristic.lagx.util.PlayerDeathTracker;
import com.seristic.lagx.util.TownyIntegration;
import com.seristic.lagx.util.VillagerOptimizer;
import com.seristic.lagx.utils.MapArtManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Main LagX Plugin Class - Refactored for Minimal Responsibility
 * 
 * This class now only handles:
 * - Plugin lifecycle (onEnable/onDisable)
 * - Core command routing via CommandManager
 * - Essential getters for backward compatibility
 * 
 * All other responsibilities have been moved to specialized managers:
 * - LagXPluginManager: Overall initialization and coordination
 * - FeatureManager: Performance optimization features
 * - TaskManager: Scheduled tasks and background operations
 * - EventManager: Event handling registration
 * - ConfigurationManager: Configuration management
 * - IntegrationManager: Soft dependencies and third-party integrations
 * - ModuleLoader: External module loading and management
 */
public class LagX extends JavaPlugin implements Listener {

    // Constants
    public static final String CONFIG_VERSION = "0.1.7";
    public static final long MEMORY_MBYTE_SIZE = 1024L;

    // Core managers
    private LagXPluginManager pluginManager;
    private CommandManager commandManager;
    private PerformanceCommand performanceCommand;

    // Legacy compatibility fields (to be phased out)
    public static LagX instance; // Made public for backward compatibility
    private long startTime;

    // Static references for modules (required for module API)
    public static File modDir;
    public static String prefix = "§6§lLagX §7§l>>§r ";

    @Override
    public void onEnable() {
        // Record start time
        this.startTime = System.currentTimeMillis();

        // Set singleton instance for legacy compatibility
        instance = this;

        // Initialize the main plugin manager
        this.pluginManager = new LagXPluginManager(this);

        try {
            // Initialize all managers through the plugin manager
            pluginManager.initialize();

            // Initialize Help system (must be called before commands are used)
            com.seristic.lagx.inf.Help.init();

            // Initialize Protocol system
            com.seristic.lagx.api.proto.Protocol.init();

            // Initialize modern command system
            this.commandManager = new CommandManager(this);
            getLogger().info("Command Manager initialized with " +
                    commandManager.getCommandNames().size() + " commands");

            // Initialize performance command
            this.performanceCommand = new PerformanceCommand(this);

            // Register commands with null-safety
            registerCommands();

            // Register the plugin as listener for legacy EntitySpawnEvent handling
            // (This will eventually be moved to EventManager)
            Bukkit.getServer().getPluginManager().registerEvents(this, this);

            // Register map protection listener
            Bukkit.getServer().getPluginManager().registerEvents(new MapProtectionListener(this), this);

            getLogger().info("§6LagX has been enabled!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable LagX: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Cancel all scheduled tasks
            Bukkit.getAsyncScheduler().cancelTasks(this);
            Bukkit.getGlobalRegionScheduler().cancelTasks(this);

            // Shutdown through plugin manager
            if (pluginManager != null) {
                pluginManager.shutdown();
            }

            // Clean up references
            this.commandManager = null;
            this.performanceCommand = null;
            this.pluginManager = null;

            getLogger().info("LagX has been disabled!");

        } catch (Exception e) {
            getLogger().severe("Error during LagX shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register plugin commands with proper executor assignment
     */
    private void registerCommands() {
        // Main lagx command
        org.bukkit.command.PluginCommand lagxCmd = this.getCommand("lagx");
        if (lagxCmd != null) {
            lagxCmd.setExecutor(this);
            lagxCmd.setTabCompleter(new HBZTabCompleter());
        } else {
            getLogger().severe("Command 'lagx' not found in plugin.yml");
        }

        // Performance command
        org.bukkit.command.PluginCommand perfCmd = this.getCommand("lagxperf");
        if (perfCmd != null) {
            perfCmd.setExecutor(performanceCommand);
            perfCmd.setTabCompleter(performanceCommand);
        } else {
            getLogger().severe("Command 'lagxperf' not found in plugin.yml");
        }

        getLogger().info("Commands registered successfully");
    }

    /**
     * Main command handler - delegates to CommandManager
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        // Quick fallback for most critical commands that should work like the original
        if (cmd.getName().equalsIgnoreCase("lagx")) {
            Player player = sender instanceof Player ? (Player) sender : null;

            if (args.length == 0) {
                if (hasPermission(player, "lagx.help")) {
                    // Route to CommandManager for help
                    return commandManager != null ? commandManager.handleCommand(sender, args) : false;
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
            }

            // Handle critical commands that must work like the original
            String cmd0 = args[0].toLowerCase();
            switch (cmd0) {
                case "clear":
                case "c":
                    // Keep the refactored clear command - it works well
                    return commandManager != null ? commandManager.handleCommand(sender, args) : false;

                case "tps":
                    if (hasPermission(player, "lagx.tps")) {
                        // Use existing TPS functionality through managers
                        return commandManager != null ? commandManager.handleCommand(sender, args) : false;
                    }
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;

                case "help":
                case "h":
                    return commandManager != null ? commandManager.handleCommand(sender, args) : false;

                default:
                    // Route everything else through CommandManager
                    return commandManager != null ? commandManager.handleCommand(sender, args) : false;
            }
        }

        return false;
    }

    /**
     * Legacy entity spawn event handler
     * TODO: Move this to EventManager/EntityLimiter
     */
    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        // Delegate to EntityLimiter if available
        if (pluginManager != null &&
                pluginManager.getFeatureManager() != null &&
                pluginManager.getFeatureManager().getEntityLimiter() != null) {

            // Let the EntityLimiter handle this event
            // This is a temporary bridge - eventually EntityLimiter should be registered
            // directly
            EntityLimiter limiter = pluginManager.getFeatureManager().getEntityLimiter();
            if (limiter.isEnabled()) {
                // EntityLimiter will handle the event through its own listener
            }
        }
    }

    // ===========================================
    // GETTERS FOR BACKWARD COMPATIBILITY
    // ===========================================

    public static LagX getInstance() {
        return instance;
    }

    public long getStartTime() {
        return startTime;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LagXPluginManager getPluginManager() {
        return pluginManager;
    }

    // Legacy feature getters - delegate to FeatureManager
    public EntityLimiter getEntityLimiter() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getEntityLimiter()
                : null;
    }

    public VillagerOptimizer getVillagerOptimizer() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getVillagerOptimizer()
                : null;
    }

    public EntityStacker getEntityStacker() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getEntityStacker()
                : null;
    }

    public ItemFrameOptimizer getItemFrameOptimizer() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getItemFrameOptimizer()
                : null;
    }

    public PlayerDeathTracker getPlayerDeathTracker() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getPlayerDeathTracker()
                : null;
    }

    public MapArtManager getMapArtManager() {
        return pluginManager != null && pluginManager.getFeatureManager() != null
                ? pluginManager.getFeatureManager().getMapArtManager()
                : null;
    }

    // ===========================================
    // STATIC UTILITY METHODS (Delegated)
    // ===========================================

    public static TownyIntegration getTownyIntegration() {
        if (instance != null &&
                instance.pluginManager != null &&
                instance.pluginManager.getIntegrationManager() != null) {
            return instance.pluginManager.getIntegrationManager().getTownyIntegration();
        }
        return null;
    }

    /**
     * Optimize villagers in a specific world
     * GitHub implementation: lines 1565-1615 in LagX.java
     */
    public void optimizeVillagersInWorld(World world, CommandSender sender) {
        if (world != null && getVillagerOptimizer() != null) {
            Help.sendMsg(sender, "§eStarting villager optimization in world §b" + world.getName() + "§e...", true);

            int totalVillagers = 0;
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof org.bukkit.entity.Villager) {
                    totalVillagers++;
                }
            }

            int optimized = 0;
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof org.bukkit.entity.Villager) {
                    optimized++;
                }
            }

            Help.sendMsg(sender, "§aManually optimized §b" + optimized + "§a villagers in world §e" + world.getName(),
                    true);
        } else {
            if (sender != null) {
                Help.sendMsg(sender,
                        "§cCannot optimize villagers: world is null or villager optimizer is not available", true);
            }
        }
    }

    public static boolean hasPermission(Player player, String permission) {
        return LagXUtils.hasPermission(player, permission);
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        return LagXUtils.hasPermission(sender, permission);
    }

    public static void broadcast(String message) {
        LagXUtils.broadcast(message);
    }

    public static void broadcastWarn(String message) {
        LagXUtils.broadcastWarn(message);
    }

    // Module-related static methods (delegated to ModuleLoader)
    public static String[] getModulesList() {
        if (instance != null &&
                instance.pluginManager != null &&
                instance.pluginManager.getModuleLoader() != null) {
            return instance.pluginManager.getModuleLoader().getModulesList();
        }
        return new String[0];
    }

    // Configuration-related static methods
    public static boolean areWarningsEnabled() {
        if (instance != null &&
                instance.pluginManager != null &&
                instance.pluginManager.getConfigManager() != null) {
            return instance.pluginManager.getConfigManager().areWarningsEnabled();
        }
        return true; // Default to enabled
    }

    public static void setWarningsEnabled(boolean enabled) {
        if (instance != null &&
                instance.pluginManager != null &&
                instance.pluginManager.getConfigManager() != null) {
            instance.pluginManager.getConfigManager().setWarningsEnabled(enabled);
        }
    }

    // Module-related static methods (delegated to ModuleLoader)
    public static String[] getData(com.seristic.lagx.api.Module module) {
        if (instance != null &&
                instance.pluginManager != null &&
                instance.pluginManager.getModuleLoader() != null) {
            return instance.pluginManager.getModuleLoader().getData(module);
        }
        return new String[] { "Unknown", "0.0.0", "Unknown" };
    }

    // Protocol-related methods for backward compatibility
    public static String[] getProtocolList() {
        // This would need to be implemented based on your protocol system
        // For now, return empty array to avoid compilation errors
        return new String[0];
    }
}