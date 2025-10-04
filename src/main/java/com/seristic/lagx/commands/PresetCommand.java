package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PresetCommand extends LagXCommand {
    public PresetCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (plugin.getEntityLimiter() == null) {
            Help.sendMsg(sender, "§cEntity Limiter is not enabled on this server.", true);
            return true;
        }

        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: /lagx preset [info|set]", true);
            return true;
        }

        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "info":
            case "i":
                Help.sendMsg(sender, "§6Entity Limiter Preset Information:", true);
                Help.sendMsg(sender, plugin.getEntityLimiter().getStatus(), false);
                return true;

            case "set":
            case "s":
                if (args.length < 3) {
                    Help.sendMsg(sender, "§cUsage: /lagx preset set <basic|advanced|custom>", true);
                    return true;
                }

                String presetType = args[2].toLowerCase();
                if (!presetType.equals("basic") && !presetType.equals("advanced") && !presetType.equals("custom")) {
                    Help.sendMsg(sender, "§cInvalid preset type. Use 'basic', 'advanced', or 'custom'.", true);
                    return true;
                }

                plugin.getConfig().set("entity_limiter.preset_mode", presetType);
                plugin.saveConfig();
                plugin.getEntityLimiter().reload();
                Help.sendMsg(sender,
                        "§aEntity Limiter preset changed to §b" + presetType + "§a and configuration reloaded.", true);
                return true;

            default:
                Help.sendMsg(sender, "§cUnknown subcommand. Available options: info, set", true);
                return true;
        }
    }

    @Override
    public String getPermission() {
        return "lagx.preset";
    }

    @Override
    public String getUsage() {
        return "/lagx preset [info|set]";
    }

    @Override
    public String getDescription() {
        return "Switch entity limiter presets";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("info", "set");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
            return Arrays.asList("basic", "advanced", "custom");
        }
        return new ArrayList<>();
    }
}