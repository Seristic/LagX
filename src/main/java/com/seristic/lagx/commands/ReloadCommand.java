package com.seristic.lagx.commands;

import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command to reload plugin configuration and features
 */
public class ReloadCommand extends LagXCommand {

    public ReloadCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            long startTime = System.currentTimeMillis();

            Help.sendMsg(sender, "§6Reloading LagX configuration...", true);

            // Reload configuration
            plugin.reloadConfig();
            FileConfiguration config = plugin.getConfig();

            // Reinitialize features based on new configuration
            if (plugin.getEntityLimiter() != null) {
                plugin.getEntityLimiter().reloadConfig();
            }

            if (plugin.getVillagerOptimizer() != null) {
                plugin.getVillagerOptimizer().reloadConfig();
            }

            if (plugin.getEntityStacker() != null) {
                plugin.getEntityStacker().reloadConfig();
            }

            if (plugin.getItemFrameOptimizer() != null) {
                plugin.getItemFrameOptimizer().reloadConfig();
            }

            // DISABLED - Map protection feature not ready
            // if (plugin.getMapArtManager() != null) {
            // plugin.getMapArtManager().reloadConfig();
            // }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Help.sendMsg(sender, "§aLagX configuration reloaded successfully! §7(" + duration + "ms)", true);

        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
            Help.sendMsg(sender, "§cError reloading configuration! Check console for details.", true);
        }

        return true;
    }

    @Override
    public String getPermission() {
        return "lagx.reload";
    }

    @Override
    public String getUsage() {
        return "/lagx reload";
    }

    @Override
    public String getDescription() {
        return "Reload plugin configuration and features";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>(); // No tab completions needed
    }
}