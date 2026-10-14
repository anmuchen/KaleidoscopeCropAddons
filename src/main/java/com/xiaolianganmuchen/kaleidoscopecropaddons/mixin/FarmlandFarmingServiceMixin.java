package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropContext;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxCleanup;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxReconcile;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandFarmingService.class},
   remap = false
)
public abstract class FarmlandFarmingServiceMixin {
   @WrapMethod(
      method = {"tickBox"}
   )
   private static void kaleidoscope$withCrop(
      ServerLevel level, FarmlandBoxManager manager, FarmlandBoxData data, @Coerce Object boxRuntime, long gameTime, Operation<Void> original
   ) {
      if (level != null && data != null && level.isLoaded(data.boxPos())) {
         Block boxBlock = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.parse("simukraft:nsuk_farmland_box"));
         if (boxBlock != Blocks.AIR && !level.getBlockState(data.boxPos()).is(boxBlock)) {
            FarmlandBoxCleanup.purge(level, data.boxPos());
            return;
         }
      }

      CropContext.push(data);

      try {
         original.call(new Object[]{level, manager, data, boxRuntime, gameTime});
      } finally {
         CropContext.pop();
      }
   }

   @Inject(
      method = {"usesGroupedFarmScan"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void kaleidoscope$fullPlotScan(CallbackInfoReturnable<Boolean> cir) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.usesCustomSoil()) {
         cir.setReturnValue(false);
      }
   }

   @WrapMethod(
      method = {"tick"}
   )
   private static void kaleidoscope$clearContext(ServerLevel level, Operation<Void> original) {
      try {
         FarmlandBoxReconcile.reconcileAll(level);
         original.call(new Object[]{level});
      } finally {
         CropContext.clear();
      }
   }
}
