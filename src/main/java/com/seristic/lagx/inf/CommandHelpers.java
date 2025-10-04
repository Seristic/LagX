package com.seristic.lagx.inf;

/**
 * Helper class that provides methods for the CommandHandler to use.
 * This extends the Help class with methods that accept CommandSender instead of
 * Player.
 */
public class CommandHelpers {

    /**
     * Send help to a command sender
     * 
     * @param sender  The command sender
     * @param pageNum The page number
     */
    public static void sendHelp(org.bukkit.command.CommandSender sender, int pageNum) {
        if (sender instanceof org.bukkit.entity.Player) {
            Help.send((org.bukkit.entity.Player) sender, pageNum);
        } else {
            // Console version of help display (simplified without pagination)
            java.util.List<java.util.List<Help.HoverCommand>> pages = Help.getPages();
            if (pageNum < 1 || pageNum > pages.size()) {
                pageNum = 1;
            }

            Help.sendMsg(sender, "§8=== §6LagX Commands §7(Page " + pageNum + " of " + pages.size() + ")§8 ===", false);

            for (Help.HoverCommand cmd : pages.get(pageNum - 1)) {
                Help.sendMsg(sender, "§e" + cmd.command + " §7- " + cmd.description, false);
            }
        }
    }

    /**
     * Send protocol result info to a command sender
     * 
     * @param sender The command sender
     * @param r      The protocol result
     */
    public static void sendProtocolResultInfo(org.bukkit.command.CommandSender sender,
            com.seristic.lagx.api.proto.LRProtocolResult r) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        int i = 0;

        for (Object o : r.getData()) {
            s.append(i).append(": ").append(o).append(", ");
            i++;
        }

        String fin = s.toString();
        Help.sendMsg(sender, "§eProtocol: " + r.getSuper().id() + " | §7" + s.substring(0, fin.length() - 2) + "}",
                true);
    }
}