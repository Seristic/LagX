package com.seristic.hbzcleaner.api.proto;

import com.seristic.hbzcleaner.api.aparser.ProtoParse;

public interface LRProtocol {
   void init();

   String id();

   String help();

   ProtocolCategory[] category();

   LRProtocolResult run(Object[] var1);

   ProtoParse getProtocolParser();
}
