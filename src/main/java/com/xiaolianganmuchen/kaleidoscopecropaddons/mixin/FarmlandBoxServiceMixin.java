package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.DelightAddonBridge;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxCleanup;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxDataAccess;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxReconcile;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandBoxService;
import common.cn.kafei.simukraft.farmland.FarmlandBoxView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {FarmlandBoxService.class},
   remap = false
)
public abstract class FarmlandBoxServiceMixin {
   @WrapMethod(
      method = {"onRemoved"}
   )
   private static void kaleidoscope$purgeOnRemoved(ServerLevel level, BlockPos boxPos, Operation<Void> original) {
      FarmlandBoxCleanup.purge(level, boxPos);
      original.call(new Object[]{level, boxPos});
   }

   @WrapMethod(
      method = {"setCrop"}
   )
   private static boolean kaleidoscope$setAddonCrop(ServerLevel level, BlockPos boxPos, String cropId, Operation<Boolean> original) {
      if (cropId != null
         && !cropId.isBlank()
         && FarmCrop.fromId(cropId) == null
         && CropCatalog.isSelectable(cropId)
         && !DelightAddonBridge.isBridgedCrop(cropId)) {
         FarmlandBoxManager manager = FarmlandBoxManager.get(level);
         FarmlandBoxData data = manager.getOrCreate(boxPos);
         if (data.running()) {
            return false;
         } else {
            data.setCrop(FarmCrop.WHEAT);
            FarmlandBoxDataAccess.setAddonCropId(data, cropId);
            manager.persist(data);
            FarmlandBoxReconcile.markSettled(level, boxPos);
            return true;
         }
      } else {
         boolean result = (Boolean)original.call(new Object[]{level, boxPos, cropId});
         if (result && FarmCrop.fromId(cropId) != null) {
            FarmlandBoxData data = FarmlandBoxManager.get(level).get(boxPos);
            if (data != null && FarmlandBoxDataAccess.hasAddonCrop(data)) {
               FarmlandBoxDataAccess.setAddonCropId(data, "");
               FarmlandBoxManager.get(level).persist(data);
               FarmlandBoxReconcile.markSettled(level, boxPos);
            }
         }

         return result;
      }
   }

   @WrapMethod(
      method = {"buildView"}
   )
   private static FarmlandBoxView kaleidoscope$buildView(ServerLevel level, BlockPos boxPos, Operation<FarmlandBoxView> original) {
      FarmlandBoxManager manager = FarmlandBoxManager.get(level);
      FarmlandBoxData data = manager.get(boxPos);
      if (data != null) {
         FarmlandBoxReconcile.reconcileIfNeeded(level, manager, data);
      }

      FarmlandBoxView view = (FarmlandBoxView)original.call(new Object[]{level, boxPos});
      if (view != null && data != null && FarmlandBoxDataAccess.hasAddonCrop(data)) {
         String addonId = FarmlandBoxDataAccess.addonCropId(data);
         return addonId.equals(view.cropId())
            ? view
            : new FarmlandBoxView(
               view.boxPos(),
               view.hasCity(),
               addonId,
               view.hasPlot(),
               view.plotMin(),
               view.plotMax(),
               view.hasChest(),
               view.chestPos(),
               view.running(),
               view.hasFarmer(),
               view.farmerName()
            );
      } else {
         return view;
      }
   }
}
