package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import java.util.Locale;

public enum CropLayout {
   FULL,
   STEM,
   TRELLIS;

   public static CropLayout fromName(String name) {
      if (name != null && !name.isBlank()) {
         try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return FULL;
         }
      } else {
         return FULL;
      }
   }
}
