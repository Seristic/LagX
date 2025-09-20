package com.seristic.lagx.proto.bin;

import com.seristic.lagx.api.aparser.ProtoParse;
import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.api.proto.Protocol;
import com.seristic.lagx.api.proto.ProtocolCategory;
import com.seristic.lagx.api.proto.help.HelpFormatter;
import com.seristic.lagx.util.Counter;
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

public class CCEntities implements LRProtocol {
   public static EntityType[] hostile;
   public static EntityType[] peaceful;
   public static Counter counter;
   private static final String help;

   @Override
   public void init() {
      counter = Protocol.getCounter(this);
   }

   @Override
   public String id() {
      return "cc_entities";
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
      int i = 0;
      boolean count = (Boolean)args[0];
      EntityType[] toClear = (EntityType[])args[1];
      LRProtocolResult result;
      if (args.length == 2) {
         final int i2 = this.clearEntities(count, toClear);
         result = new LRProtocolResult(this) {
            @Override
            public Object[] getData() {
               return new Object[]{i2};
            }
         };
      } else if (args.length == 3) {
         if (args[2] instanceof World world) {
            for (Chunk chunk : world.getLoadedChunks()) {
               i += clearEntities(Arrays.asList(chunk.getEntities()), count, toClear);
            }
         } else if (args[2] instanceof Chunk) {
            i = clearEntities(Arrays.asList(((Chunk)args[2]).getEntities()), count, toClear);
         } else if (!(args[2] instanceof Boolean)) {
            i = 0;
         } else {
            int ii = 0;

            for (World w : Bukkit.getWorlds()) {
               for (Chunk chunk : w.getLoadedChunks()) {
                  ii += clearEntities(Arrays.asList(chunk.getEntities()), count, toClear);
               }
            }

            i = ii;
         }

         final int i3 = i;
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
         public HashMap<String, ProtoParse.ProtoParseData> getKeysToClass() {
            HashMap<String, ProtoParse.ProtoParseData> k = new HashMap<>();
            k.put("Count", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.BOOLEAN, 0));
            k.put("ToClear", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.ENTITY_TYPE_ARRAY, 1));
            k.put("World", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.WORLD, 2));
            k.put("Chunk", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.CHUNK, 2));
            k.put("AllWorlds", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.BOOLEAN, 2));
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
         for (Chunk chunk : w.getLoadedChunks()) {
            i += clearEntities(Arrays.asList(chunk.getEntities()), count, include);
         }
      }

      return i;
   }

   private static boolean orAll(EntityType entityType, EntityType... all) {
      if (all == null) {
         return true;
      } else {
         for (EntityType type : all) {
            if (entityType.equals(type)) {
               return true;
            }
         }

         return false;
      }
   }

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
      help = new HelpFormatter()
         .set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eRemoves entities from all worlds, selected worlds, or selected chunks.")
         .set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eCPU, RAM, and NETWORK")
         .set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new CCEntities().getProtocolParser()))
         .set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <(int)CCed>}")
         .make();
   }
}
