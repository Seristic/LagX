package com.seristic.lagx.util;

import com.seristic.lagx.main.HBZCleaner;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public abstract class Counter {
   private long on;
   private final long secondsDelay;
   private HashMap<Long, Counter.CountAction> actions = new HashMap<>();
   private boolean started = false;

   public abstract void onFinish();

   public Counter(long secondsDelay) {
      this.secondsDelay = secondsDelay;
      this.on = secondsDelay;
   }

   public boolean start() {
      if (!this.started) {
         this.started = true;
         this.one();
      }

      return this.started;
   }

   public void reset() {
      this.on = this.secondsDelay;
      this.started = false;
   }

   public void one() {
      if (this.actions.containsKey(this.on)) {
         this.actions.get(this.on).onTrigger();
      }

      Bukkit.getAsyncScheduler().runDelayed(HBZCleaner.getInstance(), task -> {
         if (this.started) {
            this.on--;
            if (this.on > 0L) {
               this.one();
               return;
            }

            this.reset();
            this.onFinish();
         }
      }, this.secondsDelay * 50L, TimeUnit.MILLISECONDS);
   }

   public Counter addAction(Counter.CountAction a) {
      this.actions.put(a.getTrigger(), a);
      return this;
   }

   public void setActions(HashMap<Long, Counter.CountAction> actions) {
      this.actions = actions;
   }

   public boolean isActive() {
      return this.started;
   }

   public abstract static class CountAction {
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
