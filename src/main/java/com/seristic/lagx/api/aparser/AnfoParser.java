package com.seristic.lagx.api.aparser;

import com.seristic.lagx.api.proto.LRProtocol;
import com.seristic.lagx.util.DoubleVar;
import java.util.Collections;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AnfoParser {
   public static DoubleVar<Object[], Boolean> parse(LRProtocol protocol, String dataRaw)
         throws AnfoParser.AnfoParseException, ParseException {
      JSONObject jsonData = readJSON(dataRaw);
      HashMap<String, ProtoParse.ProtoParseData> k = protocol.getProtocolParser().getKeysToClass();
      HashMap<Integer, Object> objMap = new HashMap<>();
      boolean isDelay = false;

      for (Object obj : jsonData.keySet()) {
         String key = (String) obj;
         if (key.equals("Delay")) {
            isDelay = Boolean.parseBoolean((String) jsonData.get(key));
         } else {
            if (!k.containsKey(key)) {
               throw new AnfoParser.AnfoParseException(
                     "The arguments for \"" + protocol.id() + "\" contained an unregistered key.");
            }

            ProtoParse.ProtoParseData protoParseData = k.get(key);
            String dataIn = (String) jsonData.get(key);
            if (dataIn == null) {
               objMap.put(protoParseData.getIndex(), null);
            } else {
               objMap.put(protoParseData.getIndex(),
                     dataIn.equalsIgnoreCase("null") ? null : protoParseData.getClazz().getParser().parse(dataIn));
            }
         }
      }

      Object[] oa = new Object[Collections.max(objMap.keySet()) + 1];

      for (int i = 0; i < oa.length; i++) {
         oa[i] = objMap.getOrDefault(i, null);
      }

      return new DoubleVar<>(oa, isDelay);
   }

   private static JSONObject readJSON(String data) throws ParseException {
      JSONParser parse = new JSONParser();
      return (JSONObject) parse.parse(data);
   }

   public static class AnfoParseException extends Exception {
      private final String message;

      AnfoParseException(String message) {
         this.message = message;
      }

      @Override
      public String toString() {
         return this.message;
      }
   }
}
