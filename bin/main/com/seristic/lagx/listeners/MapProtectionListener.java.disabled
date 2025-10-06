package com.seristic.lagx.listeners;

import com.seristic.lagx.main.LagX;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

/**
 * Listener for map protection events
 */
public class MapProtectionListener implements Listener {

    private final LagX plugin;

    public MapProtectionListener(LagX plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle map interaction - prevent protected maps from being modified
     */
    @EventHandler
    public void onMapUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FILLED_MAP) {
            return;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            return;
        }

        int mapId = meta.getMapView().getId();
        if (plugin.getMapArtManager().isMapProtected(mapId)) {
            // Check if player has permission to interact with protected maps
            if (!plugin.hasPermission(event.getPlayer(), "lagx.mapart.bypass")) {
                String owner = plugin.getMapArtManager().getMapOwner(mapId);
                if (!event.getPlayer().getName().equals(owner)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cThis map art is protected by " + owner + "!");
                    return;
                }
            }
        }

        // Ensure map is locked if it should be (for map art preservation)
        if (!meta.getMapView().isLocked() && plugin.getMapArtManager().isMapProtected(mapId)) {
            meta.getMapView().setLocked(true);
            item.setItemMeta(meta);
        }
    }

    /**
     * Handle map cloning to ensure proper data copying
     */
    @EventHandler
    public void onMapClone(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() != Material.FILLED_MAP) {
            return;
        }

        // Find the source filled map in the crafting grid
        ItemStack sourceMap = null;
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType() == Material.FILLED_MAP) {
                sourceMap = ingredient;
                break;
            }
        }

        if (sourceMap == null) {
            return;
        }

        MapMeta sourceMeta = (MapMeta) sourceMap.getItemMeta();
        MapMeta resultMeta = (MapMeta) result.getItemMeta();

        if (sourceMeta == null || resultMeta == null || !sourceMeta.hasMapView()) {
            return;
        }

        // Check if source map is protected
        int sourceMapId = sourceMeta.getMapView().getId();
        if (plugin.getMapArtManager().isMapProtected(sourceMapId)) {
            // Only allow cloning if player has permission
            if (event.getView().getPlayer().hasPermission("lagx.mapart.clone")) {
                // Copy the original map's view to the cloned map
                MapView view = sourceMeta.getMapView();
                resultMeta.setMapView(view);
                result.setItemMeta(resultMeta);

                plugin.getLogger().info("Player " + event.getView().getPlayer().getName() +
                        " cloned protected map ID " + sourceMapId);
            } else {
                // Cancel the crafting if they don't have permission
                event.getInventory().setResult(null);
                event.getView().getPlayer().sendMessage("§cYou don't have permission to clone protected map art!");
            }
        } else {
            // Normal map cloning - ensure data is copied correctly
            MapView view = sourceMeta.getMapView();
            resultMeta.setMapView(view);
            result.setItemMeta(resultMeta);
        }
    }

    /**
     * Handle map initialization to ensure proper setup
     */
    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) {
        MapView view = event.getMap();

        // Ensure map view is initialized correctly
        if (view.getWorld() == null) {
            // Set to the first world if world is null
            if (!plugin.getServer().getWorlds().isEmpty()) {
                view.setWorld(plugin.getServer().getWorlds().get(0));
            }
        }

        // Don't automatically lock maps - let users choose when to lock for map art
        plugin.getLogger().fine("Map " + view.getId() + " initialized in world " +
                (view.getWorld() != null ? view.getWorld().getName() : "null"));
    }
}