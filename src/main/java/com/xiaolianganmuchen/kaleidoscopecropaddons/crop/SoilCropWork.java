package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import common.cn.kafei.simukraft.material.WorkContainerService;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「自定义土壤」的普通作物（非藤架）：末影薄荷长在末地石上、灵魂辣椒长在灵魂沙土上、
 * 回响作物长在幽匿催发体上 —— 都不是耕地，宿主 NSUK 的翻地 / 播种只认耕地，所以走这里。
 *
 * <p>与 {@link TrellisCropWork} 的分工：藤架要额外立柱子、铺横梁、插葡萄藤，这里只有
 * 「把下方换成本作物的土壤 → 把种子种下去」两步，收割仍交给模组通用的
 * {@link CropHarvest}（先模拟右键、再破坏，掉落走战利品表）。</p>
 */
public final class SoilCropWork {
   private SoilCropWork() {
   }

   /**
    * 需不需要把作物格下方换成本作物的土壤。
    *
    * <p>被显式排除的方块（例如黄金葡萄排除的岩浆块）也视同要换掉，免得农民站上去受伤。</p>
    */
   public static boolean needsTill(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      if (!ready(level, crop, cropPos)) {
         return false;
      } else {
         BlockState soil = level.getBlockState(cropPos.below());
         return crop.matchesSoil(soil)
            ? false
            : soil.isAir() || soil.canBeReplaced() || isLooseSoil(soil) || crop.isExcludedSoil(soil);
      }
   }

   public static boolean applyTill(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos cropPos) {
      if (!needsTill(level, crop, cropPos)) {
         return false;
      } else {
         Item soilItem = availableSoilItem(level, chests, crop);
         if (soilItem instanceof BlockItem blockItem && WorkContainerService.consumeItem(level, chests, soilItem)) {
            BlockPos soilPos = cropPos.below();
            BlockState previous = level.getBlockState(soilPos);
            if (!previous.isAir()) {
               List<ItemStack> drops = Block.getDrops(previous, level, soilPos, level.getBlockEntity(soilPos));
               level.setBlock(soilPos, Blocks.AIR.defaultBlockState(), 3);
               WorkContainerService.depositDropsOrDrop(level, chests, drops, soilPos);
            }

            level.setBlock(soilPos, blockItem.getBlock().defaultBlockState(), 3);
            return true;
         } else {
            return false;
         }
      }
   }

   /** 土壤已就位、作物格空着，就该播种。 */
   public static boolean needsPlant(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      if (!ready(level, crop, cropPos)) {
         return false;
      } else if (!crop.matchesSoil(level.getBlockState(cropPos.below()))) {
         return false;
      } else {
         BlockState cell = level.getBlockState(cropPos);
         return cell.isAir() || cell.canBeReplaced();
      }
   }

   public static boolean applyPlant(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos cropPos) {
      if (!needsPlant(level, crop, cropPos)) {
         return false;
      } else if (!WorkContainerService.consumeItem(level, chests, crop.seed())) {
         return false;
      } else {
         level.setBlock(cropPos, crop.plantState(), 3);
         return true;
      }
   }

   /** 箱子里现成可用的土壤材料（土壤标签 + soil_blocks 推导出来的那一串）。 */
   private static Item availableSoilItem(ServerLevel level, List<BlockPos> chests, ResolvedCrop crop) {
      if (level != null && chests != null && crop != null) {
         for (Item item : crop.soilItems()) {
            if (item != null && item != Items.AIR && WorkContainerService.hasItem(level, chests, item)) {
               return item;
            }
         }
      }

      return crop != null ? crop.soilItem() : Items.AIR;
   }

   private static boolean ready(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      return level != null
         && crop != null
         && crop.hasCustomSoil()
         && !crop.isMissing()
         && cropPos != null
         && level.isLoaded(cropPos);
   }

   private static boolean isLooseSoil(BlockState soil) {
      return soil.is(Blocks.GRASS_BLOCK)
         || soil.is(Blocks.DIRT)
         || soil.is(Blocks.COARSE_DIRT)
         || soil.is(Blocks.PODZOL)
         || soil.is(Blocks.ROOTED_DIRT)
         || soil.is(Blocks.FARMLAND)
         || soil.is(Blocks.DIRT_PATH);
   }
}
