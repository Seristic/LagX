package com.seristic.hbzcleaner.api.proto;

public abstract class LRProtocolResult {
   private final LRProtocol protocol;

   public abstract Object[] getData();

   public LRProtocolResult(LRProtocol protocol) {
      this.protocol = protocol;
   }

   public LRProtocol getSuper() {
      return this.protocol;
   }
}
