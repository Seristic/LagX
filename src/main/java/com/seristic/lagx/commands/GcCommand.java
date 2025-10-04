package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.command.CommandSender;

/**
 * GC command - triggers garbage collection
 */
public class GcCommand extends LagXCommand {

    public GcCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        Runtime rt = Runtime.getRuntime();
        long before = rt.totalMemory() - rt.freeMemory();
        System.gc();
        long after = rt.totalMemory() - rt.freeMemory();
        long diff = (before - after) / 1048576L;

        Help.sendMsg(sender, "§aGarbage collection completed. Freed ~§b" + Math.max(diff, 0L) + "MB§a.", true);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.gc";
    }

    @Override
    public String getUsage() {
        return "/lagx gc";
    }

    @Override
    public String getDescription() {
        return "Triggers Java garbage collection";
    }
}