package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.RightClickHarvestCompat;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {CropLikeRegistry.class},
   remap = false
)
public abstract class CropLikeRegistryGuardMixin {
   @WrapMethod(
      method = {"checkForSingleCropHarvesting(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z"},
      remap = false
   )
   private static boolean kaleidoscope$vetoSingleCropHarvest(Player player, BlockPos pos, Operation<Boolean> original) {
      boolean addon = RightClickHarvestCompat.isAddonCropAt(player, pos);
      if (KaleidoscopeCropAddons.LOGGER.isDebugEnabled()) {
         KaleidoscopeCropAddons.LOGGER
            .debug("[ultimine] checkForSingleCropHarvesting pos={} addonCrop={} -> {}", new Object[]{pos, addon, addon ? "VETO(false)" : "delegate(original)"});
      }

      return addon ? false : (Boolean)original.call(new Object[]{player, pos});
   }
}
