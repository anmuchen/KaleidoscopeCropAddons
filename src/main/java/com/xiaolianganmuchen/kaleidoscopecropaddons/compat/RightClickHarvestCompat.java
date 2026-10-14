package com.xiaolianganmuchen.kaleidoscopecropaddons.compat;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropAge;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class RightClickHarvestCompat {
   private RightClickHarvestCompat() {
   }

   public static InteractionResult tryHarvest(Level level, BlockPos pos, BlockState state, Player player) {
      if (level != null && pos != null && state != null && !level.isClientSide) {
         BlockState live = liveState(level, pos, state);
         Optional<ResolvedCrop> resolved = cropOf(live);
         if (resolved.isEmpty()) {
            if (looksLikeAddonCrop(live)) {
               KaleidoscopeCropAddons.LOGGER
                  .debug("[rclick] unmapped block {} at {} age={} (not in catalog / disabled)", new Object[]{live.getBlock(), pos, ageValue(live)});
            }

            return null;
         } else {
            return !isHarvestReady(resolved.get(), live, pos) ? null : doHarvest(level, pos, live, player, resolved.get());
         }
      } else {
         return null;
      }
   }

   public static boolean isAddonCropAt(Player player, BlockPos pos) {
      if (pos != null && player != null && player.level() != null) {
         try {
            Level level = player.level();
            if (level.isClientSide) {
               return false;
            } else {
               BlockState state = level.getBlockState(pos);
               return cropOf(state).isPresent();
            }
         } catch (RuntimeException var4) {
            KaleidoscopeCropAddons.LOGGER.debug("addon-crop probe failed at {}", pos, var4);
            return false;
         }
      } else {
         return false;
      }
   }

   private static BlockState liveState(Level level, BlockPos pos, BlockState fallback) {
      try {
         BlockState live = level.getBlockState(pos);
         return live != null && !live.isAir() ? live : fallback;
      } catch (RuntimeException var4) {
         return fallback;
      }
   }

   public static ItemInteractionResult tryHarvestViaItemUse(Level level, BlockPos pos, BlockState state, Player player) {
      if (level != null && pos != null && state != null && !level.isClientSide) {
         BlockState live = liveState(level, pos, state);
         Optional<ResolvedCrop> resolved = cropOf(live);
         if (resolved.isEmpty()) {
            return null;
         } else if (!isHarvestReady(resolved.get(), live, pos)) {
            return null;
         } else {
            InteractionResult result = doHarvest(level, pos, live, player, resolved.get());
            return result == InteractionResult.SUCCESS ? ItemInteractionResult.SUCCESS : null;
         }
      } else {
         return null;
      }
   }

   private static boolean isHarvestReady(ResolvedCrop crop, BlockState state, BlockPos pos) {
      boolean ready = !crop.isStem() && crop.isSegmentMature(state);
      if (!ready) {
         KaleidoscopeCropAddons.LOGGER
            .debug(
               "[rclick] skip {} at {} age={} ready=false (stem={} mature={} maxage={})",
               new Object[]{crop.id(), pos, ageValue(state), crop.isStem(), crop.isSegmentMature(state), isMaxAge(state)}
            );
      }

      return ready;
   }

   private static InteractionResult doHarvest(Level level, BlockPos pos, BlockState state, Player player, ResolvedCrop crop) {
      List<ItemStack> drops = collectDrops(level, pos, state, player);
      int popped = 0;
      if (level instanceof ServerLevel serverLevel) {
         for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
               Block.popResource(serverLevel, pos, drop);
               popped++;
            }
         }
      }

      resetToSeedAge(level, pos, state);
      KaleidoscopeCropAddons.LOGGER
         .info("[rclick] harvest {} at {} age={} -> drops={} popped={}", new Object[]{crop.id(), pos, ageValue(state), describeDrops(drops), popped});
      return InteractionResult.SUCCESS;
   }

   private static int ageValue(BlockState state) {
      return findAgeProperty(state) instanceof IntegerProperty integerProperty ? (Integer)state.getValue(integerProperty) : -1;
   }

   private static boolean looksLikeAddonCrop(BlockState state) {
      if (state != null && !state.isAir() && findAgeProperty(state) != null) {
         ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
         return key != null && !"minecraft".equals(key.getNamespace());
      } else {
         return false;
      }
   }

   private static Property<?> findAgeProperty(BlockState state) {
      if (state == null) {
         return null;
      } else {
         for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty && property.getName().equals("age")) {
               return property;
            }
         }

         return null;
      }
   }

   private static String describeDrops(List<ItemStack> drops) {
      if (drops != null && !drops.isEmpty()) {
         StringBuilder builder = new StringBuilder("[");

         for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            if (i > 0) {
               builder.append(", ");
            }

            builder.append(stack.isEmpty() ? "EMPTY" : stack.getItem() + "x" + stack.getCount());
         }

         return builder.append(']').toString();
      } else {
         return "[]";
      }
   }

   private static List<ItemStack> collectDrops(Level level, BlockPos pos, BlockState state, Player player) {
      if (level instanceof ServerLevel serverLevel) {
         try {
            List<ItemStack> loot = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), player, ItemStack.EMPTY);
            if (loot != null && !loot.isEmpty()) {
               return loot;
            }
         } catch (RuntimeException var7) {
            KaleidoscopeCropAddons.LOGGER.debug("Loot table lookup failed for {}", state.getBlock(), var7);
         }
      }

      Optional<ResolvedCrop> crop = cropOf(state);
      if (crop.isEmpty()) {
         return List.of();
      } else {
         Item seed = crop.get().seed();
         if (seed != null && seed != Items.AIR) {
            int count = 1 + level.random.nextInt(3);
            return List.of(new ItemStack(seed, count));
         } else {
            return List.of();
         }
      }
   }

   private static void resetToSeedAge(Level level, BlockPos pos, BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty integerProperty && property.getName().equals("age")) {
            BlockState reset = (BlockState)state.setValue(integerProperty, 0);
            level.setBlock(pos, reset, 3);
            return;
         }
      }

      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
   }

   private static Optional<ResolvedCrop> cropOf(BlockState state) {
      if (state != null && !state.isAir()) {
         try {
            return CropCatalog.resolveByBlock(state);
         } catch (RuntimeException var2) {
            KaleidoscopeCropAddons.LOGGER.debug("Right-click harvest compatibility probe failed", var2);
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static boolean isMaxAge(BlockState state) {
      try {
         return CropAge.isMaxAge(state);
      } catch (RuntimeException var2) {
         return false;
      }
   }
}
