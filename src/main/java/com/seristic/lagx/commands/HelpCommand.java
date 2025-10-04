package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Help command - shows command help
 */
public class HelpCommand extends LagXCommand {

    public HelpCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        Help.send(sender instanceof Player ? (Player) sender : null, page);
        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.help";
    }

    @Override
    public String getUsage() {
        return "/lagx help [page]";
    }

    @Override
    public String getDescription() {
        return "Shows help for LagX commands";
    }
}