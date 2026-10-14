package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.DelightAddonBridge;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.AddonCropPersistence;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxDataExt;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
   value = {FarmlandBoxData.class},
   remap = false
)
public abstract class FarmlandBoxDataMixin implements FarmlandBoxDataExt {
   private static final String KALEIDOSCOPE_ADDON_TAG = "KaleidoscopeAddonCrop";
   @Unique
   private String kaleidoscope$addonCropId = "";

   @Override
   public String kaleidoscope$getAddonCropId() {
      return this.kaleidoscope$addonCropId == null ? "" : this.kaleidoscope$addonCropId;
   }

   @Override
   public void kaleidoscope$setAddonCropId(String id) {
      this.kaleidoscope$addonCropId = id == null ? "" : id;
   }

   @WrapMethod(
      method = {"toTag"}
   )
   private CompoundTag kaleidoscope$writeAddonCrop(Operation<CompoundTag> original) {
      CompoundTag tag = (CompoundTag)original.call(new Object[0]);
      String addonId = this.kaleidoscope$getAddonCropId();
      if (!addonId.isBlank() && tag != null) {
         tag.putString("KaleidoscopeAddonCrop", addonId);
         tag.putString("Crop", addonId);
      }

      return tag;
   }

   @WrapMethod(
      method = {"fromTag"}
   )
   private static FarmlandBoxData kaleidoscope$readAddonCrop(CompoundTag tag, Operation<FarmlandBoxData> original) {
      FarmlandBoxData data = (FarmlandBoxData)original.call(new Object[]{tag});
      if (data != null && tag != null) {
         String mine = tag.contains("KaleidoscopeAddonCrop") ? tag.getString("KaleidoscopeAddonCrop") : "";
         String legacy = tag.contains("AddonCrop") ? tag.getString("AddonCrop") : "";
         String crop = tag.contains("Crop") ? tag.getString("Crop") : "";
         String cropId = AddonCropPersistence.restoreId(crop, mine, id -> FarmCrop.fromId(id) != null);
         if (cropId.isBlank()) {
            cropId = AddonCropPersistence.restoreId(crop, legacy, id -> FarmCrop.fromId(id) != null);
         }

         if (!cropId.isBlank() && CropCatalog.isSelectable(cropId) && !DelightAddonBridge.isBridgedCrop(cropId)) {
            data.setCrop(FarmCrop.WHEAT);
            ((FarmlandBoxDataExt)(Object)data).kaleidoscope$setAddonCropId(cropId);
         }

         return data;
      } else {
         return data;
      }
   }
}
