package com.seristic.lagx.util;

import com.seristic.lagx.api.proto.Protocol;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

public class HBZTabCompleter implements TabCompleter {
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         List<String> commands = Arrays.asList(
               "help",
               "h",
               "master",
               "m",
               "ram",
               "info",
               "i",
               "world",
               "w",
               "gc",
               "tps",
               "status",
               "modules",
               "mo",
               "protocol",
               "pr",
               "unload",
               "u",
               "ping",
               "p",
               "clear",
               "c",
               "count",
               "ct",
               "preset",
               "presets",
               "entities",
               "limiter",
               "villagers",
               "optimize",
               "towny",
               "town",
               "stacker",
               "stack",
               "reload",
               "rl",
               "warnings",
               "warn",
               "test");
         return this.filterCompletions(commands, args[0]);
      } else {
         if (args.length >= 2) {
            String mainCommand = args[0].toLowerCase();
            switch (mainCommand) {
               case "help":
               case "h":
                  return this.filterCompletions(Arrays.asList("1", "2", "3", "4", "5"), args[1]);
               case "world":
               case "w":
                  return this.getWorldNames(args[1]);
               case "protocol":
               case "pr":
                  return this.handleProtocolCompletion(args);
               case "unload":
               case "u":
                  return this.getWorldNames(args[1]);
               case "ping":
               case "p":
                  return this.getPlayerNames(args[1]);
               case "clear":
               case "c":
               case "count":
               case "ct":
                  return this.handleClearCompletion(args);
               case "preset":
               case "presets":
                  // Expect: /lagx preset [info|set] and then preset names when set is chosen
                  if (args.length == 2) {
                     return this.filterCompletions(Arrays.asList("info", "set"), args[1]);
                  }
                  if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
                     return this.filterCompletions(Arrays.asList("basic", "advanced", "custom"), args[2]);
                  }
                  return new ArrayList<>();
               case "entities":
                  return this.handleEntitiesCompletion(args);
               case "limiter":
                  return this.handleLimiterCompletion(args);
               case "villagers":
               case "optimize":
                  return this.handleVillagerCompletion(args);
               case "stacker":
               case "stack":
                  return this.filterCompletions(Arrays.asList("info", "debug", "reload", "stack"), args[1]);
               case "test":
                  return this.handleTestCompletion(args);
               case "warnings":
               case "warn":
                  return this.filterCompletions(Arrays.asList("status", "on", "off", "toggle"), args[1]);
            }
         }

         return completions;
      }
   }

   private List<String> handleProtocolCompletion(String[] args) {
      if (args.length == 2) {
         return this.filterCompletions(Arrays.asList("help", "h", "run", "r", "list", "l"), args[1]);
      } else if (args.length != 3 || !args[1].equalsIgnoreCase("run") && !args[1].equalsIgnoreCase("r")) {
         return (List<String>) (args.length != 4 || !args[1].equalsIgnoreCase("run") && !args[1].equalsIgnoreCase("r")
               ? new ArrayList<>()
               : this.filterCompletions(Arrays.asList("true", "false"), args[3]));
      } else {
         List<String> protocols = new ArrayList<>();

         try {
            Protocol.getProtocols().forEach(protocol -> protocols.add(protocol.id()));
         } catch (Exception var4) {
            protocols.addAll(Arrays.asList("cc_items", "cc_entities", "lrgc"));
         }

         return this.filterCompletions(protocols, args[2]);
      }
   }

   private List<String> handleClearCompletion(String[] args) {
      if (args.length == 2) {
         return this.filterCompletions(Arrays.asList("items", "i", "entities", "e", "type", "t", "area", "a"), args[1]);
      } else {
         if (args.length == 3) {
            String subCommand = args[1].toLowerCase();
            if (subCommand.equals("entities") || subCommand.equals("e")) {
               return this.filterCompletions(Arrays.asList("hostile", "h", "peaceful", "p", "all", "a"), args[2]);
            }

            if (subCommand.equals("type") || subCommand.equals("t")) {
               return this.filterCompletions(Arrays.asList("list", "l", "none", "n"), args[2]);
            }

            if (subCommand.equals("area") || subCommand.equals("a")) {
               return this.filterCompletions(Arrays.asList("c:1", "c:3", "c:5", "c:10", "b:50", "b:100", "b:200"),
                     args[2]);
            }
         }

         if (args.length == 4) {
            String subCommandx = args[1].toLowerCase();
            return !subCommandx.equals("area") && !subCommandx.equals("a")
                  ? this.getWorldNames(args[3])
                  : this.filterCompletions(Arrays.asList("all", "hostile", "peaceful", "items", "animals", "mobs"),
                        args[3]);
         } else if (args.length < 4 || !args[1].equalsIgnoreCase("type") && !args[1].equalsIgnoreCase("t")) {
            return new ArrayList<>();
         } else {
            List<String> entityTypes = Arrays.stream(EntityType.values()).map(Enum::name).map(String::toLowerCase)
                  .collect(Collectors.toList());
            return this.filterCompletions(entityTypes, args[args.length - 1]);
         }
      }
   }

   private List<String> handleEntitiesCompletion(String[] args) {
      // Expect: /lagx entities [info|count|stats]
      if (args.length == 2) {
         return this.filterCompletions(Arrays.asList("info", "i", "count", "c", "stats", "s"), args[1]);
      }
      return new ArrayList<>();
   }

   private List<String> handleLimiterCompletion(String[] args) {
      // Provide simple control over limiter component
      if (args.length == 2) {
         return this.filterCompletions(Arrays.asList("info", "status", "reload", "enable", "disable"), args[1]);
      }
      return new ArrayList<>();
   }

   private List<String> handleTestCompletion(String[] args) {
      if (args.length == 2) {
         return this.filterCompletions(
               Arrays.asList("death", "unprotected", "stack", "clear", "spawn", "protection", "full"), args[1]);
      } else if (args.length == 3 && args[1].equalsIgnoreCase("spawn")) {
         return this.filterCompletions(Arrays.asList("zombie", "item", "skeleton", "cow", "pig", "chicken"), args[2]);
      } else {
         return (List<String>) (args.length == 4 && args[1].equalsIgnoreCase("spawn")
               ? this.filterCompletions(Arrays.asList("1", "5", "10", "20", "50"), args[3])
               : new ArrayList<>());
      }
   }

   private List<String> handleVillagerCompletion(String[] args) {
      if (args.length == 2) {
         return this.filterCompletions(Arrays.asList("status", "reload", "enable", "disable", "optimize", "stats"),
               args[1]);
      } else {
         return (List<String>) (args.length == 3 && args[1].equalsIgnoreCase("optimize") ? this.getWorldNames(args[2])
               : new ArrayList<>());
      }
   }

   private List<String> getWorldNames(String partial) {
      List<String> worldNames = Bukkit.getWorlds().stream().<String>map(WorldInfo::getName)
            .collect(Collectors.toList());
      return this.filterCompletions(worldNames, partial);
   }

   private List<String> getPlayerNames(String partial) {
      List<String> playerNames = Bukkit.getOnlinePlayers().stream().<String>map(Player::getName)
            .collect(Collectors.toList());
      return this.filterCompletions(playerNames, partial);
   }

   private List<String> filterCompletions(List<String> options, String partial) {
      if (partial != null && !partial.isEmpty()) {
         String lowerPartial = partial.toLowerCase();
         return options.stream().filter(option -> option.toLowerCase().startsWith(lowerPartial))
               .collect(Collectors.toList());
      } else {
         return options;
      }
   }
}
