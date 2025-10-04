package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import com.seristic.lagx.util.TownyIntegration;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Towny integration information command - status and info only
 * GitHub: lines 1062-1135 in LagX.java
 * Note: Towny integration is automatic - no enable/disable commands
 */
public class TownyCommand extends LagXCommand {

    public TownyCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        TownyIntegration towny = LagX.getTownyIntegration();

        if (towny == null || !towny.isEnabled()) {
            Help.sendMsg(sender, "§cTowny integration is not enabled on this server.", true);
            return true;
        }

        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "status":
            case "s":
                showStatus(sender, towny);
                break;
            case "info":
            case "i":
                showInfo(sender, towny);
                break;
            default:
                Help.sendMsg(sender, "§cUnknown subcommand. Available options: status, info", true);
        }

        return true;
    }

    /**
     * Show Towny integration status (enabled, version)
     * GitHub: lines 1085-1093
     */
    private void showStatus(CommandSender sender, TownyIntegration towny) {
        Help.sendMsg(sender, "§6Towny Integration Status:", true);
        Help.sendMsg(sender, "§eEnabled: §a" + towny.isEnabled(), false);
        Help.sendMsg(sender, "§eVersion: §a" + towny.getVersion(), false);
    }

    /**
     * Show player's current location protection and town name (requires player)
     * GitHub: lines 1094-1123
     */
    private void showInfo(CommandSender sender, TownyIntegration towny) {
        if (!(sender instanceof Player)) {
            Help.sendMsg(sender, "§cThis command can only be used by players.", true);
            return;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();
        boolean isProtected = towny.isLocationProtected(loc);
        String townName = towny.getTownAtLocation(loc);

        Help.sendMsg(sender, "§6Towny Information for your location:", true);
        Help.sendMsg(sender, "§eProtected: §" + (isProtected ? "a" : "c") + isProtected, false);
        Help.sendMsg(sender, "§eTown: §a" + (townName != null ? townName : "None"), false);
    }

    @Override
    public String getPermission() {
        return "lagx.towny";
    }

    @Override
    public String getUsage() {
        return "/lagx towny [status|info]";
    }

    @Override
    public String getDescription() {
        return "Towny integration information";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("status", "s", "info", "i");
        }
        return new ArrayList<>();
    }
}