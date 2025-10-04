package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entity limiter control command - status, reload, enable, disable
 * GitHub: lines 705-749 in LagX.java
 */
public class LimiterCommand extends LagXCommand {

    public LimiterCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (plugin.getEntityLimiter() == null) {
            Help.sendMsg(sender, "§cEntity Limiter is not enabled on this server.", true);
            return true;
        }

        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "status":
            case "s":
                showStatus(sender);
                break;
            case "reload":
            case "r":
                reloadLimiter(sender);
                break;
            case "enable":
            case "e":
                enableLimiter(sender);
                break;
            case "disable":
            case "d":
                disableLimiter(sender);
                break;
            default:
                Help.sendMsg(sender, "§cUnknown subcommand. Available: status, reload, enable, disable", true);
        }

        return true;
    }

    /**
     * Show entity limiter status
     * GitHub: lines 716-719
     */
    private void showStatus(CommandSender sender) {
        Help.sendMsg(sender, "§6Entity Limiter Status:", true);
        Help.sendMsg(sender, plugin.getEntityLimiter().getStatus(), false);
    }

    /**
     * Reload entity limiter configuration
     * GitHub: lines 720-723
     */
    private void reloadLimiter(CommandSender sender) {
        plugin.getEntityLimiter().reload();
        Help.sendMsg(sender, "§aEntity Limiter configuration reloaded!", true);
    }

    /**
     * Enable entity limiter (sets config + saves + reloads)
     * GitHub: lines 724-729
     */
    private void enableLimiter(CommandSender sender) {
        plugin.getConfig().set("entity_limiter.enabled", true);
        plugin.saveConfig();
        plugin.getEntityLimiter().reload();
        Help.sendMsg(sender, "§aEntity Limiter has been enabled.", true);
    }

    /**
     * Disable entity limiter (sets config + saves + reloads)
     * GitHub: lines 730-735
     */
    private void disableLimiter(CommandSender sender) {
        plugin.getConfig().set("entity_limiter.enabled", false);
        plugin.saveConfig();
        plugin.getEntityLimiter().reload();
        Help.sendMsg(sender, "§cEntity Limiter has been disabled.", true);
    }

    @Override
    public String getPermission() {
        return "lagx.limiter";
    }

    @Override
    public String getUsage() {
        return "/lagx limiter [status|reload|enable|disable]";
    }

    @Override
    public String getDescription() {
        return "Entity limiter control";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("info", "status", "reload", "enable", "disable");
        }
        return new ArrayList<>();
    }
}