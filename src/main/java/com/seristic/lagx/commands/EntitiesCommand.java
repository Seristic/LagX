package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity limiter overview and statistics command
 * GitHub: lines 1135-1229 in LagX.java
 */
public class EntitiesCommand extends LagXCommand {

    public EntitiesCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "info":
            case "i":
                showInfo(sender);
                break;
            case "count":
            case "c":
                showCount(sender);
                break;
            case "stats":
            case "s":
                showStats(sender);
                break;
            default:
                Help.sendMsg(sender, "§cUnknown subcommand. Available options: info, count, stats", true);
        }

        return true;
    }

    /**
     * Show entity limiter configuration
     * GitHub: lines 1143-1158
     */
    private void showInfo(CommandSender sender) {
        if (plugin.getEntityLimiter() != null && plugin.getEntityLimiter().isEnabled()) {
            Help.sendMsg(sender, "§6Entity Limiter Configuration:", true);
            Help.sendMsg(sender, plugin.getEntityLimiter().getStatus(), false);
        } else {
            Help.sendMsg(sender, "§cEntity Limiter is not enabled on this server.", true);
        }
    }

    /**
     * Global count by entity type (top 10)
     * GitHub: lines 1159-1197
     */
    private void showCount(CommandSender sender) {
        Map<String, Integer> entityCounts = new HashMap<>();
        int totalEntities = 0;

        // Count all entities across all worlds
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String type = entity.getType().toString();
                entityCounts.put(type, entityCounts.getOrDefault(type, 0) + 1);
                totalEntities++;
            }
        }

        Help.sendMsg(sender, "§6Entity Count by Type:", true);
        Help.sendMsg(sender, "§eTotal Entities: §b" + totalEntities, false);

        // Sort by count and show top 10
        List<Map.Entry<String, Integer>> sortedEntities = new ArrayList<>(entityCounts.entrySet());
        sortedEntities.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        int shown = 0;
        for (Map.Entry<String, Integer> entry : sortedEntities) {
            if (shown >= 10) {
                break;
            }
            Help.sendMsg(sender, "§e" + entry.getKey() + ": §b" + entry.getValue(), false);
            shown++;
        }
    }

    /**
     * Per-world entity statistics with averages
     * GitHub: lines 1198-1227
     */
    private void showStats(CommandSender sender) {
        Help.sendMsg(sender, "§6Entity Statistics by World:", true);

        for (World world : Bukkit.getWorlds()) {
            int entityCount = world.getEntities().size();
            int chunkCount = world.getLoadedChunks().length;
            double avgPerChunk = chunkCount > 0 ? (double) entityCount / (double) chunkCount : 0.0;

            Help.sendMsg(sender,
                    "§e" + world.getName() + ": §b" + entityCount +
                            " §eentities in §b" + chunkCount +
                            " §echunks (avg: §b" + String.format("%.2f", avgPerChunk) + " §eper chunk)",
                    false);
        }
    }

    @Override
    public String getPermission() {
        return "lagx.entities";
    }

    @Override
    public String getUsage() {
        return "/lagx entities [info|count|stats]";
    }

    @Override
    public String getDescription() {
        return "Entity limiter overview and stats";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(Arrays.asList("info", "i", "count", "c", "stats", "s"));
        }

        return completions;
    }
}