package com.seristic.lagx.api.proto.help;

import com.seristic.lagx.api.aparser.ProtoParse;
import com.seristic.lagx.util.DoubleVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class HelpFormatter {
   private HashMap<HelpFormatter.HelpFormatterType, String> parts = new HashMap<>();

   public HelpFormatter set(HelpFormatter.HelpFormatterType type, String var) {
      if (!type.equals(HelpFormatter.HelpFormatterType.RETURNS)) {
         var = var + "\n";
      }

      this.parts.put(type, var);
      return this;
   }

   @NotNull
   public String make0() throws HelpFormatter.HelpFormatException {
      if (this.parts.size() != 4) {
         throw new HelpFormatter.HelpFormatException(
               "You must set all help fields in order to compile help information.");
      } else {
         return "§aDescription: "
               + this.parts.get(HelpFormatter.HelpFormatterType.DESCRIPTION)
               + "§aCategory(s): "
               + this.parts.get(HelpFormatter.HelpFormatterType.CATEGORIES)
               + "§aArgument(s): "
               + this.parts.get(HelpFormatter.HelpFormatterType.ARGUMENTS)
               + "§aReturn(s): "
               + this.parts.get(HelpFormatter.HelpFormatterType.RETURNS);
      }
   }

   public String make() {
      try {
         return this.make0();
      } catch (HelpFormatter.HelpFormatException var2) {
         return null;
      }
   }

   public String getInfo(HelpFormatter.HelpFormatterType type) {
      return this.parts.get(type);
   }

   @NotNull
   public static String generateArgs(ProtoParse protoParse) {
      HashMap<String, ProtoParse.ProtoParseData> var = protoParse.getKeysToClass();
      StringBuilder sb = new StringBuilder();
      HashMap<Integer, List<DoubleVar<String, ProtoParse.ProtoParseData>>> var1 = new HashMap<>();

      for (String s : var.keySet()) {
         ProtoParse.ProtoParseData protoParseData = var.get(s);
         List<DoubleVar<String, ProtoParse.ProtoParseData>> list = (List<DoubleVar<String, ProtoParse.ProtoParseData>>) (var1
               .containsKey(
                     protoParseData.getIndex())
                           ? var1.get(protoParseData.getIndex())
                           : new ArrayList<>());
         list.add(new DoubleVar<>(s, protoParseData));
         var1.put(protoParseData.getIndex(), list);
      }

      sb.append("§e{");
      int v1length = var1.size();

      for (Integer num : var1.keySet()) {
         int i = num;
         sb.append(i).append(": ");
         List<DoubleVar<String, ProtoParse.ProtoParseData>> list = var1.get(i);
         int size = list.size();

         for (int j = 0; j < size; j++) {
            DoubleVar<String, ProtoParse.ProtoParseData> vag = list.get(j);
            sb.append(vag.getVar2().getClazz().getProperName()).append("(").append(vag.getVar1()).append(")");
            if (j + 1 < size) {
               sb.append(" | ");
            }
         }

         if (i + 1 != v1length) {
            sb.append(", ");
         }
      }

      sb.append("}");
      return sb.toString();
   }

   public static class HelpFormatException extends Exception {
      private final String message;

      HelpFormatException(String message) {
         this.message = message;
      }

      @Override
      public String toString() {
         return this.message;
      }
   }

   public static enum HelpFormatterType {
      DESCRIPTION,
      CATEGORIES,
      ARGUMENTS,
      RETURNS;
   }
}
