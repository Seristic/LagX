package com.seristic.lagx.util;

import com.seristic.lagx.api.aparser.AnfoParser;
import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.api.proto.Protocol;
import com.seristic.lagx.main.LagX;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.parser.ParseException;

public class HBZConfig {
   public static double lagConstant;
   public static long ramConstant;
   public static long smartaicooldown;
   public static boolean thinMobs;
   public static int thinAt;
   public static boolean isAIActive;
   public static boolean autoChunk;
   public static boolean noSpawnChunks;
   public static boolean autoLagRemoval;
   public static boolean doRelativeAction;
   public static boolean doOnlyItemsForRelative;
   public static boolean dontDoFriendlyMobsForRelative;
   public static int autoLagRemovalTime;
   public static int localLagRemovalCooldown;
   public static int chatDelay;
   public static int localLagRadius;
   public static int localLagTriggered;
   public static float localThinPercent;
   public static HashMap<LRProtocol, Counter> counters;
   public static HashMap<LRProtocol, DoubleVar<Object[], Boolean>> periodic_protocols;
   public static HashMap<LRProtocol, DoubleVar<Object[], Boolean>> ramProtocols;
   public static HashMap<LRProtocol, DoubleVar<Object[], Boolean>> tpsProtocols;

   public static void reload() {
      FileConfiguration configuration = new YamlConfiguration();

      try {
         configuration.load(new File(LagX.getInstance().getDataFolder(), "config.yml"));
      } catch (InvalidConfigurationException | IOException var10) {
         LagX.getInstance().getLogger().severe("Error loading configuration:");
         var10.printStackTrace();
         configuration = LagX.getInstance().getConfig();
      }

      lagConstant = configuration.getDouble("TPS");
      ramConstant = configuration.getLong("RAM");
      autoChunk = configuration.getBoolean("autoChunk");
      thinMobs = configuration.getBoolean("thinMobs");
      thinAt = configuration.getInt("thinAt");
      autoLagRemovalTime = configuration.getInt("auto-lag-removal.every", 10);
      autoLagRemoval = configuration.getBoolean("auto-lag-removal.run", true);
      LagX.getInstance()
            .getLogger()
            .info("Config loaded - auto-lag-removal.run = " + autoLagRemoval + ", auto-lag-removal.every = "
                  + autoLagRemovalTime);
      LagX.getInstance().getLogger().info("Automatic cleanup: "
            + (autoLagRemoval ? "enabled (every " + autoLagRemovalTime + " minutes)" : "disabled"));
      noSpawnChunks = configuration.getBoolean("noSpawnChunks");
      isAIActive = configuration.getBoolean("smartlagai");
      List<String> noSaveWorlds = configuration.getStringList("nosaveworlds");
      counters = new HashMap<>();
      periodic_protocols = new HashMap<>();
      ramProtocols = new HashMap<>();
      tpsProtocols = new HashMap<>();
      localLagRemovalCooldown = configuration.getInt("localLagRemovalCooldown");
      chatDelay = configuration.getInt("chatDelay");
      doRelativeAction = configuration.getBoolean("doRelativeAction");
      doOnlyItemsForRelative = configuration.getBoolean("doOnlyItemsForRelative");
      dontDoFriendlyMobsForRelative = configuration.getBoolean("dontDoFriendlyMobsForRelative");
      localLagRadius = configuration.getInt("localLagRadius");
      localLagTriggered = configuration.getInt("localLagTriggered");
      localThinPercent = (float) configuration.getInt("localThinPercent") / 100.0F;
      smartaicooldown = configuration.getLong("smartaicooldown");

      for (LRProtocol p : Protocol.getProtocols()) {
         if (configuration.contains("protocol_warnings." + p.id())) {
            counters.put(p, Protocol.getCounter(p));
         }

         String lpk = "lag_protocols.periodically." + p.id();
         String lpk_ram = "lag_protocols.low_ram." + p.id();
         String lpk_tps = "lag_protocols.low_tps." + p.id();
         DoubleVar<Object[], Boolean> dat3;
         if (configuration.contains(lpk) && (dat3 = loadP(p, lpk, configuration)) != null) {
            periodic_protocols.put(p, dat3);
         }

         DoubleVar<Object[], Boolean> dat2;
         if (configuration.contains(lpk_ram) && (dat2 = loadP(p, lpk_ram, configuration)) != null) {
            ramProtocols.put(p, dat2);
         }

         DoubleVar<Object[], Boolean> dat;
         if (configuration.contains(lpk_tps) && (dat = loadP(p, lpk_tps, configuration)) != null) {
            tpsProtocols.put(p, dat);
         }
      }

      if (!noSaveWorlds.contains("DISABLED")) {
         for (World w : Bukkit.getWorlds()) {
            if (noSaveWorlds.contains(w.getName())) {
               w.setAutoSave(false);
               LagX.getInstance().getLogger().info("World \"" + w.getName() + "\" will not automatically save.");
            }
         }
      }
   }

   private static DoubleVar<Object[], Boolean> loadP(LRProtocol p, String lpk, FileConfiguration f) {
      try {
         String configValue = f.getString(lpk);
         if (configValue == null) {
            LagX.getInstance().getLogger().warning("Protocol config '" + lpk + "' is null, skipping.");
            return null;
         }
         return AnfoParser.parse(p, configValue);
      } catch (ParseException | AnfoParser.AnfoParseException var4) {
         LagX.getInstance().getLogger().info("Error parsing protocol info for \"" + lpk + "\": " + var4.toString());
         return null;
      }
   }

   public static void init() {
      File config = new File(LagX.getInstance().getDataFolder(), "config.yml");
      if (!config.exists()) {
         LagX.getInstance().saveDefaultConfig();
      }

      if (DrewMath.intFrom(Objects.requireNonNull(LagX.instance.getConfig().getString("version"))) < 16) {
         LagX.instance
               .getLogger()
               .info(
                     "The saved version is not compatible with this version of LagX and could not be updated by the automatic configuration updater. LagX will back up the current configuration and generate a new one for you. Please manually copy over any old settings.");

         try {
            FileWriter w = new FileWriter(
                  new File(
                        LagX.instance.getDataFolder(),
                        "config(backup-"
                              + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Calendar.getInstance().getTime())
                              + ").yml"));
            w.write(new String(Files.readAllBytes(config.toPath())));
            w.flush();
            w.close();
         } catch (IOException var2) {
            LagX.instance.getLogger()
                  .info("An error occurred when backing up the old configuration (" + var2.getMessage() + ").");
         }

         if (config.delete()) {
            LagX.instance.saveDefaultConfig();
         } else {
            LagX.instance
                  .getLogger()
                  .info("Could not delete old configuration. Please delete it manually and restart your server to prevent imminent errors.");
         }
      }

      check("0.1.7");
      reload();
   }

   private static void check(String version) {
      try {
         if (!Objects.equals(LagX.instance.getConfig().getString("version"), version)) {
            updateConfig(version);
         }
      } catch (Exception var2) {
         updateConfig(version);
      }
   }

   private static void updateConfig(String config_version) {
      HashMap<String, Object> newConfig = getConfigVals();
      FileConfiguration c = LagX.instance.getConfig();

      for (String var : c.getKeys(false)) {
         newConfig.remove(var);
      }

      if (newConfig.size() != 0) {
         for (String key : newConfig.keySet()) {
            c.set(key, newConfig.get(key));
         }

         try {
            c.set("version", config_version);
            c.save(new File(LagX.instance.getDataFolder(), "config.yml"));
         } catch (IOException var5) {
         }
      }

      LagX.instance.getLogger().info("Your configuration file was updated to v" + config_version);
   }

   private static HashMap<String, Object> getConfigVals() {
      HashMap<String, Object> var = new HashMap<>();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.loadFromString(stringFromInputStream(LagX.class.getResourceAsStream("/config.yml")));
      } catch (InvalidConfigurationException var4) {
      }

      for (String key : config.getKeys(false)) {
         var.put(key, config.get(key));
      }

      return var;
   }

   private static String stringFromInputStream(InputStream in) {
      String var2;
      try (Scanner scanner = new Scanner(in)) {
         var2 = scanner.useDelimiter("\\A").next();
      }

      return var2;
   }
}
