package com.seristic.lagx.commands;

import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.api.proto.Protocol;
import com.seristic.lagx.inf.Help;
import com.seristic.lagx.main.LagX;
import com.seristic.lagx.proto.bin.CCEntities;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command to clear items and entities using the protocol system
 */
public class ClearCommand extends LagXCommand {

    public ClearCommand(LagX plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
            return true;
        }

        String target = args[1].toLowerCase();

        try {
            // Support both full names and abbreviations
            if (target.equals("items") || target.equals("i")) {
                clearItems(sender, args);
            } else if (target.equals("entities") || target.equals("e")) {
                clearEntities(sender, args);
            } else if (target.equals("all")) {
                clearAll(sender, args);
            } else if (target.equals("area") || target.equals("a")) {
                clearArea(sender, args);
            } else {
                // Try to parse as entity type
                EntityType entityType = parseEntityType(target);
                if (entityType != null) {
                    clearEntityType(sender, entityType, args);
                } else {
                    Help.sendMsg(sender, "§cUsage: " + getUsage(), true);
                }
            }
        } catch (Exception e) {
            Help.sendMsg(sender, "§cError executing clear command: " + e.getMessage(), true);
            plugin.getLogger().warning("Clear command error: " + e.getMessage());
        }

        return true;
    }

    private void clearItems(CommandSender sender, String[] args) {
        World targetWorld = null;

        // Check if world is specified, otherwise use player's world
        if (args.length > 2) {
            targetWorld = Bukkit.getWorld(args[2]);
            if (targetWorld == null) {
                Help.sendMsg(sender, "§cWorld '" + args[2] + "' not found!", true);
                return;
            }
        } else if (sender instanceof Player) {
            // Default to player's current world when no world specified
            targetWorld = ((Player) sender).getWorld();
        }
        // If sender is console and no world specified, clear all worlds (targetWorld
        // stays null)

        try {
            LRProtocolResult result;
            if (targetWorld != null) {
                // Clear items from specific world
                result = Protocol.run("cc_items", new Object[] { false, targetWorld });
            } else {
                // Clear items from all worlds
                result = Protocol.run("cc_items", new Object[] { false });
            }

            if (result != null && result.getData().length > 0) {
                int cleared = (Integer) result.getData()[0];
                String worldMsg = targetWorld != null ? " in world " + targetWorld.getName() : " across all worlds";
                Help.sendMsg(sender, "§aCleared " + cleared + " items" + worldMsg + "!", true);
            } else {
                Help.sendMsg(sender, "§eNo items found to clear.", true);
            }
        } catch (Exception e) {
            Help.sendMsg(sender, "§cError clearing items: " + e.getMessage(), true);
        }
    }

    private void clearEntities(CommandSender sender, String[] args) {
        World targetWorld = null;
        EntityType[] entityTypes = null;

        // Parse additional arguments
        if (args.length > 2) {
            String arg = args[2];
            if (arg.equalsIgnoreCase("hostile")) {
                entityTypes = CCEntities.hostile;
            } else if (arg.equalsIgnoreCase("peaceful")) {
                entityTypes = CCEntities.peaceful;
            } else {
                targetWorld = Bukkit.getWorld(arg);
                if (targetWorld == null) {
                    Help.sendMsg(sender, "§cWorld '" + arg + "' not found!", true);
                    return;
                }
            }
        }

        // If no world specified and sender is player, use player's world
        if (targetWorld == null && sender instanceof Player) {
            targetWorld = ((Player) sender).getWorld();
        }

        try {
            LRProtocolResult result;
            if (targetWorld != null) {
                result = Protocol.run("cc_entities", new Object[] { false, entityTypes, targetWorld });
            } else {
                result = Protocol.run("cc_entities", new Object[] { false, entityTypes });
            }

            if (result != null && result.getData().length > 0) {
                int cleared = (Integer) result.getData()[0];
                String typeMsg = entityTypes == CCEntities.hostile ? " hostile"
                        : entityTypes == CCEntities.peaceful ? " peaceful" : "";
                String worldMsg = targetWorld != null ? " in world " + targetWorld.getName() : " across all worlds";
                Help.sendMsg(sender, "§aCleared " + cleared + typeMsg + " entities" + worldMsg + "!", true);
            } else {
                Help.sendMsg(sender, "§eNo entities found to clear.", true);
            }
        } catch (Exception e) {
            Help.sendMsg(sender, "§cError clearing entities: " + e.getMessage(), true);
        }
    }

    private void clearEntityType(CommandSender sender, EntityType entityType, String[] args) {
        World targetWorld = null;

        if (args.length > 2) {
            targetWorld = Bukkit.getWorld(args[2]);
            if (targetWorld == null) {
                Help.sendMsg(sender, "§cWorld '" + args[2] + "' not found!", true);
                return;
            }
        }

        try {
            LRProtocolResult result;
            EntityType[] types = new EntityType[] { entityType };

            if (targetWorld != null) {
                result = Protocol.run("cc_entities", new Object[] { false, types, targetWorld });
            } else {
                result = Protocol.run("cc_entities", new Object[] { false, types, true });
            }

            if (result != null && result.getData().length > 0) {
                int cleared = (Integer) result.getData()[0];
                String worldMsg = targetWorld != null ? " in world " + targetWorld.getName() : " across all worlds";
                Help.sendMsg(sender, "§aCleared " + cleared + " " + entityType.name().toLowerCase() +
                        " entities" + worldMsg + "!", true);
            } else {
                Help.sendMsg(sender, "§eNo " + entityType.name().toLowerCase() + " entities found to clear.", true);
            }
        } catch (Exception e) {
            Help.sendMsg(sender, "§cError clearing " + entityType.name().toLowerCase() + " entities: " + e.getMessage(),
                    true);
        }
    }

    private void clearAll(CommandSender sender, String[] args) {
        World targetWorld = null;

        if (args.length > 2) {
            targetWorld = Bukkit.getWorld(args[2]);
            if (targetWorld == null) {
                Help.sendMsg(sender, "§cWorld '" + args[2] + "' not found!", true);
                return;
            }
        }

        try {
            // Clear items first
            LRProtocolResult itemResult;
            if (targetWorld != null) {
                itemResult = Protocol.run("cc_items", new Object[] { false, targetWorld });
            } else {
                itemResult = Protocol.run("cc_items", new Object[] { false });
            }

            // Clear entities
            LRProtocolResult entityResult;
            if (targetWorld != null) {
                entityResult = Protocol.run("cc_entities", new Object[] { false, (EntityType[]) null, targetWorld });
            } else {
                entityResult = Protocol.run("cc_entities", new Object[] { false, (EntityType[]) null, true });
            }

            int clearedItems = (itemResult != null && itemResult.getData().length > 0)
                    ? (Integer) itemResult.getData()[0]
                    : 0;
            int clearedEntities = (entityResult != null && entityResult.getData().length > 0)
                    ? (Integer) entityResult.getData()[0]
                    : 0;

            String worldMsg = targetWorld != null ? " in world " + targetWorld.getName() : " across all worlds";
            Help.sendMsg(sender, "§aCleared " + clearedItems + " items and " + clearedEntities +
                    " entities" + worldMsg + "!", true);

        } catch (Exception e) {
            Help.sendMsg(sender, "§cError clearing all: " + e.getMessage(), true);
        }
    }

    private void clearArea(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Help.sendMsg(sender, "§cOnly players can use area clearing!", true);
            return;
        }

        Player player = (Player) sender;
        Chunk chunk = player.getLocation().getChunk();

        try {
            // Clear items in chunk
            LRProtocolResult itemResult = Protocol.run("cc_items", new Object[] { false, chunk });

            // Clear entities in chunk
            LRProtocolResult entityResult = Protocol.run("cc_entities",
                    new Object[] { false, (EntityType[]) null, chunk });

            int clearedItems = (itemResult != null && itemResult.getData().length > 0)
                    ? (Integer) itemResult.getData()[0]
                    : 0;
            int clearedEntities = (entityResult != null && entityResult.getData().length > 0)
                    ? (Integer) entityResult.getData()[0]
                    : 0;

            Help.sendMsg(sender, "§aCleared " + clearedItems + " items and " + clearedEntities +
                    " entities from your current chunk!", true);

        } catch (Exception e) {
            Help.sendMsg(sender, "§cError clearing area: " + e.getMessage(), true);
        }
    }

    private EntityType parseEntityType(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try common aliases
            switch (name.toLowerCase()) {
                case "zombie":
                case "zombies":
                    return EntityType.ZOMBIE;
                case "skeleton":
                case "skeletons":
                    return EntityType.SKELETON;
                case "creeper":
                case "creepers":
                    return EntityType.CREEPER;
                case "spider":
                case "spiders":
                    return EntityType.SPIDER;
                case "cow":
                case "cows":
                    return EntityType.COW;
                case "pig":
                case "pigs":
                    return EntityType.PIG;
                case "chicken":
                case "chickens":
                    return EntityType.CHICKEN;
                case "sheep":
                    return EntityType.SHEEP;
                case "villager":
                case "villagers":
                    return EntityType.VILLAGER;
                default:
                    return null;
            }
        }
    }

    @Override
    public String getPermission() {
        return "lagx.clear";
    }

    @Override
    public String getUsage() {
        return "/lagx clear <items(i)|entities(e)|all|area(a)|<entity_type>> [world]";
    }

    @Override
    public String getDescription() {
        return "Clear items, entities, or both from worlds/areas";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // args[0] = "clear", args[1] = first subcommand, args[2] = second subcommand,
        // etc.

        if (args.length == 2) {
            // First argument after "clear" - show main options
            String partial = args[1].toLowerCase();

            // Include abbreviations in tab completion
            List<String> options = Arrays.asList("items", "i", "entities", "e", "all", "area", "a");

            // Filter by what user is typing
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }

            // Also suggest common entity types
            List<String> entityTypes = Arrays.asList("zombie", "skeleton", "creeper", "spider", "enderman",
                    "cow", "pig", "sheep", "chicken", "villager", "item_frame", "armor_stand");
            for (String type : entityTypes) {
                if (type.startsWith(partial)) {
                    completions.add(type);
                }
            }

        } else if (args.length == 3) {
            // Second argument after "clear <subcommand>"
            String subCmd = args[1].toLowerCase();
            String partial = args[2].toLowerCase();

            switch (subCmd) {
                case "items":
                case "i":
                case "all":
                    // These accept world as arg 3
                    for (World world : Bukkit.getWorlds()) {
                        if (world.getName().toLowerCase().startsWith(partial)) {
                            completions.add(world.getName());
                        }
                    }
                    break;

                case "entities":
                case "e":
                    // Entities can take hostile/peaceful/world
                    if ("hostile".startsWith(partial))
                        completions.add("hostile");
                    if ("peaceful".startsWith(partial))
                        completions.add("peaceful");

                    for (World world : Bukkit.getWorlds()) {
                        if (world.getName().toLowerCase().startsWith(partial)) {
                            completions.add(world.getName());
                        }
                    }
                    break;

                case "area":
                case "a":
                    // Area command doesn't take additional arguments
                    break;

                default:
                    // For entity types, suggest worlds
                    if (parseEntityType(subCmd) != null) {
                        for (World world : Bukkit.getWorlds()) {
                            if (world.getName().toLowerCase().startsWith(partial)) {
                                completions.add(world.getName());
                            }
                        }
                    }
                    break;
            }
        }

        return completions;
    }
}