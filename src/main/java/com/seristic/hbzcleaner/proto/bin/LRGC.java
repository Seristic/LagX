package com.seristic.hbzcleaner.proto.bin;

import com.seristic.hbzcleaner.api.aparser.ProtoParse;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.api.proto.ProtocolCategory;
import com.seristic.hbzcleaner.api.proto.help.HelpFormatter;
import com.seristic.hbzcleaner.util.Counter;
import java.util.HashMap;

public class LRGC implements LRProtocol {
   public static Counter counter;
   private static String help = new HelpFormatter()
      .set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eReduces RAM usage by removing unneeded items stored in RAM.")
      .set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eRAM")
      .set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new LRGC().getProtocolParser()))
      .set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <(long)ramMB>}")
      .make();

   @Override
   public void init() {
      counter = Protocol.getCounter(this);
   }

   @Override
   public String id() {
      return "lr_gc";
   }

   @Override
   public String help() {
      return help;
   }

   @Override
   public ProtocolCategory[] category() {
      return new ProtocolCategory[]{ProtocolCategory.RAM};
   }

   @Override
   public LRProtocolResult run(Object[] args) {
      Runtime r = Runtime.getRuntime();
      long Lused = (r.totalMemory() - r.freeMemory()) / 1024L / 1024L;
      System.gc();
      final long used = Lused - (r.totalMemory() - r.freeMemory()) / 1024L / 1024L;
      return new LRProtocolResult(this) {
         @Override
         public Object[] getData() {
            return new Object[]{used};
         }
      };
   }

   @Override
   public ProtoParse getProtocolParser() {
      return new ProtoParse() {
         @Override
         public HashMap<String, ProtoParse.ProtoParseData> getKeysToClass() {
            return new HashMap<>();
         }
      };
   }
}
