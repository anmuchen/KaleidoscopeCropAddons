package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin.client;

import client.cn.kafei.simukraft.client.farmland.FarmlandBoxScreenOpener;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropDisplayNames;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {FarmlandBoxScreenOpener.class},
   remap = false
)
public abstract class FarmlandBoxScreenOpenerMixin {
   @WrapMethod(
      method = {"cropLine"}
   )
   private static Component kaleidoscope$cropLine(FarmlandBoxOpenResponsePacket packet, Operation<Component> original) {
      Component resolved = CropDisplayNames.of(packet.cropId());
      return Component.translatable("gui.simukraft.farmland_box.crop_line", new Object[]{resolved});
   }
}
