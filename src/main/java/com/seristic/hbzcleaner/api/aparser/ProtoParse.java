package com.seristic.hbzcleaner.api.aparser;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public abstract class ProtoParse {
   public abstract HashMap<String, ProtoParse.ProtoParseData> getKeysToClass();

   public abstract static class KeywordParser {
      public abstract Object parse(String var1);
   }

   public static class ProtoParseData {
      private final ProtoParse.ProtoParseKeywords clazz;
      private final int index;

      public ProtoParseData(ProtoParse.ProtoParseKeywords clazz, int index) {
         this.clazz = clazz;
         this.index = index;
      }

      public int getIndex() {
         return this.index;
      }

      public ProtoParse.ProtoParseKeywords getClazz() {
         return this.clazz;
      }
   }

   public static enum ProtoParseKeywords {
      BOOLEAN("Boolean", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            return Boolean.parseBoolean(data);
         }
      }),
      INTEGER("Integer", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            return Integer.parseInt(data);
         }
      }),
      STRING("String", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            return data;
         }
      }),
      WORLD("World", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            return Bukkit.getWorld(data);
         }
      }),
      CHUNK("Chunk", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            String[] pos = data.split(",");
            World world = Bukkit.getWorld(pos[0]);
            return world == null ? null : world.getChunkAt(Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
         }
      }),
      ENTITY_TYPE_ARRAY("EntityType[]", new ProtoParse.KeywordParser() {
         @Override
         public Object parse(String data) {
            String[] s = data.split(",");
            EntityType[] entityTypes = new EntityType[s.length];

            for (int i = 0; i < entityTypes.length; i++) {
               entityTypes[i] = EntityType.valueOf(s[i]);
            }

            return entityTypes;
         }
      });

      private final ProtoParse.KeywordParser parser;
      private final String name;

      private ProtoParseKeywords(String name, ProtoParse.KeywordParser parser) {
         this.parser = parser;
         this.name = name;
      }

      public ProtoParse.KeywordParser getParser() {
         return this.parser;
      }

      public String getProperName() {
         return this.name;
      }
   }
}
