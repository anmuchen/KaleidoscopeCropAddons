package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropContext;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropHarvest;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.SoilCropWork;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.TrellisCropWork;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import common.cn.kafei.simukraft.material.WorkContainerService;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandFarmingService.class},
   remap = false
)
public abstract class FarmlandHarvestServiceMixin {
   @WrapMethod(
      method = {"needsWaterWork"}
   )
   private static boolean kaleidoscope$needsWaterWork(
      ServerLevel level, FarmlandBoxData data, List<BlockPos> chestPositions, BlockPos cropPos, Operation<Boolean> original
   ) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      return crop != null && crop.usesCustomSoil() ? false : (Boolean)original.call(new Object[]{level, data, chestPositions, cropPos});
   }

   @WrapMethod(
      method = {"needsTillWork"}
   )
   private static boolean kaleidoscope$needsTillWork(ServerLevel level, FarmlandBoxData data, BlockPos cropPos, Operation<Boolean> original) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop == null) {
         return (Boolean)original.call(new Object[]{level, data, cropPos});
      } else if (crop.isTrellis()) {
         return TrellisCropWork.needsTill(level, crop, cropPos);
      } else if (crop.hasCustomSoil()) {
         return SoilCropWork.needsTill(level, crop, cropPos);
      } else {
         return (Boolean)original.call(new Object[]{level, data, cropPos});
      }
   }

   @Inject(
      method = {"applyTillWork"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void kaleidoscope$applyTillWork(ServerLevel level, FarmlandBoxData data, BlockPos cropPos, CallbackInfoReturnable<Object> cir) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.usesCustomSoil()) {
         boolean trellis = crop.isTrellis();
         boolean needed = trellis ? TrellisCropWork.needsTill(level, crop, cropPos) : SoilCropWork.needsTill(level, crop, cropPos);
         if (!needed) {
            finish(cir, "SKIPPED");
         } else {
            List<BlockPos> chests = WorkContainerService.adjacentContainers(level, data.boxPos());
            boolean acted = trellis ? TrellisCropWork.applyTill(level, crop, chests, cropPos) : SoilCropWork.applyTill(level, crop, chests, cropPos);
            finish(cir, acted ? "PROCESSED" : "WAITING_SEED");
         }
      }
   }

   @WrapMethod(
      method = {"needsPlantWork"}
   )
   private static boolean kaleidoscope$needsPlantWork(ServerLevel level, FarmlandBoxData data, BlockPos cropPos, Operation<Boolean> original) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop == null) {
         return (Boolean)original.call(new Object[]{level, data, cropPos});
      } else if (crop.isTrellis()) {
         return TrellisCropWork.needsPlant(level, crop, cropPos);
      } else if (crop.hasCustomSoil()) {
         return SoilCropWork.needsPlant(level, crop, cropPos);
      } else {
         return (Boolean)original.call(new Object[]{level, data, cropPos});
      }
   }

   @Inject(
      method = {"applyPlantWork"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void kaleidoscope$applyPlantWork(
      ServerLevel level, FarmlandBoxData data, List<BlockPos> chestPositions, BlockPos cropPos, CallbackInfoReturnable<Object> cir
   ) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.usesCustomSoil()) {
         boolean trellis = crop.isTrellis();
         boolean needed = trellis ? TrellisCropWork.needsPlant(level, crop, cropPos) : SoilCropWork.needsPlant(level, crop, cropPos);
         if (!needed) {
            finish(cir, "SKIPPED");
         } else {
            // 只看这一次到底有没有做成事：藤架是「先放横梁、再插葡萄藤」两步，
            // 放下横梁后“下一步还需要插藤”属于正常进度，不能报缺料，
            // 否则农民头上会一直在“铺设中：藤架”和“等待中：葡萄藤”之间跳。
            boolean acted = trellis
               ? TrellisCropWork.applyPlant(level, crop, chestPositions, cropPos)
               : SoilCropWork.applyPlant(level, crop, chestPositions, cropPos);
            finish(cir, acted ? "PROCESSED" : "WAITING_SEED");
         }
      }
   }

   @WrapMethod(
      method = {"needsHarvestWork"}
   )
   private static boolean kaleidoscope$needsHarvestWork(ServerLevel level, FarmlandBoxData data, BlockPos cropPos, Operation<Boolean> original) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.isTrellis()) {
         return TrellisCropWork.isReadyToHarvest(level, crop, cropPos);
      } else {
         return crop != null && !crop.isStem() ? CropHarvest.isReady(level, crop, cropPos) : (Boolean)original.call(new Object[]{level, data, cropPos});
      }
   }

   @WrapMethod(
      method = {"harvestBlock"}
   )
   private static void kaleidoscope$harvestBlock(ServerLevel level, List<BlockPos> chestPositions, BlockPos pos, BlockState state, Operation<Void> original) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.isTrellis()) {
         TrellisCropWork.harvest(level, crop, chestPositions, pos);
      } else if (crop != null && !crop.isStem() && crop.isOwnPlant(state)) {
         CropHarvest.harvest(level, crop, chestPositions, pos);
      } else {
         original.call(new Object[]{level, chestPositions, pos, state});
      }
   }

   @WrapMethod(
      method = {"needsBonemealWork"}
   )
   private static boolean kaleidoscope$needsBonemealWork(
      ServerLevel level, FarmlandBoxData data, List<BlockPos> chestPositions, BlockPos cropPos, Operation<Boolean> original
   ) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.isTrellis()) {
         return TrellisCropWork.needsBonemeal(level, crop, cropPos) && hasBoneMeal(level, chestPositions);
      } else {
         return crop != null && !crop.isStem() && CropHarvest.isReady(level, crop, cropPos)
            ? false
            : (Boolean)original.call(new Object[]{level, data, chestPositions, cropPos});
      }
   }

   @Inject(
      method = {"applyBonemealWork"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void kaleidoscope$applyBonemealWork(
      ServerLevel level, FarmlandBoxData data, List<BlockPos> chestPositions, BlockPos cropPos, CallbackInfoReturnable<Object> cir
   ) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.isTrellis()) {
         if (TrellisCropWork.needsBonemeal(level, crop, cropPos) && consumeBoneMeal(level, chestPositions)) {
            TrellisCropWork.applyBonemeal(level, crop, cropPos);
            finish(cir, "PROCESSED");
         } else {
            finish(cir, "SKIPPED");
         }
      }
   }

   /**
    * 宿主在「箱子里没有种子」时会整段跳过 PLANT 阶段，连藤架的立柱与横梁也一起被跳过 ——
    * 表现为农民一根藤架都铺不动、头顶一直卡在「等待藤架」。
    *
    * <p>藤架本身不需要种子，所以对藤架作物让这一步始终有效：先把整片藤架铺满，
    * 之后再因缺葡萄藤而报 {@code WAITING_SEED}，头顶就会显示「等待种子：葡萄藤」。</p>
    */
   @WrapMethod(
      method = {"hasSeed"}
   )
   private static boolean kaleidoscope$hasSeed(
      ServerLevel level, List<BlockPos> chestPositions, FarmCrop crop, Operation<Boolean> original
   ) {
      ResolvedCrop resolved = CropContext.current().orElse(null);
      return resolved != null && resolved.isTrellis() ? true : (Boolean)original.call(new Object[]{level, chestPositions, crop});
   }

   private static void finish(CallbackInfoReturnable<Object> cir, String resultName) {
      cir.setReturnValue(workResult(resultName));
      cir.cancel();
   }

   private static Object workResult(String name) {
      try {
         Class<?> type = Class.forName("common.cn.kafei.simukraft.farmland.FarmlandWorkResult");
         Field field = type.getDeclaredField(name);
         field.setAccessible(true);
         return field.get(null);
      } catch (ReflectiveOperationException var3) {
         throw new IllegalStateException("NSUK farmland work result " + name, var3);
      }
   }

   private static boolean hasBoneMeal(ServerLevel level, List<BlockPos> chestPositions) {
      return WorkContainerService.hasItem(level, chestPositions, Items.BONE_MEAL);
   }

   private static boolean consumeBoneMeal(ServerLevel level, List<BlockPos> chestPositions) {
      return WorkContainerService.consumeItem(level, chestPositions, Items.BONE_MEAL);
   }

   @WrapMethod(
      method = {"farmerStatusLabel"}
   )
   private static String kaleidoscope$farmerStatusLabel(ServerLevel level, String translationKey, FarmCrop crop, Operation<String> original) {
      ResolvedCrop resolved = CropContext.current().orElse(null);
      if (resolved == null) {
         return (String)original.call(new Object[]{level, translationKey, crop});
      } else if (resolved.usesCustomSoil() && isSupplyStatus(translationKey)) {
         // 文字必须和正在做的事一致：材料按「当前阶段」取（翻地→土壤；建造/播种→藤架或种子），
         // 前缀也按阶段选（等待中：土壤 / 等待藤架 / 等待种子：xxx / 铺设中 / 搭建中 / 播种中）。
         Item item = TrellisCropWork.statusItemForPhase(level, resolved, CropContext.currentBox().orElse(null), translationKey);
         Component itemName = item != null && item != Items.AIR ? item.getName(new ItemStack(item)) : statusName(resolved);
         boolean missingSeed = "gui.simukraft.farmland.status.missing_seed".equals(translationKey);
         boolean till = "gui.simukraft.farmland.status.till".equals(translationKey);
         boolean soilItem = item != null && item != Items.AIR && resolved.soilItems().contains(item);
         boolean seedItem = item != null && item != Items.AIR && item == resolved.seed();
         String key;
         if (missingSeed && soilItem) {
            key = "gui.kaleidoscopecropaddons.waiting_soil";
         } else if (missingSeed && seedItem) {
            key = translationKey;
         } else if (missingSeed) {
            key = "gui.kaleidoscopecropaddons.trellis.waiting_item";
         } else if (till) {
            key = "gui.kaleidoscopecropaddons.trellis.placing_soil";
         } else if (resolved.isTrellis() && !seedItem) {
            key = "gui.kaleidoscopecropaddons.trellis.placing_item";
         } else {
            key = translationKey;
         }

         return Serializer.toJson(Component.translatable(key, new Object[]{itemName}), level.registryAccess());
      } else {
         return Serializer.toJson(Component.translatable(translationKey, new Object[]{statusName(resolved)}), level.registryAccess());
      }
   }

   private static boolean isSupplyStatus(String translationKey) {
      return "gui.simukraft.farmland.status.missing_seed".equals(translationKey)
         || "gui.simukraft.farmland.status.till".equals(translationKey)
         || "gui.simukraft.farmland.status.plant".equals(translationKey);
   }

   private static Component statusName(ResolvedCrop crop) {
      return Component.translatableWithFallback(crop.statusTranslationKey(), Component.translatable(crop.seedTranslationKey()).getString());
   }
}
