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
        
        // Initialize Towny Integration
        townyIntegration = new TownyIntegration(this);
        getLogger().info("Towny Integration " + (townyIntegration.isTownyEnabled() ? "enabled" : "disabled"));
        
        // Register tab completer
        Objects.requireNonNull(getCommand("hbzlag")).setTabCompleter(new LRTabCompleter());
        
        getLogger().info("§6HBZCleaner has been enabled! §7(Enhanced LaggRemover for Folia servers)");
    }

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

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (cmd.getName().equalsIgnoreCase("hbzlag")) {
            if (args.length == 0) {
                Help.send(player, 1);
                return true;
            } else if (!LRCommand.onCommand(player, args)) {
                for (Module m : loaded.keySet()) {
                    if (m.onCommand(sender, label, args)) {
                        return true;
                    }
                }
                Help.sendMsg(player, "§cCommand not found! Use /hbzlag help for a list of commands.", true);
                return true;
            } else {
                return true;
            }
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
            for (LRProtocol p : LRConfig.periodic_protocols.keySet()) {
                DoubleVar<Object[], Boolean> dat = LRConfig.periodic_protocols.get(p);
                if (dat.getVar2()) {
                    Protocol.rund(p, dat.getVar1(), new DelayedLRProtocolResult() { // from class: drew6017.lr.main.LaggRemover.3.1
                        @Override
                        public void receive(LRProtocolResult result) {
                        }
                    });
                } else {
                    p.run(dat.getVar1());
                }
            }
            LaggRemover.this.autoLagRemovalLoop();
        }, 1200L * LRConfig.autoLagRemovalTime * 50L, TimeUnit.MILLISECONDS); // Convert ticks to milliseconds
    }
    
    public static TownyIntegration getTownyIntegration() {
        return townyIntegration;
    }
}
