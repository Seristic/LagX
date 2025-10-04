package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entity stacker management command - info, debug, reload, stack
 * GitHub: lines 1229-1323 in LagX.java
 */
public class StackerCommand extends LagXCommand {

    public StackerCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        if (plugin.getEntityStacker() == null) {
            Help.sendMsg(sender, "§cEntity Stacker is not enabled on this server.", true);
            return true;
        }

        switch (subCommand) {
            case "info":
                showInfo(sender);
                break;
            case "debug":
                showDebug(sender);
                break;
            case "reload":
                reloadStacker(sender);
                break;
            case "stack":
                stackEntities(sender, args);
                break;
            default:
                Help.sendMsg(sender, "§cUnknown subcommand. Available options: info, debug, reload, stack", true);
        }

        return true;
    }

    /**
     * Show stacker statistics
     * GitHub: lines 1239-1258
     */
    private void showInfo(CommandSender sender) {
        Help.sendMsg(sender, "§6Entity Stacker Statistics:", true);
        Help.sendMsg(sender, "§eEnabled: §a" + plugin.getEntityStacker().isEnabled(), false);
        Help.sendMsg(sender, "§eSingle Kill Mode: §a" + plugin.getEntityStacker().isSingleKillEnabled(), false);
        Help.sendMsg(sender, "§eStacked Items: §a" + plugin.getEntityStacker().getStackedItemsCount(), false);
        Help.sendMsg(sender, "§eStacked Entities: §a" + plugin.getEntityStacker().getStackedEntitiesCount(), false);
    }

    /**
     * Show debug information blob
     * GitHub: lines 1259-1262
     */
    private void showDebug(CommandSender sender) {
        Help.sendMsg(sender, plugin.getEntityStacker().getDebugInfo(), true);
    }

    /**
     * Reload stacker configuration
     * GitHub: lines 1263-1267
     */
    private void reloadStacker(CommandSender sender) {
        plugin.getEntityStacker().reloadConfig();
        Help.sendMsg(sender, "§aEntity Stacker configuration reloaded!", true);
    }

    /**
     * Manual stacking in radius (default 50, requires player, 1-200 range)
     * GitHub: lines 1268-1312
     */
    private void stackEntities(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Help.sendMsg(sender, "§cThis command can only be used by players.", true);
            return;
        }

        Player player = (Player) sender;
        int radius = 50; // Default radius

        if (args.length >= 3) {
            try {
                radius = Integer.parseInt(args[2]);
                if (radius < 1 || radius > 200) {
                    Help.sendMsg(sender, "§cRadius must be between 1 and 200 blocks.", true);
                    return;
                }
            } catch (NumberFormatException e) {
                Help.sendMsg(sender, "§cInvalid radius. Please use a number between 1 and 200.", true);
                return;
            }
        }

        int stacksCreated = plugin.getEntityStacker().stackEntitiesInRadius(player.getLocation(), radius);
        Help.sendMsg(sender,
                "§aSuccessfully stacked entities in a " + radius + " block radius. Created " + stacksCreated
                        + " stacks.",
                true);
    }

    @Override
    public String getPermission() {
        return "lagx.stacker";
    }

    @Override
    public String getUsage() {
        return "/lagx stacker [info|debug|reload|stack <radius>]";
    }

    @Override
    public String getDescription() {
        return "Entity stacker management";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(Arrays.asList("info", "debug", "reload", "stack"));
        } else if (args.length == 3 && args[1].equalsIgnoreCase("stack")) {
            // Suggest common radius values
            completions.addAll(Arrays.asList("10", "25", "50", "100", "200"));
        }

        return completions;
    }
}