package com.seristic.lagx.commands;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.inf.Help;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Modern command manager for LagX plugin
 * Handles all commands with proper tab completion and permission checking
 */
public class CommandManager implements TabCompleter {

    private final LagX plugin;
    private final Map<String, LagXCommand> commands = new HashMap<>();
    private final Map<String, String> aliases = new HashMap<>();

    public CommandManager(LagX plugin) {
        this.plugin = plugin;
        registerCommands();

        // Set this as tab completer for the lagx command
        Objects.requireNonNull(plugin.getCommand("lagx")).setTabCompleter(this);
    }

    /**
     * Register all commands and their aliases
     */
    private void registerCommands() {
        // Basic commands
        registerCommand(new HelpCommand(plugin), "help", "h");
        registerCommand(new PingCommand(plugin), "ping", "p");
        registerCommand(new TpsCommand(plugin), "tps");
        registerCommand(new RamCommand(plugin), "ram");
        registerCommand(new GcCommand(plugin), "gc");
        registerCommand(new InfoCommand(plugin), "info", "i");
        registerCommand(new StatusCommand(plugin), "status", "master", "m");
        registerCommand(new ProtocolCommand(plugin), "protocol", "pr");

        // Utility commands
        registerCommand(new ClearCommand(plugin), "clear", "c");
        registerCommand(new CountCommand(plugin), "count", "ct");
        registerCommand(new WorldCommand(plugin), "world", "w");
        registerCommand(new UnloadCommand(plugin), "unload", "u");

        // Feature commands - now with proper method implementations in managers
        registerCommand(new StackerCommand(plugin), "stacker", "stack");
        registerCommand(new EntitiesCommand(plugin), "entities", "ent");
        registerCommand(new LimiterCommand(plugin), "limiter", "lim");
        registerCommand(new TownyCommand(plugin), "towny", "town");
        registerCommand(new PresetCommand(plugin), "preset");
        registerCommand(new VillagersCommand(plugin), "villagers", "optimize");
        registerCommand(new ReloadCommand(plugin), "reload", "rl");
        registerCommand(new WarningsCommand(plugin), "warnings", "warn");

        // Map art protection commands
        registerCommand(new MapArtCommand(plugin), "mapart", "map");
    }

    /**
     * Register a command with its aliases
     */
    private void registerCommand(LagXCommand command, String name, String... aliases) {
        commands.put(name.toLowerCase(), command);
        for (String alias : aliases) {
            this.aliases.put(alias.toLowerCase(), name.toLowerCase());
        }
    }

    /**
     * Handle a command execution
     */
    public boolean handleCommand(CommandSender sender, String[] args) {
        try {
            if (args.length == 0) {
                // No arguments - show help
                if (hasPermission(sender, "lagx.help")) {
                    Help.send(sender instanceof Player ? (Player) sender : null, 1);
                    return true;
                } else {
                    Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
                    return true;
                }
            }

            String commandName = args[0].toLowerCase();

            // Check for alias
            if (aliases.containsKey(commandName)) {
                commandName = aliases.get(commandName);
            }

            LagXCommand command = commands.get(commandName);
            if (command != null) {
                // Check permission before executing
                String permission = command.getPermission();
                if (permission != null && !hasPermission(sender, permission)) {
                    Help.sendMsg(sender, "§cYou don't have permission to use this command.", true);
                    return true;
                }

                return command.execute(sender, args);
            }

            // Unknown command
            Help.sendMsg(sender, "§cUnknown command. Use §e/lagx help §cfor a list of commands.", true);
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            Help.sendMsg(sender, "§cAn error occurred while executing the command.", true);
            return true;
        }
    }

    /**
     * Handle tab completion
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias,
            String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                // Complete first argument (command names)
                String partial = args[0].toLowerCase();

                // Add all commands that the sender has permission for
                for (Map.Entry<String, LagXCommand> entry : commands.entrySet()) {
                    String cmdName = entry.getKey();
                    LagXCommand cmd = entry.getValue();

                    // Check permission
                    String permission = cmd.getPermission();
                    if (permission == null || hasPermission(sender, permission)) {
                        if (cmdName.startsWith(partial)) {
                            completions.add(cmdName);
                        }
                    }
                }

                // Add aliases
                for (Map.Entry<String, String> entry : aliases.entrySet()) {
                    String aliasName = entry.getKey();
                    String realCommand = entry.getValue();
                    LagXCommand cmd = commands.get(realCommand);

                    if (cmd != null) {
                        String permission = cmd.getPermission();
                        if (permission == null || hasPermission(sender, permission)) {
                            if (aliasName.startsWith(partial)) {
                                completions.add(aliasName);
                            }
                        }
                    }
                }

            } else if (args.length > 1) {
                // Complete subcommands
                String commandName = args[0].toLowerCase();

                // Check for alias
                if (aliases.containsKey(commandName)) {
                    commandName = aliases.get(commandName);
                }

                LagXCommand cmd = commands.get(commandName);
                if (cmd != null) {
                    // Check permission
                    String permission = cmd.getPermission();
                    if (permission == null || hasPermission(sender, permission)) {
                        // Get tab completions from the command
                        List<String> subCompletions = cmd.getTabCompletions(sender, args);
                        if (subCompletions != null) {
                            completions.addAll(subCompletions);
                        }
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error in tab completion: " + e.getMessage());
        }

        // Sort and return
        Collections.sort(completions);
        return completions;
    }

    /**
     * Get all registered command names
     */
    public Set<String> getCommandNames() {
        return new HashSet<>(commands.keySet());
    }

    /**
     * Get all registered aliases
     */
    public Set<String> getAliases() {
        return new HashSet<>(aliases.keySet());
    }

    /**
     * Get all available commands (including aliases) for a sender
     */
    public List<String> getAvailableCommands(CommandSender sender) {
        List<String> available = new ArrayList<>();

        for (Map.Entry<String, LagXCommand> entry : commands.entrySet()) {
            String permission = entry.getValue().getPermission();
            if (permission == null || hasPermission(sender, permission)) {
                available.add(entry.getKey());
            }
        }

        return available;
    }

    /**
     * Check if sender has permission
     */
    public boolean hasPermission(CommandSender sender, String permission) {
        return LagX.hasPermission(sender, permission);
    }

    /**
     * Get the plugin instance
     */
    public LagX getPlugin() {
        return plugin;
    }

    /**
     * Get command by name (including alias resolution)
     */
    public LagXCommand getCommand(String name) {
        String commandName = name.toLowerCase();

        // Check for alias
        if (aliases.containsKey(commandName)) {
            commandName = aliases.get(commandName);
        }

        return commands.get(commandName);
    }
}