package com.seristic.hbzcleaner.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.seristic.hbzcleaner.main.LaggRemover;

public class TownyIntegration {
    
    private final LaggRemover plugin;
    private boolean townyEnabled = false;
    
    // Materials that are allowed to be cleared in towns (junk blocks/items)
    private static final Set<Material> ALLOWED_JUNK_MATERIALS = new HashSet<>(Arrays.asList(
        // Common junk blocks
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
        // Common junk items people drop
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
        // Mob drops that accumulate
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
    ));
    
    public TownyIntegration(LaggRemover plugin) {
        this.plugin = plugin;
        checkTownyIntegration();
    }
    
    private void checkTownyIntegration() {
        try {
            Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
                townyEnabled = true;
                plugin.getLogger().info("Towny integration enabled - entities in towns will be protected");
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Towny not found - running without town protection");
        }
    }
    
    public boolean isTownyEnabled() {
        return townyEnabled;
    }
    
    /**
     * Check if an entity should be protected from clearing based on Towny rules
     * In towns: Only allow clearing of whitelisted junk items/materials
     * In wilderness: Allow clearing of everything (except players)
     */
    public boolean isEntityProtected(Entity entity) {
        if (!townyEnabled || entity instanceof Player) {
            return false;
        }
        
        Location loc = entity.getLocation();
        
        try {
            TownyAPI api = TownyAPI.getInstance();
            
            // Check if location is in wilderness (not in any town)
            if (api.isWilderness(loc)) {
                return false; // Not in a town, no protection - allow all clearing
            }
            
            // We're in a town - only allow clearing of junk materials
            if (entity instanceof Item) {
                Item item = (Item) entity;
                Material itemMaterial = item.getItemStack().getType();
                
                // Allow clearing only if it's a junk material
                return !ALLOWED_JUNK_MATERIALS.contains(itemMaterial);
            }
            
            // For non-item entities in towns, protect everything except junk mobs
            if (entity instanceof Monster) {
                // Allow clearing hostile mobs (zombies, skeletons, etc.) in towns
                return false;
            }
            
            // Protect all other living entities in towns (villagers, animals, pets, etc.)
            return true;
            
        } catch (Exception e) {
            // If there's any error with Towny API, err on the side of caution
            plugin.getLogger().warning("Error checking Towny protection for entity: " + e.getMessage());
            return true;
        }
    }
    
    /**
     * Check if a location is within a town
     */
    public boolean isInTown(Location location) {
        if (!townyEnabled) {
            return false;
        }
        
        try {
            return !TownyAPI.getInstance().isWilderness(location);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get town name at location (null if not in town)
     */
    public String getTownName(Location location) {
        if (!townyEnabled) {
            return null;
        }
        
        try {
            TownyAPI api = TownyAPI.getInstance();
            if (!api.isWilderness(location)) {
                Town town = api.getTown(location);
                return town != null ? town.getName() : null;
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        return null;
    }
    
    /**
     * Check if player has permission to clear entities in a town
     */
    public boolean canPlayerClearInTown(Player player, Location location) {
        if (!townyEnabled) {
            return true;
        }
        
        try {
            TownyAPI api = TownyAPI.getInstance();
            
            if (api.isWilderness(location)) {
                return true; // Always allow in wilderness
            }
            
            // Server operators can always clear
            if (player.isOp()) {
                return true;
            }
            
            Town town = api.getTown(location);
            if (town == null) {
                return true;
            }
            
            // Check if player is a resident of the town
            Resident resident = api.getResident(player);
            if (resident != null && resident.hasTown()) {
                try {
                    if (resident.getTown().equals(town)) {
                        return true; // Resident of the town
                    }
                    
                    // Check if player is mayor
                    if (town.isMayor(resident)) {
                        return true;
                    }
                } catch (NotRegisteredException e) {
                    // Player not in a town
                }
            }
            
            return false; // Not authorized to clear in this town
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking Towny permissions: " + e.getMessage());
            return false; // Err on the side of caution
        }
    }
    
    /**
     * Get protection summary for a location
     */
    public String getProtectionInfo(Location location) {
        if (!townyEnabled) {
            return "§7No town protection (Towny not enabled)";
        }
        
        try {
            TownyAPI api = TownyAPI.getInstance();
            
            if (api.isWilderness(location)) {
                return "§7Wilderness - no protection";
            }
            
            Town town = api.getTown(location);
            if (town == null) {
                return "§7Unknown area";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("§6Town: §e").append(town.getName());
            
            TownBlock townBlock = api.getTownBlock(location);
            if (townBlock != null && townBlock.hasResident()) {
                try {
                    info.append(" §7(Plot: §e").append(townBlock.getResident().getName()).append("§7)");
                } catch (NotRegisteredException e) {
                    info.append(" §7(Claimed plot)");
                }
            }
            
            return info.toString();
            
        } catch (Exception e) {
            return "§cError checking town info";
        }
    }
}
