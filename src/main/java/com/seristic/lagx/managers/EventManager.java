package com.seristic.lagx.managers;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.listeners.MapProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all event listeners for LagX
 * Centralizes listener registration and lifecycle management
 */
public class EventManager {

    private final LagX plugin;
    private final List<Listener> registeredListeners = new ArrayList<>();

    public EventManager(LagX plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getLogger().info("Registering event listeners...");

        // Register main plugin as listener (for entity spawn events)
        registerListener(plugin);

        // Register map protection listener
        registerListener(new MapProtectionListener(plugin));

        plugin.getLogger().info("Registered " + registeredListeners.size() + " event listeners");
    }

    /**
     * Register a listener and track it
     */
    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    /**
     * Unregister a specific listener
     */
    public void unregisterListener(Listener listener) {
        org.bukkit.event.HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
    }

    public void shutdown() {
        plugin.getLogger().info("Unregistering event listeners...");

        // Unregister all tracked listeners
        for (Listener listener : registeredListeners) {
            org.bukkit.event.HandlerList.unregisterAll(listener);
        }

        registeredListeners.clear();
    }

    public List<Listener> getRegisteredListeners() {
        return new ArrayList<>(registeredListeners);
    }
}