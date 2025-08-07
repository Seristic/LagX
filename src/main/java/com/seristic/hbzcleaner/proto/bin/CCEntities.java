package com.seristic.hbzcleaner.proto.bin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;

import com.seristic.hbzcleaner.api.aparser.ProtoParse;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.api.proto.ProtocolCategory;
import com.seristic.hbzcleaner.api.proto.help.HelpFormatter;
import com.seristic.hbzcleaner.util.Counter;

public class CCEntities implements LRProtocol {
    public static EntityType[] hostile;
    public static EntityType[] peaceful;
    public static Counter counter;
    private static final String help;

    static {
        List<EntityType> hostEnts = new ArrayList<>();
        List<EntityType> peaceEnts = new ArrayList<>();
        for (EntityType ent : EntityType.values()) {
            if (ent.getEntityClass() != null && LivingEntity.class.isAssignableFrom(ent.getEntityClass())) {
                if (Monster.class.isAssignableFrom(ent.getEntityClass())) {
                    hostEnts.add(ent);
                } else {
                    peaceEnts.add(ent);
                }
            }
        }
        hostile = hostEnts.toArray(new EntityType[0]);
        peaceful = peaceEnts.toArray(new EntityType[0]);
        help = new HelpFormatter().set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eRemoves entities from all worlds, selected worlds, or selected chunks.").set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eCPU, RAM, and NETWORK").set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new CCEntities().getProtocolParser())).set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <(int)CCed>}").make();
    }

    @Override // drew6017.lr.api.proto.LRProtocol
    public void init() {
        counter = Protocol.getCounter(this);
    }

    @Override // drew6017.lr.api.proto.LRProtocol
    public String id() {
        return "cc_entities";
    }

    @Override // drew6017.lr.api.proto.LRProtocol
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
        int i = 0;
        boolean count = (Boolean) args[0];
        EntityType[] toClear = (EntityType[]) args[1];
        if (args.length == 2) {
            final int i2 = clearEntities(count, toClear);
            result = new LRProtocolResult(this) {
                @Override
                public Object[] getData() {
                    return new Object[] { i2 };
                }
            };
        } else if (args.length == 3) {
            if (args[2] instanceof World) {
                World world = (World) args[2];
                // Iterate through all loaded chunks in the world to get entities
                for (Chunk chunk : world.getLoadedChunks()) {
                    i += clearEntities(Arrays.asList(chunk.getEntities()), count, toClear);
                }
            } else if (args[2] instanceof Chunk) {
                i = clearEntities(Arrays.asList(((Chunk) args[2]).getEntities()), count, toClear);
            } else if (args[2] instanceof Boolean) {
                int ii = 0;
                for (World w : Bukkit.getWorlds()) {
                    // Iterate through all loaded chunks in each world to get entities
                    for (Chunk chunk : w.getLoadedChunks()) {
                        ii += clearEntities(Arrays.asList(chunk.getEntities()), count, toClear);
                    }
                }
                i = ii;
            } else {
                i = 0;
            }
            final int i3 = i;
            result = new LRProtocolResult(this) {
                @Override
                public Object[] getData() {
                    return new Object[] { i3 };
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
                k.put("ToClear", new ProtoParseData(ProtoParseKeywords.ENTITY_TYPE_ARRAY, 1));
                k.put("World", new ProtoParseData(ProtoParseKeywords.WORLD, 2));
                k.put("Chunk", new ProtoParseData(ProtoParseKeywords.CHUNK, 2));
                k.put("AllWorlds", new ProtoParseData(ProtoParseKeywords.BOOLEAN, 2));
                return k;
            }
        };
    }

    public static int clearEntities(List<Entity> ents, boolean count, EntityType... include) {
        int i = 0;
        for (Entity e : ents) {
            if (!e.getType().equals(EntityType.PLAYER) && orAll(e.getType(), include)) {
                if (!count) {
                    e.remove();
                }
                i++;
            }
        }
        return i;
    }

    private int clearEntities(boolean count, EntityType... include) {
        int i = 0;
        for (World w : Bukkit.getWorlds()) {
            // Iterate through all loaded chunks in the world to get entities
            for (Chunk chunk : w.getLoadedChunks()) {
                i += clearEntities(Arrays.asList(chunk.getEntities()), count, include);
            }
        }
        return i;
    }

    private static boolean orAll(EntityType entityType, EntityType... all) {
        if (all == null) {
            return true;
        }
        for (EntityType type : all) {
            if (entityType.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
