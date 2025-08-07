package com.seristic.hbzcleaner.main;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.seristic.hbzcleaner.api.aparser.AnfoParser;
import com.seristic.hbzcleaner.api.proto.DelayedLRProtocolResult;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.inf.Help;
import com.seristic.hbzcleaner.proto.bin.CCEntities;
import com.seristic.hbzcleaner.proto.bin.CCItems;
import com.seristic.hbzcleaner.proto.bin.LRGC;
import com.seristic.hbzcleaner.util.BitString;
import com.seristic.hbzcleaner.util.DoubleVar;
import com.seristic.hbzcleaner.util.DrewMath;
import com.seristic.hbzcleaner.util.LRConfig;
import com.seristic.hbzcleaner.util.ServerMetrics;
import com.seristic.hbzcleaner.util.TownyIntegration;

/* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/main/LRCommand.class */
public class LRCommand {
    public static boolean onCommand(final Player p, String[] args) {
        World w;
        String raw_fin;
        World w2;
        EntityType[] ents;
        World w3;
        World w4;
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
            if (hasPerm(p, "hbzlag.help")) {
                if (args.length != 1) {
                    try {
                        int pageNum = Integer.parseInt(args[1]);
                        if (Help.isValidPage(pageNum)) {
                            Help.send(p, pageNum);
                        } else {
                            Help.sendMsg(p, "§cPage #" + pageNum + " does not exist. Valid pages: 1-" + Help.getTotalPages(), true);
                        }
                        return true;
                    } catch (Exception e) {
                        Help.sendMsg(p, "§cPlease enter a valid number!", true);
                        return true;
                    }
                }
                Help.send(p, 1);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("master") || args[0].equalsIgnoreCase("m")) {
            if (hasPerm(p, "hbzlag.master")) {
                // Get server metrics using the new utility
                ServerMetrics.RamInfo ramInfo = ServerMetrics.getRamInfo();
                ServerMetrics.WorldStats worldStats = ServerMetrics.getWorldStats();
                double avgPing = ServerMetrics.getAveragePing();
                
                String symbol = p == null ? "" : BitString.SQUARE.getComp();
                StringBuilder sb = new StringBuilder();
                
                // Header with enhanced styling
                sb.append("\n§r§7§l══════════════════════════════════════");
                sb.append("\n§r               §6§l✦ HBZCleaner Status §7§l✦");
                sb.append("\n§r§7§l══════════════════════════════════════");
                
                // Performance metrics
                sb.append("\n§r§e").append(symbol).append(" §f§lPerformance:");
                sb.append("\n§r§e").append(symbol).append("   TPS: ").append(ServerMetrics.getFormattedTPS());
                sb.append("\n§r§e").append(symbol).append("   RAM: ").append(ramInfo.getFormattedUsage());
                sb.append("\n§r§e").append(symbol).append("   Avg Ping: §7");
                if (avgPing == 0.0) {
                    sb.append("(no players)");
                } else {
                    String pingColor = avgPing < 100 ? "§a" : avgPing < 200 ? "§e" : "§c";
                    sb.append(pingColor).append(ServerMetrics.formatNumber(avgPing)).append("ms");
                }
                
                // World information
                sb.append("\n§r§e").append(symbol).append(" §f§lWorld Data:");
                sb.append("\n§r§e").append(symbol).append("   Worlds: §7").append(worldStats.worldCount);
                sb.append("\n§r§e").append(symbol).append("   Loaded Chunks: §7").append(ServerMetrics.formatNumber(worldStats.totalChunks));
                sb.append("\n§r§e").append(symbol).append("   Total Entities: §7").append(ServerMetrics.formatNumber(worldStats.totalEntities));
                sb.append("\n§r§e").append(symbol).append("   Online Players: §7").append(ServerMetrics.formatNumber(worldStats.totalPlayers));
                
                // Footer
                sb.append("\n§r§7§l══════════════════════════════════════");
                
                Help.sendMsg(p, sb.toString(), false);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("ram")) {
            if (hasPerm(p, "hbzlag.ram")) {
                ServerMetrics.RamInfo ramInfo = ServerMetrics.getRamInfo();
                Runtime runtime = Runtime.getRuntime();
                
                // Additional detailed RAM info
                long totalMemory = runtime.totalMemory() / (1024 * 1024);
                long freeMemory = runtime.freeMemory() / (1024 * 1024);
                long allocatedMemory = totalMemory - freeMemory;
                
                StringBuilder sb = new StringBuilder();
                sb.append("§7[§6HBZCleaner§7] §f§lMemory Usage Report:");
                sb.append("\n§e◆ §fAllocated: §7").append(ServerMetrics.formatNumber(allocatedMemory)).append("MB");
                sb.append("\n§e◆ §fTotal Available: §7").append(ServerMetrics.formatNumber(ramInfo.maxMB)).append("MB");
                sb.append("\n§e◆ §fUsage: ").append(ramInfo.getFormattedUsage());
                sb.append("\n§e◆ §fFree: §a").append(ServerMetrics.formatNumber(freeMemory)).append("MB");
                
                Help.sendMsg(p, sb.toString(), true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
            StringBuilder sb = new StringBuilder();
            sb.append("§6§l✦ HBZCleaner ✦");
            sb.append("\n§e◆ §fAuthor: §bHBZ §7(forked from LaggRemover by drew6017)");
            sb.append("\n§e◆ §fVersion: §b").append(LaggRemover.instance.getDescription().getVersion());
            sb.append("\n§e◆ §fPlatform: §bFolia-Optimized");
            sb.append("\n§e◆ §fDescription: §7Enhanced lag prevention and cleanup");
            sb.append("\n§7  plugin optimized for Folia servers with improved");
            sb.append("\n§7  performance monitoring and automated maintenance.");
            sb.append("\n§e◆ §fOriginal: §7http://dev.bukkit.org/bukkit-plugins/laggremover/");
            
            Help.sendMsg(p, sb.toString(), false);
            return true;
        } else if (args[0].equalsIgnoreCase("world") || args[0].equalsIgnoreCase("w")) {
            if (hasPerm(p, "hbzlag.world")) {
                if (args.length == 2) {
                    w = Bukkit.getWorld(args[1]);
                } else if (p == null) {
                    LaggRemover.instance.getLogger().info("You must be a player to not specify an argument here.");
                    return true;
                } else {
                    w = p.getWorld();
                }
                if (w == null) {
                    Help.sendMsg(p, "§cThe world specified was invalid or not found.", true);
                    return true;
                }
                long size = DrewMath.getSize(w.getWorldFolder());
                String s2 = p == null ? "" : BitString.SQUARE.getComp();
                Help.sendMsg(p, "§6§lWorld " + w.getName() + "§r\n§e" + s2 + " Seed:§7 " + Long.toString(w.getSeed()) + "\n§e" + s2 + " Spawn Chunks:§7 " + (w.getKeepSpawnInMemory() ? "Yes" : "No") + "\n§e" + s2 + " Loaded Chunks:§7 " + NumberFormat.getNumberInstance().format(w.getLoadedChunks().length) + "\n§e" + s2 + " Entities:§7 " + NumberFormat.getNumberInstance().format(w.getEntities().size()) + "\n§e" + s2 + " Players:§7 " + NumberFormat.getNumberInstance().format(w.getPlayers().size()) + "\n§e" + s2 + " Time:§7 " + Long.toString(w.getTime()) + " (" + DrewMath.getTagForTime(w.getTime()) + ")\n§e" + s2 + " Size on Disk:§7 " + NumberFormat.getNumberInstance().format(size / 1000) + "KB (" + NumberFormat.getNumberInstance().format(size) + " bytes)", true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("gc")) {
            if (hasPerm(p, "hbzlag.gc")) {
                // Get RAM usage before GC
                ServerMetrics.RamInfo beforeGC = ServerMetrics.getRamInfo();
                
                // Run garbage collection
                Object gcResult = Protocol.run(new LRGC(), (Object[]) null).getData()[0];
                long freedMB = ((Number) gcResult).longValue();
                
                // Get RAM usage after GC
                ServerMetrics.RamInfo afterGC = ServerMetrics.getRamInfo();
                
                String message = "§7[§6HBZCleaner§7] §a✓ §fGarbage collection completed!\n" +
                               "§e◆ §fFreed: §a" + ServerMetrics.formatNumber(freedMB) + "MB §fof memory\n" +
                               "§e◆ §fBefore: §7" + beforeGC.getFormattedUsage() + "\n" +
                               "§e◆ §fAfter: §7" + afterGC.getFormattedUsage();
                
                Help.sendMsg(p, message, true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("tps")) {
            if (hasPerm(p, "hbzlag.tps")) {
                double tps = TickPerSecond.getTPS();
                String tpsFormatted = ServerMetrics.getFormattedTPS();
                String status;
                String icon;
                
                if (tps >= 19.0) {
                    status = "§a§lExcellent";
                    icon = "§a✓";
                } else if (tps >= 16.0) {
                    status = "§e§lGood";
                    icon = "§e⚠";
                } else if (tps >= 12.0) {
                    status = "§6§lPoor";
                    icon = "§6⚡";
                } else {
                    status = "§c§lCritical";
                    icon = "§c✗";
                }
                
                if (p == null) {
                    LaggRemover.instance.getLogger().info("TPS: " + Double.toString(DrewMath.round(tps, 2)) + " (" + status + ")");
                    return true;
                }
                
                Help.sendMsg(p, "§7[§6HBZCleaner§7] " + icon + " §fServer TPS: " + tpsFormatted + " §7(" + status + "§7)", true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("modules") || args[0].equalsIgnoreCase("mo")) {
            if (hasPerm(p, "hbzlag.modules")) {
                String[] ms = LaggRemover.getModulesList();
                Help.sendMsg(p, "§eModules (" + ms[1] + "): §a" + ms[0], true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("protocol") || args[0].equalsIgnoreCase("pr")) {
            if (hasPerm(p, "hbzlag.protocol")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("help") || args[1].equalsIgnoreCase("h")) {
                        if (args.length == 2) {
                            Help.sendMsg(p, "§eYou can use this command to view the help and description of all of the protocols currently loaded into HBZCleaner. Simply type:\n /hbzlag protocol(p) help(h) <protocol>", true);
                            return true;
                        }
                        String pname = args[2].toLowerCase();
                        Help.sendMsg(p, "§6§lProtocol " + pname + "§r\n" + Protocol.getProtocol(pname).help(), false);
                        return true;
                    } else if (args[1].equalsIgnoreCase("run") || args[1].equalsIgnoreCase("r")) {
                        Help.sendMsg(p, "§cThis feature is not fully supported yet. Expect bugs.", true);
                        if (args.length < 4) {
                            Help.sendMsg(p, "§cCorrect usage: /hbzlag protocol(p) run(r) <protocol> <(Boolean)seeRawResult> <data>", true);
                            return true;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        if (args.length > 4) {
                            for (int i = 5; i < args.length; i++) {
                                sb2.append(args[i]).append(" ");
                            }
                            String raw_fin2 = sb2.toString();
                            raw_fin = raw_fin2.substring(0, raw_fin2.length() - 1);
                        } else {
                            raw_fin = "{\"Delay\":\"false\"}";
                        }
                        LRProtocol pk = Protocol.getProtocol(args[2]);
                        final boolean seeResult = Boolean.parseBoolean(args[3].toLowerCase());
                        try {
                            DoubleVar<Object[], Boolean> dat = AnfoParser.parse(pk, raw_fin);
                            if (dat.getVar2().booleanValue()) {
                                Protocol.rund(pk, dat.getVar1(), new DelayedLRProtocolResult() { // from class: drew6017.lr.main.LRCommand.1
                                    @Override // drew6017.lr.api.proto.DelayedLRProtocolResult
                                    public void receive(LRProtocolResult result) {
                                        if (seeResult) {
                                            Help.sendProtocolResultInfo(p, result);
                                        }
                                    }
                                });
                                return true;
                            } else if (seeResult) {
                                Help.sendProtocolResultInfo(p, pk.run(dat.getVar1()));
                                return true;
                            } else {
                                return true;
                            }
                        } catch (AnfoParser.AnfoParseException | ParseException e2) {
                            Help.sendMsg(p, "§cError parsing protocol: §7" + e2.getMessage(), true);
                            Help.sendMsg(p, "§cMaybe you used the command invalidly? Correct usage: /hbzlag protocol(p) run(r) <protocol> <(Boolean)seeRawResult> <data>", true);
                            return true;
                        }
                    } else if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("l")) {
                        String[] ms2 = LaggRemover.getProtocolList();
                        Help.sendMsg(p, "§eProtocols (" + ms2[1] + "): §a" + ms2[0], true);
                        return true;
                    } else {
                        return true;
                    }
                }
                Help.sendMsg(p, "§cCorrect usage: /hbzlag protocol(p) [help(h):run(r):list(l)] <options>", true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("unload") || args[0].equalsIgnoreCase("u")) {
            if (hasPerm(p, "hbzlag.unload")) {
                if (args.length == 2) {
                    World w6 = Bukkit.getWorld(args[1]);
                    if (w6 == null) {
                        Help.sendMsg(p, "§cWorld \"" + args[1] + "\" could not be found.", true);
                        return true;
                    } else if (w6.getPlayers().size() != 0) {
                        Help.sendMsg(p, "§cUnloading the chunks of worlds that contain players has been disabled due to bugs.", true);
                        return true;
                    } else {
                        int chunks2 = 0;
                        for (Chunk c : w6.getLoadedChunks()) {
                            w6.unloadChunk(c);
                            chunks2++;
                        }
                        Help.sendMsg(p, "§e" + Integer.toString(chunks2) + " chunks in world " + w6.getName() + " have been unloaded", true);
                        return true;
                    }
                }
                Help.sendMsg(p, "§cCorrect usage: /hbzlag unload(u) <world>", true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("ping") || args[0].equalsIgnoreCase("p")) {
            if (hasPerm(p, "hbzlag.ping")) {
                if (args.length == 2) {
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 != null) {
                        Help.sendMsg(p, "§ePlayer \"" + args[1] + "\" has a ping of §b" + p1.getPing() + "§ems", true);
                        return true;
                    }
                    Help.sendMsg(p, "§c\"" + args[1] + "\" is not a valid player.", true);
                    return true;
                } else if (args.length == 1) {
                    if (p == null) {
                        Help.sendMsg(null, "You must specify a player if using this command from the command line.", true);
                        return true;
                    }
                    Help.sendMsg(p, "§eYour current ping is §b" + p.getPing() + "§ems", true);
                    return true;
                } else {
                    Help.sendMsg(p, "§cCorrect usage: /hbzlag ping(p) <player:none>", true);
                    return true;
                }
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("ct")) {
            if (hasPerm(p, "hbzlag.clear")) {
                boolean isCount = args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("ct");
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("items") || args[1].equalsIgnoreCase("i")) {
                        if (args.length == 2) {
                            int i2 = ((Integer) Protocol.run(new CCItems(), new Object[]{Boolean.valueOf(isCount)}).getData()[0]).intValue();
                            Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i2 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance(Locale.US).format(i2)).append(i2 == 1 ? " item" : " items").append(" on the ground between all worlds.").toString(), true);
                            return true;
                        }
                        try {
                            w2 = Bukkit.getWorld(args[2]);
                        } catch (Exception e3) {
                            w2 = null;
                        }
                        if (w2 == null) {
                            Help.sendMsg(p, "§cWorld \"" + args[2] + "\" was not found.", true);
                            return true;
                        }
                        int i3 = ((Integer) Protocol.run(new CCItems(), new Object[]{Boolean.valueOf(isCount), w2}).getData()[0]).intValue();
                        Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i3 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance(Locale.US).format(i3)).append(i3 == 1 ? " item" : " items").append(" in world \"").append(w2.getName()).append("\"").toString(), true);
                        return true;
                    } else if (args[1].equalsIgnoreCase("entities") || args[1].equalsIgnoreCase("e")) {
                        if (args.length > 2) {
                            if (args[2].equalsIgnoreCase("hostile") || args[2].equalsIgnoreCase("h")) {
                                ents = CCEntities.hostile;
                            } else if (args[2].equalsIgnoreCase("peaceful") || args[2].equalsIgnoreCase("p")) {
                                ents = CCEntities.peaceful;
                            } else if (!args[2].equalsIgnoreCase("all") && !args[2].equalsIgnoreCase("a")) {
                                Help.sendMsg(p, "§c" + args[2] + " is an invalid entity generality. Valid generalities are hostile, peaceful, or all.", true);
                                return true;
                            } else {
                                ents = null;
                            }
                            if (args.length == 3) {
                                // No world specified, use player's current world if player, or all worlds if console
                                if (p != null) {
                                    // Player command - use current world
                                    World currentWorld = p.getWorld();
                                    int i4 = ((Integer) Protocol.run(new CCEntities(), new Object[]{Boolean.valueOf(isCount), ents, currentWorld}).getData()[0]).intValue();
                                    Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i4 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance(Locale.US).format(i4)).append(i4 == 1 ? " entity" : " entities").append(" from world \"").append(currentWorld.getName()).append("\"").toString(), true);
                                } else {
                                    // Console command - use all worlds
                                    int i4 = ((Integer) Protocol.run(new CCEntities(), new Object[]{Boolean.valueOf(isCount), ents}).getData()[0]).intValue();
                                    Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i4 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance(Locale.US).format(i4)).append(i4 == 1 ? " entity" : " entities").append(" between all worlds.").toString(), true);
                                }
                                return true;
                            } else if (args.length == 4) {
                                try {
                                    w3 = Bukkit.getWorld(args[3]);
                                } catch (Exception e4) {
                                    w3 = null;
                                }
                                if (w3 == null) {
                                    Help.sendMsg(p, "§cWorld \"" + args[3] + "\" was not found.", true);
                                    return true;
                                }
                                int i5 = ((Integer) Protocol.run(new CCEntities(), new Object[]{Boolean.valueOf(isCount), ents, w3}).getData()[0]).intValue();
                                Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i5 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance(Locale.US).format(i5)).append(i5 == 1 ? " entity" : " entities").append(" from world \"").append(w3.getName()).append("\"").toString(), true);
                                return true;
                            } else {
                                Help.sendMsg(p, "§cCorrect usage: /hbzlag " + (isCount ? "count(ct)" : "clear(c)") + " entities(e) [hostile(h):peaceful(p):all(a)] <world>", true);
                                return true;
                            }
                        }
                        Help.sendMsg(p, "§cCorrect usage: /hbzlag " + (isCount ? "count(ct)" : "clear(c)") + " entities(e) [hostile(h):peaceful(p):all(a)] <world>", true);
                        return true;
                    } else if (args[1].equalsIgnoreCase("type") || args[1].equalsIgnoreCase("t")) {
                        if (args.length >= 3) {
                            if (args[2].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("l")) {
                                StringBuilder sb3 = new StringBuilder();
                                EntityType[] allEnt = EntityType.values();
                                for (EntityType pe : allEnt) {
                                    sb3.append(pe.name());
                                    sb3.append("§7, §a");
                                }
                                String sbs = sb3.toString();
                                if (!sbs.equals("")) {
                                    sbs = sbs.substring(0, sbs.length() - 2);
                                }
                                Help.sendMsg(p, "§eEntity Types (" + Integer.toString(allEnt.length) + "): §a" + sbs, true);
                                return true;
                            } else if (args.length >= 4) {
                                List<EntityType> types = new ArrayList<>();
                                for (int i6 = 3; i6 < args.length; i6++) {
                                    try {
                                        EntityType var = EntityType.valueOf(args[i6].toUpperCase());
                                        types.add(var);
                                    } catch (IllegalArgumentException e5) {
                                        Help.sendMsg(p, "§c" + args[i6] + " is an invalid entity type. Please use /hbzlag " + (isCount ? "count(ct)" : "clear(c)") + " type(t) list(l)", true);
                                        return true;
                                    }
                                }
                                EntityType[] ents2 = (EntityType[]) types.toArray(new EntityType[types.size()]);
                                if (args[2].equalsIgnoreCase("none") || args[2].equalsIgnoreCase("n")) {
                                    int i7 = ((Integer) Protocol.run(new CCEntities(), new Object[]{Boolean.valueOf(isCount), ents2}).getData()[0]).intValue();
                                    Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i7 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance().format(i7)).append(i7 == 1 ? " entity" : " entities").append(" by the ").append(ents2.length == 1 ? "type" : "types").append(" provided.").toString(), true);
                                    return true;
                                }
                                try {
                                    w4 = Bukkit.getWorld(args[2]);
                                } catch (Exception e6) {
                                    w4 = null;
                                }
                                if (w4 == null) {
                                    Help.sendMsg(p, "§cWorld \"" + args[2] + "\" was not found.", true);
                                    return true;
                                }
                                int i8 = ((Integer) Protocol.run(new CCEntities(), new Object[]{Boolean.valueOf(isCount), ents2, w4}).getData()[0]).intValue();
                                Help.sendMsg(p, new StringBuilder().append(isCount ? "§eThere " + (i8 == 1 ? "is " : "are ") : "§eRemoved ").append(NumberFormat.getNumberInstance().format(i8)).append(i8 == 1 ? " entity" : " entities").append(" by the ").append(ents2.length == 1 ? "type" : "types").append(" provided in world ").append(w4.getName()).append(".").toString(), true);
                                return true;
                            } else {
                                Help.sendMsg(p, "§cPlease list entity types to work with if you are not using the list(l) sub-command. Ex: SNOWBALL FIREWORK", true);
                                return true;
                            }
                        }
                        Help.sendMsg(p, "§cCorrect usage: /hbzlag " + (isCount ? "count(ct)" : "clear(c)") + " type(t) [list(l):none(n):<world>] <none:types>", true);
                        return true;
                    } else if (args[1].equalsIgnoreCase("area") || args[1].equalsIgnoreCase("a")) {
                        return handleAreaClear(p, args, isCount);
                    }
                }
                Help.sendMsg(p, "§cCorrect usage: /hbzlag " + (isCount ? "count(ct)" : "clear(c)") + " [items(i):entities(e):type(t):area(a)] <options>", true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("st")) {
            if (hasPerm(p, "hbzlag.status")) {
                // Quick status overview
                double tps = TickPerSecond.getTPS();
                ServerMetrics.RamInfo ramInfo = ServerMetrics.getRamInfo();
                ServerMetrics.WorldStats worldStats = ServerMetrics.getWorldStats();
                
                String tpsIcon = tps >= 19.0 ? "§a●" : tps >= 15.0 ? "§e●" : "§c●";
                String ramIcon = ramInfo.usagePercent < 60 ? "§a●" : ramInfo.usagePercent < 80 ? "§e●" : "§c●";
                String playersIcon = worldStats.totalPlayers > 0 ? "§a●" : "§7●";
                
                StringBuilder sb = new StringBuilder();
                sb.append("§7[§6HBZCleaner§7] §f§lQuick Status:");
                sb.append("\n").append(tpsIcon).append(" §fTPS: ").append(ServerMetrics.getFormattedTPS());
                sb.append("  ").append(ramIcon).append(" §fRAM: §7").append(ServerMetrics.formatNumber(ramInfo.usedMB)).append("/").append(ServerMetrics.formatNumber(ramInfo.maxMB)).append("MB");
                sb.append("  ").append(playersIcon).append(" §fPlayers: §7").append(worldStats.totalPlayers);
                
                Help.sendMsg(p, sb.toString(), true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("entities") || args[0].equalsIgnoreCase("limiter")) {
            if (hasPerm(p, "hbzlag.entities")) {
                return handleEntitiesCommand(p, args);
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("villagers") || args[0].equalsIgnoreCase("optimize")) {
            if (hasPerm(p, "hbzlag.villagers")) {
                return handleVillagersCommand(p, args);
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("preset") || args[0].equalsIgnoreCase("presets")) {
            if (hasPerm(p, "hbzlag.entities")) {
                return handlePresetCommand(p, args);
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("towny") || args[0].equalsIgnoreCase("town")) {
            if (hasPerm(p, "hbzlag.towny")) {
                if (p == null) {
                    Help.sendMsg(p, "§cThis command can only be used by players.", true);
                    return true;
                }
                
                TownyIntegration towny = LaggRemover.getTownyIntegration();
                if (towny == null || !towny.isTownyEnabled()) {
                    Help.sendMsg(p, "§cTowny integration is not available.", true);
                    return true;
                }
                
                org.bukkit.Location playerLoc = p.getLocation();
                String protectionInfo = towny.getProtectionInfo(playerLoc);
                
                StringBuilder sb = new StringBuilder();
                sb.append("§6§l✦ Towny Protection Info ✦");
                sb.append("\n§e◆ §fLocation: ").append(protectionInfo);
                sb.append("\n§e◆ §fAdmin Access: §a✓ Granted §7(Admin Only)");
                
                if (towny.isInTown(playerLoc)) {
                    sb.append("\n§e◆ §fProtection: §7Limited clearing in towns");
                    sb.append("\n§7  • Only junk items can be cleared (cobblestone, dirt, etc.)");
                    sb.append("\n§7  • All living entities are protected in towns");
                    sb.append("\n§7  • Hostile mobs can be cleared in towns");
                } else {
                    sb.append("\n§e◆ §fProtection: §7No protection (wilderness area)");
                    sb.append("\n§7  • All entities/items can be cleared");
                }
                
                Help.sendMsg(p, sb.toString(), true);
                return true;
            }
            noPerm(p);
            return true;
        } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            if (hasPerm(p, "hbzlag.reload")) {
                try {
                    LRConfig.reload();
                    Help.sendMsg(p, "§7[§6HBZCleaner§7] §a✓ Configuration reloaded successfully!", true);
                    Help.sendMsg(p, "§7  • Config file refreshed", false);
                    Help.sendMsg(p, "§7  • All protocols reinitialized", false);
                    Help.sendMsg(p, "§7  • World settings updated", false);
                    return true;
                } catch (Exception e) {
                    Help.sendMsg(p, "§7[§6HBZCleaner§7] §c✗ Failed to reload configuration!", true);
                    Help.sendMsg(p, "§7  Error: " + e.getMessage(), false);
                    LaggRemover.getInstance().getLogger().warning("Config reload failed: " + e.getMessage());
                    return true;
                }
            }
            noPerm(p);
            return true;
        } else {
            return false;
        }
    }

    private static boolean handleEntitiesCommand(Player p, String[] args) {
        if (args.length == 1) {
            // Show entity limiter status
            if (LaggRemover.getInstance().getEntityLimiter() != null) {
                String status = LaggRemover.getInstance().getEntityLimiter().getStatus();
                Help.sendMsg(p, "§6§lEntity Limiter Status:\n" + status, true);
            } else {
                Help.sendMsg(p, "§cEntity Limiter is not available", true);
            }
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "status":
                if (LaggRemover.getInstance().getEntityLimiter() != null) {
                    String status = LaggRemover.getInstance().getEntityLimiter().getStatus();
                    Help.sendMsg(p, "§6§lEntity Limiter Status:\n" + status, true);
                } else {
                    Help.sendMsg(p, "§cEntity Limiter is not available", true);
                }
                break;
            case "reload":
                if (LaggRemover.getInstance().getEntityLimiter() != null) {
                    LaggRemover.getInstance().getEntityLimiter().reload();
                    Help.sendMsg(p, "§aEntity Limiter configuration reloaded!", true);
                } else {
                    Help.sendMsg(p, "§cEntity Limiter is not available", true);
                }
                break;
            case "stats":
                showEntityStats(p, args.length > 2 ? args[2] : null);
                break;
            default:
                Help.sendMsg(p, "§cUsage: /hbzlag entities [status|reload|stats] [world]", true);
                break;
        }
        return true;
    }

    private static boolean handleVillagersCommand(Player p, String[] args) {
        if (args.length == 1) {
            // Show villager optimizer status
            if (LaggRemover.getInstance().getVillagerOptimizer() != null) {
                String status = LaggRemover.getInstance().getVillagerOptimizer().getStatus();
                Help.sendMsg(p, "§6§lVillager Optimizer Status:\n" + status, true);
            } else {
                Help.sendMsg(p, "§cVillager Optimizer is not available", true);
            }
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "status":
                if (LaggRemover.getInstance().getVillagerOptimizer() != null) {
                    String status = LaggRemover.getInstance().getVillagerOptimizer().getStatus();
                    Help.sendMsg(p, "§6§lVillager Optimizer Status:\n" + status, true);
                } else {
                    Help.sendMsg(p, "§cVillager Optimizer is not available", true);
                }
                break;
            case "reload":
                if (LaggRemover.getInstance().getVillagerOptimizer() != null) {
                    LaggRemover.getInstance().getVillagerOptimizer().reload();
                    Help.sendMsg(p, "§aVillager Optimizer configuration reloaded!", true);
                } else {
                    Help.sendMsg(p, "§cVillager Optimizer is not available", true);
                }
                break;
            case "optimize":
                String worldName = args.length > 2 ? args[2] : p.getWorld().getName();
                optimizeVillagersInWorld(p, worldName);
                break;
            case "stats":
                showVillagerStats(p, args.length > 2 ? args[2] : null);
                break;
            default:
                Help.sendMsg(p, "§cUsage: /hbzlag villagers [status|reload|optimize|stats] [world]", true);
                break;
        }
        return true;
    }

    private static void showEntityStats(Player p, String worldName) {
        World world;
        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                Help.sendMsg(p, "§cWorld '" + worldName + "' not found!", true);
                return;
            }
        } else {
            world = p.getWorld();
        }
        
        int totalEntities = world.getEntityCount();
        int players = world.getPlayers().size();
        int nonPlayerEntities = totalEntities - players;
        int loadedChunks = world.getLoadedChunks().length;
        
        StringBuilder sb = new StringBuilder();
        sb.append("§6§lEntity Statistics for ").append(world.getName()).append(":\n");
        sb.append("§7Total Entities: §e").append(totalEntities).append("\n");
        sb.append("§7Players: §e").append(players).append("\n");
        sb.append("§7Non-Player Entities: §e").append(nonPlayerEntities).append("\n");
        sb.append("§7Loaded Chunks: §e").append(loadedChunks).append("\n");
        sb.append("§7Avg Entities/Chunk: §e").append(String.format("%.1f", (double) totalEntities / loadedChunks));
        
        Help.sendMsg(p, sb.toString(), true);
    }

    private static void showVillagerStats(Player p, String worldName) {
        World world;
        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                Help.sendMsg(p, "§cWorld '" + worldName + "' not found!", true);
                return;
            }
        } else {
            world = p.getWorld();
        }
        
        int villagerCount = 0;
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof org.bukkit.entity.Villager) {
                villagerCount++;
            }
        }
        
        int loadedChunks = world.getLoadedChunks().length;
        
        StringBuilder sb = new StringBuilder();
        sb.append("§6§lVillager Statistics for ").append(world.getName()).append(":\n");
        sb.append("§7Total Villagers: §e").append(villagerCount).append("\n");
        sb.append("§7Loaded Chunks: §e").append(loadedChunks).append("\n");
        sb.append("§7Avg Villagers/Chunk: §e").append(String.format("%.1f", (double) villagerCount / loadedChunks));
        
        Help.sendMsg(p, sb.toString(), true);
    }

    private static void optimizeVillagersInWorld(Player p, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Help.sendMsg(p, "§cWorld '" + worldName + "' not found!", true);
            return;
        }
        
        int villagerCount = 0;
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof org.bukkit.entity.Villager) {
                villagerCount++;
                // Apply basic optimizations
                org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) entity;
                if (villager.getTicksLived() > 1200 && Math.random() > 0.5) {
                    // Temporarily disable AI for older villagers
                    villager.setAI(false);
                    Bukkit.getScheduler().runTaskLater(LaggRemover.getInstance(), () -> villager.setAI(true), 100);
                }
            }
        }
        
        Help.sendMsg(p, "§aOptimized " + villagerCount + " villagers in world '" + worldName + "'", true);
    }

    private static boolean handlePresetCommand(Player p, String[] args) {
        if (args.length == 1) {
            // Show current preset and available presets
            String currentPreset = LaggRemover.getInstance().getConfig().getString("entity_limiter.preset_mode", "advanced");
            Help.sendMsg(p, "§6§lEntity Limiter Presets:\n" +
                "§7Current preset: §e" + currentPreset + "\n" +
                "§7Available presets:\n" +
                "§e- basic §7(Hard limit for ALL entities - simple but effective)\n" +
                "§e- advanced §7(Separate limits for different entity types - default)\n" +
                "§e- custom §7(Use your own custom configuration)\n" +
                "§7Use §e/hbzlag preset <basic|advanced|custom>§7 to switch", true);
            return true;
        }
        
        if (args.length >= 2) {
            String newPreset = args[1].toLowerCase();
            
            if (!newPreset.equals("basic") && !newPreset.equals("advanced") && !newPreset.equals("custom")) {
                Help.sendMsg(p, "§cInvalid preset! Available presets: basic, advanced, custom", true);
                return true;
            }
            
            // Update config
            LaggRemover.getInstance().getConfig().set("entity_limiter.preset_mode", newPreset);
            LaggRemover.getInstance().saveConfig();
            
            // Reload entity limiter
            if (LaggRemover.getInstance().getEntityLimiter() != null) {
                LaggRemover.getInstance().getEntityLimiter().reload();
            }
            
            Help.sendMsg(p, "§aEntity limiter preset changed to: §e" + newPreset + "\n" +
                "§7The configuration has been reloaded with the new preset.", true);
            
            // Show preset-specific info
            switch (newPreset) {
                case "basic":
                    Help.sendMsg(p, "§7§lBasic Preset Info:\n" +
                        "§7- Hard limit for ALL entities per chunk (no separate types)\n" +
                        "§7- Simpler configuration, more aggressive limiting\n" +
                        "§7- Best for servers with serious entity lag issues", false);
                    break;
                case "advanced":
                    Help.sendMsg(p, "§7§lAdvanced Preset Info:\n" +
                        "§7- Separate limits for hostile, passive, and item entities\n" +
                        "§7- More granular control and flexibility\n" +
                        "§7- Recommended for most servers", false);
                    break;
                case "custom":
                    Help.sendMsg(p, "§7§lCustom Preset Info:\n" +
                        "§7- Uses the custom_config section in config.yml\n" +
                        "§7- Complete control over all limits and settings\n" +
                        "§7- For advanced server administrators", false);
                    break;
            }
            
            return true;
        }
        
        Help.sendMsg(p, "§cUsage: /hbzlag preset [basic|advanced|custom]", true);
        return true;
    }

    private static boolean hasPerm(Player p, String permission) {
        if (p == null) {
            return true; // Console always has permission
        }
        
        // Only admins (ops) can use HBZLag commands
        if (!p.isOp()) {
            return false;
        }
        
        return p.hasPermission(permission);
    }

    private static void noPerm(Player p) {
        Help.sendMsg(p, "§cYou do not have permission to use this command. Please contact your administrator if you believe this to be an error.", true);
    }
    
    private static boolean handleAreaClear(Player p, String[] args, boolean isCount) {
        if (p == null) {
            Help.sendMsg(p, "§cArea clearing can only be used by players, not console.", true);
            return true;
        }
        
        if (args.length < 3) {
            Help.sendMsg(p, "§cUsage: /hbzlag " + (isCount ? "count" : "clear") + " area <c:chunks|b:blocks> [entity_type]", true);
            Help.sendMsg(p, "§7Examples:", false);
            Help.sendMsg(p, "§7- /hbzlag clear area c:5 §8(clear 5x5 chunks around you)", false);
            Help.sendMsg(p, "§7- /hbzlag clear area b:100 hostile §8(clear hostile mobs in 100 block radius)", false);
            Help.sendMsg(p, "§7- /hbzlag count area c:3 §8(count entities in 3x3 chunks)", false);
            return true;
        }
        
        String areaSpec = args[2];
        String entityFilter = args.length > 3 ? args[3] : "all";
        
        // Parse area specification (c:5 for chunks, b:100 for blocks)
        int radius;
        boolean useChunks;
        
        if (areaSpec.startsWith("c:")) {
            useChunks = true;
            try {
                radius = Integer.parseInt(areaSpec.substring(2));
            } catch (NumberFormatException e) {
                Help.sendMsg(p, "§cInvalid chunk radius: " + areaSpec.substring(2), true);
                return true;
            }
        } else if (areaSpec.startsWith("b:")) {
            useChunks = false;
            try {
                radius = Integer.parseInt(areaSpec.substring(2));
            } catch (NumberFormatException e) {
                Help.sendMsg(p, "§cInvalid block radius: " + areaSpec.substring(2), true);
                return true;
            }
        } else {
            Help.sendMsg(p, "§cInvalid area specification. Use c:<number> for chunks or b:<number> for blocks", true);
            return true;
        }
        
        if (radius <= 0 || radius > 100) {
            Help.sendMsg(p, "§cRadius must be between 1 and 100", true);
            return true;
        }
        
        // Perform the area clear/count
        int totalRemoved = 0;
        org.bukkit.Location playerLoc = p.getLocation();
        World world = playerLoc.getWorld();
        
        if (useChunks) {
            // Chunk-based clearing
            org.bukkit.Chunk centerChunk = playerLoc.getChunk();
            int centerX = centerChunk.getX();
            int centerZ = centerChunk.getZ();
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    org.bukkit.Chunk chunk = world.getChunkAt(centerX + dx, centerZ + dz);
                    if (chunk.isLoaded()) {
                        totalRemoved += processChunkEntities(chunk, entityFilter, isCount);
                    }
                }
            }
            
            String unit = useChunks ? "chunks" : "blocks";
            int area = (radius * 2 + 1) * (radius * 2 + 1);
            Help.sendMsg(p, "§e" + (isCount ? "Found" : "Removed") + " §a" + totalRemoved + "§e entities in §b" + area + "§e " + unit + " around you.", true);
            
        } else {
            // Block-based clearing
            double radiusSquared = radius * radius;
            int chunkRadius = (radius / 16) + 2; // Buffer for edge chunks
            TownyIntegration towny = LaggRemover.getTownyIntegration();
            
            org.bukkit.Chunk centerChunk = playerLoc.getChunk();
            int centerChunkX = centerChunk.getX();
            int centerChunkZ = centerChunk.getZ();
            
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    org.bukkit.Chunk chunk = world.getChunkAt(centerChunkX + dx, centerChunkZ + dz);
                    if (chunk.isLoaded()) {
                        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
                            if (entity != null && !(entity instanceof org.bukkit.entity.Player)) {
                                double distanceSquared = entity.getLocation().distanceSquared(playerLoc);
                                if (distanceSquared <= radiusSquared) {
                                    // Check Towny protection
                                    if (towny != null && towny.isEntityProtected(entity)) {
                                        continue; // Skip protected entities
                                    }
                                    
                                    if (matchesEntityFilter(entity, entityFilter)) {
                                        if (!isCount) {
                                            entity.remove();
                                        }
                                        totalRemoved++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Help.sendMsg(p, "§e" + (isCount ? "Found" : "Removed") + " §a" + totalRemoved + "§e entities within §b" + radius + "§e blocks of you.", true);
        }
        
        return true;
    }
    
    private static int processChunkEntities(org.bukkit.Chunk chunk, String entityFilter, boolean isCount) {
        int count = 0;
        TownyIntegration towny = LaggRemover.getTownyIntegration();
        
        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
            if (entity != null && !(entity instanceof org.bukkit.entity.Player)) {
                // Check Towny protection first
                if (towny != null && towny.isEntityProtected(entity)) {
                    continue; // Skip protected entities
                }
                
                if (matchesEntityFilter(entity, entityFilter)) {
                    if (!isCount) {
                        entity.remove();
                    }
                    count++;
                }
            }
        }
        return count;
    }
    
    private static boolean matchesEntityFilter(org.bukkit.entity.Entity entity, String filter) {
        switch (filter.toLowerCase()) {
            case "all":
            case "a":
                return true;
            case "hostile":
            case "h":
                return entity instanceof org.bukkit.entity.Monster;
            case "peaceful":
            case "passive":
            case "p":
                return entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof org.bukkit.entity.Monster);
            case "items":
            case "i":
                return entity instanceof org.bukkit.entity.Item;
            case "animals":
                return entity instanceof org.bukkit.entity.Animals;
            case "mobs":
                return entity instanceof org.bukkit.entity.LivingEntity;
            default:
                // Try to match specific entity type
                try {
                    org.bukkit.entity.EntityType targetType = org.bukkit.entity.EntityType.valueOf(filter.toUpperCase());
                    return entity.getType() == targetType;
                } catch (IllegalArgumentException e) {
                    return false;
                }
        }
    }
}
