package com.seristic.lagx.inf;

import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.main.HBZCleaner;
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
   commandsHelp.add(new Help.HoverCommand("/lagx help(h) <num>", "Lists all available commands.", "lagx.help", true));
   commandsHelp.add(new Help.HoverCommand("/lagx status(st)", "Quick server health overview with key metrics.", "lagx.status", true));
   commandsHelp.add(new Help.HoverCommand("/lagx master(m)", "Displays comprehensive server performance overview.", "lagx.master", true));
   commandsHelp.add(new Help.HoverCommand("/lagx tps", "Shows current server TPS with health status.", "lagx.tps", true));
   commandsHelp.add(new Help.HoverCommand("/lagx gc", "Runs garbage collection and shows memory improvement.", "lagx.gc", true));
   commandsHelp.add(new Help.HoverCommand("/lagx ram", "Displays detailed memory usage statistics.", "lagx.ram", true));
   commandsHelp.add(new Help.HoverCommand("/lagx protocol(pr) <options>", "Advanced protocol management and execution.", "lagx.protocol", true));
   commandsHelp.add(new Help.HoverCommand("/lagx clear(c)", "Removes various entities/items from worlds.", "lagx.clear", true));
   commandsHelp.add(new Help.HoverCommand("/lagx count(ct)", "Counts various entities/items in worlds.", "lagx.clear", true));
      commandsHelp.add(
         new Help.HoverCommand("/lagx clear area <c:chunks|b:blocks> [type]", "Clear entities in area around you.", "lagx.clear", true)
      );
   commandsHelp.add(new Help.HoverCommand("/lagx world(w) <world>", "Shows detailed statistics for a specific world.", "lagx.world", true));
   commandsHelp.add(new Help.HoverCommand("/lagx unload(u) <world>", "Unloads all chunks in the specified world.", "lagx.unload", true));
   commandsHelp.add(new Help.HoverCommand("/lagx modules(mo)", "Lists all loaded modules.", "lagx.modules", true));
   // Rebrand info description text
   commandsHelp.add(new Help.HoverCommand("/lagx info(i)", "Shows LagX version and information.", "lagx.help", true));
   commandsHelp.add(new Help.HoverCommand("/lagx ping(p) <player:none>", "Displays player connection latency.", "lagx.ping", true));
   commandsHelp.add(new Help.HoverCommand("/lagx preset [basic|advanced|custom]", "Switch entity limiter presets.", "lagx.entities", true));
   commandsHelp.add(new Help.HoverCommand("/lagx entities [status|reload|stats]", "Manage entity limiting system.", "lagx.entities", true));
      commandsHelp.add(
         new Help.HoverCommand("/lagx villagers [status|reload|optimize|stats]", "Optimize villager AI performance.", "lagx.villagers", true)
      );
      commandsHelp.add(
         new Help.HoverCommand(
            "/lagx stacker(stack) [info|reload|stack <radius>]", "Manage entity stacking system to reduce entity count.", "lagx.stacker", true
         )
      );
   commandsHelp.add(new Help.HoverCommand("/lagx reload(rl)", "Reload plugin configuration without restarting the server.", "lagx.reload", true));
   commandsHelp.add(new Help.HoverCommand("/lagx warnings(warn) [status|on|off|toggle]", "Toggle or view protocol warnings.", "lagx.warn", true));
   }

   public static void send(Player p, int pageNum) {
      List<List<Help.HoverCommand>> pages = getPages();
      int maxPages = pages.size();
      if (pageNum > maxPages) {
         sendMsg(p, "§cHelp page #" + pageNum + " does not exist.", true);
      } else {
         // Rebrand header to LagX
         Component header = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text(
                                 "✦ LagX Help ", NamedTextColor.GOLD
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
            sendMsg(p, "§6§l✦ LagX Help §7(Page §b" + pageNum + "§7/§b" + maxPages + "§7) §6§l✦", false);
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
            .clickEvent(ClickEvent.runCommand("/lagx help " + (currentPage - 1)));
         pagination = pagination.append(prevButton);
      } else {
         pagination = pagination.append(Component.text("« Previous", NamedTextColor.DARK_GRAY));
      }

      pagination = pagination.append(Component.text(" §8[§7" + currentPage + "§8/§7" + maxPages + "§8] ", NamedTextColor.GRAY));
      if (currentPage < maxPages) {
         Component nextButton = ((TextComponent)Component.text("Next »", NamedTextColor.GREEN)
               .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1), NamedTextColor.GRAY))))
            .clickEvent(ClickEvent.runCommand("/lagx help " + (currentPage + 1)));
         pagination = pagination.append(nextButton);
      } else {
         pagination = pagination.append(Component.text("Next »", NamedTextColor.DARK_GRAY));
      }

      p.sendMessage(pagination);
      if (maxPages > 2) {
      Component footer = ((TextComponent)Component.text("§7Tip: Use ", NamedTextColor.GRAY)
         .append(
         Component.text("/lagx help <page>", NamedTextColor.YELLOW)
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
