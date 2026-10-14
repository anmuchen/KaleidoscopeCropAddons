package com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk;

import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;

public final class FarmlandBoxDataAccess {
   private FarmlandBoxDataAccess() {
   }

   public static String addonCropId(FarmlandBoxData data) {
      if (data == null) {
         return "";
      } else {
         String id = ext(data).kaleidoscope$getAddonCropId();
         return id == null ? "" : id;
      }
   }

   public static void setAddonCropId(FarmlandBoxData data, String id) {
      if (data != null) {
         ext(data).kaleidoscope$setAddonCropId(id == null ? "" : id);
      }
   }

   private static FarmlandBoxDataExt ext(FarmlandBoxData data) {
      // FarmlandBoxData 是 final 类，接口由 FarmlandBoxDataMixin 在运行时混入，
      // 因此必须经由 Object 做一次转换，javac 才允许这个接口转换。
      return (FarmlandBoxDataExt)(Object)data;
   }

   public static boolean hasAddonCrop(FarmlandBoxData data) {
      return !addonCropId(data).isBlank();
   }

   public static String selectedCropId(FarmlandBoxData data) {
      if (data == null) {
         return "";
      } else {
         String addonId = addonCropId(data);
         if (!addonId.isBlank()) {
            return addonId;
         } else {
            return data.crop() != null ? data.crop().id() : "";
         }
      }
   }

   public static void clearSelection(FarmlandBoxData data) {
      if (data != null) {
         data.setCrop(null);
         setAddonCropId(data, "");
         data.setRunning(false);
      }
   }

   public static void applySelection(FarmlandBoxData data, String cropId) {
      if (data != null) {
         data.setRunning(false);
         if (cropId != null && !cropId.isBlank()) {
            FarmCrop vanilla = FarmCrop.fromId(cropId);
            if (vanilla != null) {
               data.setCrop(vanilla);
               setAddonCropId(data, "");
            } else {
               data.setCrop(FarmCrop.WHEAT);
               setAddonCropId(data, cropId);
            }
         } else {
            clearSelection(data);
         }
      }
   }
}
