package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to unload chunks for performance optimization
 */
public class UnloadCommand extends LagXCommand {

    public UnloadCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "chunks":
                if (args.length >= 3) {
                    unloadChunksInWorld(sender, args[2]);
                } else {
                    unloadChunksInAllWorlds(sender);
                }
                break;
            case "unused":
                unloadUnusedChunks(sender);
                break;
            case "world":
                if (args.length < 3) {
                    Help.sendMsg(sender, "§cUsage: /lagx unload world <world>", true);
                    return true;
                }
                unloadWorld(sender, args[2]);
                break;
            default:
                Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
        }

        return true;
    }

    private void unloadChunksInAllWorlds(CommandSender sender) {
        int totalUnloaded = 0;
        StringBuilder msg = new StringBuilder("§6Unloading chunks in all worlds...\n");

        for (World world : Bukkit.getWorlds()) {
            int unloaded = unloadChunksInWorldInternal(world);
            totalUnloaded += unloaded;
            msg.append("§7- §e").append(world.getName())
                    .append("§7: §f").append(unloaded).append(" chunks unloaded\n");
        }

        msg.append("§6Total: §f").append(totalUnloaded).append(" chunks unloaded");
        Help.sendMsg(sender, msg.toString(), true);
    }

    private void unloadChunksInWorld(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Help.sendMsg(sender, "§cWorld '" + worldName + "' not found!", true);
            return;
        }

        int unloaded = unloadChunksInWorldInternal(world);
        Help.sendMsg(sender, "§6Unloaded §f" + unloaded + " §6chunks in world §e" + world.getName(), true);
    }

    private int unloadChunksInWorldInternal(World world) {
        Chunk[] loadedChunks = world.getLoadedChunks();
        int unloaded = 0;

        for (Chunk chunk : loadedChunks) {
            // Don't unload chunks with players nearby
            boolean hasNearbyPlayers = world.getPlayers().stream()
                    .anyMatch(player -> {
                        Chunk playerChunk = player.getLocation().getChunk();
                        int chunkX = chunk.getX();
                        int chunkZ = chunk.getZ();
                        int playerX = playerChunk.getX();
                        int playerZ = playerChunk.getZ();

                        // Keep chunks within 3 chunk radius of players
                        return Math.abs(chunkX - playerX) <= 3 && Math.abs(chunkZ - playerZ) <= 3;
                    });

            if (!hasNearbyPlayers && chunk.unload(true)) {
                unloaded++;
            }
        }

        return unloaded;
    }

    private void unloadUnusedChunks(CommandSender sender) {
        int totalUnloaded = 0;
        StringBuilder msg = new StringBuilder("§6Unloading unused chunks...\n");

        for (World world : Bukkit.getWorlds()) {
            Chunk[] loadedChunks = world.getLoadedChunks();
            int worldUnloaded = 0;

            for (Chunk chunk : loadedChunks) {
                // Check if chunk is truly unused (no players, no important structures)
                boolean isUnused = world.getPlayers().stream()
                        .noneMatch(player -> {
                            Chunk playerChunk = player.getLocation().getChunk();
                            return Math.abs(chunk.getX() - playerChunk.getX()) <= 5
                                    && Math.abs(chunk.getZ() - playerChunk.getZ()) <= 5;
                        });

                if (isUnused && chunk.getEntities().length == 0 && chunk.unload(true)) {
                    worldUnloaded++;
                }
            }

            totalUnloaded += worldUnloaded;
            if (worldUnloaded > 0) {
                msg.append("§7- §e").append(world.getName())
                        .append("§7: §f").append(worldUnloaded).append(" unused chunks unloaded\n");
            }
        }

        msg.append("§6Total: §f").append(totalUnloaded).append(" unused chunks unloaded");
        Help.sendMsg(sender, msg.toString(), true);
    }

    private void unloadWorld(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Help.sendMsg(sender, "§cWorld '" + worldName + "' not found!", true);
            return;
        }

        if (world.getPlayers().size() > 0) {
            Help.sendMsg(sender, "§cCannot unload world '" + worldName + "' - players are still in it!", true);
            return;
        }

        if (Bukkit.unloadWorld(world, true)) {
            Help.sendMsg(sender, "§6Successfully unloaded world: §e" + worldName, true);
        } else {
            Help.sendMsg(sender, "§cFailed to unload world: §e" + worldName, true);
        }
    }

    @Override
    public String getPermission() {
        return "lagx.unload";
    }

    @Override
    public String getUsage() {
        return "/lagx unload <chunks|unused|world> [world]";
    }

    @Override
    public String getDescription() {
        return "Unload chunks or worlds to free memory";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(Arrays.asList("chunks", "unused", "world"));
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("chunks") || args[1].equalsIgnoreCase("world")) {
                completions.addAll(Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList()));
            }
        }

        return completions;
    }
}