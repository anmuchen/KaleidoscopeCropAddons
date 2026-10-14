package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class CropAge {
   private CropAge() {
   }

   public static boolean isMaxAge(BlockState state) {
      if (state == null) {
         return false;
      } else {
         IntegerProperty declared = findAgeProperty(state);
         if (declared != null) {
            int highest = max(declared);
            int current = (Integer)state.getValue(declared);
            if (current >= highest) {
               return true;
            }
         }

         if (state.getBlock() instanceof CropBlock cropBlock) {
            try {
               return cropBlock.isMaxAge(state);
            } catch (RuntimeException var4) {
            }
         }

         if (state.hasProperty(BlockStateProperties.AGE_7)) {
            return (Integer)state.getValue(BlockStateProperties.AGE_7) >= 7;
         } else if (state.hasProperty(BlockStateProperties.AGE_5)) {
            return (Integer)state.getValue(BlockStateProperties.AGE_5) >= 5;
         } else if (state.hasProperty(BlockStateProperties.AGE_4)) {
            return (Integer)state.getValue(BlockStateProperties.AGE_4) >= 4;
         } else if (state.hasProperty(BlockStateProperties.AGE_3)) {
            return (Integer)state.getValue(BlockStateProperties.AGE_3) >= 3;
         } else if (state.hasProperty(BlockStateProperties.AGE_2)) {
            return (Integer)state.getValue(BlockStateProperties.AGE_2) >= 2;
         } else {
            return state.hasProperty(BlockStateProperties.AGE_1) ? (Integer)state.getValue(BlockStateProperties.AGE_1) >= 1 : false;
         }
      }
   }

   /**
    * 把方块状态里的生长年龄复位到最小值，也就是玩家在界面上看到的 0%。
    * 找不到年龄属性时原样返回（调用方需自行判断结果是否仍是目标方块）。
    */
   public static BlockState resetToMinAge(BlockState state) {
      if (state == null) {
         return null;
      } else {
         IntegerProperty age = findAgeProperty(state);
         if (age == null) {
            return state;
         } else {
            int lowest = Integer.MAX_VALUE;
            for (int value : age.getPossibleValues()) {
               if (value < lowest) {
                  lowest = value;
               }
            }
            return lowest == Integer.MAX_VALUE ? state : state.setValue(age, lowest);
         }
      }
   }

   /** 把方块状态里的生长年龄拉到最大值（个别作物需要直接种成成熟，见 CropSpec#plantMature）。 */
   public static BlockState resetToMaxAge(BlockState state) {
      if (state == null) {
         return null;
      } else {
         IntegerProperty age = findAgeProperty(state);
         if (age == null) {
            return state;
         } else {
            int highest = 0;
            for (int value : age.getPossibleValues()) {
               if (value > highest) {
                  highest = value;
               }
            }

            return state.setValue(age, highest);
         }
      }
   }

   public static int ageGapToMature(BlockState state) {
      if (state == null) {
         return -1;
      } else {
         IntegerProperty age = findAgeProperty(state);
         if (age == null) {
            return -1;
         } else {
            int highest = max(age);
            int current = (Integer)state.getValue(age);
            return Math.max(0, highest - current);
         }
      }
   }

   public static boolean hasAge(BlockState state) {
      return state == null
         ? false
         : state.getBlock() instanceof CropBlock
            || state.hasProperty(BlockStateProperties.AGE_7)
            || state.hasProperty(BlockStateProperties.AGE_5)
            || state.hasProperty(BlockStateProperties.AGE_4)
            || state.hasProperty(BlockStateProperties.AGE_3)
            || state.hasProperty(BlockStateProperties.AGE_2)
            || state.hasProperty(BlockStateProperties.AGE_1)
            || findAgeProperty(state) != null;
   }

   private static IntegerProperty findAgeProperty(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty integerProperty && CropRules.isAgePropertyName(property.getName())) {
            return integerProperty;
         }
      }

      return null;
   }

   private static int max(IntegerProperty property) {
      int highest = 0;

      for (int value : property.getPossibleValues()) {
         if (value > highest) {
            highest = value;
         }
      }

      return highest;
   }
}
