package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.command.CommandSender;

/**
 * RAM command - shows memory usage
 */
public class RamCommand extends LagXCommand {

    public RamCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory() / 1048576L;
        long total = rt.totalMemory() / 1048576L;
        long free = rt.freeMemory() / 1048576L;
        long used = total - free;

        Help.sendMsg(sender,
                String.format("§eRAM Usage: §b%dMB used§7/§b%dMB total§7 (max §b%dMB§7)", used, total, max),
                true);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.ram";
    }

    @Override
    public String getUsage() {
        return "/lagx ram";
    }

    @Override
    public String getDescription() {
        return "Shows server memory usage";
    }
}