package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Villager optimizer command - status, reload, enable, disable, optimize, stats
 * GitHub: lines 858-1015 in LagX.java
 */
public class VillagersCommand extends LagXCommand {

    public VillagersCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (plugin.getVillagerOptimizer() == null) {
            Help.sendMsg(sender, "§cVillager Optimizer is not enabled on this server.", true);
            return true;
        }

        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "status":
            case "s":
                showStatus(sender);
                break;
            case "reload":
            case "r":
                reloadOptimizer(sender);
                break;
            case "enable":
            case "e":
                enableOptimizer(sender);
                break;
            case "disable":
            case "d":
                disableOptimizer(sender);
                break;
            case "optimize":
            case "o":
                optimizeVillagers(sender, args);
                break;
            case "stats":
                showStats(sender);
                break;
            default:
                Help.sendMsg(sender,
                        "§cUnknown subcommand. Available options: status, reload, enable, disable, optimize, stats",
                        true);
        }

        return true;
    }

    /**
     * Show villager optimizer status
     * GitHub: lines 867-876
     */
    private void showStatus(CommandSender sender) {
        Help.sendMsg(sender, "§6Villager Optimizer Status:", true);
        Help.sendMsg(sender, plugin.getVillagerOptimizer().getStatus(), false);
    }

    /**
     * Reload villager optimizer configuration
     * GitHub: lines 877-882
     */
    private void reloadOptimizer(CommandSender sender) {
        plugin.getVillagerOptimizer().reload();
        Help.sendMsg(sender, "§aVillager Optimizer configuration reloaded!", true);
    }

    /**
     * Enable villager optimizer
     * GitHub: lines 883-895
     */
    private void enableOptimizer(CommandSender sender) {
        if (plugin.getVillagerOptimizer().isEnabled()) {
            Help.sendMsg(sender, "§eVillager Optimizer is already enabled.", true);
            return;
        }

        plugin.getConfig().set("villager_optimization.enabled", true);
        plugin.saveConfig();
        plugin.getVillagerOptimizer().reload();
        Help.sendMsg(sender, "§aVillager Optimizer has been enabled.", true);
    }

    /**
     * Disable villager optimizer
     * GitHub: lines 896-908
     */
    private void disableOptimizer(CommandSender sender) {
        if (!plugin.getVillagerOptimizer().isEnabled()) {
            Help.sendMsg(sender, "§eVillager Optimizer is already disabled.", true);
            return;
        }

        plugin.getConfig().set("villager_optimization.enabled", false);
        plugin.saveConfig();
        plugin.getVillagerOptimizer().reload();
        Help.sendMsg(sender, "§cVillager Optimizer has been disabled.", true);
    }

    /**
     * Manually optimize villagers in a world
     * GitHub: lines 909-950
     */
    private void optimizeVillagers(CommandSender sender, String[] args) {
        World targetWorld = null;

        if (args.length >= 3) {
            targetWorld = Bukkit.getWorld(args[2]);
            if (targetWorld == null) {
                Help.sendMsg(sender, "§cWorld not found: " + args[2], true);
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                Help.sendMsg(sender, "§cPlease specify a world name when running from console.", true);
                return;
            }
            targetWorld = ((Player) sender).getWorld();
        }

        plugin.optimizeVillagersInWorld(targetWorld, sender);
    }

    /**
     * Show per-world villager statistics
     * GitHub: lines 951-1002
     */
    private void showStats(CommandSender sender) {
        Help.sendMsg(sender, "§6Villager Statistics by World:", true);
        int totalVillagers = 0;

        for (World world : Bukkit.getWorlds()) {
            int villagerCount = 0;
            int chunkCount = 0;
            Map<Chunk, Integer> villagersPerChunk = new HashMap<>();

            for (Entity entity : world.getEntities()) {
                if (entity instanceof Villager) {
                    villagerCount++;
                    totalVillagers++;
                    Chunk chunk = entity.getLocation().getChunk();
                    villagersPerChunk.put(chunk, villagersPerChunk.getOrDefault(chunk, 0) + 1);
                }
            }

            chunkCount = villagersPerChunk.size();
            int maxInChunk = 0;

            for (int count : villagersPerChunk.values()) {
                maxInChunk = Math.max(maxInChunk, count);
            }

            if (villagerCount > 0) {
                Help.sendMsg(sender,
                        "§e" + world.getName() + ": §b" + villagerCount +
                                " §evillagers in §b" + chunkCount +
                                " §echunks (max §b" + maxInChunk + " §eper chunk)",
                        false);
            }
        }

        Help.sendMsg(sender, "§eTotal villagers across all worlds: §b" + totalVillagers, false);
    }

    @Override
    public String getPermission() {
        return "lagx.villagers";
    }

    @Override
    public String getUsage() {
        return "/lagx villagers [status|reload|enable|disable|optimize|stats]";
    }

    @Override
    public String getDescription() {
        return "Villager optimizer controls";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(Arrays.asList("status", "reload", "enable", "disable", "optimize", "stats"));
        } else if (args.length == 3 && args[1].equalsIgnoreCase("optimize")) {
            completions.addAll(Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}