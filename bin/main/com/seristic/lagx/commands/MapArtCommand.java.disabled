package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.utils.MapArtManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

/**
 * Map Art command - manages map art protection
 */
public class MapArtCommand extends LagXCommand {

    public MapArtCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Help.sendMsg(sender, "§cThis command can only be used by players.", true);
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            Help.sendMsg(player, "§eMap Art Protection Commands:", true);
            Help.sendMsg(player, "§b/lagx mapart protect §7- Protect held map from changes", true);
            Help.sendMsg(player, "§b/lagx mapart unprotect §7- Remove protection from held map", true);
            Help.sendMsg(player, "§b/lagx mapart lock §7- Lock held map (prevents updates)", true);
            Help.sendMsg(player, "§b/lagx mapart info §7- Show map information", true);
            Help.sendMsg(player, "§b/lagx mapart list §7- List protected maps", true);
            Help.sendMsg(player, "§6Admin Commands:", true);
            Help.sendMsg(player, "§b/lagx mapart auto §7- Toggle automatic protection", true);
            Help.sendMsg(player, "§b/lagx mapart interval <minutes> §7- Set scan interval", true);
            Help.sendMsg(player, "§b/lagx mapart status §7- Show auto-protection status", true);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "protect":
                return protectMap(player);
            case "unprotect":
                return unprotectMap(player);
            case "lock":
                return lockMap(player);
            case "unlock":
                return unlockMap(player);
            case "info":
                return showMapInfo(player);
            case "list":
                return listProtectedMaps(player);
            case "auto":
                return toggleAutoProtection(player);
            case "interval":
                return setInterval(player, args);
            case "status":
                return showStatus(player);
            default:
                Help.sendMsg(player, "§cUnknown subcommand. Use §e/lagx mapart §cfor help.", true);
                return true;
        }
    }

    private boolean protectMap(Player player) {
        if (!hasPermission(player, "lagx.mapart.protect")) {
            Help.sendMsg(player, "§cYou don't have permission to protect maps.", true);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            Help.sendMsg(player, "§cYou must be holding a filled map to protect it.", true);
            return true;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            Help.sendMsg(player, "§cInvalid map item.", true);
            return true;
        }

        int mapId = meta.getMapView().getId();
        plugin.getMapArtManager().protectMap(mapId, player.getName());

        Help.sendMsg(player, "§aMap ID §b" + mapId + "§a has been protected from changes!", true);
        return true;
    }

    private boolean unprotectMap(Player player) {
        if (!hasPermission(player, "lagx.mapart.unprotect")) {
            Help.sendMsg(player, "§cYou don't have permission to unprotect maps.", true);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            Help.sendMsg(player, "§cYou must be holding a filled map to unprotect it.", true);
            return true;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            Help.sendMsg(player, "§cInvalid map item.", true);
            return true;
        }

        int mapId = meta.getMapView().getId();

        if (!plugin.getMapArtManager().isMapProtected(mapId)) {
            Help.sendMsg(player, "§cThis map is not protected.", true);
            return true;
        }

        // Check if player owns the map or has admin permission
        String owner = plugin.getMapArtManager().getMapOwner(mapId);
        if (!player.getName().equals(owner) && !hasPermission(player, "lagx.mapart.admin")) {
            Help.sendMsg(player, "§cYou can only unprotect maps that you own.", true);
            return true;
        }

        plugin.getMapArtManager().unprotectMap(mapId);
        Help.sendMsg(player, "§aMap ID §b" + mapId + "§a has been unprotected.", true);
        return true;
    }

    private boolean lockMap(Player player) {
        if (!hasPermission(player, "lagx.mapart.lock")) {
            Help.sendMsg(player, "§cYou don't have permission to lock maps.", true);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            Help.sendMsg(player, "§cYou must be holding a filled map to lock it.", true);
            return true;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            Help.sendMsg(player, "§cInvalid map item.", true);
            return true;
        }

        // Lock the map to prevent updates
        meta.getMapView().setLocked(true);
        item.setItemMeta(meta);

        Help.sendMsg(player, "§aMap has been locked and will no longer update!", true);
        return true;
    }

    private boolean unlockMap(Player player) {
        if (!hasPermission(player, "lagx.mapart.unlock")) {
            Help.sendMsg(player, "§cYou don't have permission to unlock maps.", true);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            Help.sendMsg(player, "§cYou must be holding a filled map to unlock it.", true);
            return true;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            Help.sendMsg(player, "§cInvalid map item.", true);
            return true;
        }

        // Unlock the map to allow updates
        meta.getMapView().setLocked(false);
        item.setItemMeta(meta);

        Help.sendMsg(player, "§aMap has been unlocked and will now update normally.", true);
        return true;
    }

    private boolean showMapInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            Help.sendMsg(player, "§cYou must be holding a filled map to view its info.", true);
            return true;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasMapView()) {
            Help.sendMsg(player, "§cInvalid map item.", true);
            return true;
        }

        int mapId = meta.getMapView().getId();
        boolean isLocked = meta.getMapView().isLocked();
        boolean isProtected = plugin.getMapArtManager().isMapProtected(mapId);
        String owner = plugin.getMapArtManager().getMapOwner(mapId);

        Help.sendMsg(player, "§eMap Information:", true);
        Help.sendMsg(player, "§7Map ID: §b" + mapId, true);
        Help.sendMsg(player, "§7Locked: " + (isLocked ? "§aYes" : "§cNo"), true);
        Help.sendMsg(player, "§7Protected: " + (isProtected ? "§aYes" : "§cNo"), true);
        if (isProtected && owner != null) {
            Help.sendMsg(player, "§7Owner: §b" + owner, true);
        }

        return true;
    }

    private boolean listProtectedMaps(Player player) {
        if (!hasPermission(player, "lagx.mapart.list")) {
            Help.sendMsg(player, "§cYou don't have permission to list protected maps.", true);
            return true;
        }

        var protectedMaps = plugin.getMapArtManager().getProtectedMaps();
        if (protectedMaps.isEmpty()) {
            Help.sendMsg(player, "§eThere are no protected maps.", true);
            return true;
        }

        Help.sendMsg(player, "§eProtected Maps:", true);
        for (var entry : protectedMaps.entrySet()) {
            int mapId = entry.getKey();
            String owner = entry.getValue();
            Help.sendMsg(player, "§7- Map ID §b" + mapId + "§7 owned by §b" + owner, true);
        }

        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.mapart";
    }

    @Override
    public String getUsage() {
        return "/lagx mapart <protect|unprotect|lock|unlock|info|list|auto|interval|status>";
    }

    @Override
    public String getDescription() {
        return "Manage map art protection";
    }

    /**
     * Toggle automatic protection on/off
     */
    private boolean toggleAutoProtection(Player player) {
        if (!hasPermission(player, "lagx.mapart.admin")) {
            Help.sendMsg(player, "§cYou don't have permission to manage auto-protection.", true);
            return true;
        }

        boolean currentState = plugin.getMapArtManager().isAutoProtectionEnabled();
        plugin.getMapArtManager().setAutoProtectionEnabled(!currentState);

        String status = !currentState ? "§aenabled" : "§cdisabled";
        Help.sendMsg(player, "§eAutomatic map protection has been " + status + "§e!", true);

        return true;
    }

    /**
     * Set the scanning interval
     */
    private boolean setInterval(Player player, String[] args) {
        if (!hasPermission(player, "lagx.mapart.admin")) {
            Help.sendMsg(player, "§cYou don't have permission to manage auto-protection.", true);
            return true;
        }

        if (args.length < 3) {
            Help.sendMsg(player, "§cUsage: /lagx mapart interval <minutes>", true);
            return true;
        }

        try {
            int minutes = Integer.parseInt(args[2]);
            if (minutes < 1) {
                Help.sendMsg(player, "§cInterval must be at least 1 minute.", true);
                return true;
            }

            plugin.getMapArtManager().setScanInterval(minutes);
            Help.sendMsg(player, "§eScan interval set to §b" + minutes + " minutes§e!", true);

        } catch (NumberFormatException e) {
            Help.sendMsg(player, "§cInvalid number: " + args[2], true);
        }

        return true;
    }

    /**
     * Show auto-protection status
     */
    private boolean showStatus(Player player) {
        if (!hasPermission(player, "lagx.mapart.admin")) {
            Help.sendMsg(player, "§cYou don't have permission to view auto-protection status.", true);
            return true;
        }

        MapArtManager manager = plugin.getMapArtManager();

        Help.sendMsg(player, "§eMap Art Auto-Protection Status:", true);
        Help.sendMsg(player, "§7Status: " + (manager.isAutoProtectionEnabled() ? "§aEnabled" : "§cDisabled"), true);
        Help.sendMsg(player, "§7Scan Interval: §b" + manager.getScanInterval() + " minutes", true);
        Help.sendMsg(player, "§7Protect All Maps: " + (manager.isProtectingAllMaps() ? "§aYes" : "§cNo"), true);
        Help.sendMsg(player, "§7Protected Maps: §b" + manager.getProtectedMapIds().size(), true);

        return true;
    }
}