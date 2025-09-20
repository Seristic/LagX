package com.seristic.lagx.proto.bin;

import com.seristic.lagx.api.aparser.ProtoParse;
import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.api.proto.LRProtocolResult;
import com.seristic.lagx.api.proto.Protocol;
import com.seristic.lagx.api.proto.ProtocolCategory;
import com.seristic.lagx.api.proto.help.HelpFormatter;
import com.seristic.lagx.main.HBZCleaner;
import com.seristic.lagx.util.Counter;
import com.seristic.lagx.util.PlayerDeathTracker;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public class CCItems implements LRProtocol {
   public static Counter counter;
   private static final String help = new HelpFormatter()
      .set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eRemoves items from all worlds, selected worlds, or selected chunks.")
      .set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eCPU, RAM, and NETWORK")
      .set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new CCItems().getProtocolParser()))
      .set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <(int)CCed>}")
      .make();

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
      boolean count = (Boolean)args[0];
      LRProtocolResult result;
      if (args.length == 1) {
         final int i = this.clearItems(count);
         result = new LRProtocolResult(this) {
            @Override
            public Object[] getData() {
               return new Object[]{i};
            }
         };
      } else if (args.length == 2) {
         final int i3 = args[1] instanceof World
            ? this.clearItemsFromWorld((World)args[1], count)
            : this.clearItems(Arrays.asList(((Chunk)args[1]).getEntities()), count);
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
            k.put("World", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.WORLD, 1));
            k.put("Chunk", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.CHUNK, 1));
            return k;
         }
      };
   }

   private int clearItems(List<Entity> ents, boolean count) {
      int i = 0;
      int protectedItems = 0;
      PlayerDeathTracker deathTracker = null;

      try {
         deathTracker = HBZCleaner.getInstance().getPlayerDeathTracker();
      } catch (Exception var10) {
      }

      for (Entity e : ents) {
         if (e instanceof Item) {
            Item item = (Item)e;
            boolean isProtected = deathTracker != null && deathTracker.isItemProtected(item);
            if (isProtected) {
               protectedItems++;
               if (count) {
               }
            } else {
               if (!count) {
                  e.remove();
               }

               i++;
            }
         }
      }

      if (protectedItems > 0) {
         HBZCleaner.getInstance().getLogger().info("Protected " + protectedItems + " items from auto-clear due to recent player deaths");
      }

      return i;
   }

   private int clearItems(boolean count) {
      PlayerDeathTracker deathTracker = null;

      try {
         deathTracker = HBZCleaner.getInstance().getPlayerDeathTracker();
      } catch (Exception var17) {
      }

      int protectedItems = 0;
      if (count) {
         int i = 0;

         for (World w : Bukkit.getWorlds()) {
            for (Chunk chunk : w.getLoadedChunks()) {
               for (Entity e : chunk.getEntities()) {
                  if (e instanceof Item) {
                     Item item = (Item)e;
                     boolean isProtected = deathTracker != null && deathTracker.isItemProtected(item);
                     if (isProtected) {
                        protectedItems++;
                     } else {
                        i++;
                     }
                  }
               }
            }
         }

         if (protectedItems > 0) {
            HBZCleaner.getInstance().getLogger().info("Protected " + protectedItems + " items from auto-clear due to recent player deaths");
         }

         return i;
      } else {
         int totalItems = 0;

         for (World w : Bukkit.getWorlds()) {
            for (Chunk chunk : w.getLoadedChunks()) {
               for (Entity ex : chunk.getEntities()) {
                  if (ex instanceof Item) {
                     Item item = (Item)ex;
                     boolean isProtected = deathTracker != null && deathTracker.isItemProtected(item);
                     if (isProtected) {
                        protectedItems++;
                     } else {
                        totalItems++;
                     }
                  }
               }
            }
         }

         for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
               PlayerDeathTracker finalDeathTracker = deathTracker;
               Bukkit.getRegionScheduler().run(HBZCleaner.getInstance(), world, chunk.getX(), chunk.getZ(), task -> {
                  for (Entity exx : chunk.getEntities()) {
                     if (exx instanceof Item) {
                        Item itemx = (Item)exx;
                        boolean isProtectedx = finalDeathTracker != null && finalDeathTracker.isItemProtected(itemx);
                        if (!isProtectedx) {
                           try {
                              exx.remove();
                           } catch (Exception var10x) {
                           }
                        }
                     }
                  }
               });
            }
         }

         if (protectedItems > 0) {
            HBZCleaner.getInstance().getLogger().info("Protected " + protectedItems + " items from auto-clear due to recent player deaths");
         }

         return totalItems;
      }
   }

   private int clearItemsFromWorld(World world, boolean count) {
      PlayerDeathTracker deathTracker = null;

      try {
         deathTracker = HBZCleaner.getInstance().getPlayerDeathTracker();
      } catch (Exception var16) {
      }

      int protectedItems = 0;
      if (count) {
         int i = 0;

         for (Chunk chunk : world.getLoadedChunks()) {
            for (Entity e : chunk.getEntities()) {
               if (e instanceof Item) {
                  Item item = (Item)e;
                  boolean isProtected = deathTracker != null && deathTracker.isItemProtected(item);
                  if (isProtected) {
                     protectedItems++;
                  } else {
                     i++;
                  }
               }
            }
         }

         if (protectedItems > 0) {
            HBZCleaner.getInstance()
               .getLogger()
               .info("Protected " + protectedItems + " items from auto-clear due to recent player deaths in world " + world.getName());
         }

         return i;
      } else {
         int totalItems = 0;

         for (Chunk chunk : world.getLoadedChunks()) {
            for (Entity ex : chunk.getEntities()) {
               if (ex instanceof Item) {
                  Item item = (Item)ex;
                  boolean isProtected = deathTracker != null && deathTracker.isItemProtected(item);
                  if (isProtected) {
                     protectedItems++;
                  } else {
                     totalItems++;
                  }
               }
            }
         }

         for (Chunk chunk : world.getLoadedChunks()) {
            PlayerDeathTracker finalDeathTracker = deathTracker;
            Bukkit.getRegionScheduler().run(HBZCleaner.getInstance(), world, chunk.getX(), chunk.getZ(), task -> {
               for (Entity exx : chunk.getEntities()) {
                  if (exx instanceof Item) {
                     Item itemx = (Item)exx;
                     boolean isProtectedx = finalDeathTracker != null && finalDeathTracker.isItemProtected(itemx);
                     if (!isProtectedx) {
                        try {
                           exx.remove();
                        } catch (Exception var10) {
                        }
                     }
                  }
               }
            });
         }

         if (protectedItems > 0) {
            HBZCleaner.getInstance()
               .getLogger()
               .info("Protected " + protectedItems + " items from auto-clear due to recent player deaths in world " + world.getName());
         }

         return totalItems;
      }
   }
}
