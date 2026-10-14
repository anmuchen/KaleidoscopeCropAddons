package com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropRules;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropSpec;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.TrellisCropWork;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandPlot;
import common.cn.kafei.simukraft.util.SaveScopedCacheKey;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmlandBoxReconcile {
   private static final ResourceLocation BOX_ID = ResourceLocation.parse("simukraft:nsuk_farmland_box");
   private static final Set<String> SETTLED = ConcurrentHashMap.newKeySet();

   private FarmlandBoxReconcile() {
   }

   public static void clear() {
      SETTLED.clear();
   }

   public static void markSettled(ServerLevel level, BlockPos boxPos) {
      if (level != null && boxPos != null) {
         SETTLED.add(key(level, boxPos));
      }
   }

   public static void forget(ServerLevel level, BlockPos boxPos) {
      if (level != null && boxPos != null) {
         SETTLED.remove(key(level, boxPos));
      }
   }

   public static void reconcileAll(ServerLevel level) {
      if (level != null && !level.isClientSide()) {
         FarmlandBoxManager manager = FarmlandBoxManager.get(level);

         for (FarmlandBoxData data : manager.all()) {
            reconcileIfNeeded(level, manager, data);
         }
      }
   }

   public static void reconcileIfNeeded(ServerLevel level, FarmlandBoxManager manager, FarmlandBoxData data) {
      if (level != null && manager != null && data != null) {
         String key = key(level, data.boxPos());
         if (!SETTLED.contains(key) && level.isLoaded(data.boxPos())) {
            Block boxBlock = (Block)BuiltInRegistries.BLOCK.get(BOX_ID);
            if (boxBlock != Blocks.AIR && !level.getBlockState(data.boxPos()).is(boxBlock)) {
               FarmlandBoxCleanup.purge(level, data.boxPos());
            } else if (CropCatalog.isPopulated()) {
               String selectedId = FarmlandBoxDataAccess.selectedCropId(data);
               if (!FarmlandBoxDataAccess.hasAddonCrop(data)) {
                  SETTLED.add(key);
               } else if (selectedId.isBlank() || FarmCrop.fromId(selectedId) != null || catalogReadyFor(selectedId)) {
                  FarmlandPlot plot = data.plot();
                  if (plot == null) {
                     SETTLED.add(key);
                  } else {
                     List<ResolvedCrop> knownCrops = CropCatalog.selectable();
                     FarmlandBoxReconcile.Scan scan = scanPlot(level, data, plot, selectedId, knownCrops);
                     if (scan.ready) {
                        CropRules.BoxCropAction action = CropRules.decideBoxCrop(!selectedId.isBlank(), !scan.fieldCropIds.isEmpty());
                        SETTLED.add(key);
                        if (action == CropRules.BoxCropAction.CLEAR) {
                           FarmlandBoxDataAccess.clearSelection(data);
                           FarmlandBoxCleanup.clearFarmerFarmStatus(level, data.boxPos());
                           manager.persist(data);
                        } else if (!selectedId.isBlank()) {
                           manager.persist(data);
                        }

                        settleTrellisFrame(level, selectedId, plot);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean catalogReadyFor(String cropId) {
      Optional<CropSpec> spec = CropCatalog.spec(cropId);
      return spec.isEmpty() ? true : ResolvedCrop.resolve(spec.get()).isPresent();
   }

   /**
    * 藤架作物专用：把整座藤架里已建成方块的 shape 按当前邻居补正一次。
    *
    * <p>藤架的 {@code type} 由邻居推导，而方块只会在“邻居变动”时重算自己；老存档里
    * 最后建成的那批角落立柱因此可能一直停在初始的 {@code east_west}（看起来就是
    * “角落没有立柱、横梁朝外支棱”）。这里趁着每轮会话首次认领箱子的时机统一修一次，
    * 玩家不必拆掉重搭。新放置的方块由 {@link TrellisCropWork#refreshShape} 当场收敛。</p>
    */
   private static void settleTrellisFrame(ServerLevel level, String selectedId, FarmlandPlot plot) {
      if (selectedId != null && !selectedId.isBlank()) {
         CropCatalog.resolve(selectedId).filter(ResolvedCrop::isTrellis).ifPresent(crop -> {
            int settled = TrellisCropWork.settleFrame(level, crop, plot);
            if (settled > 0) {
               KaleidoscopeCropAddons.LOGGER.debug("Settled {} trellis shape(s) for crop {}", settled, selectedId);
            }
         });
      }
   }

   private static FarmlandBoxReconcile.Scan scanPlot(
      ServerLevel level, FarmlandBoxData data, FarmlandPlot plot, String selectedId, List<ResolvedCrop> knownCrops
   ) {
      int unloaded = 0;
      Set<String> otherIds = new LinkedHashSet<>();
      int cells = Math.max(1, plot.cellCount());
      BlockPos boxPos = data.boxPos();

      for (int index = 0; index < cells; index++) {
         BlockPos cropPos = plot.cellAt(index);
         if (cropPos.getX() != boxPos.getX() || cropPos.getZ() != boxPos.getZ()) {
            if (!level.isLoaded(cropPos)) {
               unloaded++;
            } else {
               BlockState state = level.getBlockState(cropPos);
               if (!state.isAir() && !state.canBeReplaced() && !state.is(Blocks.WATER) && !state.is(Blocks.FARMLAND)) {
                  String foundId = identifyCropId(state, knownCrops);
                  if (!foundId.isBlank() && !foundId.equals(selectedId)) {
                     otherIds.add(foundId);
                  }
               }
            }
         }
      }

      return new FarmlandBoxReconcile.Scan(unloaded == 0 || !otherIds.isEmpty(), otherIds);
   }

   private static String identifyCropId(BlockState state, List<ResolvedCrop> knownCrops) {
      for (ResolvedCrop crop : knownCrops) {
         if (crop.isOwnPlant(state)) {
            return crop.id();
         }
      }

      for (FarmCrop vanilla : FarmCrop.values()) {
         if (vanilla.isOwnPlant(state) || vanilla.isProduce(state)) {
            return vanilla.id();
         }
      }

      return "";
   }

   private static String key(ServerLevel level, BlockPos boxPos) {
      return SaveScopedCacheKey.levelKey(level).toLowerCase(Locale.ROOT) + "|" + boxPos.asLong();
   }

   private static record Scan(boolean ready, Set<String> fieldCropIds) {
   }
}
