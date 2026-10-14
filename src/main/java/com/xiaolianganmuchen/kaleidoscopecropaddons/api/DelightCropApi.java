package com.xiaolianganmuchen.kaleidoscopecropaddons.api;

import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropSpec;

public final class DelightCropApi {
   private DelightCropApi() {
   }

   public static boolean register(CropSpec spec) {
      return CropCatalog.register(spec);
   }

   public static void registerSourceLabel(String modId, String translationKey) {
      CropCatalog.registerSourceLabel(modId, translationKey);
   }
}
