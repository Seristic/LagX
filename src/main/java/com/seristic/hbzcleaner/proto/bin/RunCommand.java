package com.seristic.hbzcleaner.proto.bin;

import com.seristic.hbzcleaner.api.aparser.ProtoParse;
import com.seristic.hbzcleaner.api.proto.LRProtocol;
import com.seristic.hbzcleaner.api.proto.LRProtocolResult;
import com.seristic.hbzcleaner.api.proto.Protocol;
import com.seristic.hbzcleaner.api.proto.ProtocolCategory;
import com.seristic.hbzcleaner.api.proto.help.HelpFormatter;
import com.seristic.hbzcleaner.util.Counter;
import java.util.HashMap;
import org.bukkit.Bukkit;

public class RunCommand implements LRProtocol {
   public static Counter counter;
   private static final String help = new HelpFormatter()
      .set(HelpFormatter.HelpFormatterType.DESCRIPTION, "§eRuns a server command from LagX or another plugin.")
      .set(HelpFormatter.HelpFormatterType.CATEGORIES, "§eUNKNOWN")
      .set(HelpFormatter.HelpFormatterType.ARGUMENTS, HelpFormatter.generateArgs(new RunCommand().getProtocolParser()))
      .set(HelpFormatter.HelpFormatterType.RETURNS, "§e{0: <none>}")
      .make();

   @Override
   public void init() {
      counter = Protocol.getCounter(this);
   }

   @Override
   public String id() {
      return "run_c";
   }

   @Override
   public String help() {
      return help;
   }

   @Override
   public ProtocolCategory[] category() {
      return new ProtocolCategory[]{ProtocolCategory.UNKNOWN};
   }

   @Override
   public LRProtocolResult run(Object[] args) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), (String)args[0]);
      return new LRProtocolResult(this) {
         @Override
         public Object[] getData() {
            return new Object[0];
         }
      };
   }

   @Override
   public ProtoParse getProtocolParser() {
      return new ProtoParse() {
         @Override
         public HashMap<String, ProtoParse.ProtoParseData> getKeysToClass() {
            HashMap<String, ProtoParse.ProtoParseData> k = new HashMap<>();
            k.put("Command", new ProtoParse.ProtoParseData(ProtoParse.ProtoParseKeywords.STRING, 0));
            return k;
         }
      };
   }
}
