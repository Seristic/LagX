package com.seristic.hbzcleaner.main;

import com.seristic.hbzcleaner.api.Module;
import com.seristic.hbzcleaner.api.proto.DelayedLRProtocolResult;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.commands.PerformanceCommand;
import com.seristic.hbzcleaner.inf.Help;
import com.seristic.hbzcleaner.proto.bin.CCEntities;
import com.seristic.hbzcleaner.util.DoubleVar;
import com.seristic.hbzcleaner.util.EntityLimiter;
import com.seristic.hbzcleaner.util.EntityStacker;
import com.seristic.hbzcleaner.util.HBZConfig;
import com.seristic.hbzcleaner.util.HBZTabCompleter;
import com.seristic.hbzcleaner.util.ItemFrameOptimizer;
import com.seristic.hbzcleaner.util.PlayerDeathTracker;
import com.seristic.hbzcleaner.util.TownyIntegration;
import com.seristic.hbzcleaner.util.VillagerOptimizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class HBZCleaner extends JavaPlugin implements Listener {
   public static final String CONFIG_VERSION = "0.1.7";
   public static final long MEMORY_MBYTE_SIZE = 1024L;
   public static HBZCleaner instance;
   // Update default chat prefix branding to LagX
   public static String prefix = "§6§lLagX §7§l>>§r ";
   public static File modDir;
   private static HashMap<Module, String[]> loaded;
   private long startTime;
   private EntityLimiter entityLimiter;
   private VillagerOptimizer villagerOptimizer;
   private EntityStacker entityStacker;
   private ItemFrameOptimizer itemFrameOptimizer;
   private PlayerDeathTracker playerDeathTracker;
   private static TownyIntegration townyIntegration;
   private PerformanceCommand performanceCommand;

   public static HBZCleaner getInstance() {
      return instance;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public EntityLimiter getEntityLimiter() {
      return this.entityLimiter;
   }

   public VillagerOptimizer getVillagerOptimizer() {
      return this.villagerOptimizer;
   }

   public EntityStacker getEntityStacker() {
      return this.entityStacker;
   }

   public ItemFrameOptimizer getItemFrameOptimizer() {
      return this.itemFrameOptimizer;
   }

   public PlayerDeathTracker getPlayerDeathTracker() {
      return this.playerDeathTracker;
   }

   public void onEnable() {
      this.startTime = System.currentTimeMillis();
      instance = this;
      Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
      Logger serverLogger = this.getServer().getLogger();
      serverLogger.setFilter(new Filter() {
         @Override
         public boolean isLoggable(LogRecord record) {
            String message = record.getMessage();
            return !message.contains("[x") && message.contains("died");
         }
      });
      Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> new TickPerSecond().run(), 5L, 50L,
            TimeUnit.MILLISECONDS);
      Help.init();
      Protocol.init();
      HBZConfig.init();
      loaded = new HashMap<>();
      String cfgPrefix = Objects.requireNonNull(this.getConfig().getString("prefix"));
      if (cfgPrefix.contains("HBZCleaner")) {
         String migrated = cfgPrefix.replace("HBZCleaner", "LagX");
         this.getConfig().set("prefix", migrated);
         this.saveConfig();
         this.getLogger().info("Migrated legacy prefix to LagX in config.yml");
         cfgPrefix = migrated;
      }
      prefix = cfgPrefix.replaceAll("&", "§");
      if (HBZConfig.autoChunk) {
         Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
            for (World world : this.getServer().getWorlds()) {
               if (world.getPlayers().size() == 0) {
                  for (Chunk chunk : world.getLoadedChunks()) {
                     Bukkit.getRegionScheduler().run(this, world, chunk.getX(), chunk.getZ(), regionTask -> {
                        if (!world.unloadChunkRequest(chunk.getX(), chunk.getZ())) {
                           this.getLogger().info("Failed to unload chunk (" + chunk.getX() + ", " + chunk.getZ() + ")");
                        }
                     });
                  }
               }
            }
         }, 10L, 10L, TimeUnit.SECONDS);
      }

      if (HBZConfig.autoLagRemoval) {
         this.getLogger().info("Auto lag removal is enabled - starting periodic cleanup every "
               + HBZConfig.autoLagRemovalTime + " minutes");
         this.autoLagRemovalLoop();
      } else {
         this.getLogger().info("Auto lag removal is disabled in config");
      }

      modDir = new File(this.getDataFolder(), "Modules");
      if (!modDir.exists()) {
         modDir.mkdirs();
      }

      for (File f : Objects.requireNonNull(modDir.listFiles())) {
         if (!f.isDirectory() && f.getName().endsWith(".jar")) {
            try (ZipFile zipFile = new ZipFile(f)) {
               URL[] classes = new URL[] { f.toURI().toURL() };
               URLClassLoader loader = new URLClassLoader(classes, HBZCleaner.class.getClassLoader());
               YamlConfiguration c = new YamlConfiguration();
               c.load(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("module.yml"))));
               String name = c.getString("name");
               String version = c.getString("version");
               String author = c.getString("author");
               this.getLogger()
                     .info("Loading module \"" + name + "-" + version + "\" created by \"" + author + "\"...");
               Class<?> plugin = Class.forName(c.getString("main"), true, loader);
               Module module = (Module) plugin.getDeclaredConstructor().newInstance();
               loaded.put(module, new String[] { name, version, author });
               module.onEnable();
            } catch (InvalidConfigurationException | ReflectiveOperationException | IOException var18) {
               // Rebrand invalid module message
               this.getLogger().info("LagX located an invalid module named \"" + f.getName() + "\"");
            }
         }
      }

      this.getLogger().info("Loaded " + loaded.size() + " module(s)");
      this.entityLimiter = new EntityLimiter(this);
      Bukkit.getServer().getPluginManager().registerEvents(this.entityLimiter, this);
      this.getLogger().info("Entity Limiter " + (this.entityLimiter.isEnabled() ? "enabled" : "disabled"));
      this.villagerOptimizer = new VillagerOptimizer(this);
      Bukkit.getServer().getPluginManager().registerEvents(this.villagerOptimizer, this);
      this.getLogger().info("Villager Optimizer " + (this.villagerOptimizer.isEnabled() ? "enabled" : "disabled"));

      try {
         townyIntegration = new TownyIntegration(this);
         this.getLogger().info("Towny Integration " + (townyIntegration.isTownyEnabled() ? "enabled" : "disabled"));
      } catch (Throwable var15) {
         this.getLogger().warning("Failed to initialize Towny integration: " + var15.getMessage());
         this.getLogger().warning("Plugin will continue without Towny support");
         townyIntegration = new TownyIntegration(this);
      }

      this.entityStacker = new EntityStacker(this);
      this.getLogger().info("Entity Stacker " + (this.entityStacker.isEnabled() ? "enabled" : "disabled"));
      this.itemFrameOptimizer = new ItemFrameOptimizer(this);
      this.getLogger().info("Item Frame Optimizer " + (this.itemFrameOptimizer.isEnabled() ? "enabled" : "disabled"));
      this.playerDeathTracker = new PlayerDeathTracker(this);
      this.getLogger().info("Player Death Tracker " + (this.playerDeathTracker.isEnabled() ? "enabled" : "disabled"));
      Objects.requireNonNull(this.getCommand("lagx")).setTabCompleter(new HBZTabCompleter());
      this.performanceCommand = new PerformanceCommand(this);
      Objects.requireNonNull(this.getCommand("lagxperf")).setExecutor(this.performanceCommand);
      Objects.requireNonNull(this.getCommand("lagxperf")).setTabCompleter(this.performanceCommand);
      this.getLogger().info("Performance command registered and ready to use");
      // Rebrand enable log
      this.getLogger().info("§6LagX has been enabled!");
   }

   public void onDisable() {
      Bukkit.getAsyncScheduler().cancelTasks(this);
      Bukkit.getGlobalRegionScheduler().cancelTasks(this);

      for (Module module : loaded.keySet()) {
         module.onDisable();
      }

      if (this.entityLimiter != null) {
         this.entityLimiter = null;
      }

      if (this.villagerOptimizer != null) {
         this.villagerOptimizer = null;
      }

      if (this.entityStacker != null) {
         this.entityStacker = null;
      }

      if (this.itemFrameOptimizer != null) {
         this.itemFrameOptimizer = null;
      }

      if (this.playerDeathTracker != null) {
         this.playerDeathTracker = null;
      }

      if (this.performanceCommand != null) {
         this.performanceCommand = null;
      }

      townyIntegration = null;
      instance = null;
      // Rebrand disable log
      this.getLogger().info("LagX has been disabled!");
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

      return new String[] { sbs, Integer.toString(loaded.size()) };
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

      return new String[] { sbs, Integer.toString(protocols.size()) };
   }

   @EventHandler
   public void onSpawn(EntitySpawnEvent e) {
      Entity ent = e.getEntity();
      if (!ent.hasMetadata("NPC") && HBZConfig.thinMobs
            && ent.getLocation().getChunk().getEntities().length > HBZConfig.thinAt) {
         e.setCancelled(true);
      }
   }

   public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
      Player player = sender instanceof Player ? (Player) sender : null;
      if (cmd.getName().equalsIgnoreCase("lagx")) {
         if (args.length == 0) {
            if (hasPermission(player, "lagx.help")) {
               Help.send(player, 1);
               return true;
            } else {
               Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
               return true;
            }
         } else if (!args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("h")) {
            if (args[0].equalsIgnoreCase("tps")) {
               if (hasPermission(player, "lagx.tps")) {
                  Help.sendMsg(player, "§eTPS: " + TickPerSecond.format(), true);
               } else {
                  Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
               }

               return true;
            } else if (args[0].equalsIgnoreCase("ram")) {
               if (hasPermission(player, "lagx.ram")) {
                  Runtime rt = Runtime.getRuntime();
                  long max = rt.maxMemory() / 1048576L;
                  long total = rt.totalMemory() / 1048576L;
                  long free = rt.freeMemory() / 1048576L;
                  long used = total - free;
                  Help.sendMsg(player,
                        String.format("§eRAM Usage: §b%dMB used§7/§b%dMB total§7 (max §b%dMB§7)", used, total, max),
                        true);
               } else {
                  Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
               }

               return true;
            } else if (args[0].equalsIgnoreCase("gc")) {
               if (hasPermission(player, "lagx.gc")) {
                  Runtime rt = Runtime.getRuntime();
                  long before = rt.totalMemory() - rt.freeMemory();
                  System.gc();
                  long after = rt.totalMemory() - rt.freeMemory();
                  long diff = (before - after) / 1048576L;
                  Help.sendMsg(player, "§aGarbage collection completed. Freed ~§b" + Math.max(diff, 0L) + "MB§a.",
                        true);
               } else {
                  Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
               }

               return true;
            } else if (!args[0].equalsIgnoreCase("info") && !args[0].equalsIgnoreCase("i")) {
               if (!args[0].equalsIgnoreCase("status") && !args[0].equalsIgnoreCase("master")
                     && !args[0].equalsIgnoreCase("m")) {
                  if (!args[0].equalsIgnoreCase("world") && !args[0].equalsIgnoreCase("w")) {
                     if (!args[0].equalsIgnoreCase("unload") && !args[0].equalsIgnoreCase("u")) {
                        if (!args[0].equalsIgnoreCase("protocol") && !args[0].equalsIgnoreCase("pr")) {
                           if (args[0].equalsIgnoreCase("clear")
                                 || args[0].equalsIgnoreCase("c")
                                 || args[0].equalsIgnoreCase("count")
                                 || args[0].equalsIgnoreCase("ct")) {
                              boolean isCount = args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("ct");
                              if (!hasPermission(player, "lagx.clear")) {
                                 Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                                 return true;
                              } else if (args.length < 2) {
                                 Help.sendMsg(player, "§cUsage: /lagx " + (isCount ? "count" : "clear")
                                       + " <items|entities|area|type> [...]", true);
                                 return true;
                              } else {
                                 String mode = args[1].toLowerCase();

                                 try {
                                    switch (mode) {
                                       case "items":
                                       case "i":
                                          World worldxx = null;
                                          if (args.length >= 3) {
                                             worldxx = Bukkit.getWorld(args[2]);
                                             if (worldxx == null) {
                                                Help.sendMsg(player, "§cWorld not found: " + args[2], true);
                                                return true;
                                             }
                                          }

                                          LRProtocol pItems = Protocol.getProtocol("cc_items");
                                          LRProtocolResult rx;
                                          if (worldxx == null) {
                                             rx = Protocol.run(pItems, new Object[] { isCount });
                                          } else {
                                             rx = Protocol.run(pItems, new Object[] { isCount, worldxx });
                                          }

                                          int affectedItems = rx != null && rx.getData().length > 0
                                                ? (Integer) rx.getData()[0]
                                                : 0;
                                          if (isCount) {
                                             Help.sendMsg(
                                                   player, "§eCounted §b" + affectedItems + " §eitems"
                                                         + (worldxx != null ? " in §b" + worldxx.getName() : ""),
                                                   true);
                                          } else {
                                             Help.sendMsg(
                                                   player, "§aCleared §b" + affectedItems + " §aitems"
                                                         + (worldxx != null ? " in §b" + worldxx.getName() : ""),
                                                   true);
                                          }

                                          return true;
                                       case "entities":
                                       case "e":
                                          if (args.length < 3) {
                                             Help.sendMsg(
                                                   player,
                                                   "§cUsage: /lagx " + (isCount ? "count" : "clear")
                                                         + " entities <hostile|peaceful|all> [world]",
                                                   true);
                                             return true;
                                          } else {
                                             String typeSel = args[2].toLowerCase();
                                             World worldx = null;
                                             if (args.length >= 4) {
                                                worldx = Bukkit.getWorld(args[3]);
                                                if (worldx == null) {
                                                   Help.sendMsg(player, "§cWorld not found: " + args[3], true);
                                                   return true;
                                                }
                                             }

                                             EntityType[] set;
                                             if (typeSel.startsWith("hostile") || typeSel.equals("h")) {
                                                set = CCEntities.hostile;
                                             } else if (!typeSel.startsWith("peace") && !typeSel.equals("p")) {
                                                set = null;
                                             } else {
                                                set = CCEntities.peaceful;
                                             }

                                             LRProtocol pEnt = Protocol.getProtocol("cc_entities");
                                             LRProtocolResult r;
                                             if (worldx == null) {
                                                r = Protocol.run(pEnt, new Object[] { isCount, set });
                                             } else {
                                                r = Protocol.run(pEnt, new Object[] { isCount, set, worldx });
                                             }

                                             int affectedEntities = r != null && r.getData().length > 0
                                                   ? (Integer) r.getData()[0]
                                                   : 0;
                                             String scope = set == null ? "all"
                                                   : (set == CCEntities.hostile ? "hostile" : "peaceful");
                                             if (isCount) {
                                                Help.sendMsg(
                                                      player,
                                                      "§eCounted §b"
                                                            + affectedEntities
                                                            + " §e"
                                                            + scope
                                                            + " entities"
                                                            + (worldx != null ? " in §b" + worldx.getName() : ""),
                                                      true);
                                             } else {
                                                Help.sendMsg(
                                                      player,
                                                      "§aCleared §b"
                                                            + affectedEntities
                                                            + " §a"
                                                            + scope
                                                            + " entities"
                                                            + (worldx != null ? " in §b" + worldx.getName() : ""),
                                                      true);
                                             }

                                             return true;
                                          }
                                       case "type":
                                       case "t":
                                          if (args.length < 3) {
                                             Help.sendMsg(
                                                   player, "§cUsage: /lagx " + (isCount ? "count" : "clear")
                                                         + " type <ENTITY_TYPE,...> [world]",
                                                   true);
                                             return true;
                                          } else {
                                             String typeList = args[2];
                                             if (!typeList.equalsIgnoreCase("list")
                                                   && !typeList.equalsIgnoreCase("l")) {
                                                World world = null;
                                                if (args.length >= 4) {
                                                   world = Bukkit.getWorld(args[3]);
                                                   if (world == null) {
                                                      Help.sendMsg(player, "§cWorld not found: " + args[3], true);
                                                      return true;
                                                   }
                                                }

                                                List<EntityType> parsed = new ArrayList<>();

                                                for (String name : typeList.split(",")) {
                                                   try {
                                                      parsed.add(EntityType.valueOf(name.trim().toUpperCase()));
                                                   } catch (IllegalArgumentException var36) {
                                                      Help.sendMsg(player, "§cInvalid type: " + name, true);
                                                      return true;
                                                   }
                                                }

                                                EntityType[] arr = parsed.toArray(new EntityType[0]);
                                                LRProtocol pEnt = Protocol.getProtocol("cc_entities");
                                                LRProtocolResult r = world == null
                                                      ? Protocol.run(pEnt, new Object[] { isCount, arr })
                                                      : Protocol.run(pEnt, new Object[] { isCount, arr, world });
                                                int affectedTypes = r != null && r.getData().length > 0
                                                      ? (Integer) r.getData()[0]
                                                      : 0;
                                                if (isCount) {
                                                   Help.sendMsg(
                                                         player,
                                                         "§eCounted §b"
                                                               + affectedTypes
                                                               + " §eentities of specified types"
                                                               + (world != null ? " in §b" + world.getName() : ""),
                                                         true);
                                                } else {
                                                   Help.sendMsg(
                                                         player,
                                                         "§aCleared §b"
                                                               + affectedTypes
                                                               + " §aentities of specified types"
                                                               + (world != null ? " in §b" + world.getName() : ""),
                                                         true);
                                                }

                                                return true;
                                             }

                                             String all = Arrays.stream(EntityType.values())
                                                   .map(Enum::name)
                                                   .limit(100L)
                                                   .reduce((a, b) -> a + ", " + b)
                                                   .orElse("none");
                                             Help.sendMsg(player, "§eEntity Types: §7" + all, true);
                                             return true;
                                          }
                                       case "area":
                                       case "a":
                                          if (player == null) {
                                             Help.sendMsg(player, "§cThis subcommand requires a player (for location).",
                                                   true);
                                             return true;
                                          } else if (args.length < 3) {
                                             Help.sendMsg(
                                                   player,
                                                   "§cUsage: /lagx "
                                                         + (isCount ? "count" : "clear")
                                                         + " area <b:radiusBlocks|c:radiusChunks> [hostile|peaceful|all|items]",
                                                   true);
                                             return true;
                                          } else {
                                             String radiusArg = args[2];
                                             boolean blocks = radiusArg.startsWith("b:");
                                             boolean chunksMode = radiusArg.startsWith("c:");
                                             if (!blocks && !chunksMode) {
                                                Help.sendMsg(player, "§cRadius must start with b: or c:", true);
                                                return true;
                                             } else {
                                                int radius;
                                                try {
                                                   radius = Integer.parseInt(radiusArg.substring(2));
                                                } catch (NumberFormatException var35) {
                                                   Help.sendMsg(player, "§cInvalid radius number.", true);
                                                   return true;
                                                }

                                                String filter = args.length >= 4 ? args[3].toLowerCase() : "all";
                                                int affectedArea = 0;
                                                Location loc = player.getLocation();
                                                int blockRadius = blocks ? radius : radius * 16;
                                                Set<Entity> nearby = new HashSet<>();
                                                int minX = loc.getBlockX() - blockRadius;
                                                int maxX = loc.getBlockX() + blockRadius;
                                                int minZ = loc.getBlockZ() - blockRadius;
                                                int maxZ = loc.getBlockZ() + blockRadius;

                                                for (Chunk ch : loc.getWorld().getLoadedChunks()) {
                                                   int cx = ch.getX() << 4;
                                                   int cz = ch.getZ() << 4;
                                                   if (cx <= maxX && cx + 15 >= minX && cz <= maxZ && cz + 15 >= minZ) {
                                                      for (Entity e : ch.getEntities()) {
                                                         nearby.add(e);
                                                      }
                                                   }
                                                }

                                                for (Entity e : nearby) {
                                                   if (!(e instanceof Player)) {
                                                      double dx = Math.abs(e.getLocation().getX() - loc.getX());
                                                      double dz = Math.abs(e.getLocation().getZ() - loc.getZ());
                                                      if (!(dx > (double) blockRadius)
                                                            && !(dz > (double) blockRadius)) {
                                                         if (switch (filter) {
                                                            case "hostile", "h" -> e instanceof Monster;
                                                            case "peaceful", "p" ->
                                                               e instanceof LivingEntity && !(e instanceof Monster);
                                                            case "items", "item" -> e instanceof Item;
                                                            default -> true;
                                                         }) {
                                                            if (isCount) {
                                                               affectedArea++;
                                                            } else {
                                                               e.remove();
                                                               affectedArea++;
                                                            }
                                                         }
                                                      }
                                                   }
                                                }

                                                Help.sendMsg(
                                                      player,
                                                      (isCount ? "§eCounted §b" : "§aCleared §b") + affectedArea
                                                            + " §eentities/items in area (" + radiusArg + ")",
                                                      true);
                                                return true;
                                             }
                                          }
                                       default:
                                          Help.sendMsg(player, "§cUnknown mode. Use items/entities/type/area", true);
                                          return true;
                                    }
                                 } catch (Throwable var40) {
                                    Help.sendMsg(player, "§cClear error: " + var40.getMessage(), true);
                                    return true;
                                 }
                              }
                           } else if (args[0].equalsIgnoreCase("limiter") || args[0].equalsIgnoreCase("lim")) {
                              // Entity Limiter management commands
                              if (!hasPermission(player, "lagx.entities") && !hasPermission(player, "lagx.admin")) {
                                 Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                                 return true;
                              }

                              if (this.entityLimiter == null) {
                                 Help.sendMsg(player, "§cEntity Limiter is not enabled on this server.", true);
                                 return true;
                              }

                              if (args.length < 2) {
                                 Help.sendMsg(player, "§cUsage: /lagx limiter [status|reload|enable|disable]", true);
                                 return true;
                              }

                              String sub = args[1].toLowerCase();
                              switch (sub) {
                                 case "status":
                                 case "s":
                                    Help.sendMsg(player, "§6Entity Limiter Status:", true);
                                    Help.sendMsg(player, this.entityLimiter.getStatus(), false);
                                    return true;
                                 case "reload":
                                 case "r":
                                    this.entityLimiter.reload();
                                    Help.sendMsg(player, "§aEntity Limiter configuration reloaded!", true);
                                    return true;
                                 case "enable":
                                 case "e":
                                    this.getConfig().set("entity_limiter.enabled", true);
                                    this.saveConfig();
                                    this.entityLimiter.reload();
                                    Help.sendMsg(player, "§aEntity Limiter has been enabled.", true);
                                    return true;
                                 case "disable":
                                 case "d":
                                    this.getConfig().set("entity_limiter.enabled", false);
                                    this.saveConfig();
                                    this.entityLimiter.reload();
                                    Help.sendMsg(player, "§cEntity Limiter has been disabled.", true);
                                    return true;
                                 default:
                                    Help.sendMsg(player,
                                          "§cUnknown subcommand. Available: status, reload, enable, disable", true);
                                    return true;
                              }
                           } else if (!args[0].equalsIgnoreCase("ping") && !args[0].equalsIgnoreCase("p")) {
                              if (!args[0].equalsIgnoreCase("stacker") && !args[0].equalsIgnoreCase("stack")) {
                                 if (!args[0].equalsIgnoreCase("entities") && !args[0].equalsIgnoreCase("ent")) {
                                    if (!args[0].equalsIgnoreCase("limiter") && !args[0].equalsIgnoreCase("lim")) {
                                       if (!args[0].equalsIgnoreCase("towny") && !args[0].equalsIgnoreCase("town")) {
                                          if (!args[0].equalsIgnoreCase("preset") && !args[0].equalsIgnoreCase("pr")) {
                                             if (!args[0].equalsIgnoreCase("villagers")
                                                   && !args[0].equalsIgnoreCase("optimize")) {
                                                if (!args[0].equalsIgnoreCase("reload")
                                                      && !args[0].equalsIgnoreCase("rl")) {
                                                   if (!args[0].equalsIgnoreCase("warnings")
                                                         && !args[0].equalsIgnoreCase("warn")) {
                                                      for (Module m : loaded.keySet()) {
                                                         if (m.onCommand(sender, label, args)) {
                                                            return true;
                                                         }
                                                      }

                                                      Help.sendMsg(player,
                                                            "§cCommand not found! Use /lagx help for a list of commands.",
                                                            true);
                                                      return true;
                                                   } else {
                                                      boolean allowed = player == null
                                                            || hasPermission(player, "lagx.warn.toggle")
                                                            || hasPermission(player, "lagx.admin");
                                                      if (!allowed) {
                                                         Help.sendMsg(player,
                                                               "§cYou don't have permission to modify/view warnings.",
                                                               true);
                                                         return true;
                                                      } else if (args.length != 1
                                                            && !args[1].equalsIgnoreCase("status")) {
                                                         String sub = args[1].toLowerCase();
                                                         switch (sub) {
                                                            case "on":
                                                               setWarningsEnabled(true);
                                                               Help.sendMsg(player,
                                                                     "§aProtocol warnings have been ENABLED.", true);
                                                               return true;
                                                            case "off":
                                                               setWarningsEnabled(false);
                                                               Help.sendMsg(player,
                                                                     "§cProtocol warnings have been DISABLED.", true);
                                                               return true;
                                                            case "toggle":
                                                               boolean now = !areWarningsEnabled();
                                                               setWarningsEnabled(now);
                                                               Help.sendMsg(player, "§eProtocol warnings toggled to §6"
                                                                     + (now ? "ENABLED" : "DISABLED"), true);
                                                               return true;
                                                            default:
                                                               Help.sendMsg(player,
                                                                     "§cUsage: /lagx warnings [status|on|off|toggle]",
                                                                     true);
                                                               return true;
                                                         }
                                                      } else {
                                                         boolean on = areWarningsEnabled();
                                                         Help.sendMsg(player, "§eProtocol warnings are currently §6"
                                                               + (on ? "ENABLED" : "DISABLED"), true);
                                                         return true;
                                                      }
                                                   }
                                                } else if (hasPermission(player, "lagx.reload")) {
                                                   try {
                                                      HBZConfig.reload();
                                                      // Rebrand reload success/failure messages
                                                      Help.sendMsg(player,
                                                            "§7[§6LagX§7] §a✓ Configuration reloaded successfully!",
                                                            true);
                                                      return true;
                                                   } catch (Exception var33) {
                                                      Help.sendMsg(player,
                                                            "§7[§6LagX§7] §c✗ Failed to reload configuration!", true);
                                                      Help.sendMsg(player, "§7[§6LagX§7] §c" + var33.getMessage(),
                                                            false);
                                                      this.getLogger().warning(
                                                            () -> "Config reload failed: " + var33.getMessage());
                                                      return true;
                                                   }
                                                } else {
                                                   Help.sendMsg(player,
                                                         "§cYou don't have permission to use this command.", true);
                                                   return true;
                                                }
                                             } else if (!hasPermission(player, "lagx.villagers")) {
                                                Help.sendMsg(player, "§cYou don't have permission to use this command.",
                                                      true);
                                                return true;
                                             } else if (this.villagerOptimizer == null) {
                                                Help.sendMsg(player,
                                                      "§cVillager Optimizer is not enabled on this server.", true);
                                                return true;
                                             } else if (args.length < 2) {
                                                Help.sendMsg(player,
                                                      "§cUsage: /lagx villagers [status|reload|enable|disable|optimize|stats]",
                                                      true);
                                                return true;
                                             } else {
                                                String subCommand = args[1].toLowerCase();
                                                switch (subCommand) {
                                                   case "status":
                                                   case "s":
                                                      Help.sendMsg(player, "§6Villager Optimizer Status:", true);
                                                      Help.sendMsg(player, this.villagerOptimizer.getStatus(), false);
                                                      return true;
                                                   case "reload":
                                                   case "r":
                                                      this.villagerOptimizer.reload();
                                                      Help.sendMsg(player,
                                                            "§aVillager Optimizer configuration reloaded!", true);
                                                      return true;
                                                   case "enable":
                                                   case "e":
                                                      if (this.villagerOptimizer.isEnabled()) {
                                                         Help.sendMsg(player,
                                                               "§eVillager Optimizer is already enabled.", true);
                                                         return true;
                                                      }

                                                      this.getConfig().set("villager_optimization.enabled", true);
                                                      this.saveConfig();
                                                      this.villagerOptimizer.reload();
                                                      Help.sendMsg(player, "§aVillager Optimizer has been enabled.",
                                                            true);
                                                      return true;
                                                   case "disable":
                                                   case "d":
                                                      if (!this.villagerOptimizer.isEnabled()) {
                                                         Help.sendMsg(player,
                                                               "§eVillager Optimizer is already disabled.", true);
                                                         return true;
                                                      }

                                                      this.getConfig().set("villager_optimization.enabled", false);
                                                      this.saveConfig();
                                                      this.villagerOptimizer.reload();
                                                      Help.sendMsg(player, "§cVillager Optimizer has been disabled.",
                                                            true);
                                                      return true;
                                                   case "optimize":
                                                   case "o":
                                                      World targetWorld = null;
                                                      if (args.length >= 3) {
                                                         targetWorld = Bukkit.getWorld(args[2]);
                                                         if (targetWorld == null) {
                                                            Help.sendMsg(player, "§cWorld not found: " + args[2], true);
                                                            return true;
                                                         }
                                                      } else {
                                                         if (player == null) {
                                                            Help.sendMsg(player,
                                                                  "§cPlease specify a world name when running from console.",
                                                                  true);
                                                            return true;
                                                         }

                                                         targetWorld = player.getWorld();
                                                      }

                                                      this.optimizeVillagersInWorld(targetWorld, player);
                                                      return true;
                                                   case "stats":
                                                      Help.sendMsg(player, "§6Villager Statistics by World:", true);
                                                      int totalVillagers = 0;

                                                      for (World worldxxx : Bukkit.getWorlds()) {
                                                         int villagerCount = 0;
                                                         int chunkCount = 0;
                                                         Map<Chunk, Integer> villagersPerChunk = new HashMap<>();

                                                         for (Entity entity : worldxxx.getEntities()) {
                                                            if (entity instanceof Villager) {
                                                               villagerCount++;
                                                               totalVillagers++;
                                                               Chunk chunk = entity.getLocation().getChunk();
                                                               villagersPerChunk.put(chunk,
                                                                     villagersPerChunk.getOrDefault(chunk, 0) + 1);
                                                            }
                                                         }

                                                         chunkCount = villagersPerChunk.size();
                                                         int maxInChunk = 0;

                                                         for (int count : villagersPerChunk.values()) {
                                                            maxInChunk = Math.max(maxInChunk, count);
                                                         }

                                                         if (villagerCount > 0) {
                                                            Help.sendMsg(
                                                                  player,
                                                                  "§e"
                                                                        + worldxxx.getName()
                                                                        + ": §b"
                                                                        + villagerCount
                                                                        + " §evillagers in §b"
                                                                        + chunkCount
                                                                        + " §echunks (max §b"
                                                                        + maxInChunk
                                                                        + " §eper chunk)",
                                                                  false);
                                                         }
                                                      }

                                                      Help.sendMsg(player,
                                                            "§eTotal villagers across all worlds: §b" + totalVillagers,
                                                            false);
                                                      return true;
                                                   default:
                                                      Help.sendMsg(
                                                            player,
                                                            "§cUnknown subcommand. Available options: status, reload, enable, disable, optimize, stats",
                                                            true);
                                                      return true;
                                                }
                                             }
                                          } else if (!hasPermission(player, "lagx.preset")) {
                                             Help.sendMsg(player, "§cYou don't have permission to use this command.",
                                                   true);
                                             return true;
                                          } else if (this.entityLimiter == null) {
                                             Help.sendMsg(player, "§cEntity Limiter is not enabled on this server.",
                                                   true);
                                             return true;
                                          } else if (args.length < 2) {
                                             Help.sendMsg(player, "§cUsage: /lagx preset [info|set]", true);
                                             return true;
                                          } else {
                                             String subCommand = args[1].toLowerCase();
                                             switch (subCommand) {
                                                case "info":
                                                case "i":
                                                   Help.sendMsg(player, "§6Entity Limiter Preset Information:", true);
                                                   Help.sendMsg(player, this.entityLimiter.getStatus(), false);
                                                   return true;
                                                case "set":
                                                case "s":
                                                   if (args.length < 3) {
                                                      Help.sendMsg(player,
                                                            "§cUsage: /lagx preset set <basic|advanced|custom>", true);
                                                      return true;
                                                   } else {
                                                      String presetType = args[2].toLowerCase();
                                                      if (!presetType.equals("basic") && !presetType.equals("advanced")
                                                            && !presetType.equals("custom")) {
                                                         Help.sendMsg(player,
                                                               "§cInvalid preset type. Use 'basic', 'advanced', or 'custom'.",
                                                               true);
                                                         return true;
                                                      }

                                                      this.getConfig().set("entity_limiter.preset_mode", presetType);
                                                      this.saveConfig();
                                                      this.entityLimiter.reload();
                                                      Help.sendMsg(
                                                            player, "§aEntity Limiter preset changed to §b" + presetType
                                                                  + "§a and configuration reloaded.",
                                                            true);
                                                      return true;
                                                   }
                                                default:
                                                   Help.sendMsg(player,
                                                         "§cUnknown subcommand. Available options: info, set", true);
                                                   return true;
                                             }
                                          }
                                       } else if (hasPermission(player, "lagx.towny")) {
                                          TownyIntegration towny = getTownyIntegration();
                                          if (towny == null || !towny.isEnabled()) {
                                             Help.sendMsg(player, "§cTowny integration is not enabled on this server.",
                                                   true);
                                             return true;
                                          } else if (args.length < 2) {
                                             Help.sendMsg(player, "§cUsage: /lagx towny [status|info]", true);
                                             return true;
                                          } else {
                                             String subCommand = args[1].toLowerCase();
                                             switch (subCommand) {
                                                case "status":
                                                case "s":
                                                   Help.sendMsg(player, "§6Towny Integration Status:", true);
                                                   Help.sendMsg(player, "§eEnabled: §a" + towny.isEnabled(), false);
                                                   Help.sendMsg(player, "§eVersion: §a" + towny.getVersion(), false);
                                                   return true;
                                                case "info":
                                                case "i":
                                                   if (player == null) {
                                                      Help.sendMsg(player,
                                                            "§cThis command can only be used by players.", true);
                                                      return true;
                                                   }

                                                   Location loc = player.getLocation();
                                                   boolean isProtected = towny.isLocationProtected(loc);
                                                   String townName = towny.getTownAtLocation(loc);
                                                   Help.sendMsg(player, "§6Towny Information for your location:", true);
                                                   Help.sendMsg(player,
                                                         "§eProtected: §" + (isProtected ? "a" : "c") + isProtected,
                                                         false);
                                                   Help.sendMsg(player,
                                                         "§eTown: §a" + (townName != null ? townName : "None"), false);
                                                   return true;
                                                default:
                                                   Help.sendMsg(player,
                                                         "§cUnknown subcommand. Available options: status, info", true);
                                                   return true;
                                             }
                                          }
                                       } else {
                                          Help.sendMsg(player, "§cYou don't have permission to use this command.",
                                                true);
                                          return true;
                                       }
                                    } else if (!hasPermission(player, "lagx.entities")) {
                                       Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                                       return true;
                                    } else if (args.length < 2) {
                                       Help.sendMsg(player, "§cUsage: /lagx entities [info|count|stats]", true);
                                       return true;
                                    } else {
                                       String subCommand = args[1].toLowerCase();
                                       switch (subCommand) {
                                          case "info":
                                          case "i":
                                             if (this.entityLimiter != null && this.entityLimiter.isEnabled()) {
                                                Help.sendMsg(player, "§6Entity Limiter Configuration:", true);
                                                Help.sendMsg(player, this.entityLimiter.getStatus(), false);
                                                return true;
                                             }

                                             Help.sendMsg(player, "§cEntity Limiter is not enabled on this server.",
                                                   true);
                                             return true;
                                          case "count":
                                          case "c":
                                             Map<String, Integer> entityCounts = new HashMap<>();
                                             int totalEntities = 0;

                                             for (World worldxxx : Bukkit.getWorlds()) {
                                                for (Entity entityx : worldxxx.getEntities()) {
                                                   String type = entityx.getType().toString();
                                                   entityCounts.put(type, entityCounts.getOrDefault(type, 0) + 1);
                                                   totalEntities++;
                                                }
                                             }

                                             Help.sendMsg(player, "§6Entity Count by Type:", true);
                                             Help.sendMsg(player, "§eTotal Entities: §b" + totalEntities, false);
                                             List<Entry<String, Integer>> sortedEntities = new ArrayList<>(
                                                   entityCounts.entrySet());
                                             sortedEntities.sort(Entry.<String, Integer>comparingByValue().reversed());
                                             int shown = 0;

                                             for (Entry<String, Integer> entry : sortedEntities) {
                                                if (shown >= 10) {
                                                   break;
                                                }

                                                Help.sendMsg(player, "§e" + entry.getKey() + ": §b" + entry.getValue(),
                                                      false);
                                                shown++;
                                             }

                                             return true;
                                          case "stats":
                                          case "s":
                                             Help.sendMsg(player, "§6Entity Statistics by World:", true);

                                             for (World worldxxx : Bukkit.getWorlds()) {
                                                int entityCount = worldxxx.getEntities().size();
                                                int chunkCount = worldxxx.getLoadedChunks().length;
                                                double avgPerChunk = chunkCount > 0
                                                      ? (double) entityCount / (double) chunkCount
                                                      : 0.0;
                                                Help.sendMsg(
                                                      player,
                                                      "§e"
                                                            + worldxxx.getName()
                                                            + ": §b"
                                                            + entityCount
                                                            + " §eentities in §b"
                                                            + chunkCount
                                                            + " §echunks (avg: §b"
                                                            + String.format("%.2f", avgPerChunk)
                                                            + " §eper chunk)",
                                                      false);
                                             }

                                             return true;
                                          default:
                                             Help.sendMsg(player,
                                                   "§cUnknown subcommand. Available options: info, count, stats", true);
                                             return true;
                                       }
                                    }
                                 } else if (!hasPermission(player, "lagx.stacker")) {
                                    Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                                    return true;
                                 } else if (args.length < 2) {
                                    Help.sendMsg(player, "§cUsage: /lagx stacker [info|debug|reload|stack]", true);
                                    return true;
                                 } else {
                                    String subCommand = args[1].toLowerCase();
                                    if (this.entityStacker == null) {
                                       Help.sendMsg(player, "§cEntity Stacker is not enabled on this server.", true);
                                       return true;
                                    } else {
                                       switch (subCommand) {
                                          case "info":
                                             Help.sendMsg(player, "§6Entity Stacker Statistics:", true);
                                             Help.sendMsg(player, "§eEnabled: §a" + this.entityStacker.isEnabled(),
                                                   false);
                                             Help.sendMsg(player,
                                                   "§eSingle Kill Mode: §a" + this.entityStacker.isSingleKillEnabled(),
                                                   false);
                                             Help.sendMsg(player,
                                                   "§eStacked Items: §a" + this.entityStacker.getStackedItemsCount(),
                                                   false);
                                             Help.sendMsg(player, "§eStacked Entities: §a"
                                                   + this.entityStacker.getStackedEntitiesCount(), false);
                                             return true;
                                          case "debug":
                                             Help.sendMsg(player, this.entityStacker.getDebugInfo(), true);
                                             return true;
                                          case "reload":
                                             this.entityStacker.reloadConfig();
                                             Help.sendMsg(player, "§aEntity Stacker configuration reloaded!", true);
                                             return true;
                                          case "stack":
                                             if (player == null) {
                                                Help.sendMsg(player, "§cThis command can only be used by players.",
                                                      true);
                                                return true;
                                             } else {
                                                int radius = 50;
                                                if (args.length >= 3) {
                                                   try {
                                                      radius = Integer.parseInt(args[2]);
                                                      if (radius <= 0 || radius > 200) {
                                                         Help.sendMsg(player, "§cRadius must be between 1 and 200.",
                                                               true);
                                                         return true;
                                                      }
                                                   } catch (NumberFormatException var39) {
                                                      Help.sendMsg(player,
                                                            "§cInvalid radius value. Please enter a number.", true);
                                                      return true;
                                                   }
                                                }

                                                int stacksCreated = this.entityStacker
                                                      .stackEntitiesInRadius(player.getLocation(), radius);
                                                Help.sendMsg(
                                                      player,
                                                      "§aSuccessfully stacked entities in a " + radius
                                                            + " block radius. Created " + stacksCreated + " stacks.",
                                                      true);
                                                return true;
                                             }
                                          default:
                                             Help.sendMsg(player,
                                                   "§cUnknown subcommand. Available options: info, debug, reload, stack",
                                                   true);
                                             return true;
                                       }
                                    }
                                 }
                              } else if (!hasPermission(player, "lagx.ping")) {
                                 Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                                 return true;
                              } else {
                                 Player target = player;
                                 if (args.length >= 2) {
                                    target = Bukkit.getPlayerExact(args[1]);
                                 }

                                 if (target == null) {
                                    Help.sendMsg(player, "§cPlayer not found.", true);
                                    return true;
                                 } else {
                                    int ping = 0;

                                    try {
                                       ping = target.getPing();
                                    } catch (Throwable var34) {
                                    }

                                    Help.sendMsg(player, "§ePing for §b" + target.getName() + "§e: §b" + ping + "ms",
                                          true);
                                    return true;
                                 }
                              }
                           } else if (!hasPermission(player, "lagx.protocol")) {
                              Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                              return true;
                           } else if (args.length == 1 || args[1].equalsIgnoreCase("list")
                                 || args[1].equalsIgnoreCase("l")) {
                              String list = Protocol.getProtocols().stream().map(LRProtocol::id)
                                    .reduce((a, b) -> a + ", " + b).orElse("none");
                              Help.sendMsg(player, "§eProtocols: §b" + list, true);
                              return true;
                           } else if (!args[1].equalsIgnoreCase("run") && !args[1].equalsIgnoreCase("r")) {
                              Help.sendMsg(player, "§cUsage: /lagx protocol [list|run]", true);
                              return true;
                           } else if (args.length < 3) {
                              Help.sendMsg(player, "§cUsage: /lagx protocol run <id> [count:true|false]", true);
                              return true;
                           } else {
                              LRProtocol proto = Protocol.getProtocol(args[2]);
                              if (proto == null) {
                                 Help.sendMsg(player, "§cUnknown protocol: " + args[2], true);
                                 return true;
                              } else {
                                 boolean count = true;
                                 if (args.length >= 4) {
                                    count = Boolean.parseBoolean(args[3]);
                                 }

                                 try {
                                    LRProtocolResult res = Protocol.run(proto, new Object[] { count });
                                    if (res != null) {
                                       Help.sendProtocolResultInfo(player, res);
                                    } else {
                                       Help.sendMsg(player, "§eProtocol executed.", true);
                                    }
                                 } catch (Throwable var37) {
                                    Help.sendMsg(player, "§cProtocol error: " + var37.getMessage(), true);
                                 }

                                 return true;
                              }
                           }
                        } else if (!hasPermission(player, "lagx.unload")) {
                           Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                           return true;
                        } else {
                           World worldxxx = null;
                           if (args.length >= 2) {
                              worldxxx = Bukkit.getWorld(args[1]);
                           } else if (player != null) {
                              worldxxx = player.getWorld();
                           }

                           if (worldxxx == null) {
                              Help.sendMsg(player, "§cWorld not found. Usage: /lagx unload <world>", true);
                              return true;
                           } else {
                              World targetx = worldxxx;
                              int[] attempted = new int[] { 0 };

                              for (Chunk chunk : worldxxx.getLoadedChunks()) {
                                 attempted[0]++;
                                 Bukkit.getRegionScheduler()
                                       .run(this, targetx, chunk.getX(), chunk.getZ(),
                                             task -> targetx.unloadChunkRequest(chunk.getX(), chunk.getZ()));
                              }

                              Help.sendMsg(player,
                                    "§aRequested unload for §b" + attempted[0] + "§a chunks in §e" + targetx.getName(),
                                    true);
                              return true;
                           }
                        }
                     } else if (!hasPermission(player, "lagx.world")) {
                        Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                        return true;
                     } else {
                        World worldxxxx = null;
                        if (args.length >= 2) {
                           worldxxxx = Bukkit.getWorld(args[1]);
                        } else if (player != null) {
                           worldxxxx = player.getWorld();
                        }

                        if (worldxxxx == null) {
                           Help.sendMsg(player, "§cWorld not found. Usage: /lagx world <world>", true);
                           return true;
                        } else {
                           int chunkCount = worldxxxx.getLoadedChunks().length;
                           int entityCount = worldxxxx.getEntities().size();
                           Help.sendMsg(player, "§6World §e" + worldxxxx.getName() + ": §eChunks=§b" + chunkCount
                                 + " §eEntities=§b" + entityCount, true);
                           return true;
                        }
                     }
                  } else {
                     if (hasPermission(player, args[0].equalsIgnoreCase("status") ? "lagx.status" : "lagx.master")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("§6Server Status:\n");
                        sb.append("§eTPS: §b").append(TickPerSecond.format()).append("\n");
                        int players = Bukkit.getOnlinePlayers().size();
                        sb.append("§ePlayers: §b").append(players).append("\n");
                        int chunks = 0;
                        int entities = 0;

                        for (World w : Bukkit.getWorlds()) {
                           chunks += w.getLoadedChunks().length;
                           entities += w.getEntities().size();
                        }

                        sb.append("§eLoaded Chunks: §b").append(chunks).append("  §eEntities: §b").append(entities);
                        Help.sendMsg(player, sb.toString(), true);
                     } else {
                        Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                     }

                     return true;
                  }
               } else {
                  if (hasPermission(player, "lagx.help")) {
                     String ver = this.getPluginMeta().getVersion();
                     Help.sendMsg(player, "§eLagX §7version §b" + ver, true);
                     Help.sendMsg(player, "§eModules Loaded: §b" + loaded.size(), false);
                     Help.sendMsg(
                           player,
                           "§eFeatures: §7Limiter="
                                 + this.entityLimiter.isEnabled()
                                 + ", Villagers="
                                 + this.villagerOptimizer.isEnabled()
                                 + ", Stacker="
                                 + this.entityStacker.isEnabled()
                                 + ", ItemFrames="
                                 + this.itemFrameOptimizer.isEnabled()
                                 + ", DeathProt="
                                 + this.playerDeathTracker.isEnabled(),
                           false);
                  } else {
                     Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
                  }

                  return true;
               }
            } else if (hasPermission(player, "lagx.help")) {
               if (args.length == 2) {
                  try {
                     int page = Integer.parseInt(args[1]);
                     if (Help.isValidPage(page)) {
                        Help.send(player, page);
                     } else {
                        Help.sendMsg(player,
                              "§cPage #" + page + " does not exist. Valid pages: 1-" + Help.getTotalPages(), true);
                     }
                  } catch (NumberFormatException var38) {
                     Help.sendMsg(player, "§cPlease enter a valid page number.", true);
                  }

                  return true;
               } else {
                  Help.send(player, 1);
                  return true;
               }
            } else {
               Help.sendMsg(player, "§cYou don't have permission to use this command.", true);
               return true;
            }
         } else {
            return true;
         }
      }
      // Default false when command name doesn't match "lagx"
      return false;
   }

   public static void broadcast(String msg) {
      for (Player p : Bukkit.getOnlinePlayers()) {
         p.sendMessage(msg);
      }
   }

   public static void broadcastWarn(String msg) {
      boolean enabled = true;

      try {
         enabled = getInstance().getConfig().getBoolean("protocol_warnings.enabled", true);
      } catch (Throwable var4) {
      }

      if (enabled) {
         for (Player p : Bukkit.getOnlinePlayers()) {
            if (hasPermission(p, "lagx.warn")) {
               p.sendMessage(msg);
            }
         }
      }
   }

   public static boolean areWarningsEnabled() {
      try {
         return getInstance().getConfig().getBoolean("protocol_warnings.enabled", true);
      } catch (Throwable var1) {
         return true;
      }
   }

   public static void setWarningsEnabled(boolean enabled) {
      try {
         HBZCleaner plugin = getInstance();
         plugin.getConfig().set("protocol_warnings.enabled", enabled);
         plugin.saveConfig();
      } catch (Throwable var2) {
      }
   }

   public static String[] getData(Module m) {
      return loaded.get(m);
   }

   private void optimizeVillagersInWorld(World world, CommandSender sender) {
      if (world != null && this.villagerOptimizer != null) {
         AtomicInteger optimized = new AtomicInteger(0);
         AtomicInteger processed = new AtomicInteger(0);
         int totalVillagers = this.countVillagers(world);
         Help.sendMsg(sender, "§eStarting villager optimization in world §b" + world.getName() + "§e...", true);

         for (Entity entity : world.getEntities()) {
            if (entity instanceof Villager villager) {
               Bukkit.getRegionScheduler().execute(this, villager.getLocation(), () -> {
                  if (!villager.isAdult()) {
                     processed.incrementAndGet();
                     this.checkProgress(sender, optimized.get(), processed.get(), totalVillagers, world);
                  } else {
                     try {
                        if (!villager.getPersistentDataContainer().has(new NamespacedKey(this, "hbz_optimized"),
                              PersistentDataType.INTEGER)) {
                           villager.setAI(false);
                           Bukkit.getRegionScheduler().runDelayed(this, villager.getLocation(), task -> {
                              if (villager.isValid()) {
                                 villager.setAI(true);
                                 villager.getPersistentDataContainer().set(new NamespacedKey(this, "hbz_optimized"),
                                       PersistentDataType.INTEGER, 1);
                              }

                              optimized.incrementAndGet();
                              processed.incrementAndGet();
                              this.checkProgress(sender, optimized.get(), processed.get(), totalVillagers, world);
                           }, 1L);
                        } else {
                           processed.incrementAndGet();
                           this.checkProgress(sender, optimized.get(), processed.get(), totalVillagers, world);
                        }
                     } catch (Exception var8x) {
                        processed.incrementAndGet();
                        this.getLogger().warning("Failed to optimize villager: " + var8x.getMessage());
                        this.checkProgress(sender, optimized.get(), processed.get(), totalVillagers, world);
                     }
                  }
               });
            }
         }
      } else {
         if (sender != null) {
            Help.sendMsg(sender, "§cCannot optimize villagers: world is null or villager optimizer is not available",
                  true);
         }
      }
   }

   private int countVillagers(World world) {
      if (world == null) {
         return 0;
      } else {
         int count = 0;

         for (Entity entity : world.getEntities()) {
            if (entity instanceof Villager) {
               count++;
            }
         }

         return count;
      }
   }

   private void checkProgress(CommandSender sender, int optimized, int processed, int total, World world) {
      if (processed >= total && sender != null) {
         Help.sendMsg(sender, "§aManually optimized §b" + optimized + "§a villagers in world §e" + world.getName(),
               true);
      }
   }

   public void autoLagRemovalLoop() {
      if (!HBZConfig.autoLagRemoval) {
         this.getLogger().info("Auto lag removal loop canceled - disabled in config");
      } else {
         Bukkit.getAsyncScheduler()
               .runDelayed(
                     this,
                     task -> {
                        this.getLogger().info("Preparing for scheduled cleanup...");
                        final Set<String> reportedProtocols = new HashSet<>();

                        for (LRProtocol p : HBZConfig.periodic_protocols.keySet()) {
                           DoubleVar<Object[], Boolean> dat = HBZConfig.periodic_protocols.get(p);
                           final String protocolId = p.id();
                           if (dat.getVar2()) {
                              Protocol.rund(
                                    p,
                                    dat.getVar1(),
                                    new DelayedLRProtocolResult() {
                                       @Override
                                       public void receive(LRProtocolResult result) {
                                          if (result != null && result.getData() != null && result.getData().length > 0
                                                && !reportedProtocols.contains(protocolId)) {
                                             HBZCleaner.this.getLogger()
                                                   .info(String.format(
                                                         "Scheduled cleanup '%s' completed: %s entities affected",
                                                         protocolId, result.getData()[0]));
                                             reportedProtocols.add(protocolId);
                                          }
                                       }
                                    });
                           } else {
                              LRProtocolResult result = p.run(dat.getVar1());
                              if (result != null && result.getData() != null && result.getData().length > 0
                                    && !reportedProtocols.contains(protocolId)) {
                                 this.getLogger()
                                       .info(String.format("Scheduled cleanup '%s' completed: %s entities affected",
                                             protocolId, result.getData()[0]));
                                 reportedProtocols.add(protocolId);
                              }
                           }
                        }

                        this.autoLagRemovalLoop();
                     },
                     1200L * (long) HBZConfig.autoLagRemovalTime * 50L,
                     TimeUnit.MILLISECONDS);
      }
   }

   public static TownyIntegration getTownyIntegration() {
      return townyIntegration;
   }

   public static boolean hasPermission(Player player, String permission) {
      if (player == null) {
         return true;
      } else if (player.isOp()) {
         return true;
      } else if (player.hasPermission(permission)) {
         return true;
      } else {
         return player.hasPermission("lagx.*") ? true : player.hasPermission("lagx.admin");
      }
   }
}
