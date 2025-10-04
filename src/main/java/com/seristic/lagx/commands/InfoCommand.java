package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.command.CommandSender;

/**
 * Info command - shows plugin information
 */
public class InfoCommand extends LagXCommand {

    public InfoCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        // TODO: Implement info command logic from original LagX.java
        Help.sendMsg(sender, "§eInfo command - implementation pending", true);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.info";
    }

    @Override
    public String getUsage() {
        return "/lagx info";
    }

    @Override
    public String getDescription() {
        return "Shows plugin and server information";
    }
}