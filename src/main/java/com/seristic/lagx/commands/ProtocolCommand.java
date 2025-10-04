package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.api.proto.Protocol;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Protocol command - manages and runs protocols
 */
public class ProtocolCommand extends LagXCommand {

    public ProtocolCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
            return true;
        }

        if (args.length == 1
                || (args.length >= 2 && (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("l")))) {
            // List protocols
            String list = Protocol.getProtocols().stream()
                    .map(LRProtocol::id)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
            Help.sendMsg(sender, "§eProtocols: §b" + list, true);
            return true;
        }

        if (args.length < 2 || (!args[1].equalsIgnoreCase("run") && !args[1].equalsIgnoreCase("r"))) {
            Help.sendMsg(sender, "§cUsage: /lagx protocol [list|run <id> [count:true|false]]", true);
            return true;
        }

        if (args.length < 3) {
            Help.sendMsg(sender, "§cUsage: /lagx protocol run <id> [count:true|false]", true);
            return true;
        }

        // Run protocol
        LRProtocol proto = Protocol.getProtocol(args[2]);
        if (proto == null) {
            Help.sendMsg(sender, "§cUnknown protocol: " + args[2], true);
            return true;
        }

        boolean count = true;
        if (args.length >= 4) {
            count = Boolean.parseBoolean(args[3]);
        }

        try {
            LRProtocolResult res = Protocol.run(proto, new Object[] { count });
            if (res != null) {
                if (sender instanceof Player) {
                    Help.sendProtocolResultInfo((Player) sender, res);
                } else {
                    Help.sendMsg(sender, "Protocol executed with results (console output limited)", true);
                }
            } else {
                Help.sendMsg(sender, "§eProtocol executed.", true);
            }
        } catch (Throwable var37) {
            Help.sendMsg(sender, "§cProtocol error: " + var37.getMessage(), true);
        }

        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.protocol";
    }

    @Override
    public String getUsage() {
        return "/lagx protocol [list|run <id> [count:true|false]]";
    }

    @Override
    public String getDescription() {
        return "Manage and run internal protocols";
    }
}