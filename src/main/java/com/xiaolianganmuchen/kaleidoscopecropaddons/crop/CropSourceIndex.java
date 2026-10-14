package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CropSourceIndex {
   private static final Map<String, String> PREFIX_BY_MOD = new ConcurrentHashMap<>();
   private static final Set<String> DISCOVER_NAMESPACES = ConcurrentHashMap.newKeySet();

   private CropSourceIndex() {
   }

   public static void register(String modId, String prefix, boolean discover) {
      if (modId != null && !modId.isBlank()) {
         PREFIX_BY_MOD.put(modId, prefix != null && !prefix.isBlank() ? prefix : fallbackPrefix(modId));
         CropCatalog.registerSourceLabel(modId, "source.kaleidoscopecropaddons." + modId);
         if (discover) {
            DISCOVER_NAMESPACES.add(modId);
         }
      }
   }

   public static String prefixFor(String modId) {
      return modId != null && !modId.isBlank() ? PREFIX_BY_MOD.getOrDefault(modId, fallbackPrefix(modId)) : "x";
   }

   public static Set<String> discoverNamespaces() {
      return Set.copyOf(DISCOVER_NAMESPACES);
   }

   private static String fallbackPrefix(String modId) {
      String compact = modId.replace("_", "");
      return compact.length() <= 3 ? compact : compact.substring(0, 3);
   }

   static {
      register("kaleidoscope_cookery", "kc", false);
      register("kaleidoscope_chinesefood", "kcf", false);
      register("kaleidoscope_grilling", "kg", false);
      register("kaleidoscope_dim_wine", "kdw", false);
      register("kaleidoscope_twilight", "kt", false);
      register("kaleidoscope_orchard", "ko", false);
      register("kaleidoscope_tavern", "ktv", false);
      register("custom", "cu", false);
   }
}
