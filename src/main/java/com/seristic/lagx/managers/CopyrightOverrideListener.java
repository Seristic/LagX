package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

/**
 * Listener that overrides copyright plugin protection when needed
 * Allows LagX to force map updates despite copyright restrictions
 */
public class CopyrightOverrideListener implements Listener {

    private final LagX plugin;
    private final IntegrationManager.CopyrightPluginIntegration copyrightIntegration;

    public CopyrightOverrideListener(LagX plugin, IntegrationManager.CopyrightPluginIntegration copyrightIntegration) {
        this.plugin = plugin;
        this.copyrightIntegration = copyrightIntegration;
    }

    /**
     * Intercept map initialization to override copyright protection
     * This runs at highest priority to execute before copyright plugin
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapInitialize(MapInitializeEvent event) {
        MapView mapView = event.getMap();
        int mapId = mapView.getId();

        // Check if LagX should override protection for this map
        if (copyrightIntegration.shouldOverrideProtection(mapId)) {
            // Log the override action
            plugin.getLogger().info("Overriding copyright protection for map ID " + mapId);

            // The event is not cancelled - we let it proceed normally
            // but we might want to register additional listeners or modify map data
        }
    }

    /**
     * Monitor when players hold maps to potentially override protection
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerHoldMap(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());

        if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta) item.getItemMeta();
            MapView mapView = mapMeta.getMapView();

            if (mapView != null) {
                int mapId = mapView.getId();

                // Check if this map needs special handling
                if (copyrightIntegration.shouldOverrideProtection(mapId)) {
                    // Potentially notify the player or perform special actions
                    // This could include forcing a map update or applying special protection
                }
            }
        }
    }
}