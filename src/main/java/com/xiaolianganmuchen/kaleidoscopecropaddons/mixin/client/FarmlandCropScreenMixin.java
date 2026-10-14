package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin.client;

import client.cn.kafei.simukraft.client.farmland.FarmlandCropScreen;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.client.DelightCropSelectScreen;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {FarmlandCropScreen.class},
   remap = false
)
public abstract class FarmlandCropScreenMixin {
   @WrapMethod(
      method = {"open"}
   )
   private static void kaleidoscope$openGroupedCropScreen(FarmlandBoxOpenResponsePacket packet, Operation<Void> original) {
      DelightCropSelectScreen.open(packet);
   }
}
