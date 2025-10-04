package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command to count entities in worlds
 */
public class CountCommand extends LagXCommand {

    public CountCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String target = args[1].toLowerCase();

        if (target.equals("entities")) {
            countEntities(sender, args);
        } else if (target.equals("chunks")) {
            countChunks(sender);
        } else if (target.equals("worlds")) {
            countWorlds(sender);
        } else {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
        }

        return true;
    }

    private void countEntities(CommandSender sender, String[] args) {
        int totalEntities = 0;
        StringBuilder msg = new StringBuilder("§6Entity Count Summary:\n");

        for (World world : Bukkit.getWorlds()) {
            int worldEntities = world.getEntities().size();
            totalEntities += worldEntities;
            msg.append("§7- §e").append(world.getName())
                    .append("§7: §f").append(worldEntities).append(" entities\n");
        }

        msg.append("§6Total: §f").append(totalEntities).append(" entities");
        Help.sendMsg(sender, msg.toString(), true);
    }

    private void countChunks(CommandSender sender) {
        int totalChunks = 0;
        StringBuilder msg = new StringBuilder("§6Chunk Count Summary:\n");

        for (World world : Bukkit.getWorlds()) {
            int worldChunks = world.getLoadedChunks().length;
            totalChunks += worldChunks;
            msg.append("§7- §e").append(world.getName())
                    .append("§7: §f").append(worldChunks).append(" chunks\n");
        }

        msg.append("§6Total: §f").append(totalChunks).append(" loaded chunks");
        Help.sendMsg(sender, msg.toString(), true);
    }

    private void countWorlds(CommandSender sender) {
        List<World> worlds = Bukkit.getWorlds();
        StringBuilder msg = new StringBuilder("§6World Count Summary:\n");
        msg.append("§7Total worlds: §f").append(worlds.size()).append("\n");

        for (World world : worlds) {
            msg.append("§7- §e").append(world.getName())
                    .append(" §7(").append(world.getEnvironment().toString().toLowerCase()).append(")\n");
        }

        Help.sendMsg(sender, msg.toString(), true);
    }

    @Override
    public String getPermission() {
        return "lagx.count";
    }

    @Override
    public String getUsage() {
        return "/lagx count <entities|chunks|worlds>";
    }

    @Override
    public String getDescription() {
        return "Count entities, chunks, or worlds";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("entities", "chunks", "worlds");
        }
        return new ArrayList<>();
    }
}