package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.RightClickHarvestCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {BlockBehaviour.class},
   remap = true
)
public abstract class CropRightClickHarvestMixin {
   @WrapMethod(
      method = {"useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"},
      remap = true
   )
   private InteractionResult kaleidoscope$harvestAddonCrop(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, Operation<InteractionResult> original
   ) {
      InteractionResult handled = RightClickHarvestCompat.tryHarvest(level, pos, state, player);
      return handled != null ? handled : (InteractionResult)original.call(new Object[]{state, level, pos, player, hitResult});
   }
}
