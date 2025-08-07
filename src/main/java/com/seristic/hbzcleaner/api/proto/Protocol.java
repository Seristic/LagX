package com.seristic.hbzcleaner.api.proto;

import com.seristic.hbzcleaner.main.LaggRemover;
import com.seristic.hbzcleaner.proto.bin.CCEntities;
import com.seristic.hbzcleaner.proto.bin.CCItems;
import com.seristic.hbzcleaner.proto.bin.LRGC;
import com.seristic.hbzcleaner.proto.bin.RunCommand;
import com.seristic.hbzcleaner.util.Counter;
import com.seristic.hbzcleaner.util.LRConfig;
import java.util.Collection;
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
        List<String> args = LaggRemover.instance.getConfig().getStringList("protocol_warnings." + p + ".stages");
        for (String arg : args) {
            final String[] a = arg.replaceAll("&", "§").replaceAll("%PREFIX%", LaggRemover.prefix).split(":");
            if (a[0].equalsIgnoreCase("f")) {
                var = a[1];
            } else {
                actions.put(Long.parseLong(a[0]), new Counter.CountAction(Long.parseLong(a[0])) { // from class: drew6017.lr.api.proto.Protocol.1
                    @Override // drew6017.lr.util.Counter.CountAction
                    public void onTrigger() {
                        LaggRemover.broadcast(a[1]);
                    }
                });
            }
        }
        final String var2 = var;
        Counter counter = new Counter(LaggRemover.instance.getConfig().getLong("protocol_warnings." + p + ".time")) { // from class: drew6017.lr.api.proto.Protocol.2
            @Override
            public void onFinish() {
                if (var2 != null) {
                    LaggRemover.broadcast(var2);
                }
            }
        };
        counter.setActions(actions);
        return counter;
    }

    public static void rund(LRProtocol p, Object[] args, DelayedLRProtocolResult res) {
        Counter c = LRConfig.counters.get(p);
        if (c.start()) {
            delayLoop(c, res, p, args);
        }
    }

    public static void rund(String p, Object[] args, DelayedLRProtocolResult res) {
        rund(getProtocol(p), args, res);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void delayLoop(final Counter c, final DelayedLRProtocolResult res, final LRProtocol p, final Object[] args) {
        Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
            if (c.isActive()) {
                Protocol.delayLoop(c, res, p, args);
            } else {
                res.receive(run(p, args));
            }
        }, 50L, TimeUnit.MILLISECONDS); // 1 tick = 50ms
    }
}
