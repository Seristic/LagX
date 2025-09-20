package com.seristic.hbzcleaner.inf;

import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.main.HBZCleaner;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help {
   private static final List<Help.HoverCommand> commandsHelp = new ArrayList<>();
   private static final int COMMANDS_PER_PAGE = 8;

   public static void init() {
      commandsHelp.clear();
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner help(h) <num>", "Lists all available commands.", "hbzcleaner.help", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner status(st)", "Quick server health overview with key metrics.", "hbzcleaner.status", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner master(m)", "Displays comprehensive server performance overview.", "hbzcleaner.master", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner tps", "Shows current server TPS with health status.", "hbzcleaner.tps", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner gc", "Runs garbage collection and shows memory improvement.", "hbzcleaner.gc", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner ram", "Displays detailed memory usage statistics.", "hbzcleaner.ram", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner protocol(pr) <options>", "Advanced protocol management and execution.", "hbzcleaner.protocol", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner clear(c)", "Removes various entities/items from worlds.", "hbzcleaner.clear", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner count(ct)", "Counts various entities/items in worlds.", "hbzcleaner.clear", true));
      commandsHelp.add(
         new Help.HoverCommand("/hbzcleaner clear area <c:chunks|b:blocks> [type]", "Clear entities in area around you.", "hbzcleaner.clear", true)
      );
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner world(w) <world>", "Shows detailed statistics for a specific world.", "hbzcleaner.world", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner unload(u) <world>", "Unloads all chunks in the specified world.", "hbzcleaner.unload", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner modules(mo)", "Lists all loaded modules.", "hbzcleaner.modules", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner info(i)", "Shows HBZCleaner version and information.", "hbzcleaner.help", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner ping(p) <player:none>", "Displays player connection latency.", "hbzcleaner.ping", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner preset [basic|advanced|custom]", "Switch entity limiter presets.", "hbzcleaner.entities", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner entities [status|reload|stats]", "Manage entity limiting system.", "hbzcleaner.entities", true));
      commandsHelp.add(
         new Help.HoverCommand("/hbzcleaner villagers [status|reload|optimize|stats]", "Optimize villager AI performance.", "hbzcleaner.villagers", true)
      );
      commandsHelp.add(
         new Help.HoverCommand("/hbzcleaner towny(town)", "Check Towny protection info (only junk items cleared in towns).", "hbzcleaner.towny", true)
      );
      commandsHelp.add(
         new Help.HoverCommand(
            "/hbzcleaner stacker(stack) [info|reload|stack <radius>]", "Manage entity stacking system to reduce entity count.", "hbzcleaner.stacker", true
         )
      );
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner reload(rl)", "Reload plugin configuration without restarting the server.", "hbzcleaner.reload", true));
      commandsHelp.add(new Help.HoverCommand("/hbzcleaner warnings(warn) [status|on|off|toggle]", "Toggle or view protocol warnings.", "hbzcleaner.warn", true));
   }

   public static void send(Player p, int pageNum) {
      List<List<Help.HoverCommand>> pages = getPages();
      int maxPages = pages.size();
      if (pageNum > maxPages) {
         sendMsg(p, "§cHelp page #" + pageNum + " does not exist.", true);
      } else {
         Component header = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text(
                                 "✦ HBZCleaner Help ", NamedTextColor.GOLD
                              )
                              .decoration(TextDecoration.BOLD, true))
                           .append(Component.text("(Page ", NamedTextColor.GRAY)))
                        .append(Component.text(pageNum, NamedTextColor.AQUA)))
                     .append(Component.text("/", NamedTextColor.GRAY)))
                  .append(Component.text(maxPages, NamedTextColor.AQUA)))
               .append(Component.text(") ", NamedTextColor.GRAY)))
            .append(Component.text("✦", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
         if (p != null) {
            p.sendMessage(header);
         } else {
            sendMsg(p, "§6§l✦ HBZCleaner Help §7(Page §b" + pageNum + "§7/§b" + maxPages + "§7) §6§l✦", false);
         }

         for (Help.HoverCommand cmd : pages.get(pageNum - 1)) {
            if (p != null) {
               p.sendMessage(cmd.toComponent());
            } else {
               sendMsg(p, "§e" + cmd.command + " §7- " + cmd.description, false);
            }
         }

         if (maxPages > 1 && p != null) {
            sendPaginationButtons(p, pageNum, maxPages);
         }
      }
   }

   private static void sendPaginationButtons(Player p, int currentPage, int maxPages) {
      Component pagination = Component.empty();
      if (currentPage > 1) {
         Component prevButton = ((TextComponent)Component.text("« Previous", NamedTextColor.GREEN)
               .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage - 1), NamedTextColor.GRAY))))
            .clickEvent(ClickEvent.runCommand("/hbzcleaner help " + (currentPage - 1)));
         pagination = pagination.append(prevButton);
      } else {
         pagination = pagination.append(Component.text("« Previous", NamedTextColor.DARK_GRAY));
      }

      pagination = pagination.append(Component.text(" §8[§7" + currentPage + "§8/§7" + maxPages + "§8] ", NamedTextColor.GRAY));
      if (currentPage < maxPages) {
         Component nextButton = ((TextComponent)Component.text("Next »", NamedTextColor.GREEN)
               .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1), NamedTextColor.GRAY))))
            .clickEvent(ClickEvent.runCommand("/hbzcleaner help " + (currentPage + 1)));
         pagination = pagination.append(nextButton);
      } else {
         pagination = pagination.append(Component.text("Next »", NamedTextColor.DARK_GRAY));
      }

      p.sendMessage(pagination);
      if (maxPages > 2) {
         Component footer = ((TextComponent)Component.text("§7Tip: Use ", NamedTextColor.GRAY)
               .append(
                  Component.text("/hbzcleaner help <page>", NamedTextColor.YELLOW)
                     .hoverEvent(HoverEvent.showText(Component.text("Jump to specific page", NamedTextColor.GRAY)))
               ))
            .append(Component.text(" to jump to any page", NamedTextColor.GRAY));
         p.sendMessage(footer);
      }
   }

   public static void send(Player p) {
      send(p, 1);
   }

   public static int getTotalPages() {
      return getPages().size();
   }

   public static boolean isValidPage(int pageNum) {
      return pageNum >= 1 && pageNum <= getTotalPages();
   }

   public static int getTotalCommands() {
      return commandsHelp.size();
   }

   public static void sendMsg(Player p, String msg, boolean pre) {
      if (p == null) {
         HBZCleaner.instance.getLogger().info(msg.replaceAll("§[0-9a-fk-or]", ""));
      } else {
         if (pre) {
            msg = HBZCleaner.prefix + msg;
         }

         p.sendMessage(msg);
      }
   }

   public static void sendMsg(CommandSender sender, String msg, boolean pre) {
      if (sender == null) {
         HBZCleaner.instance.getLogger().info(msg.replaceAll("§[0-9a-fk-or]", ""));
      } else {
         if (pre) {
            msg = HBZCleaner.prefix + msg;
         }

         sender.sendMessage(msg);
      }
   }

   public static void addCommandH(Help.HoverCommand cmd) {
      commandsHelp.add(cmd);
   }

   private static List<List<Help.HoverCommand>> getPages() {
      List<List<Help.HoverCommand>> h = new ArrayList<>();
      List<Help.HoverCommand> c = new ArrayList<>();

      for (Help.HoverCommand s : commandsHelp) {
         if (c.size() == 8) {
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
      sendMsg(p, "§eProtocol: " + r.getSuper().id() + " | §7" + s.substring(0, fin.length() - 2) + "}", true);
   }

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
         Component hoverText = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text("Description: ", NamedTextColor.GOLD)
                        .append(Component.text(this.description, NamedTextColor.WHITE)))
                     .append(Component.text("\nPermission: ", NamedTextColor.GOLD)))
                  .append(Component.text(this.permission, NamedTextColor.WHITE)))
               .append(Component.text("\n")))
            .append(this.adminOnly ? Component.text("ADMIN ONLY", NamedTextColor.RED) : Component.text("All players", NamedTextColor.GREEN));
         return Component.text(this.command, NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(hoverText));
      }
   }
}
