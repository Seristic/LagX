package com.seristic.hbzcleaner.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.seristic.hbzcleaner.api.Module;
import com.seristic.hbzcleaner.api.proto.DelayedLRProtocolResult;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.inf.Help;
import com.seristic.hbzcleaner.util.DoubleVar;
import com.seristic.hbzcleaner.util.EntityLimiter;
import com.seristic.hbzcleaner.util.EntityStacker;
import com.seristic.hbzcleaner.util.LRConfig;
import com.seristic.hbzcleaner.util.LRTabCompleter;
import com.seristic.hbzcleaner.util.TownyIntegration;
import com.seristic.hbzcleaner.util.VillagerOptimizer;

public class LaggRemover extends JavaPlugin implements Listener {
    public static final String CONFIG_VERSION = "0.1.7";
    public static final long MEMORY_MBYTE_SIZE = 1024;
    public static LaggRemover instance;
    public static String prefix = "§6§lHBZCleaner §7§l>>§r ";
    public static File modDir;
    private static HashMap<Module, String[]> loaded;
    private long startTime;
    private EntityLimiter entityLimiter;
    private VillagerOptimizer villagerOptimizer;
    private EntityStacker entityStacker;
    private static TownyIntegration townyIntegration;
    
    public static LaggRemover getInstance() {
        return instance;
    }

    public long getStartTime() {
        return startTime;
    }

    public EntityLimiter getEntityLimiter() {
        return entityLimiter;
    }

    public VillagerOptimizer getVillagerOptimizer() {
        return villagerOptimizer;
    }
    
    public EntityStacker getEntityStacker() {
        return entityStacker;
    }

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis();
        instance = this;

        Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);

        // Use async scheduler for TPS tracking (Folia compatible)
        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> new TickPerSecond().run(), 5, 50, TimeUnit.MILLISECONDS);

        Help.init();
        Protocol.init();
        LRConfig.init();
        loaded = new HashMap<>();
        prefix = Objects.requireNonNull(getConfig().getString("prefix")).replaceAll("&", "§");

        if (LRConfig.autoChunk) {
            // Use async scheduler for chunk unloading (Folia compatible)
            Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
                for (World world : LaggRemover.this.getServer().getWorlds()) {
                    if (world.getPlayers().size() == 0) {
                        for (Chunk chunk : world.getLoadedChunks()) {
                            // Schedule chunk unloading on the region scheduler
                            Bukkit.getRegionScheduler().run(this, world, chunk.getX(), chunk.getZ(), regionTask -> {
                                if (!world.unloadChunkRequest(chunk.getX(), chunk.getZ())) {
                                    getLogger().info("Failed to unload chunk (" + chunk.getX() + ", " + chunk.getZ() + ")");
                                }
                            });
                        }
                    }
                }
            }, 10, 10, TimeUnit.SECONDS);
        }

        if (LRConfig.autoLagRemoval) {
            autoLagRemovalLoop();
        }
        modDir = new File(getDataFolder(), "Modules");
        if (!modDir.exists()) {
            modDir.mkdirs();
        }
        for (File f : Objects.requireNonNull(modDir.listFiles())) {
            if (!f.isDirectory() && f.getName().endsWith(".jar")) {
                try (ZipFile zipFile = new ZipFile(f)) {
                    URL[] classes = { f.toURI().toURL() };
                    URLClassLoader loader = new URLClassLoader(classes, LaggRemover.class.getClassLoader());
                    YamlConfiguration c = new YamlConfiguration();
                    c.load(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("module.yml"))));
                    String name = c.getString("name");
                    String version = c.getString("version");
                    String author = c.getString("author");
                    getLogger().info("Loading module \"" + name + "-" + version + "\" created by \"" + author + "\"...");
                    Class<?> plugin = Class.forName(c.getString("main"), true, loader);
                    Module module = (Module) plugin.getDeclaredConstructor().newInstance();
                    loaded.put(module, new String[]{name, version, author});
                    module.onEnable();
                } catch (IOException | InvalidConfigurationException | ReflectiveOperationException exception) {
                    getLogger().info("LaggRemover located an invalid module named \"" + f.getName() + "\"");
                }
            }
        }
        getLogger().info("Loaded " + loaded.size() + " module(s)");
        
        // Initialize Entity Limiter
        entityLimiter = new EntityLimiter(this);
        Bukkit.getServer().getPluginManager().registerEvents(entityLimiter, this);
        getLogger().info("Entity Limiter " + (entityLimiter.isEnabled() ? "enabled" : "disabled"));
        
        // Initialize Villager Optimizer
        villagerOptimizer = new VillagerOptimizer(this);
        Bukkit.getServer().getPluginManager().registerEvents(villagerOptimizer, this);
        getLogger().info("Villager Optimizer " + (villagerOptimizer.isEnabled() ? "enabled" : "disabled"));
        
        // Initialize Towny Integration with proper error handling
        try {
            townyIntegration = new TownyIntegration(this);
            getLogger().info("Towny Integration " + (townyIntegration.isTownyEnabled() ? "enabled" : "disabled"));
        } catch (Throwable e) {
            getLogger().warning("Failed to initialize Towny integration: " + e.getMessage());
            getLogger().warning("Plugin will continue without Towny support");
            // Create a dummy integration instance that always returns false for isTownyEnabled
            townyIntegration = new TownyIntegration(this);
        }
        
        // Initialize Entity Stacker
        entityStacker = new EntityStacker(this);
        Bukkit.getServer().getPluginManager().registerEvents(entityStacker, this);
        getLogger().info("Entity Stacker " + (entityStacker.isEnabled() ? "enabled" : "disabled"));
        
        // Register tab completer
        Objects.requireNonNull(getCommand("hbzlag")).setTabCompleter(new LRTabCompleter());
        
        getLogger().info("§6HBZCleaner has been enabled! §7(Enhanced LaggRemover for Folia servers)");
    }

    @Override
    public void onDisable() {
        // Folia compatible - cancel tasks for this plugin
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        
        for (Module module : loaded.keySet()) {
            module.onDisable();
        }
        instance = null;
        getLogger().info("HBZCleaner has been disabled!");
    }

    public static String[] getModulesList() {
        StringBuilder sb = new StringBuilder();
        for (String[] s : loaded.values()) {
            sb.append(s[0]);
            sb.append(", ");
        }
        String sbs = sb.toString();
        if (!sbs.equals("")) {
            sbs = sbs.substring(0, sbs.length() - 2);
        }
        return new String[]{ sbs, Integer.toString(loaded.size()) };
    }

    public static String[] getProtocolList() {
        StringBuilder sb = new StringBuilder();
        Collection<LRProtocol> protocols = Protocol.getProtocols();
        for (LRProtocol p : protocols) {
            sb.append(p.id());
            sb.append(", ");
        }
        String sbs = sb.toString();
        if (!sbs.equals("")) {
            sbs = sbs.substring(0, sbs.length() - 2);
        }
        return new String[]{sbs, Integer.toString(protocols.size())};
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        Entity ent = e.getEntity();
        if (!ent.hasMetadata("NPC") && LRConfig.thinMobs && ent.getLocation().getChunk().getEntities().length > LRConfig.thinAt) {
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (cmd.getName().equalsIgnoreCase("hbzlag")) {
            // Handle direct command
            if (args.length == 0) {
                if (hasPermission(player, "hbzlag.help")) {
                    Help.send(player, 1);
                    return true;
                } else {
                    Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                    return true;
                }
            }
            
            // Help subcommand
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
                if (hasPermission(player, "hbzlag.help")) {
                    if (args.length == 2) {
                        try {
                            int page = Integer.parseInt(args[1]);
                            if (Help.isValidPage(page)) {
                                Help.send(player, page);
                            } else {
                                Help.sendMsg(player, "§cPage #" + page + " does not exist. Valid pages: 1-" + Help.getTotalPages(), true);
                            }
                        } catch (NumberFormatException ex) {
                            Help.sendMsg(player, "§cPlease enter a valid page number.", true);
                        }
                        return true;
                    }
                    Help.send(player, 1);
                    return true;
                } else {
                    Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                    return true;
                }
            }
            
            // Handle stacker command
            if (args[0].equalsIgnoreCase("stacker") || args[0].equalsIgnoreCase("stack")) {
                if (hasPermission(player, "hbzlag.stacker")) {
                    if (args.length < 2) {
                        Help.sendMsg(player, "§cUsage: /hbzlag stacker [info|debug|reload|stack]", true);
                        return true;
                    }
                    
                    String subCommand = args[1].toLowerCase();
                    
                    if (entityStacker == null) {
                        Help.sendMsg(player, "§cEntity Stacker is not enabled on this server.", true);
                        return true;
                    }
                    
                    switch (subCommand) {
                        case "info":
                            Help.sendMsg(player, "§6Entity Stacker Statistics:", true);
                            Help.sendMsg(player, "§eEnabled: §a" + entityStacker.isEnabled(), false);
                            Help.sendMsg(player, "§eStacked Items: §a" + entityStacker.getStackedItemsCount(), false);
                            Help.sendMsg(player, "§eStacked Entities: §a" + entityStacker.getStackedEntitiesCount(), false);
                            return true;
                            
                        case "debug":
                            Help.sendMsg(player, entityStacker.getDebugInfo(), true);
                            return true;
                            
                        case "reload":
                            entityStacker.reloadConfig();
                            Help.sendMsg(player, "§aEntity Stacker configuration reloaded!", true);
                            return true;
                            
                        case "stack":
                            if (player == null) {
                                Help.sendMsg(player, "§cThis command can only be used by players.", true);
                                return true;
                            }
                            
                            int radius = 50; // Default radius
                            if (args.length >= 3) {
                                try {
                                    radius = Integer.parseInt(args[2]);
                                    if (radius <= 0 || radius > 200) {
                                        Help.sendMsg(player, "§cRadius must be between 1 and 200.", true);
                                        return true;
                                    }
                                } catch (NumberFormatException e) {
                                    Help.sendMsg(player, "§cInvalid radius value. Please enter a number.", true);
                                    return true;
                                }
                            }
                            
                            int stacksCreated = entityStacker.stackEntitiesInRadius(player.getLocation(), radius);
                            Help.sendMsg(player, "§aSuccessfully stacked entities in a " + radius + " block radius. Created " + stacksCreated + " stacks.", true);
                            return true;
                            
                        default:
                            Help.sendMsg(player, "§cUnknown subcommand. Available options: info, debug, reload, stack", true);
                            return true;
                    }
                } else {
                    Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                    return true;
                }
            }
            
            // Handle reload command
            else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                if (hasPermission(player, "hbzlag.reload")) {
                    try {
                        LRConfig.reload();
                        Help.sendMsg(player, "§7[§6HBZCleaner§7] §a✓ Configuration reloaded successfully!", true);
                        return true;
                    } catch (Exception e) {
                        Help.sendMsg(player, "§7[§6HBZCleaner§7] §c✗ Failed to reload configuration!", true);
                        Help.sendMsg(player, "§7[§6HBZCleaner§7] §c" + e.getMessage(), false);
                        getLogger().warning(() -> "Config reload failed: " + e.getMessage());
                        return true;
                    }
                } else {
                    Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                    return true;
                }
            }
            
            // Delegate remaining subcommands to LRCommand for backward-compatible functionality
            try {
                if (LRCommand.onCommand(player, args)) {
                    return true;
                }
            } catch (Throwable t) {
                getLogger().warning(() -> "Error executing command: " + t.getMessage());
            }
            
            // If not handled, try modules
            for (Module m : loaded.keySet()) {
                if (m.onCommand(sender, label, args)) {
                    return true;
                }
            }
            
            // Command not found
            Help.sendMsg(player, "§cCommand not found! Use /hbzlag help for a list of commands.", true);
            return true;
        }
        return true;
    }

    public static void broadcast(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public static String[] getData(Module m) {
        return loaded.get(m);
    }

    public void autoLagRemovalLoop() {
        // Use async scheduler for auto lag removal (Folia compatible)
        Bukkit.getAsyncScheduler().runDelayed(this, task -> {
            // Log when automatic cleaning is being prepared
            getLogger().info("Preparing for scheduled cleanup...");
            
            // To avoid duplicate messages, track which protocols have been reported
            final java.util.Set<String> reportedProtocols = new java.util.HashSet<>();
            
            for (LRProtocol p : LRConfig.periodic_protocols.keySet()) {
                DoubleVar<Object[], Boolean> dat = LRConfig.periodic_protocols.get(p);
                final String protocolId = p.id();
                
                if (dat.getVar2()) {
                    Protocol.rund(p, dat.getVar1(), new DelayedLRProtocolResult() {
                        @Override
                        public void receive(LRProtocolResult result) {
                            if (result != null && result.getData() != null && result.getData().length > 0) {
                                // Only report each protocol once
                                if (!reportedProtocols.contains(protocolId)) {
                                    getLogger().info(String.format("Scheduled cleanup '%s' completed: %s entities affected", 
                                        protocolId, result.getData()[0]));
                                    reportedProtocols.add(protocolId);
                                }
                            }
                        }
                    });
                } else {
                    LRProtocolResult result = p.run(dat.getVar1());
                    if (result != null && result.getData() != null && result.getData().length > 0) {
                        // Only report each protocol once
                        if (!reportedProtocols.contains(protocolId)) {
                            getLogger().info(String.format("Scheduled cleanup '%s' completed: %s entities affected", 
                                protocolId, result.getData()[0]));
                            reportedProtocols.add(protocolId);
                        }
                    }
                }
            }
            
            // Schedule the next run
            LaggRemover.this.autoLagRemovalLoop();
        }, 1200L * LRConfig.autoLagRemovalTime * 50L, TimeUnit.MILLISECONDS); // Convert minutes to milliseconds
    }
    
    public static TownyIntegration getTownyIntegration() {
        return townyIntegration;
    }
    
    /**
     * Check if a player has a specific permission or the wildcard permission
     * @param player The player to check
     * @param permission The permission to check for
     * @return true if the player has the permission or is an operator
     */
    public static boolean hasPermission(Player player, String permission) {
        if (player == null) return true; // Console always has permission
        
        // Check for OP status (always has permission)
        if (player.isOp()) return true;
        
        // Check for specific permission
        if (player.hasPermission(permission)) return true;
        
        // Check for wildcard permission (hbzlag.*)
        if (player.hasPermission("hbzlag.*")) return true;
        
        // Check for admin permission (redundant if we have hbzlag.* but kept for backward compatibility)
        return player.hasPermission("hbzlag.admin");
    }
}
