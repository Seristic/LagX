package com.seristic.hbzcleaner.util;

import com.seristic.hbzcleaner.main.HBZCleaner;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyIntegration {
   private final HBZCleaner plugin;
   private boolean townyEnabled = false;
   private Class<?> townyAPIClass;
   private Object townyAPIInstance;
   private Method isWildernessMethod;
   private Method getTownMethod;
   private Method getResidentMethod;
   private Method hasTownMethod;
   private Method getTownByResidentMethod;
   private Method isMayorMethod;
   private Method getNameMethod;
   private Method getTownBlockMethod;
   private Method hasResidentMethod;
   private Method getResidentFromTownBlockMethod;
   private static final Set<Material> ALLOWED_JUNK_MATERIALS = new HashSet<>(
      Arrays.asList(
         Material.COBBLESTONE,
         Material.DEEPSLATE,
         Material.DIORITE,
         Material.ANDESITE,
         Material.GRANITE,
         Material.STONE,
         Material.DIRT,
         Material.GRAVEL,
         Material.SAND,
         Material.RED_SAND,
         Material.NETHERRACK,
         Material.BLACKSTONE,
         Material.BASALT,
         Material.TUFF,
         Material.CALCITE,
         Material.COBBLED_DEEPSLATE,
         Material.POLISHED_BLACKSTONE,
         Material.SMOOTH_BASALT,
         Material.ROTTEN_FLESH,
         Material.BONE,
         Material.STRING,
         Material.SPIDER_EYE,
         Material.GUNPOWDER,
         Material.POISONOUS_POTATO,
         Material.KELP,
         Material.SEAGRASS,
         Material.BAMBOO,
         Material.WHEAT_SEEDS,
         Material.BEETROOT_SEEDS,
         Material.MELON_SEEDS,
         Material.PUMPKIN_SEEDS,
         Material.LEATHER,
         Material.FEATHER,
         Material.EGG,
         Material.MUTTON,
         Material.BEEF,
         Material.PORKCHOP,
         Material.CHICKEN,
         Material.RABBIT,
         Material.COD,
         Material.SALMON,
         Material.TROPICAL_FISH,
         Material.PUFFERFISH
      )
   );

   public TownyIntegration(HBZCleaner plugin) {
      this.plugin = plugin;

      try {
         this.initTownyReflection();
      } catch (Throwable var3) {
         this.townyEnabled = false;
         plugin.getLogger().warning("Failed to initialize Towny integration: " + var3.getMessage());
      }
   }

   private void initTownyReflection() {
      Plugin townyPlugin = this.plugin.getServer().getPluginManager().getPlugin("Towny");
      if (townyPlugin != null && townyPlugin.isEnabled()) {
         try {
            this.townyAPIClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Method getInstance = this.townyAPIClass.getMethod("getInstance");
            this.townyAPIInstance = getInstance.invoke(null);
            this.isWildernessMethod = this.townyAPIClass.getMethod("isWilderness", Location.class);
            this.getTownMethod = this.townyAPIClass.getMethod("getTown", Location.class);
            this.getResidentMethod = this.townyAPIClass.getMethod("getResident", Player.class);
            Class<?> residentClass = Class.forName("com.palmergames.bukkit.towny.object.Resident");
            this.hasTownMethod = residentClass.getMethod("hasTown");
            this.getTownByResidentMethod = residentClass.getMethod("getTown");
            Class<?> townClass = Class.forName("com.palmergames.bukkit.towny.object.Town");
            this.isMayorMethod = townClass.getMethod("isMayor", residentClass);
            this.getNameMethod = townClass.getMethod("getName");
            this.getTownBlockMethod = this.townyAPIClass.getMethod("getTownBlock", Location.class);
            Class<?> townBlockClass = Class.forName("com.palmergames.bukkit.towny.object.TownBlock");
            this.hasResidentMethod = townBlockClass.getMethod("hasResident");
            this.getResidentFromTownBlockMethod = townBlockClass.getMethod("getResident");
            this.townyEnabled = true;
            this.plugin.getLogger().info("Towny integration enabled - using reflection for compatibility");
         } catch (Exception var6) {
            this.townyEnabled = false;
            this.plugin.getLogger().warning("Failed to initialize Towny integration: " + var6.getMessage());
            this.plugin.getLogger().warning("Town protection features will be disabled");
         }
      } else {
         this.townyEnabled = false;
         this.plugin.getLogger().info("Towny not found - town protection features disabled");
      }
   }

   public boolean isTownyEnabled() {
      return this.townyEnabled;
   }

   private boolean isWilderness(Location location) {
      if (!this.townyEnabled) {
         return true;
      } else {
         try {
            return (Boolean)this.isWildernessMethod.invoke(this.townyAPIInstance, location);
         } catch (Exception var3) {
            return true;
         }
      }
   }

   public boolean isEntityProtected(Entity entity) {
      if (this.townyEnabled && !(entity instanceof Player)) {
         Location loc = entity.getLocation();

         try {
            if (this.isWilderness(loc)) {
               return false;
            } else if (entity instanceof Item item) {
               Material itemMaterial = item.getItemStack().getType();
               return !ALLOWED_JUNK_MATERIALS.contains(itemMaterial);
            } else {
               return !(entity instanceof Monster);
            }
         } catch (Exception var5) {
            this.plugin.getLogger().warning("Error checking Towny protection for entity: " + var5.getMessage());
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean isInTown(Location location) {
      return !this.townyEnabled ? false : !this.isWilderness(location);
   }

   public String getTownName(Location location) {
      if (!this.townyEnabled) {
         return null;
      } else {
         try {
            if (!this.isWilderness(location)) {
               Object town = this.getTownMethod.invoke(this.townyAPIInstance, location);
               if (town != null) {
                  return (String)this.getNameMethod.invoke(town);
               }
            }
         } catch (Exception var3) {
         }

         return null;
      }
   }

   public boolean canPlayerClearInTown(Player player, Location location) {
      if (!this.townyEnabled) {
         return true;
      } else {
         try {
            if (this.isWilderness(location)) {
               return true;
            } else if (HBZCleaner.hasPermission(player, "hbzcleaner.towny.bypass")) {
               return true;
            } else {
               Object town = this.getTownMethod.invoke(this.townyAPIInstance, location);
               if (town == null) {
                  return true;
               } else {
                  Object resident = this.getResidentMethod.invoke(this.townyAPIInstance, player);
                  if (resident != null && (Boolean)this.hasTownMethod.invoke(resident)) {
                     try {
                        Object residentTown = this.getTownByResidentMethod.invoke(resident);
                        if (residentTown.equals(town)) {
                           return true;
                        }

                        if ((Boolean)this.isMayorMethod.invoke(town, resident)) {
                           return true;
                        }
                     } catch (Exception var6) {
                     }
                  }

                  return false;
               }
            }
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Error checking Towny permissions: " + var7.getMessage());
            return false;
         }
      }
   }

   public String getProtectionInfo(Location location) {
      if (!this.townyEnabled) {
         return "§7No town protection (Towny not enabled)";
      } else {
         try {
            if (this.isWilderness(location)) {
               return "§7Wilderness - no protection";
            } else {
               Object town = this.getTownMethod.invoke(this.townyAPIInstance, location);
               if (town == null) {
                  return "§7Unknown area";
               } else {
                  StringBuilder info = new StringBuilder();
                  info.append("§6Town: §e").append(this.getNameMethod.invoke(town));
                  Object townBlock = this.getTownBlockMethod.invoke(this.townyAPIInstance, location);
                  if (townBlock != null && (Boolean)this.hasResidentMethod.invoke(townBlock)) {
                     try {
                        Object blockResident = this.getResidentFromTownBlockMethod.invoke(townBlock);
                        info.append(" §7(Plot: §e").append(this.getNameMethod.invoke(blockResident)).append("§7)");
                     } catch (Exception var6) {
                        info.append(" §7(Claimed plot)");
                     }
                  }

                  return info.toString();
               }
            }
         } catch (Exception var7) {
            return "§cError checking town info: " + var7.getMessage();
         }
      }
   }

   public boolean isEnabled() {
      return this.townyEnabled;
   }

   public String getVersion() {
      if (!this.townyEnabled) {
         return "Not installed";
      } else {
         Plugin townyPlugin = this.plugin.getServer().getPluginManager().getPlugin("Towny");
         return townyPlugin == null ? "Not installed" : townyPlugin.getDescription().getVersion();
      }
   }

   public String getTownAtLocation(Location location) {
      if (!this.townyEnabled) {
         return null;
      } else {
         try {
            Object townObj = this.getTownMethod.invoke(this.townyAPIInstance, location);
            return townObj == null ? null : (String)this.getNameMethod.invoke(townObj);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public boolean isLocationProtected(Location location) {
      if (!this.townyEnabled) {
         return false;
      } else {
         try {
            Boolean isWild = (Boolean)this.isWildernessMethod.invoke(this.townyAPIInstance, location);
            return !isWild;
         } catch (Exception var3) {
            return false;
         }
      }
   }
}
