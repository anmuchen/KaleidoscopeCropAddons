package com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk;

import java.util.function.Predicate;

public final class AddonCropPersistence {
   public static final String ADDON_CROP_TAG = "AddonCrop";

   private AddonCropPersistence() {
   }

   public static String restoreId(String crop, String addonCrop, Predicate<String> vanillaCrop) {
      String id = firstNonBlank(addonCrop, crop);
      return id == null || id.isBlank() || vanillaCrop != null && vanillaCrop.test(id) ? "" : id;
   }

   public static String firstNonBlank(String primary, String fallback) {
      if (primary != null && !primary.isBlank()) {
         return primary;
      } else {
         return fallback != null && !fallback.isBlank() ? fallback : "";
      }
   }
}
