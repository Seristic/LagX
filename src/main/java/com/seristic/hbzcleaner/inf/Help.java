package com.seristic.hbzcleaner.inf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.main.LaggRemover;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/inf/Help.class */
public class Help {
    // Updated help system: Clean interface with hover tooltips instead of cluttered "ADMIN ONLY" text
    // Hover shows: Description, Permission node, and Access level
    private static final List<HoverCommand> commandsHelp = new ArrayList<>();
    
    // Pagination settings
    private static final int COMMANDS_PER_PAGE = 8; // Increased from 6 for better use of chat space

    // Helper class to store command information for hover tooltips
    public static class HoverCommand {
        public final String command;
        public final String description;
        public final String permission;
        public final boolean adminOnly;
        
        public HoverCommand(String command, String description, String permission, boolean adminOnly) {
            this.command = command;
            this.description = description;
            this.permission = permission;
            this.adminOnly = adminOnly;
        }
        
        public Component toComponent() {
            Component hoverText = Component.text("Description: ", NamedTextColor.GOLD)
                .append(Component.text(description, NamedTextColor.WHITE))
                .append(Component.text("\nPermission: ", NamedTextColor.GOLD))
                .append(Component.text(permission, NamedTextColor.WHITE))
                .append(Component.text("\n"))
                .append(adminOnly ? 
                    Component.text("ADMIN ONLY", NamedTextColor.RED) : 
                    Component.text("All players", NamedTextColor.GREEN));
            
            return Component.text(command, NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(hoverText));
        }
    }

    public static void init() {
        commandsHelp.add(new HoverCommand("/hbzlag help(h) <num>", "Lists all available commands.", "hbzlag.help", true));
        commandsHelp.add(new HoverCommand("/hbzlag status(st)", "Quick server health overview with key metrics.", "hbzlag.status", true));
        commandsHelp.add(new HoverCommand("/hbzlag master(m)", "Displays comprehensive server performance overview.", "hbzlag.master", true));
        commandsHelp.add(new HoverCommand("/hbzlag tps", "Shows current server TPS with health status.", "hbzlag.tps", true));
        commandsHelp.add(new HoverCommand("/hbzlag gc", "Runs garbage collection and shows memory improvement.", "hbzlag.gc", true));
        commandsHelp.add(new HoverCommand("/hbzlag ram", "Displays detailed memory usage statistics.", "hbzlag.ram", true));
        commandsHelp.add(new HoverCommand("/hbzlag protocol(pr) <options>", "Advanced protocol management and execution.", "hbzlag.protocol", true));
        commandsHelp.add(new HoverCommand("/hbzlag clear(c)", "Removes various entities/items from worlds.", "hbzlag.clear", true));
        commandsHelp.add(new HoverCommand("/hbzlag count(ct)", "Counts various entities/items in worlds.", "hbzlag.clear", true));
        commandsHelp.add(new HoverCommand("/hbzlag clear area <c:chunks|b:blocks> [type]", "Clear entities in area around you.", "hbzlag.clear", true));
        commandsHelp.add(new HoverCommand("/hbzlag world(w) <world>", "Shows detailed statistics for a specific world.", "hbzlag.world", true));
        commandsHelp.add(new HoverCommand("/hbzlag unload(u) <world>", "Unloads all chunks in the specified world.", "hbzlag.unload", true));
        commandsHelp.add(new HoverCommand("/hbzlag modules(mo)", "Lists all loaded LaggRemover modules.", "hbzlag.modules", true));
        commandsHelp.add(new HoverCommand("/hbzlag info(i)", "Shows HBZCleaner version and information.", "hbzlag.help", true));
        commandsHelp.add(new HoverCommand("/hbzlag ping(p) <player:none>", "Displays player connection latency.", "hbzlag.ping", true));
        commandsHelp.add(new HoverCommand("/hbzlag preset [basic|advanced|custom]", "Switch entity limiter presets.", "hbzlag.entities", true));
        commandsHelp.add(new HoverCommand("/hbzlag entities [status|reload|stats]", "Manage entity limiting system.", "hbzlag.entities", true));
        commandsHelp.add(new HoverCommand("/hbzlag villagers [status|reload|optimize|stats]", "Optimize villager AI performance.", "hbzlag.villagers", true));
        commandsHelp.add(new HoverCommand("/hbzlag towny(town)", "Check Towny protection info (only junk items cleared in towns).", "hbzlag.towny", true));
        commandsHelp.add(new HoverCommand("/hbzlag reload(rl)", "Reload plugin configuration without restarting the server.", "hbzlag.reload", true));
    }

    public static void send(Player p, int pageNum) {
        List<List<HoverCommand>> pages = getPages();
        int maxPages = pages.size();
        if (pageNum > maxPages) {
            sendMsg(p, "§cHelp page #" + pageNum + " does not exist.", true);
            return;
        }
        
        // Header with page info
        Component header = Component.text("✦ HBZCleaner Help ", NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .append(Component.text("(Page ", NamedTextColor.GRAY))
            .append(Component.text(pageNum, NamedTextColor.AQUA))
            .append(Component.text("/", NamedTextColor.GRAY))
            .append(Component.text(maxPages, NamedTextColor.AQUA))
            .append(Component.text(") ", NamedTextColor.GRAY))
            .append(Component.text("✦", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
        
        if (p != null) {
            p.sendMessage(header);
        } else {
            sendMsg(p, "§6§l✦ HBZCleaner Help §7(Page §b" + pageNum + "§7/§b" + maxPages + "§7) §6§l✦", false);
        }
        
        // Send hover commands for current page
        for (HoverCommand cmd : pages.get(pageNum - 1)) {
            if (p != null) {
                p.sendMessage(cmd.toComponent());
            } else {
                // Fallback for console
                sendMsg(p, "§e" + cmd.command + " §7- " + cmd.description, false);
            }
        }
        
        // Add pagination navigation if there are multiple pages
        if (maxPages > 1 && p != null) {
            sendPaginationButtons(p, pageNum, maxPages);
        }
    }
    
    private static void sendPaginationButtons(Player p, int currentPage, int maxPages) {
        Component pagination = Component.empty();
        
        // Previous page button
        if (currentPage > 1) {
            Component prevButton = Component.text("« Previous", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage - 1), NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/hbzlag help " + (currentPage - 1)));
            pagination = pagination.append(prevButton);
        } else {
            pagination = pagination.append(Component.text("« Previous", NamedTextColor.DARK_GRAY));
        }
        
        // Page info
        pagination = pagination.append(Component.text(" §8[§7" + currentPage + "§8/§7" + maxPages + "§8] ", NamedTextColor.GRAY));
        
        // Next page button
        if (currentPage < maxPages) {
            Component nextButton = Component.text("Next »", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1), NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/hbzlag help " + (currentPage + 1)));
            pagination = pagination.append(nextButton);
        } else {
            pagination = pagination.append(Component.text("Next »", NamedTextColor.DARK_GRAY));
        }
        
        p.sendMessage(pagination);
        
        // Footer with quick navigation
        if (maxPages > 2) {
            Component footer = Component.text("§7Tip: Use ", NamedTextColor.GRAY)
                .append(Component.text("/hbzlag help <page>", NamedTextColor.YELLOW)
                    .hoverEvent(HoverEvent.showText(Component.text("Jump to specific page", NamedTextColor.GRAY))))
                .append(Component.text(" to jump to any page", NamedTextColor.GRAY));
            p.sendMessage(footer);
        }
    }
    
    // Overload for default page 1
    public static void send(Player p) {
        send(p, 1);
    }
    
    // Method to get total number of pages
    public static int getTotalPages() {
        return getPages().size();
    }
    
    // Method to validate page number
    public static boolean isValidPage(int pageNum) {
        return pageNum >= 1 && pageNum <= getTotalPages();
    }
    
    // Get total number of commands
    public static int getTotalCommands() {
        return commandsHelp.size();
    }

    public static void sendMsg(Player p, String msg, boolean pre) {
        if (p == null) {
            LaggRemover.instance.getLogger().info(msg.replaceAll("§[0-9a-fk-or]", ""));
            return;
        }
        if (pre) {
            msg = LaggRemover.prefix + msg;
        }
        p.sendMessage(msg);
    }

    public static void addCommandH(HoverCommand cmd) {
        commandsHelp.add(cmd);
    }

    private static List<List<HoverCommand>> getPages() {
        List<List<HoverCommand>> h = new ArrayList<>();
        List<HoverCommand> c = new ArrayList<>();
        for (HoverCommand s : commandsHelp) {
            if (c.size() == COMMANDS_PER_PAGE) {
                h.add(c);
                c = new ArrayList<>();
            }
            c.add(s);
        }
        if (!c.isEmpty()) {
            h.add(c);
        }
        return h;
    }

    public static void sendProtocolResultInfo(Player p, LRProtocolResult r) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        int i = 0;
        for (Object o : r.getData()) {
            s.append(i).append(": ").append(o).append(", ");
            i++;
        }
        String fin = s.toString();
        sendMsg(p, "§eProtocol: " + r.getSuper().id() + " | §7" + (s.substring(0, fin.length() - 2) + "}"), true);
    }
}
