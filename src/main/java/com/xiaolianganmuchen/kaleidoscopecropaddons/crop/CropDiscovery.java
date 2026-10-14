package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.config.DelightCropConfig;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;

public final class CropDiscovery {
   private CropDiscovery() {
   }

   public static void discover(Set<String> namespaces) {
      if (DelightCropConfig.autoDiscovery() && namespaces != null && !namespaces.isEmpty()) {
         int added = 0;

         for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BlockItem) {
               BlockItem blockItem = (BlockItem)item;
               Block seedId = blockItem.getBlock();
               if (seedId instanceof CropBlock) {
                  CropBlock cropBlock = (CropBlock)seedId;
                  ResourceLocation seedIdx = BuiltInRegistries.ITEM.getKey(item);
                  if (seedIdx != null && namespaces.contains(seedIdx.getNamespace())) {
                     ResourceLocation plantId = BuiltInRegistries.BLOCK.getKey(cropBlock);
                     if (!shouldSkip(seedIdx, plantId, cropBlock) && !CropCatalog.isSeedRegistered(seedIdx)) {
                        String prefix = CropSourceIndex.prefixFor(seedIdx.getNamespace());
                        String key = CropIds.keyFromSeedPath(seedIdx.getPath());
                        String id = CropIds.compose(prefix, key);
                        if (!CropCatalog.contains(id)) {
                           CropSpec spec = CropSpec.builder(id)
                              .sourceModId(seedIdx.getNamespace())
                              .sourcePrefix(prefix)
                              .seedId(seedIdx)
                              .plantBlockId(plantId)
                              .origin(CropOrigin.DISCOVERY)
                              .build();
                           if (CropCatalog.register(spec)) {
                              added++;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (added > 0) {
            KaleidoscopeCropAddons.LOGGER.info("Auto-discovered {} farmland CropBlock(s) from {}", added, namespaces);
         }
      }
   }

   static boolean shouldSkip(ResourceLocation seedId, ResourceLocation plantId, Block plant) {
      if (!(plant instanceof StemBlock) && !(plant instanceof AttachedStemBlock) && !(plant instanceof LeavesBlock) && !(plant instanceof SaplingBlock)) {
         String typeName = plant.getClass().getName().toLowerCase(Locale.ROOT);
         return !typeName.contains("stemblock") && !typeName.contains("leafcrop") && !typeName.contains("sapling")
            ? isExcludedPath(seedId) || isExcludedPath(plantId) || isFarmerRice(seedId) || isFarmerRice(plantId)
            : true;
      } else {
         return true;
      }
   }

   private static boolean isFarmerRice(ResourceLocation id) {
      return id != null && "farmersdelight".equals(id.getNamespace()) && "rice".equals(id.getPath());
   }

   private static boolean isExcludedPath(ResourceLocation id) {
      if (id == null) {
         return false;
      } else {
         String path = id.getPath();
         return path.contains("saguaro")
            || path.contains("cactus")
            || path.contains("sapling")
            || path.contains("leaves")
            || path.contains("nether_wart")
            || path.contains("chorus")
            || path.contains("grape")
            || path.contains("trellis")
            || isNonFarmlandKaleidoscope(id)
            || isNonFarmlandYoukai(id)
            || isNonFarmlandImmortalers(id);
      }
   }

   private static boolean isNonFarmlandKaleidoscope(ResourceLocation id) {
      if (id != null && id.getNamespace().startsWith("kaleidoscope")) {
         String path = id.getPath();
         return path.contains("rice")
            || path.contains("mint")
            || path.contains("soul_pepper")
            || path.contains("poisonous_fruit")
            || path.contains("echo_crop")
            || path.contains("tea_tree");
      } else {
         return false;
      }
   }

   private static boolean isNonFarmlandYoukai(ResourceLocation id) {
      if (id != null && "youkaisfeasts".equals(id.getNamespace())) {
         String path = id.getPath();
         return "redbean".equals(path) || "tea".equals(path) || path.startsWith("tea_");
      } else {
         return false;
      }
   }

   private static boolean isNonFarmlandImmortalers(ResourceLocation id) {
      if (id != null && "immortalers_delight".equals(id.getNamespace())) {
         String path = id.getPath();
         return path.contains("gelpitaya")
            || path.contains("leisamboo")
            || path.contains("himekaido")
            || path.contains("pearlipearl")
            || path.contains("obsidian_walnut");
      } else {
         return false;
      }
   }
}
