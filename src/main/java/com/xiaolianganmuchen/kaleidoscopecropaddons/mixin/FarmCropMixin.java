package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropContext;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {FarmCrop.class},
   remap = false
)
public abstract class FarmCropMixin {
   @WrapMethod(
      method = {"seed"}
   )
   private Item kaleidoscope$seed(Operation<Item> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().seed() : (Item)original.call(new Object[0]);
   }

   @WrapMethod(
      method = {"plantState"}
   )
   private BlockState kaleidoscope$plantState(Operation<BlockState> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().plantState() : (BlockState)original.call(new Object[0]);
   }

   @WrapMethod(
      method = {"plantBlock"}
   )
   private Block kaleidoscope$plantBlock(Operation<Block> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().plantBlock() : (Block)original.call(new Object[0]);
   }

   @WrapMethod(
      method = {"produceBlock"}
   )
   private Block kaleidoscope$produceBlock(Operation<Block> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().produceBlock() : (Block)original.call(new Object[0]);
   }

   @WrapMethod(
      method = {"isStem"}
   )
   private boolean kaleidoscope$isStem(Operation<Boolean> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().isStem() : (Boolean)original.call(new Object[0]);
   }

   @WrapMethod(
      method = {"shouldPlantAt"}
   )
   private boolean kaleidoscope$shouldPlantAt(int x, int z, Operation<Boolean> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().shouldPlantAt(x, z) : (Boolean)original.call(new Object[]{x, z});
   }

   @WrapMethod(
      method = {"isOwnPlant"}
   )
   private boolean kaleidoscope$isOwnPlant(BlockState state, Operation<Boolean> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().isOwnPlant(state) : (Boolean)original.call(new Object[]{state});
   }

   @WrapMethod(
      method = {"isMatureFull"}
   )
   private boolean kaleidoscope$isMatureFull(BlockState state, Operation<Boolean> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().isMatureFull(state) : (Boolean)original.call(new Object[]{state});
   }

   @WrapMethod(
      method = {"isProduce"}
   )
   private boolean kaleidoscope$isProduce(BlockState state, Operation<Boolean> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().isProduce(state) : (Boolean)original.call(new Object[]{state});
   }

   @WrapMethod(
      method = {"translationKey"}
   )
   private String kaleidoscope$translationKey(Operation<String> original) {
      Optional<ResolvedCrop> crop = CropContext.current();
      return crop.isPresent() ? crop.get().translationKey() : (String)original.call(new Object[0]);
   }
}
