package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

public final class CropRules {
   private CropRules() {
   }

   public static boolean isAgePropertyName(String name) {
      return name != null && ("age".equals(name) || name.endsWith("_age"));
   }

   public static boolean columnIsComplete(int requiredHeight, int ownPlantCount, boolean allSegmentsMature) {
      return requiredHeight > 1 ? ownPlantCount >= requiredHeight && allSegmentsMature : ownPlantCount >= 1 && allSegmentsMature;
   }

   public static boolean isHarvestableStage(
      int harvestHeight,
      boolean hasExtraHarvestBlocks,
      boolean isExtraHarvestBlock,
      boolean isUpperSegment,
      boolean hasAge,
      boolean maxAge,
      boolean harvestMaxAgePlant
   ) {
      if (harvestHeight > 1) {
         return hasAge ? maxAge : isUpperSegment || isExtraHarvestBlock;
      } else if (isExtraHarvestBlock) {
         return !hasAge || maxAge;
      } else {
         return hasExtraHarvestBlocks && !harvestMaxAgePlant ? false : hasAge && maxAge;
      }
   }

   public static CropRules.BoxCropAction decideBoxCrop(boolean hasSelection, boolean sawForeignCrop) {
      return hasSelection && sawForeignCrop ? CropRules.BoxCropAction.CLEAR : CropRules.BoxCropAction.KEEP;
   }

   public static enum BoxCropAction {
      KEEP,
      CLEAR;
   }
}
