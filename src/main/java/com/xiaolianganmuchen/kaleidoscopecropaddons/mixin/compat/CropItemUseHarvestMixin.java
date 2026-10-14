package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.RightClickHarvestCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {BlockBehaviour.class},
   remap = true
)
public abstract class CropItemUseHarvestMixin {
   @WrapMethod(
      method = {"useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;"},
      remap = true
   )
   private ItemInteractionResult kaleidoscope$harvestOnItemUse(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult,
      Operation<ItemInteractionResult> original
   ) {
      ItemInteractionResult fallback = (ItemInteractionResult)original.call(new Object[]{stack, state, level, pos, player, hand, hitResult});
      ItemInteractionResult harvested = RightClickHarvestCompat.tryHarvestViaItemUse(level, pos, state, player);
      if (harvested != null) {
         return harvested;
      } else {
         KaleidoscopeCropAddons.LOGGER.debug("[rclick] item-use passthrough at {} -> {} (stack={})", new Object[]{pos, fallback, stack.getItem()});
         return fallback;
      }
   }
}
