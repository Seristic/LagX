package com.seristic.hbzcleaner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.seristic.hbzcleaner.api.proto.Protocol;

public class LRTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - main commands
            List<String> commands = Arrays.asList(
                "help", "h",
                "master", "m", 
                "ram",
                "info", "i",
                "world", "w",
                "gc",
                "tps",
                "status",
                "modules", "mo",
                "protocol", "pr",
                "unload", "u",
                "ping", "p",
                "clear", "c",
                "count", "ct",
                "preset", "presets",
                "entities", "limiter",
                "villagers", "optimize",
                "towny", "town"
            );
            
            return filterCompletions(commands, args[0]);
        }
        
        if (args.length >= 2) {
            String mainCommand = args[0].toLowerCase();
            
            switch (mainCommand) {
                case "help":
                case "h":
                    return filterCompletions(Arrays.asList("1", "2", "3", "4", "5"), args[1]);
                
                case "world":
                case "w":
                    return getWorldNames(args[1]);
                
                case "protocol":
                case "pr":
                    return handleProtocolCompletion(args);
                
                case "unload":
                case "u":
                    return getWorldNames(args[1]);
                
                case "ping":
                case "p":
                    return getPlayerNames(args[1]);
                
                case "clear":
                case "c":
                case "count":
                case "ct":
                    return handleClearCompletion(args);
                
                case "preset":
                case "presets":
                    return filterCompletions(Arrays.asList("basic", "advanced", "custom"), args[1]);
                
                case "entities":
                    return handleEntitiesCompletion(args);
                
                case "limiter":
                    return handleLimiterCompletion(args);
                
                case "villagers":
                case "optimize":
                    return handleVillagerCompletion(args);
            }
        }
        
        return completions;
    }
    
    private List<String> handleProtocolCompletion(String[] args) {
        if (args.length == 2) {
            return filterCompletions(Arrays.asList("help", "h", "run", "r", "list", "l"), args[1]);
        }
        
        if (args.length == 3 && (args[1].equalsIgnoreCase("run") || args[1].equalsIgnoreCase("r"))) {
            // Get available protocols
            List<String> protocols = new ArrayList<>();
            try {
                Protocol.getProtocols().forEach(protocol -> protocols.add(protocol.id()));
            } catch (Exception e) {
                // Fallback protocols
                protocols.addAll(Arrays.asList("cc_items", "cc_entities", "lrgc"));
            }
            return filterCompletions(protocols, args[2]);
        }
        
        if (args.length == 4 && (args[1].equalsIgnoreCase("run") || args[1].equalsIgnoreCase("r"))) {
            return filterCompletions(Arrays.asList("true", "false"), args[3]);
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleClearCompletion(String[] args) {
        if (args.length == 2) {
            return filterCompletions(Arrays.asList("items", "i", "entities", "e", "type", "t", "area", "a"), args[1]);
        }
        
        if (args.length == 3) {
            String subCommand = args[1].toLowerCase();
            
            if (subCommand.equals("entities") || subCommand.equals("e")) {
                return filterCompletions(Arrays.asList("hostile", "h", "peaceful", "p", "all", "a"), args[2]);
            }
            
            if (subCommand.equals("type") || subCommand.equals("t")) {
                return filterCompletions(Arrays.asList("list", "l", "none", "n"), args[2]);
            }
            
            if (subCommand.equals("area") || subCommand.equals("a")) {
                return filterCompletions(Arrays.asList("c:1", "c:3", "c:5", "c:10", "b:50", "b:100", "b:200"), args[2]);
            }
        }
        
        if (args.length == 4) {
            String subCommand = args[1].toLowerCase();
            
            if (subCommand.equals("area") || subCommand.equals("a")) {
                return filterCompletions(Arrays.asList("all", "hostile", "peaceful", "items", "animals", "mobs"), args[3]);
            }
            
            // World names for most clear commands
            return getWorldNames(args[3]);
        }
        
        if (args.length >= 4 && (args[1].equalsIgnoreCase("type") || args[1].equalsIgnoreCase("t"))) {
            // Entity types for type clearing
            List<String> entityTypes = Arrays.stream(EntityType.values())
                .map(EntityType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
            return filterCompletions(entityTypes, args[args.length - 1]);
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleEntitiesCompletion(String[] args) {
        if (args.length == 2) {
            return filterCompletions(Arrays.asList("status", "reload", "enable", "disable", "limits", "stats"), args[1]);
        }
        
        if (args.length == 3 && args[1].equalsIgnoreCase("limits")) {
            return getWorldNames(args[2]);
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleLimiterCompletion(String[] args) {
        if (args.length == 2) {
            return filterCompletions(Arrays.asList("status", "reload", "enable", "disable", "check", "stats"), args[1]);
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleVillagerCompletion(String[] args) {
        if (args.length == 2) {
            return filterCompletions(Arrays.asList("status", "reload", "enable", "disable", "optimize", "stats"), args[1]);
        }
        
        if (args.length == 3 && args[1].equalsIgnoreCase("optimize")) {
            return getWorldNames(args[2]);
        }
        
        return new ArrayList<>();
    }
    
    private List<String> getWorldNames(String partial) {
        List<String> worldNames = Bukkit.getWorlds().stream()
            .map(World::getName)
            .collect(Collectors.toList());
        return filterCompletions(worldNames, partial);
    }
    
    private List<String> getPlayerNames(String partial) {
        List<String> playerNames = Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
        return filterCompletions(playerNames, partial);
    }
    
    private List<String> filterCompletions(List<String> options, String partial) {
        if (partial == null || partial.isEmpty()) {
            return options;
        }
        
        String lowerPartial = partial.toLowerCase();
        return options.stream()
            .filter(option -> option.toLowerCase().startsWith(lowerPartial))
            .collect(Collectors.toList());
    }
}
