package com.seristic.lagx.util;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class FormatUtil {
   private static final DecimalFormat df = new DecimalFormat("#,##0.00");
   private static final DecimalFormat intFormat = new DecimalFormat("#,##0");

   public static String formatDuration(long millis) {
      long days = TimeUnit.MILLISECONDS.toDays(millis);
      millis -= TimeUnit.DAYS.toMillis(days);
      long hours = TimeUnit.MILLISECONDS.toHours(millis);
      millis -= TimeUnit.HOURS.toMillis(hours);
      long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
      millis -= TimeUnit.MINUTES.toMillis(minutes);
      long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
      StringBuilder sb = new StringBuilder();
      if (days > 0L) {
         sb.append(days).append("d ");
      }

      if (hours > 0L || days > 0L) {
         sb.append(hours).append("h ");
      }

      if (minutes > 0L || hours > 0L || days > 0L) {
         sb.append(minutes).append("m ");
      }

      sb.append(seconds).append("s");
      return sb.toString();
   }

   public static String formatDecimal(double number) {
      return df.format(number);
   }

   public static String formatInteger(int number) {
      return intFormat.format((long) number);
   }

   public static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else if (bytes < 1048576L) {
         return formatDecimal((double) bytes / 1024.0) + " KB";
      } else {
         return bytes < 1073741824L ? formatDecimal((double) bytes / 1048576.0) + " MB"
               : formatDecimal((double) bytes / 1.0737418E9F) + " GB";
      }
   }

   public static String formatBoolean(boolean value) {
      return value ? "§aYes" : "§cNo";
   }

   public static String formatPercentage(long value, long total) {
      return total == 0L ? "0%" : formatDecimal((double) value / (double) total * 100.0) + "%";
   }
}
