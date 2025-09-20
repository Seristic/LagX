package com.seristic.lagx.api.proto;

import com.seristic.lagx.main.LagX;
import com.seristic.lagx.proto.bin.CCEntities;
import com.seristic.lagx.proto.bin.CCItems;
import com.seristic.lagx.proto.bin.LRGC;
import com.seristic.lagx.proto.bin.RunCommand;
import com.seristic.lagx.util.Counter;
import com.seristic.lagx.util.HBZConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public class Protocol {
   private static HashMap<String, LRProtocol> protocols;

   public static void init() {
      protocols = new HashMap<>();
      register(new CCEntities(), new CCItems(), new LRGC(), new RunCommand());
   }

   public static void register(LRProtocol... pros) {
      for (LRProtocol p : pros) {
         register(p);
      }
   }

   public static void register(LRProtocol p) {
      p.init();
      protocols.put(p.id(), p);
   }

   public static Collection<LRProtocol> getProtocols() {
      return protocols.values();
   }

   public static LRProtocol getProtocol(String name) {
      return protocols.get(name);
   }

   public static LRProtocolResult run(String p, Object[] args) {
      return protocols.get(p).run(args);
   }

   public static LRProtocolResult run(LRProtocol p, Object[] args) {
      return p.run(args);
   }

   public static Counter getCounter(LRProtocol p) {
      return getCounter(p.id());
   }

   public static Counter getCounter(String p) {
      String var = null;
      HashMap<Long, Counter.CountAction> actions = new HashMap<>();
      List<String> args = LagX.instance.getConfig().getStringList("protocol_warnings." + p + ".stages");
      if (args == null) {
         args = Collections.emptyList();
      }

      for (String arg : args) {
         final String[] a = arg.replaceAll("&", "§").replaceAll("%PREFIX%", LagX.prefix).split(":");
         if (a[0].equalsIgnoreCase("f")) {
            var = a[1];
         } else {
            actions.put(Long.parseLong(a[0]), new Counter.CountAction(Long.parseLong(a[0])) {
               @Override
               public void onTrigger() {
                  LagX.broadcastWarn(a[1]);
               }
            });
         }
      }

      final String var2 = var;
      Counter counter = new Counter(LagX.instance.getConfig().getLong("protocol_warnings." + p + ".time")) {
         @Override
         public void onFinish() {
            if (var2 != null) {
               LagX.broadcastWarn(var2);
            }
         }
      };
      counter.setActions(actions);
      return counter;
   }

   public static void rund(LRProtocol p, Object[] args, DelayedLRProtocolResult res) {
      Counter c = HBZConfig.counters.get(p);
      if (c.start()) {
         delayLoop(c, res, p, args);
      }
   }

   public static void rund(String p, Object[] args, DelayedLRProtocolResult res) {
      rund(getProtocol(p), args, res);
   }

   public static void delayLoop(Counter c, DelayedLRProtocolResult res, LRProtocol p, Object[] args) {
      Bukkit.getAsyncScheduler().runDelayed(LagX.getInstance(), task -> {
         if (c.isActive()) {
            delayLoop(c, res, p, args);
         } else {
            res.receive(run(p, args));
         }
      }, 50L, TimeUnit.MILLISECONDS);
   }
}
