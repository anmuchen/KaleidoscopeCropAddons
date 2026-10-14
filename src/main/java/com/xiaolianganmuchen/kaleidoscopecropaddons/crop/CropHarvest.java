package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import com.mojang.authlib.GameProfile;
import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import common.cn.kafei.simukraft.material.WorkContainerService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class CropHarvest {
   private static final int MAX_SCAN_HEIGHT = 3;
   private static final Random RANDOM = new Random();
   private static final GameProfile FARMER_PROFILE = new GameProfile(
      UUID.nameUUIDFromBytes("kaleidoscopecropaddons:farmer".getBytes(StandardCharsets.UTF_8)), "NSUKDelightFarmer"
   );

   private CropHarvest() {
   }

   public static boolean isReady(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && cropPos != null && !crop.isStem()) {
         if (crop.harvestHeight() <= 1) {
            return crop.isSegmentMature(level.getBlockState(cropPos));
         } else {
            List<BlockPos> column = column(level, crop, cropPos);
            boolean allMature = true;

            for (BlockPos pos : column) {
               if (!crop.isSegmentMature(level.getBlockState(pos))) {
                  allMature = false;
                  break;
               }
            }

            return CropRules.columnIsComplete(crop.harvestHeight(), column.size(), allMature);
         }
      } else {
         return false;
      }
   }

   public static void harvest(ServerLevel level, ResolvedCrop crop, List<BlockPos> chestPositions, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && cropPos != null) {
         List<BlockPos> column = column(level, crop, cropPos);
         if (!column.isEmpty()) {
            AABB area = harvestArea(column);
            Set<UUID> before = itemIds(level, area);
            List<ItemStack> drops = new ArrayList<>();

            for (int index = column.size() - 1; index >= 0; index--) {
               harvestSegment(level, crop, column.get(index), drops);
            }

            WorkContainerService.depositDropsOrDrop(level, chestPositions, drops, cropPos);
            collectNewItems(level, chestPositions, area, before);
         }
      }
   }

   public static void replantIfNeeded(ServerLevel level, ResolvedCrop crop, List<BlockPos> chestPositions, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && !crop.isStem() && cropPos != null && crop.shouldPlantAt(cropPos.getX(), cropPos.getZ())) {
         BlockState cropState = level.getBlockState(cropPos);
         BlockState soilState = level.getBlockState(cropPos.below());
         if ((cropState.isAir() || cropState.canBeReplaced())
            && soilState.is(Blocks.FARMLAND)
            && WorkContainerService.consumeItem(level, chestPositions, crop.seed())) {
            level.setBlock(cropPos, crop.plantState(), 3);
         }
      }
   }

   static List<BlockPos> column(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      List<BlockPos> positions = new ArrayList<>();
      int limit = crop.harvestHeight() > 1 ? Math.max(crop.harvestHeight(), 3) : (crop.hasExtraHarvestBlocks() ? 3 : 1);

      for (int dy = 0; dy < limit; dy++) {
         BlockPos pos = cropPos.above(dy);
         if (!level.isLoaded(pos)) {
            break;
         }

         BlockState state = level.getBlockState(pos);
         if (!crop.isOwnPlant(state)) {
            break;
         }

         positions.add(pos.immutable());
      }

      return positions;
   }

   private static void harvestSegment(ServerLevel level, ResolvedCrop crop, BlockPos pos, List<ItemStack> drops) {
      BlockState state = level.getBlockState(pos);
      if (crop.isOwnPlant(state) && crop.isSegmentMature(state)) {
         if (tryRightClickHarvest(level, pos, state)) {
            BlockState after = level.getBlockState(pos);
            if (!crop.isOwnPlant(after) || !crop.isSegmentMature(after)) {
               return;
            }
         }

         BlockState current = level.getBlockState(pos);
         if (crop.isOwnPlant(current)) {
            FakePlayer farmer = fakeFarmer(level);
            ItemStack tool = new ItemStack(Items.IRON_HOE);
            List<ItemStack> harvested = Block.getDrops(current, level, pos, level.getBlockEntity(pos), farmer, tool);
            // 多格作物的「附带格」（如回响作物的顶部）本来就没有掉落物，
            // 不要给它套用种子兜底，否则收割一次会凭空多出种子；只清掉方块即可。
            if (isEffectivelyEmpty(harvested) && !crop.isExtraHarvestBlock(current) && !crop.isUpperSegment(current)) {
               List<ItemStack> fallback = fallbackDrops(crop);
               if (!fallback.isEmpty()) {
                  KaleidoscopeCropAddons.LOGGER.debug("Crop {} produced no loot (missing loot table?); using fallback drops {}", crop.id(), fallback);
                  drops.addAll(fallback);
                  level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                  return;
               }
            }

            drops.addAll(harvested);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
         }
      }
   }

   private static boolean isEffectivelyEmpty(List<ItemStack> drops) {
      if (drops != null && !drops.isEmpty()) {
         for (ItemStack stack : drops) {
            if (stack != null && !stack.isEmpty() && stack.getItem() != Items.AIR) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private static List<ItemStack> fallbackDrops(ResolvedCrop crop) {
      if (crop != null && !crop.isMissing()) {
         Item seed = crop.seed();
         if (seed != null && seed != Items.AIR) {
            int count = 1 + RANDOM.nextInt(3);
            return List.of(new ItemStack(seed, count));
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static boolean tryRightClickHarvest(ServerLevel level, BlockPos pos, BlockState state) {
      try {
         FakePlayer farmer = fakeFarmer(level);
         farmer.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
         BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
         InteractionResult result = state.useWithoutItem(level, farmer, hit);
         return result.consumesAction();
      } catch (RuntimeException var61) {
         return false;
      }
   }

   /**
    * 用指定工具模拟玩家右键收割某一格，并把因此弹出的掉落物收进箱子。
    *
    * <p>酒馆的葡萄串只有“手持剪刀右键”才会产出掉落物（{@code GrapeCropBlock.useItemOn}
    * 要求 {@code SHEARS_HARVEST}），空手右键不会有任何反应，所以这里必须带上工具，
    * 空手退化为 {@code useWithoutItem} 只是为了兼容其它自带右键收割的作物。</p>
    *
    * @return 是否发生了有效交互（消费了动作）
    */
   public static boolean rightClickHarvest(ServerLevel level, List<BlockPos> chestPositions, BlockPos pos, ItemStack tool) {
      if (level != null && pos != null && level.isLoaded(pos)) {
         try {
            BlockState state = level.getBlockState(pos);
            FakePlayer farmer = fakeFarmer(level);
            farmer.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            AABB area = new AABB(pos).inflate(1.0);
            Set<UUID> before = itemIds(level, area);
            boolean consumed = false;
            if (tool != null && !tool.isEmpty()) {
               consumed = state.useItemOn(tool, level, farmer, InteractionHand.MAIN_HAND, hit).consumesAction();
            }

            if (!consumed) {
               consumed = state.useWithoutItem(level, farmer, hit).consumesAction();
            }

            collectNewItems(level, chestPositions, area, before);
            return consumed;
         } catch (RuntimeException var91) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static FakePlayer fakeFarmer(ServerLevel level) {
      return FakePlayerFactory.get(level, FARMER_PROFILE);
   }

   private static AABB harvestArea(List<BlockPos> column) {
      BlockPos first = column.getFirst();
      BlockPos last = column.getLast();
      return new AABB(first).minmax(new AABB(last)).inflate(1.0);
   }

   private static Set<UUID> itemIds(ServerLevel level, AABB area) {
      Set<UUID> ids = new HashSet<>();

      for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
         ids.add(entity.getUUID());
      }

      return ids;
   }

   private static void collectNewItems(ServerLevel level, List<BlockPos> chestPositions, AABB area, Set<UUID> before) {
      for (ItemEntity entity : List.copyOf(level.getEntitiesOfClass(ItemEntity.class, area))) {
         if (entity.isAlive() && !before.contains(entity.getUUID())) {
            ItemStack leftover = WorkContainerService.insertIntoAny(level, chestPositions, entity.getItem());
            if (leftover.isEmpty()) {
               entity.discard();
            } else {
               entity.setItem(leftover);
            }
         }
      }
   }
}
