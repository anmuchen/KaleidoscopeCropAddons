package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record CropSpec(
   String id,
   String sourceModId,
   String sourcePrefix,
   ResourceLocation seedId,
   ResourceLocation plantBlockId,
   List<ResourceLocation> extraHarvestBlockIds,
   List<ResourceLocation> growthBlockIds,
   CropLayout layout,
   ResourceLocation produceBlockId,
   boolean waterlogOnPlant,
   boolean allowNonCropBlock,
   int harvestHeight,
   boolean harvestMaxAge,
   CropOrigin origin,
   ResourceLocation fruitBlockId,
   ResourceLocation supportBlockId,
   ResourceLocation soilTagId,
   ResourceLocation soilItemId,
   ResourceLocation fruitItemId,
   List<ResourceLocation> soilExcludeIds,
   List<ResourceLocation> soilBlockIds,
   boolean plantMature
) {
   public static final int MAX_ID_LENGTH = 32;
   public static final int MAX_HARVEST_HEIGHT = 8;

   public CropSpec(
      String id,
      String sourceModId,
      String sourcePrefix,
      ResourceLocation seedId,
      ResourceLocation plantBlockId,
      List<ResourceLocation> extraHarvestBlockIds,
      List<ResourceLocation> growthBlockIds,
      CropLayout layout,
      ResourceLocation produceBlockId,
      boolean waterlogOnPlant,
      boolean allowNonCropBlock,
      int harvestHeight,
      boolean harvestMaxAge,
      CropOrigin origin,
      ResourceLocation fruitBlockId,
      ResourceLocation supportBlockId,
      ResourceLocation soilTagId,
      ResourceLocation soilItemId,
      ResourceLocation fruitItemId,
      List<ResourceLocation> soilExcludeIds,
      List<ResourceLocation> soilBlockIds,
      boolean plantMature
   ) {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(sourceModId, "sourceModId");
      Objects.requireNonNull(sourcePrefix, "sourcePrefix");
      Objects.requireNonNull(seedId, "seedId");
      extraHarvestBlockIds = extraHarvestBlockIds == null ? List.of() : List.copyOf(extraHarvestBlockIds);
      growthBlockIds = growthBlockIds == null ? List.of() : List.copyOf(growthBlockIds);
      soilExcludeIds = soilExcludeIds == null ? List.of() : List.copyOf(soilExcludeIds);
      soilBlockIds = soilBlockIds == null ? List.of() : List.copyOf(soilBlockIds);
      layout = layout == null ? CropLayout.FULL : layout;
      origin = origin == null ? CropOrigin.API : origin;
      harvestHeight = Math.max(0, Math.min(8, harvestHeight));
      if (id.length() > 32) {
         throw new IllegalArgumentException("Crop id '" + id + "' exceeds NSUK farmland packet limit of 32");
      } else {
         this.id = id;
         this.sourceModId = sourceModId;
         this.sourcePrefix = sourcePrefix;
         this.seedId = seedId;
         this.plantBlockId = plantBlockId;
         this.extraHarvestBlockIds = extraHarvestBlockIds;
         this.growthBlockIds = growthBlockIds;
         this.layout = layout;
         this.produceBlockId = produceBlockId;
         this.waterlogOnPlant = waterlogOnPlant;
         this.allowNonCropBlock = allowNonCropBlock;
         this.harvestHeight = harvestHeight;
         this.harvestMaxAge = harvestMaxAge;
         this.origin = origin;
         this.fruitBlockId = fruitBlockId;
         this.supportBlockId = supportBlockId;
         this.soilTagId = soilTagId;
         this.soilItemId = soilItemId;
         this.fruitItemId = fruitItemId;
         this.soilExcludeIds = soilExcludeIds;
         this.soilBlockIds = soilBlockIds;
         this.plantMature = plantMature;
      }
   }

   public static CropSpec.Builder builder(String id) {
      return new CropSpec.Builder(id);
   }

   public static final class Builder {
      private final String id;
      private String sourceModId = "";
      private String sourcePrefix = "";
      private ResourceLocation seedId;
      private ResourceLocation plantBlockId;
      private List<ResourceLocation> extraHarvestBlockIds = List.of();
      private List<ResourceLocation> growthBlockIds = List.of();
      private CropLayout layout = CropLayout.FULL;
      private ResourceLocation produceBlockId;
      private boolean waterlogOnPlant;
      private boolean allowNonCropBlock;
      private int harvestHeight;
      private boolean harvestMaxAge = true;
      private CropOrigin origin = CropOrigin.API;
      private ResourceLocation fruitBlockId;
      private ResourceLocation supportBlockId;
      private ResourceLocation soilTagId;
      private ResourceLocation soilItemId;
      private ResourceLocation fruitItemId;
      private List<ResourceLocation> soilExcludeIds = List.of();
      private List<ResourceLocation> soilBlockIds = List.of();
      private boolean plantMature;

      private Builder(String id) {
         this.id = id;
      }

      public CropSpec.Builder sourceModId(String sourceModId) {
         this.sourceModId = sourceModId;
         return this;
      }

      public CropSpec.Builder sourcePrefix(String sourcePrefix) {
         this.sourcePrefix = sourcePrefix;
         return this;
      }

      public CropSpec.Builder seedId(ResourceLocation seedId) {
         this.seedId = seedId;
         return this;
      }

      public CropSpec.Builder plantBlockId(ResourceLocation plantBlockId) {
         this.plantBlockId = plantBlockId;
         return this;
      }

      public CropSpec.Builder extraHarvestBlockIds(List<ResourceLocation> extraHarvestBlockIds) {
         this.extraHarvestBlockIds = extraHarvestBlockIds;
         return this;
      }

      public CropSpec.Builder growthBlockIds(List<ResourceLocation> growthBlockIds) {
         this.growthBlockIds = growthBlockIds;
         return this;
      }

      public CropSpec.Builder layout(CropLayout layout) {
         this.layout = layout;
         return this;
      }

      public CropSpec.Builder produceBlockId(ResourceLocation produceBlockId) {
         this.produceBlockId = produceBlockId;
         return this;
      }

      public CropSpec.Builder waterlogOnPlant(boolean waterlogOnPlant) {
         this.waterlogOnPlant = waterlogOnPlant;
         return this;
      }

      public CropSpec.Builder allowNonCropBlock(boolean allowNonCropBlock) {
         this.allowNonCropBlock = allowNonCropBlock;
         return this;
      }

      public CropSpec.Builder harvestHeight(int harvestHeight) {
         this.harvestHeight = harvestHeight;
         return this;
      }

      public CropSpec.Builder harvestMaxAge(boolean harvestMaxAge) {
         this.harvestMaxAge = harvestMaxAge;
         return this;
      }

      public CropSpec.Builder origin(CropOrigin origin) {
         this.origin = origin;
         return this;
      }

      public CropSpec.Builder fruitBlockId(ResourceLocation fruitBlockId) {
         this.fruitBlockId = fruitBlockId;
         return this;
      }

      public CropSpec.Builder supportBlockId(ResourceLocation supportBlockId) {
         this.supportBlockId = supportBlockId;
         return this;
      }

      public CropSpec.Builder soilTagId(ResourceLocation soilTagId) {
         this.soilTagId = soilTagId;
         return this;
      }

      public CropSpec.Builder soilItemId(ResourceLocation soilItemId) {
         this.soilItemId = soilItemId;
         return this;
      }

      public CropSpec.Builder fruitItemId(ResourceLocation fruitItemId) {
         this.fruitItemId = fruitItemId;
         return this;
      }

      /** 土壤标签里虽然写了、但不允许农民使用（也不认作有效土壤）的方块。 */
      public CropSpec.Builder soilExcludeIds(List<ResourceLocation> soilExcludeIds) {
         this.soilExcludeIds = soilExcludeIds;
         return this;
      }

      /**
       * 宿主模组没给土壤标签时（例如回响作物的幽匿催发体是硬编码判断的），
       * 直接在这里列出可用的土壤方块。与 {@code soil_tag} 同时给出时取并集。
       */
      public CropSpec.Builder soilBlockIds(List<ResourceLocation> soilBlockIds) {
         this.soilBlockIds = soilBlockIds;
         return this;
      }

      /**
       * 直接把作物种成「成熟」状态。个别模组的葡萄藤（次元酒窖的绯红/诡异葡萄）随机生长要求
       * 正下方是菌岩，而农民布局里正下方要留给果实，所以只能种下去就满熟。
       */
      public CropSpec.Builder plantMature(boolean plantMature) {
         this.plantMature = plantMature;
         return this;
      }

      public CropSpec build() {
         return new CropSpec(
            this.id,
            this.sourceModId,
            this.sourcePrefix,
            this.seedId,
            this.plantBlockId,
            this.extraHarvestBlockIds,
            this.growthBlockIds,
            this.layout,
            this.produceBlockId,
            this.waterlogOnPlant,
            this.allowNonCropBlock,
            this.harvestHeight,
            this.harvestMaxAge,
            this.origin,
            this.fruitBlockId,
            this.supportBlockId,
            this.soilTagId,
            this.soilItemId,
            this.fruitItemId,
            this.soilExcludeIds,
            this.soilBlockIds,
            this.plantMature
         );
      }
   }
}
