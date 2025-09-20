package com.seristic.hbzcleaner.util;

import com.seristic.hbzcleaner.main.TickPerSecond;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ServerMetrics {
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
   private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

   public static String getFormattedTPS() {
      double tps = TickPerSecond.getTPS();
      String color;
      if (tps >= 19.0) {
         color = "§a";
      } else if (tps >= 15.0) {
         color = "§e";
      } else {
         color = "§c";
      }

      return color + DECIMAL_FORMAT.format(tps);
   }

   public static ServerMetrics.RamInfo getRamInfo() {
      Runtime runtime = Runtime.getRuntime();
      long maxMemory = runtime.maxMemory();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long usedMemory = totalMemory - freeMemory;
      long maxMB = maxMemory / 1048576L;
      long usedMB = usedMemory / 1048576L;
      double usagePercent = (double)usedMemory / (double)maxMemory * 100.0;
      return new ServerMetrics.RamInfo(usedMB, maxMB, usagePercent);
   }

   public static ServerMetrics.WorldStats getWorldStats() {
      long totalChunks = 0L;
      long totalEntities = 0L;
      long totalPlayers = 0L;
      int worldCount = 0;

      for (World world : Bukkit.getWorlds()) {
         worldCount++;
         totalChunks += (long)world.getLoadedChunks().length;
         totalEntities += (long)world.getEntities().size();
         totalPlayers += (long)world.getPlayers().size();
      }

      return new ServerMetrics.WorldStats(worldCount, totalChunks, totalEntities, totalPlayers);
   }

   public static double getAveragePing() {
      double totalPing = 0.0;
      int validPlayers = 0;

      for (Player player : Bukkit.getOnlinePlayers()) {
         int ping = player.getPing();
         if (ping <= 10000) {
            totalPing += (double)ping;
            validPlayers++;
         }
      }

      return validPlayers > 0 ? totalPing / (double)validPlayers : 0.0;
   }

   public static String formatNumber(long number) {
      return NUMBER_FORMAT.format(number);
   }

   public static String formatNumber(double number) {
      return NUMBER_FORMAT.format(Math.round(number));
   }

   public static class RamInfo {
      public final long usedMB;
      public final long maxMB;
      public final double usagePercent;

      public RamInfo(long usedMB, long maxMB, double usagePercent) {
         this.usedMB = usedMB;
         this.maxMB = maxMB;
         this.usagePercent = usagePercent;
      }

      public String getFormattedUsage() {
         String color;
         if (this.usagePercent < 60.0) {
            color = "§a";
         } else if (this.usagePercent < 80.0) {
            color = "§e";
         } else {
            color = "§c";
         }

         return color
            + ServerMetrics.formatNumber(this.usedMB)
            + " / "
            + ServerMetrics.formatNumber(this.maxMB)
            + "MB ("
            + ServerMetrics.DECIMAL_FORMAT.format(this.usagePercent)
            + "%)";
      }
   }

   public static class WorldStats {
      public final int worldCount;
      public final long totalChunks;
      public final long totalEntities;
      public final long totalPlayers;

      public WorldStats(int worldCount, long totalChunks, long totalEntities, long totalPlayers) {
         this.worldCount = worldCount;
         this.totalChunks = totalChunks;
         this.totalEntities = totalEntities;
         this.totalPlayers = totalPlayers;
      }
   }
}
