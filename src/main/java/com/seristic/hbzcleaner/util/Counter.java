package com.seristic.hbzcleaner.util;

import com.seristic.hbzcleaner.main.LaggRemover;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

/* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/util/Counter.class */
public abstract class Counter {
    private long on;
    private final long secondsDelay;
    private HashMap<Long, CountAction> actions = new HashMap<>();
    private boolean started = false;

    public abstract void onFinish();

    public Counter(long secondsDelay) {
        this.secondsDelay = secondsDelay;
        this.on = secondsDelay;
    }

    public boolean start() {
        if (!this.started) {
            this.started = true;
            one();
        }
        return this.started;
    }

    public void reset() {
        this.on = this.secondsDelay;
        this.started = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void one() {
        if (this.actions.containsKey(this.on)) {
            this.actions.get(this.on).onTrigger();
        }
        // Use async scheduler for counter (Folia compatible)
        Bukkit.getAsyncScheduler().runDelayed(LaggRemover.getInstance(), task -> {
            if (Counter.this.started) {
                Counter.this.on -= 1;
                if (Counter.this.on > 0) {
                    Counter.this.one();
                    return;
                }
                Counter.this.reset();
                Counter.this.onFinish();
            }
        }, this.secondsDelay * 50L, TimeUnit.MILLISECONDS); // Convert ticks to milliseconds
    }

    public Counter addAction(CountAction a) {
        this.actions.put(a.getTrigger(), a);
        return this;
    }

    public void setActions(HashMap<Long, CountAction> actions) {
        this.actions = actions;
    }

    public boolean isActive() {
        return this.started;
    }

    /* loaded from: LaggRemover-2.0.6.jar:drew6017/lr/util/Counter$CountAction.class */
    public static abstract class CountAction {
        private final long trigger;

        public abstract void onTrigger();

        public CountAction(long trigger) {
            this.trigger = trigger;
        }

        public long getTrigger() {
            return this.trigger;
        }
    }
}
