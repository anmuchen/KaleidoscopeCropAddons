package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

public final class CropIds {
   private CropIds() {
   }

   public static String compose(String prefix, String key) {
      String safePrefix = sanitize(prefix);
      String safeKey = sanitize(key);
      String id = safePrefix + "_" + safeKey;
      return id.length() <= 32 ? id : id.substring(0, 32);
   }

   public static String keyFromSeedPath(String path) {
      if (path != null && !path.isBlank()) {
         String key = path.toLowerCase(Locale.ROOT);
         if (key.endsWith("_seeds")) {
            key = key.substring(0, key.length() - "_seeds".length());
         } else if (key.endsWith("_seed")) {
            key = key.substring(0, key.length() - "_seed".length());
         }

         return sanitize(key);
      } else {
         return "crop";
      }
   }

   public static ResourceLocation parse(String value, String fallbackNamespace) {
      if (value != null && !value.isBlank()) {
         ResourceLocation parsed = ResourceLocation.tryParse(value);
         return parsed != null ? parsed : ResourceLocation.tryBuild(fallbackNamespace, value);
      } else {
         return null;
      }
   }

   private static String sanitize(String value) {
      if (value != null && !value.isBlank()) {
         StringBuilder builder = new StringBuilder(value.length());

         for (char character : value.toLowerCase(Locale.ROOT).toCharArray()) {
            if (character >= 'a' && character <= 'z' || character >= '0' && character <= '9') {
               builder.append(character);
            } else {
               builder.append('_');
            }
         }

         String sanitized = builder.toString().replaceAll("_+", "_");
         if (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
         }

         if (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
         }

         return sanitized.isEmpty() ? "x" : sanitized;
      } else {
         return "x";
      }
   }
}
