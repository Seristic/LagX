package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Ping command - shows player ping
 */
public class PingCommand extends LagXCommand {

    public PingCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        Player target = (Player) sender;

        // If args length >= 2, check for target player
        if (args.length >= 2) {
            if (sender instanceof Player || sender.hasPermission("lagx.ping.others")) {
                target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    Help.sendMsg(sender, "§cPlayer not found.", true);
                    return true;
                }
            } else {
                Help.sendMsg(sender, "§cYou don't have permission to check other players' ping.", true);
                return true;
            }
        } else if (!(sender instanceof Player)) {
            Help.sendMsg(sender, "§cUsage: /lagx ping <player>", true);
            return true;
        }

        int ping = 0;
        try {
            ping = target.getPing();
        } catch (Throwable ignored) {
            // Ignore ping retrieval errors
        }

        Help.sendMsg(sender, "§ePing for §b" + target.getName() + "§e: §b" + ping + "ms", true);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.ping";
    }

    @Override
    public String getUsage() {
        return "/lagx ping [player]";
    }

    @Override
    public String getDescription() {
        return "Shows ping of yourself or another player";
    }
}