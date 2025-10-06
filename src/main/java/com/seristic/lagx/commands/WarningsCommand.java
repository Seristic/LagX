package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarningsCommand extends LagXCommand {
    public WarningsCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            showWarningStatus(sender);
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "enable":
            case "on":
                plugin.getPluginManager().getConfigManager().setWarningsEnabled(true);
                Help.sendMsg(sender, "§aGround item clearing warnings enabled!", true);
                Help.sendMsg(sender, "§7Players will now see warnings before items are cleared.", true);
                break;
            case "disable":
            case "off":
                plugin.getPluginManager().getConfigManager().setWarningsEnabled(false);
                Help.sendMsg(sender, "§cGround item clearing warnings disabled!", true);
                Help.sendMsg(sender, "§7Only players with §elagx.warnings.receive §7will see warnings.", true);
                break;
            case "status":
                showWarningStatus(sender);
                break;
            default:
                Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
        }
        return true;
    }

    private void showWarningStatus(CommandSender sender) {
        boolean enabled = plugin.getPluginManager().getConfigManager().areWarningsEnabled();
        boolean debugMode = plugin.getPluginManager().getConfigManager().isDebugMode();
        
        if (enabled) {
            Help.sendMsg(sender, "§6Ground Item Clearing Warnings: §aEnabled", true);
            Help.sendMsg(sender, "§7All players will receive warnings before items are cleared.", true);
        } else {
            Help.sendMsg(sender, "§6Ground Item Clearing Warnings: §cDisabled", true);
            Help.sendMsg(sender, "§7Only players with §elagx.warnings.receive §7will see warnings.", true);
        }
        
        if (debugMode) {
            Help.sendMsg(sender, "§c§lDEBUG MODE ACTIVE: §eClearing every 1 minute with 5 second warnings", true);
        }
    }

    @Override
    public String getPermission() {
        return "lagx.warnings";
    }

    @Override
    public String getUsage() {
        return "/lagx warnings [enable|disable|status]";
    }

    @Override
    public String getDescription() {
        return "Toggle ground item clearing warnings";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Arrays.asList("enable", "disable", "status", "on", "off");
        return new ArrayList<>();
    }
}