package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to manage worlds and display world information
 */
public class WorldCommand extends LagXCommand {

    public WorldCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            listWorlds(sender);
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "list":
                listWorlds(sender);
                break;
            case "info":
                if (args.length < 3) {
                    Help.sendMsg(sender, "§cUsage: /lagx world info <world>", true);
                    return true;
                }
                showWorldInfo(sender, args[2]);
                break;
            case "tp":
                if (!(sender instanceof Player)) {
                    Help.sendMsg(sender, "§cOnly players can use this command.", true);
                    return true;
                }
                if (args.length < 3) {
                    Help.sendMsg(sender, "§cUsage: /lagx world tp <world>", true);
                    return true;
                }
                teleportToWorld((Player) sender, args[2]);
                break;
            default:
                Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
        }

        return true;
    }

    private void listWorlds(CommandSender sender) {
        List<World> worlds = Bukkit.getWorlds();
        StringBuilder msg = new StringBuilder("§6Available Worlds:\n");

        for (World world : worlds) {
            int entities = world.getEntities().size();
            int chunks = world.getLoadedChunks().length;
            int players = world.getPlayers().size();

            msg.append("§7- §e").append(world.getName())
                    .append(" §7(").append(world.getEnvironment().toString().toLowerCase()).append(")\n")
                    .append("  §7Players: §f").append(players)
                    .append(" §7| Entities: §f").append(entities)
                    .append(" §7| Chunks: §f").append(chunks).append("\n");
        }

        Help.sendMsg(sender, msg.toString(), true);
    }

    private void showWorldInfo(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Help.sendMsg(sender, "§cWorld '" + worldName + "' not found!", true);
            return;
        }

        StringBuilder info = new StringBuilder("§6World Information - §e").append(world.getName()).append("\n");
        info.append("§7Environment: §f").append(world.getEnvironment().toString().toLowerCase()).append("\n");
        info.append("§7Seed: §f").append(world.getSeed()).append("\n");
        info.append("§7Players: §f").append(world.getPlayers().size()).append("\n");
        info.append("§7Entities: §f").append(world.getEntities().size()).append("\n");
        info.append("§7Loaded Chunks: §f").append(world.getLoadedChunks().length).append("\n");
        info.append("§7Spawn Location: §f").append(world.getSpawnLocation().getBlockX())
                .append(", ").append(world.getSpawnLocation().getBlockY())
                .append(", ").append(world.getSpawnLocation().getBlockZ()).append("\n");
        info.append("§7Difficulty: §f").append(world.getDifficulty().toString().toLowerCase()).append("\n");
        info.append("§7PvP: §f").append(world.getPVP() ? "Enabled" : "Disabled").append("\n");
        info.append("§7Game Rules:\n");
        info.append("  §7- Keep Inventory: §f").append(world.getGameRuleValue("keepInventory")).append("\n");
        info.append("  §7- Mob Spawning: §f").append(world.getGameRuleValue("doMobSpawning")).append("\n");
        info.append("  §7- Day/Night Cycle: §f").append(world.getGameRuleValue("doDaylightCycle"));

        Help.sendMsg(sender, info.toString(), true);
    }

    private void teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Help.sendMsg(player, "§cWorld '" + worldName + "' not found!", true);
            return;
        }

        player.teleport(world.getSpawnLocation());
        Help.sendMsg(player, "§aTeleported to world: §e" + world.getName(), true);
    }

    @Override
    public String getPermission() {
        return "lagx.world";
    }

    @Override
    public String getUsage() {
        return "/lagx world [list|info|tp] [world]";
    }

    @Override
    public String getDescription() {
        return "Manage and view world information";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.add("list");
            completions.add("info");
            if (sender instanceof Player) {
                completions.add("tp");
            }
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("tp"))) {
            completions.addAll(Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}