package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.TickPerSecond;
import org.bukkit.command.CommandSender;

/**
 * TPS command - shows server TPS
 */
public class TpsCommand extends LagXCommand {

    public TpsCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        Help.sendMsg(sender, "§eTPS: " + TickPerSecond.format(), true);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.tps";
    }

    @Override
    public String getUsage() {
        return "/lagx tps";
    }

    @Override
    public String getDescription() {
        return "Shows server TPS (Ticks Per Second)";
    }
}