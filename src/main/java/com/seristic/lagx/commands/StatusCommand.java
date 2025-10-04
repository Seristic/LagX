package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;

public class StatusCommand extends LagXCommand {
    public StatusCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.status";
    }

    @Override
    public String getUsage() {
        return "/lagx status";
    }

    @Override
    public String getDescription() {
        return "Shows server status";
    }
}