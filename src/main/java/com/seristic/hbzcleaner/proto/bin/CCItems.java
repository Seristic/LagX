package com.seristic.hbzcleaner.proto.bin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.seristic.hbzcleaner.api.aparser.ProtoParse;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.api.proto.ProtocolCategory;
import com.seristic.hbzcleaner.api.proto.help.HelpFormatter;
import com.seristic.hbzcleaner.main.LaggRemover;
import com.seristic.hbzcleaner.util.Counter;

public class CCItems implements LRProtocol {
    public static Counter counter;
    private static final String help = new HelpFormatter().set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eRemoves items from all worlds, selected worlds, or selected chunks.").set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eCPU, RAM, and NETWORK").set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new CCItems().getProtocolParser())).set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <(int)CCed>}").make();

    @Override
    public void init() {
        counter = Protocol.getCounter(this);
    }

    @Override
    public String id() {
        return "cc_items";
    }

    @Override
    public String help() {
        return help;
    }

    @Override
    public ProtocolCategory[] category() {
        return new ProtocolCategory[]{ProtocolCategory.CPU, ProtocolCategory.RAM, ProtocolCategory.NETWORK};
    }

    @Override
    public LRProtocolResult run(Object[] args) {
        LRProtocolResult result;
        boolean count = (Boolean) args[0];
        if (args.length == 1) {
            final int i = clearItems(count);
            result = new LRProtocolResult(this) {
                @Override
                public Object[] getData() {
                    return new Object[]{Integer.valueOf(i)};
                }
            };
        } else if (args.length == 2) {
            final int i3 = args[1] instanceof World ? clearItemsFromWorld((World) args[1], count) : clearItems(Arrays.asList(((Chunk) args[1]).getEntities()), count);
            result = new LRProtocolResult(this) {
                @Override
                public Object[] getData() {
                    return new Object[]{i3};
                }
            };
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public ProtoParse getProtocolParser() {
        return new ProtoParse() {
            @Override
            public HashMap<String, ProtoParseData> getKeysToClass() {
                HashMap<String, ProtoParseData> k = new HashMap<>();
                k.put("Count", new ProtoParseData(ProtoParseKeywords.BOOLEAN, 0));
                k.put("World", new ProtoParseData(ProtoParseKeywords.WORLD, 1));
                k.put("Chunk", new ProtoParseData(ProtoParseKeywords.CHUNK, 1));
                return k;
            }
        };
    }

    private int clearItems(List<Entity> ents, boolean count) {
        int i = 0;
        for (Entity e : ents) {
            if (e.getType().equals(EntityType.ITEM)) {
                if (!count) {
                    e.remove();
                }
                i++;
            }
        }
        return i;
    }

    private int clearItems(boolean count) {
        // For Folia compatibility, we need to schedule clearing per-chunk on the region scheduler
        // Since we can't return the count immediately in async context, we'll handle this differently
        
        if (count) {
            // For counting, iterate through chunks synchronously (read-only operation)
            int i = 0;
            for (World w : Bukkit.getWorlds()) {
                for (Chunk chunk : w.getLoadedChunks()) {
                    for (Entity e : chunk.getEntities()) {
                        if (e.getType().equals(EntityType.ITEM)) {
                            i++;
                        }
                    }
                }
            }
            return i;
        } else {
            // Track how many items we'll clear
            int totalItems = 0;
            
            // First count the items
            for (World w : Bukkit.getWorlds()) {
                for (Chunk chunk : w.getLoadedChunks()) {
                    for (Entity e : chunk.getEntities()) {
                        if (e.getType().equals(EntityType.ITEM)) {
                            totalItems++;
                        }
                    }
                }
            }
            
            // For clearing, schedule per-chunk operations on region scheduler
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    final Chunk finalChunk = chunk;
                    // Schedule clearing for this specific chunk
                    Bukkit.getRegionScheduler().run(
                        (org.bukkit.plugin.Plugin) LaggRemover.getInstance(), 
                        world, 
                        finalChunk.getX(), 
                        finalChunk.getZ(), 
                        task -> {
                            // Clear items in this specific chunk
                            for (Entity e : finalChunk.getEntities()) {
                                if (e.getType().equals(EntityType.ITEM)) {
                                    try {
                                        e.remove();
                                    } catch (Exception ex) {
                                        // Entity might have been moved to different region
                                    }
                                }
                            }
                        }
                    );
                }
            }
            
            // Return the pre-counted items
            return totalItems;
        }
    }
    
    private int clearItemsFromWorld(World world, boolean count) {
        if (count) {
            // For counting, iterate through chunks synchronously (read-only operation)
            int i = 0;
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity e : chunk.getEntities()) {
                    if (e.getType().equals(EntityType.ITEM)) {
                        i++;
                    }
                }
            }
            return i;
        } else {
            // First count the items to be cleared
            int totalItems = 0;
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity e : chunk.getEntities()) {
                    if (e.getType().equals(EntityType.ITEM)) {
                        totalItems++;
                    }
                }
            }
            
            // For clearing, schedule per-chunk operations on region scheduler
            for (Chunk chunk : world.getLoadedChunks()) {
                final Chunk finalChunk = chunk;
                // Schedule clearing for this specific chunk
                Bukkit.getRegionScheduler().run(
                    (org.bukkit.plugin.Plugin) LaggRemover.getInstance(), 
                    world, 
                    finalChunk.getX(), 
                    finalChunk.getZ(), 
                    task -> {
                        // Clear items in this specific chunk
                        for (Entity e : finalChunk.getEntities()) {
                            if (e.getType().equals(EntityType.ITEM)) {
                                try {
                                    e.remove();
                                } catch (Exception ex) {
                                    // Entity might have been moved to different region
                                }
                            }
                        }
                    }
                );
            }
            
            // Return the pre-counted items
            return totalItems;
        }
    }
}
