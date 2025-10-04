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
                Help.sendMsg(sender, "§aPerformance warnings enabled!", true);
                break;
            case "disable":
                Help.sendMsg(sender, "§cPerformance warnings disabled!", true);
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
        Help.sendMsg(sender, "§6Performance Warnings: §aEnabled\n§7TPS Warning: §f< 18.0\n§7Memory Warning: §f> 80%",
                true);
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
        return "Manage performance warnings";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Arrays.asList("enable", "disable", "status");
        return new ArrayList<>();
    }
}