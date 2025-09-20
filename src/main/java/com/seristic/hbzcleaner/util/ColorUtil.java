package com.seristic.lagx.util;

import org.bukkit.ChatColor;

public class ColorUtil {
   public static String color(String message) {
      return ChatColor.translateAlternateColorCodes('&', message);
   }

   public static String getColorFromPercentage(double percentage) {
      if (percentage >= 90.0) {
         return "&a";
      } else if (percentage >= 70.0) {
         return "&2";
      } else if (percentage >= 50.0) {
         return "&e";
      } else if (percentage >= 30.0) {
         return "&6";
      } else {
         return percentage >= 20.0 ? "&c" : "&4";
      }
   }

   public static String getColorFromTps(double tps) {
      if (tps >= 19.5) {
         return "&a";
      } else if (tps >= 18.0) {
         return "&2";
      } else if (tps >= 15.0) {
         return "&e";
      } else if (tps >= 10.0) {
         return "&6";
      } else {
         return tps >= 5.0 ? "&c" : "&4";
      }
   }

   public static String getColorFromMemory(double usedPercentage) {
      if (usedPercentage <= 60.0) {
         return "&a";
      } else if (usedPercentage <= 75.0) {
         return "&e";
      } else if (usedPercentage <= 85.0) {
         return "&6";
      } else {
         return usedPercentage <= 95.0 ? "&c" : "&4";
      }
   }
}
