package com.seristic.hbzcleaner.commands;

import com.seristic.hbzcleaner.main.HBZCleaner;
import com.seristic.hbzcleaner.util.ColorUtil;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

public class PerformanceCommand implements CommandExecutor, TabCompleter {
   private final HBZCleaner plugin;
   private final String header = "§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r";
   private final String footer = "§8§m-------------------------------------------------§r";
   private final int MAX_HISTORY = 60;
   private final List<Double> tpsHistory = new ArrayList<>();
   private long lastMeasurement = 0L;
   private final Map<String, List<Double>> regionTpsHistory = new HashMap<>();

   public PerformanceCommand(HBZCleaner plugin) {
      this.plugin = plugin;
      Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> this.updateTpsHistory(), 20L, 20L);
   }

   private void updateTpsHistory() {
      try {
         double currentTps = Bukkit.getTPS()[0];
         synchronized (this.tpsHistory) {
            this.tpsHistory.add(currentTps);

            while (this.tpsHistory.size() > 60) {
               this.tpsHistory.remove(0);
            }
         }
      } catch (Exception var16) {
         this.plugin.getLogger().fine("Could not get TPS: " + var16.getMessage());
         synchronized (this.tpsHistory) {
            double fallbackTps = this.tpsHistory.isEmpty() ? 20.0 : this.tpsHistory.get(this.tpsHistory.size() - 1);
            this.tpsHistory.add(fallbackTps);

            while (this.tpsHistory.size() > 60) {
               this.tpsHistory.remove(0);
            }
         }
      }

      try {
         for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            synchronized (this.regionTpsHistory) {
               if (!this.regionTpsHistory.containsKey(worldName)) {
                  this.regionTpsHistory.put(worldName, new ArrayList<>());
               }

               List<Double> worldHistory = this.regionTpsHistory.get(worldName);
               double worldTps;
               synchronized (this.tpsHistory) {
                  worldTps = this.tpsHistory.isEmpty() ? 20.0 : this.tpsHistory.get(this.tpsHistory.size() - 1);
               }

               worldHistory.add(worldTps);

               while (worldHistory.size() > 60) {
                  worldHistory.remove(0);
               }
            }
         }
      } catch (Exception var13) {
         this.plugin.getLogger().fine("Failed to update region TPS history: " + var13.getMessage());
      }

      this.lastMeasurement = System.currentTimeMillis();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("lagxperf.use") && !sender.hasPermission("lagx.performance")) {
         sender.sendMessage(ColorUtil.color("&cYou don't have permission to use this command."));
         return true;
      } else if (args.length == 0) {
         this.showSummary(sender);
         return true;
      } else {
         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "regions":
               if (!sender.hasPermission("lagxperf.regions") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view region performance data."));
                  return true;
               }

               this.showRegionStats(sender);
               break;
            case "memory":
               if (!sender.hasPermission("lagxperf.memory") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view memory statistics."));
                  return true;
               }

               this.showMemoryStats(sender);
               break;
            case "threads":
               if (!sender.hasPermission("lagxperf.threads") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view thread information."));
                  return true;
               }

               this.showThreadStats(sender);
               break;
            case "world":
               if (!sender.hasPermission("lagxperf.world") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view world statistics."));
                  return true;
               }

               if (args.length > 1) {
                  this.showWorldStats(sender, args[1]);
               } else {
                  sender.sendMessage(ColorUtil.color("&cPlease specify a world name"));
               }
               break;
            case "history":
               if (!sender.hasPermission("lagxperf.history") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view TPS history."));
                  return true;
               }

               this.showTpsHistory(sender);
               break;
            case "full":
               if (!sender.hasPermission("lagxperf.full") && !sender.hasPermission("lagx.performance")) {
                  sender.sendMessage(ColorUtil.color("&cYou don't have permission to view full performance data."));
                  return true;
               }

               this.showFullStats(sender);
               break;
            case "help":
            default:
               this.showHelp(sender);
         }

         return true;
      }
   }

   private void showSummary(CommandSender sender) {
      String customHeader = "§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r";
      sender.sendMessage(customHeader);
      sender.sendMessage(ColorUtil.color("&bServer Health Report"));
      int onlinePlayers = Bukkit.getOnlinePlayers().size();
      sender.sendMessage(ColorUtil.color("&b— Online Players: &3" + onlinePlayers));
      int totalRegions = this.countActiveRegions();
      sender.sendMessage(ColorUtil.color("&b— Total regions: &3" + totalRegions));
      double systemLoad = this.getSystemCpuLoad();
      sender.sendMessage(ColorUtil.color("&b— Utilisation: &3" + String.format("%.2f", systemLoad * 100.0) + "% &b/ &3100.0%"));
      double[] tps = Bukkit.getTPS();
      double loadRate = 20.0 - tps[0];
      if (loadRate < 0.0) {
         loadRate = 0.0;
      }

      sender.sendMessage(
         ColorUtil.color("&b— Load rate: &3" + String.format("%.2f", loadRate) + "&b, Gen rate: &3" + String.format("%.2f", this.estimateChunkGenRate()))
      );
      sender.sendMessage(ColorUtil.color("&b— Lowest Region TPS: &3" + this.formatTps(this.getLowestRegionTps())));
      sender.sendMessage(ColorUtil.color("&b— Median Region TPS: &3" + this.formatTps(this.getMedianRegionTps())));
      sender.sendMessage(ColorUtil.color("&b— Highest Region TPS: &3" + this.formatTps(this.getHighestRegionTps())));
      sender.sendMessage(ColorUtil.color("&bHighest 3 utilisation regions:"));
      Map<String, PerformanceCommand.RegionStats> regionStats = this.getRegionStats();
      List<Entry<String, PerformanceCommand.RegionStats>> sortedRegions = regionStats.entrySet()
         .stream()
         .sorted((a, b) -> Double.compare(b.getValue().utilization, a.getValue().utilization))
         .limit(3L)
         .collect(Collectors.toList());
      int count = 0;

      for (Entry<String, PerformanceCommand.RegionStats> entry : sortedRegions) {
         PerformanceCommand.RegionStats stats = entry.getValue();
         String worldName = stats.world;
         int blockX = stats.x;
         int blockZ = stats.z;
         if (sender instanceof Player) {
            TextComponent message = new TextComponent(ColorUtil.color("&b— Region around block "));
            TextComponent regionCoords = new TextComponent(ColorUtil.color("&3[" + worldName + "," + blockX + "," + blockZ + "]"));
            regionCoords.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtil.color("&bClick to teleport to this region"))));
            regionCoords.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/tp " + blockX + " 100 " + blockZ));
            message.addExtra(regionCoords);
            ((Player)sender).spigot().sendMessage(new BaseComponent[]{message});
         } else {
            sender.sendMessage(ColorUtil.color("&b— Region around block &3[" + worldName + "," + blockX + "," + blockZ + "]"));
         }

         sender.sendMessage(
            ColorUtil.color(
               "  &3"
                  + String.format("%.1f", stats.utilization * 100.0)
                  + "% &butil at &3"
                  + String.format("%.2f", stats.mspt)
                  + " &bMSPT at &3"
                  + this.formatTps(stats.tps)
                  + " &bTPS"
            )
         );
         sender.sendMessage(ColorUtil.color("  &bChunks: &3" + stats.chunks + " &b| Players: &3" + stats.players + " &b| Entities: &3" + stats.entities));
         count++;
      }

      while (count < 3) {
         sender.sendMessage(ColorUtil.color("&b— &8No additional active regions"));
         count++;
      }

      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private void showRegionStats(CommandSender sender) {
      sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
      sender.sendMessage(ColorUtil.color("&b&lServer Health Report"));

      try {
         int onlinePlayers = Bukkit.getOnlinePlayers().size();
         sender.sendMessage(ColorUtil.color("&b— Online Players: &3" + onlinePlayers));
         int totalRegions = this.countActiveRegions();
         sender.sendMessage(ColorUtil.color("&b— Total regions: &3" + totalRegions));
         double systemLoad = this.getSystemCpuLoad();
         sender.sendMessage(ColorUtil.color("&b— Utilisation: &3" + String.format("%.2f", systemLoad * 100.0) + "% &b/ &3100.0%"));
         double[] tps = Bukkit.getTPS();
         double loadRate = 20.0 - tps[0];
         if (loadRate < 0.0) {
            loadRate = 0.0;
         }

         sender.sendMessage(
            ColorUtil.color("&b— Load rate: &3" + String.format("%.2f", loadRate) + "&b, Gen rate: &3" + String.format("%.2f", this.estimateChunkGenRate()))
         );
         sender.sendMessage(ColorUtil.color("&b— Lowest Region TPS: &3" + this.formatTps(this.getLowestRegionTps())));
         sender.sendMessage(ColorUtil.color("&b— Median Region TPS: &3" + this.formatTps(this.getMedianRegionTps())));
         sender.sendMessage(ColorUtil.color("&b— Highest Region TPS: &3" + this.formatTps(this.getHighestRegionTps())));
         sender.sendMessage(ColorUtil.color("&3Highest 3 utilisation regions:"));
         Map<String, PerformanceCommand.RegionStats> regionStats = this.getRegionStats();
         int count = 0;

         for (Entry<String, PerformanceCommand.RegionStats> entry : regionStats.entrySet()
            .stream()
            .sorted((a, b) -> Double.compare(b.getValue().utilization, a.getValue().utilization))
            .limit(3L)
            .collect(Collectors.toList())) {
            PerformanceCommand.RegionStats stats = entry.getValue();
            String worldName = stats.world;
            int blockX = stats.x;
            int blockZ = stats.z;
            if (sender instanceof Player) {
               TextComponent message = new TextComponent(ColorUtil.color("&b— Region around block "));
               TextComponent regionCoords = new TextComponent(ColorUtil.color("&3[" + worldName + "," + blockX + "," + blockZ + "]"));
               regionCoords.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtil.color("&bClick to teleport to this region"))));
               regionCoords.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/tp " + blockX + " 100 " + blockZ));
               message.addExtra(regionCoords);
               ((Player)sender).spigot().sendMessage(new BaseComponent[]{message});
            } else {
               sender.sendMessage(ColorUtil.color("&b— Region around block &3[" + worldName + "," + blockX + "," + blockZ + "]"));
            }

            sender.sendMessage(
               ColorUtil.color(
                  "  &3"
                     + String.format("%.1f", stats.utilization * 100.0)
                     + "% &butil at &3"
                     + String.format("%.2f", stats.mspt)
                     + " &bMSPT at &3"
                     + this.formatTps(stats.tps)
                     + " &bTPS"
               )
            );
            sender.sendMessage(ColorUtil.color("  &bChunks: &3" + stats.chunks + " &b| Players: &3" + stats.players + " &b| Entities: &3" + stats.entities));
            count++;
         }

         while (count < 3) {
            sender.sendMessage(ColorUtil.color("&b— &8No additional active regions"));
            count++;
         }
      } catch (Exception var19) {
         sender.sendMessage(ColorUtil.color("&cError retrieving region information: " + var19.getMessage()));
         var19.printStackTrace();
      }

      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private void showMemoryStats(CommandSender sender) {
      sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
      sender.sendMessage(ColorUtil.color("&6&lDetailed Memory Statistics"));
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
      MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
      long heapInit = heapUsage.getInit() / 1024L / 1024L;
      long heapUsed = heapUsage.getUsed() / 1024L / 1024L;
      long heapCommitted = heapUsage.getCommitted() / 1024L / 1024L;
      long heapMax = heapUsage.getMax() / 1024L / 1024L;
      double heapPercentage = (double)heapUsed / (double)heapMax * 100.0;
      sender.sendMessage(ColorUtil.color("&6Heap Memory:"));
      sender.sendMessage(ColorUtil.color("&7  • &6Used: &f" + heapUsed + " MB &7(" + String.format("%.1f", heapPercentage) + "%)"));
      sender.sendMessage(ColorUtil.color("&7  • &6Committed: &f" + heapCommitted + " MB"));
      sender.sendMessage(ColorUtil.color("&7  • &6Max: &f" + heapMax + " MB"));
      sender.sendMessage(ColorUtil.color("&7  • &6Initial: &f" + heapInit + " MB"));
      long nonHeapInit = nonHeapUsage.getInit() / 1024L / 1024L;
      long nonHeapUsed = nonHeapUsage.getUsed() / 1024L / 1024L;
      long nonHeapCommitted = nonHeapUsage.getCommitted() / 1024L / 1024L;
      long nonHeapMax = nonHeapUsage.getMax() / 1024L / 1024L;
      sender.sendMessage(ColorUtil.color("&6Non-Heap Memory:"));
      sender.sendMessage(ColorUtil.color("&7  • &6Used: &f" + nonHeapUsed + " MB"));
      sender.sendMessage(ColorUtil.color("&7  • &6Committed: &f" + nonHeapCommitted + " MB"));
      if (nonHeapMax > 0L) {
         double nonHeapPercentage = (double)nonHeapUsed / (double)nonHeapMax * 100.0;
         sender.sendMessage(ColorUtil.color("&7  • &6Max: &f" + nonHeapMax + " MB &7(" + String.format("%.1f", nonHeapPercentage) + "%)"));
      } else {
         sender.sendMessage(ColorUtil.color("&7  • &6Max: &fUnbounded"));
      }

      sender.sendMessage(ColorUtil.color("&7  • &6Initial: &f" + nonHeapInit + " MB"));
      sender.sendMessage(ColorUtil.color("&6Last GC Run: &f" + this.getLastGCTime()));
      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private void showThreadStats(CommandSender sender) {
      sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
      sender.sendMessage(ColorUtil.color("&6&lThread Statistics"));
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      int threadCount = threadBean.getThreadCount();
      int peakThreadCount = threadBean.getPeakThreadCount();
      long totalStartedThreads = threadBean.getTotalStartedThreadCount();
      int daemonThreads = threadBean.getDaemonThreadCount();
      sender.sendMessage(ColorUtil.color("&6Current Threads: &f" + threadCount));
      sender.sendMessage(ColorUtil.color("&6Peak Thread Count: &f" + peakThreadCount));
      sender.sendMessage(ColorUtil.color("&6Total Started Threads: &f" + totalStartedThreads));
      sender.sendMessage(ColorUtil.color("&6Daemon Threads: &f" + daemonThreads));

      try {
         sender.sendMessage(ColorUtil.color("&6&lFolia Thread Analysis:"));
         int asyncThreads = 0;
         int regionThreads = 0;
         int nettyThreads = 0;
         int workerThreads = 0;
         ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();

         ThreadGroup parentGroup;
         while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
         }

         Thread[] threads = new Thread[threadBean.getThreadCount() * 2];
         int threadNum = rootGroup.enumerate(threads, true);

         for (int i = 0; i < threadNum; i++) {
            String name = threads[i].getName().toLowerCase();
            if (name.contains("async")) {
               asyncThreads++;
            } else if (name.contains("region")) {
               regionThreads++;
            } else if (name.contains("netty") || name.contains("nio")) {
               nettyThreads++;
            } else if (name.contains("worker")) {
               workerThreads++;
            }
         }

         sender.sendMessage(ColorUtil.color("&7  • &6Async Threads: &f" + asyncThreads));
         sender.sendMessage(ColorUtil.color("&7  • &6Region Threads: &f" + regionThreads));
         sender.sendMessage(ColorUtil.color("&7  • &6Network Threads: &f" + nettyThreads));
         sender.sendMessage(ColorUtil.color("&7  • &6Worker Threads: &f" + workerThreads));
         sender.sendMessage(ColorUtil.color("&7  • &6Other Threads: &f" + (threadCount - asyncThreads - regionThreads - nettyThreads - workerThreads)));
      } catch (Exception var18) {
         sender.sendMessage(ColorUtil.color("&cError retrieving Folia thread information: " + var18.getMessage()));
      }

      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private void showWorldStats(CommandSender sender, String worldName) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         sender.sendMessage(ColorUtil.color("&cWorld '" + worldName + "' not found"));
      } else {
         sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
         sender.sendMessage(ColorUtil.color("&6&lWorld Statistics: &f" + world.getName()));
         sender.sendMessage(ColorUtil.color("&6Environment: &f" + world.getEnvironment().name()));
         sender.sendMessage(ColorUtil.color("&6Loaded Chunks: &f" + world.getLoadedChunks().length));
         Map<String, Integer> entityCounts = new HashMap<>();
         int totalEntities = 0;

         for (Entity entity : world.getEntities()) {
            String type = entity.getType().name();
            entityCounts.put(type, entityCounts.getOrDefault(type, 0) + 1);
            totalEntities++;
         }

         sender.sendMessage(ColorUtil.color("&6Total Entities: &f" + totalEntities));
         if (!entityCounts.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&6Entity Breakdown:"));
            entityCounts.entrySet()
               .stream()
               .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
               .limit(10L)
               .forEach(entry -> sender.sendMessage(ColorUtil.color("&7  • &f" + entry.getKey() + ": &6" + entry.getValue())));
         }

         int playerCount = world.getPlayers().size();
         sender.sendMessage(ColorUtil.color("&6Players: &f" + playerCount));
         sender.sendMessage("§8§m-------------------------------------------------§r");
      }
   }

   private void showTpsHistory(CommandSender sender) {
      sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
      sender.sendMessage(ColorUtil.color("&6&lTPS History (Last 60 seconds)"));
      synchronized (this.tpsHistory) {
         if (this.tpsHistory.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cNo TPS history available yet"));
            sender.sendMessage("§8§m-------------------------------------------------§r");
            return;
         }

         double sum = 0.0;
         double min = Double.MAX_VALUE;
         double max = Double.MIN_VALUE;

         for (double tps : this.tpsHistory) {
            sum += tps;
            min = Math.min(min, tps);
            max = Math.max(max, tps);
         }

         double avg = sum / (double)this.tpsHistory.size();
         double mostRecent = this.tpsHistory.get(this.tpsHistory.size() - 1);
         sender.sendMessage(ColorUtil.color("&6Average TPS: &f" + String.format("%.2f", avg)));
         sender.sendMessage(ColorUtil.color("&6Min TPS: &f" + String.format("%.2f", min) + "  &6Max TPS: &f" + String.format("%.2f", max)));
         StringBuilder trend = new StringBuilder();

         for (double tps : this.sampleTpsHistory(this.tpsHistory, 20)) {
            if (tps >= 19.5) {
               trend.append("&a■");
            } else if (tps >= 18.0) {
               trend.append("&e■");
            } else if (tps >= 15.0) {
               trend.append("&6■");
            } else if (tps >= 10.0) {
               trend.append("&c■");
            } else {
               trend.append("&4■");
            }
         }

         sender.sendMessage(ColorUtil.color("&6TPS Trend: " + trend.toString()));
         sender.sendMessage(ColorUtil.color("&6Most recent TPS reading: " + this.formatTps(mostRecent)));
         long timeSinceUpdate = System.currentTimeMillis() - this.lastMeasurement;
         sender.sendMessage(ColorUtil.color("&6Last updated: &f" + timeSinceUpdate + "ms ago"));
      }

      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private void showFullStats(CommandSender sender) {
      this.showSummary(sender);
      this.showRegionStats(sender);
      this.showMemoryStats(sender);
      this.showThreadStats(sender);
      this.showTpsHistory(sender);
   }

   private void showHelp(CommandSender sender) {
      sender.sendMessage("§8§m-------------§r §6§lLagX Performance§r §8§m-------------§r");
      sender.sendMessage(ColorUtil.color("&6&lLagX Performance Commands"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf &7- Shows a summary of server performance"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf regions &7- Shows detailed region performance"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf memory &7- Shows detailed memory statistics"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf threads &7- Shows thread usage information"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf world <name> &7- Shows stats for a specific world"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf history &7- Shows TPS history and trends"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf full &7- Shows all available performance data"));
      sender.sendMessage(ColorUtil.color("&e/lagxperf help &7- Shows this help message"));
      sender.sendMessage("§8§m-------------------------------------------------§r");
   }

   private String formatTps(double tps) {
      tps = Math.min(tps, 20.0);
      if (tps >= 19.5) {
         return "§a" + String.format("%.2f", tps);
      } else if (tps >= 18.0) {
         return "§e" + String.format("%.2f", tps);
      } else if (tps >= 15.0) {
         return "§6" + String.format("%.2f", tps);
      } else {
         return tps >= 10.0 ? "§c" + String.format("%.2f", tps) : "§4" + String.format("%.2f", tps);
      }
   }

   private double getSystemCpuLoad() {
      try {
         OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
         if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMxBean) {
            return sunOsMxBean.getCpuLoad();
         } else {
            double loadAvg = operatingSystemMXBean.getSystemLoadAverage();
            if (loadAvg >= 0.0) {
               return loadAvg / (double)operatingSystemMXBean.getAvailableProcessors();
            } else {
               double tps = Bukkit.getTPS()[0];
               return 1.0 - tps / 20.0;
            }
         }
      } catch (Exception var6) {
         return 0.0;
      }
   }

   private int countActiveRegions() {
      int count = 0;

      for (World world : Bukkit.getWorlds()) {
         int chunks = world.getLoadedChunks().length;
         count += Math.max(1, chunks / 256);
      }

      return Math.max(1, count);
   }

   private double estimateChunkGenRate() {
      try {
         double systemLoad = this.getSystemCpuLoad();
         double tps = Bukkit.getTPS()[0];
         double genEstimate = systemLoad * (20.0 - tps) / 10.0;
         return Math.min(2.0, Math.max(0.0, genEstimate));
      } catch (Exception var7) {
         return 0.0;
      }
   }

   private Map<String, PerformanceCommand.RegionStats> getRegionStats() {
      Map<String, PerformanceCommand.RegionStats> results = new HashMap<>();

      for (World world : Bukkit.getWorlds()) {
         String worldName = world.getName();
         Chunk[] chunks = world.getLoadedChunks();
         Map<String, List<Chunk>> regionChunks = new HashMap<>();

         for (Chunk chunk : chunks) {
            int regionX = chunk.getX() / 8;
            int regionZ = chunk.getZ() / 8;
            String regionKey = worldName + ":" + regionX + ":" + regionZ;
            regionChunks.computeIfAbsent(regionKey, k -> new ArrayList<>()).add(chunk);
         }

         for (Entry<String, List<Chunk>> entry : regionChunks.entrySet()) {
            String[] regionParts = entry.getKey().split(":");
            int regionX = Integer.parseInt(regionParts[1]);
            int regionZ = Integer.parseInt(regionParts[2]);
            PerformanceCommand.RegionStats stats = new PerformanceCommand.RegionStats(worldName, regionX * 128, regionZ * 128);
            List<Chunk> chunkList = entry.getValue();
            stats.chunks = chunkList.size();
            int totalEntities = 0;
            Set<Player> playersInRegion = new HashSet<>();

            for (Chunk chunk : chunkList) {
               totalEntities += chunk.getEntities().length;

               for (Entity entity : chunk.getEntities()) {
                  if (entity instanceof Player) {
                     playersInRegion.add((Player)entity);
                  }
               }
            }

            stats.entities = totalEntities;
            stats.players = playersInRegion.size();
            double chunkFactor = Math.min(1.0, (double)stats.chunks / 64.0);
            double entityFactor = Math.min(1.0, (double)stats.entities / 400.0);
            double playerFactor = Math.min(1.0, (double)stats.players * 0.25);
            stats.utilization = chunkFactor * 0.3 + entityFactor * 0.4 + playerFactor * 0.3;
            double baseTps = Bukkit.getTPS()[0];
            double tpsVariation = (1.0 - stats.utilization) * 2.0;
            stats.tps = Math.min(20.0, baseTps + tpsVariation);
            double baseMspt = 50.0 * (20.0 - baseTps) / 20.0;
            stats.mspt = baseMspt + stats.utilization * 50.0;
            results.put(entry.getKey(), stats);
         }
      }

      return results;
   }

   private double getLowestRegionTps() {
      Map<String, PerformanceCommand.RegionStats> regions = this.getRegionStats();
      return regions.isEmpty() ? Bukkit.getTPS()[0] : regions.values().stream().mapToDouble(r -> r.tps).min().orElse(Bukkit.getTPS()[0]);
   }

   private double getHighestRegionTps() {
      Map<String, PerformanceCommand.RegionStats> regions = this.getRegionStats();
      return regions.isEmpty() ? 20.0 : regions.values().stream().mapToDouble(r -> r.tps).max().orElse(20.0);
   }

   private double getMedianRegionTps() {
      Map<String, PerformanceCommand.RegionStats> regions = this.getRegionStats();
      if (regions.isEmpty()) {
         return Bukkit.getTPS()[0];
      } else {
         List<Double> tpsList = regions.values().stream().map(r -> r.tps).sorted().collect(Collectors.toList());
         int size = tpsList.size();
         if (size == 0) {
            return Bukkit.getTPS()[0];
         } else {
            return size % 2 == 0 ? (tpsList.get(size / 2 - 1) + tpsList.get(size / 2)) / 2.0 : tpsList.get(size / 2);
         }
      }
   }

   private List<Double> sampleTpsHistory(List<Double> history, int maxSamples) {
      if (history.size() <= maxSamples) {
         return new ArrayList<>(history);
      } else {
         List<Double> sampled = new ArrayList<>();
         double step = (double)history.size() / (double)maxSamples;

         for (int i = 0; i < maxSamples; i++) {
            int index = Math.min(history.size() - 1, (int)Math.floor((double)i * step));
            sampled.add(history.get(index));
         }

         return sampled;
      }
   }

   private String getLastGCTime() {
      try {
         GarbageCollectorMXBean[] garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().toArray(new GarbageCollectorMXBean[0]);
         if (garbageCollectors.length > 0) {
            long maxCollectionCount = 0L;
            String gcName = "Unknown";

            for (GarbageCollectorMXBean gc : garbageCollectors) {
               long collectionCount = gc.getCollectionCount();
               if (collectionCount > maxCollectionCount) {
                  maxCollectionCount = collectionCount;
                  gcName = gc.getName();
               }
            }

            if (maxCollectionCount > 0L) {
               return gcName + " (Count: " + maxCollectionCount + ")";
            }
         }

         return "No GC data available";
      } catch (Exception var11) {
         return "Unknown";
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("lagxperf.use") && !sender.hasPermission("lagx.performance")) {
         return completions;
      } else if (args.length == 1) {
         if (sender.hasPermission("lagxperf.regions") || sender.hasPermission("lagx.performance")) {
            completions.add("regions");
         }

         if (sender.hasPermission("lagxperf.memory") || sender.hasPermission("lagx.performance")) {
            completions.add("memory");
         }

         if (sender.hasPermission("lagxperf.threads") || sender.hasPermission("lagx.performance")) {
            completions.add("threads");
         }

         if (sender.hasPermission("lagxperf.world") || sender.hasPermission("lagx.performance")) {
            completions.add("world");
         }

         if (sender.hasPermission("lagxperf.history") || sender.hasPermission("lagx.performance")) {
            completions.add("history");
         }

         if (sender.hasPermission("lagxperf.full") || sender.hasPermission("lagx.performance")) {
            completions.add("full");
         }

         completions.add("help");
         return completions.stream().filter(c -> c.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else {
         return args.length != 2
               || !args[0].equalsIgnoreCase("world")
               || !sender.hasPermission("lagxperf.world") && !sender.hasPermission("lagx.performance")
            ? completions
            : Bukkit.getWorlds()
               .stream()
               .<String>map(WorldInfo::getName)
               .filter(w -> w.toLowerCase().startsWith(args[1].toLowerCase()))
               .collect(Collectors.toList());
      }
   }

   private static class RegionStats {
      String world;
      int x;
      int z;
      double utilization;
      double mspt;
      double tps;
      int chunks;
      int players;
      int entities;

      public RegionStats(String world, int x, int z) {
         this.world = world;
         this.x = x;
         this.z = z;
      }
   }
}
