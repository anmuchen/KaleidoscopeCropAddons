package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

public final class ResolvedCrop {
   private static final ResolvedCrop MISSING = new ResolvedCrop(
      null, Items.AIR, Blocks.AIR, List.of(), List.of(), null, Blocks.AIR, Blocks.AIR, null, Items.AIR, Items.AIR, List.of(), List.of()
   );
   private final CropSpec spec;
   private final Item seed;
   private final Block plantBlock;
   private final List<Block> extraHarvestBlocks;
   private final List<Block> growthBlocks;
   private final Block produceBlock;
   private final Block fruitBlock;
   private final Block supportBlock;
   private final TagKey<Block> soilTag;
   private final Item soilItem;
   private final Item fruitItem;
   private final List<Block> soilExcludes;
   private final List<Block> soilBlocks;
   private List<Item> soilItems;

   private ResolvedCrop(
      CropSpec spec,
      Item seed,
      Block plantBlock,
      List<Block> extraHarvestBlocks,
      List<Block> growthBlocks,
      Block produceBlock,
      Block fruitBlock,
      Block supportBlock,
      TagKey<Block> soilTag,
      Item soilItem,
      Item fruitItem,
      List<Block> soilExcludes,
      List<Block> soilBlocks
   ) {
      this.spec = spec;
      this.seed = seed;
      this.plantBlock = plantBlock;
      this.extraHarvestBlocks = extraHarvestBlocks;
      this.growthBlocks = growthBlocks;
      this.produceBlock = produceBlock;
      this.fruitBlock = fruitBlock;
      this.supportBlock = supportBlock;
      this.soilTag = soilTag;
      this.soilItem = soilItem;
      this.fruitItem = fruitItem;
      this.soilExcludes = soilExcludes == null ? List.of() : List.copyOf(soilExcludes);
      this.soilBlocks = soilBlocks == null ? List.of() : List.copyOf(soilBlocks);
   }

   public static ResolvedCrop missing() {
      return MISSING;
   }

   public boolean isMissing() {
      return this == MISSING || this.spec == null;
   }

   public static Optional<ResolvedCrop> resolve(CropSpec spec) {
      if (spec == null) {
         return Optional.empty();
      } else {
         Item seed = (Item)BuiltInRegistries.ITEM.get(spec.seedId());
         if (seed != null && seed != Items.AIR) {
            Block plantBlock = lookupBlock(spec.plantBlockId());
            if (plantBlock == Blocks.AIR && seed instanceof BlockItem blockItem) {
               plantBlock = blockItem.getBlock();
            }

            if (plantBlock == Blocks.AIR) {
               return Optional.empty();
            } else if (spec.layout() == CropLayout.TRELLIS) {
               Block fruitBlock = lookupBlock(spec.fruitBlockId());
               Block supportBlock = lookupBlock(spec.supportBlockId());
               // 土壤既可以用标签（soil_tag），也可以直接列方块（soil_blocks）——
               // 以前这里只认标签，导致「只写了 soil_blocks 的藤架作物」被判定为不可用而整个跳过。
               if (fruitBlock != Blocks.AIR
                  && supportBlock != Blocks.AIR
                  && (spec.soilTagId() != null || !spec.soilBlockIds().isEmpty())) {
                  TagKey<Block> soilTag = spec.soilTagId() == null
                     ? null
                     : TagKey.create(BuiltInRegistries.BLOCK.key(), spec.soilTagId());
                  Item soilItem = lookupItem(spec.soilItemId());
                  Item fruitItem = lookupItem(spec.fruitItemId());
                  List<Block> soilExcludes = lookupDistinctBlocks(spec.soilExcludeIds(), null);
                  List<Block> soilBlocks = lookupDistinctBlocks(spec.soilBlockIds(), null);
                  return Optional.of(
                     new ResolvedCrop(
                        spec, seed, plantBlock, List.of(), List.of(), null, fruitBlock, supportBlock, soilTag, soilItem, fruitItem, soilExcludes, soilBlocks
                     )
                  );
               } else {
                  return Optional.empty();
               }
            } else if (!(plantBlock instanceof CropBlock) && !spec.allowNonCropBlock()) {
               return Optional.empty();
            } else {
               List<Block> extraHarvestBlocks = lookupDistinctBlocks(spec.extraHarvestBlockIds(), plantBlock);
               List<Block> growthBlocks = lookupDistinctBlocks(spec.growthBlockIds(), plantBlock);
               growthBlocks.removeAll(extraHarvestBlocks);
               Block produceBlock = lookupBlock(spec.produceBlockId());
               if (produceBlock == Blocks.AIR) {
                  produceBlock = null;
               }

               boolean customSoil = spec.soilTagId() != null || !spec.soilBlockIds().isEmpty();
               return Optional.of(
                  new ResolvedCrop(
                     spec,
                     seed,
                     plantBlock,
                     List.copyOf(extraHarvestBlocks),
                     List.copyOf(growthBlocks),
                     produceBlock,
                     Blocks.AIR,
                     Blocks.AIR,
                     spec.soilTagId() == null ? null : TagKey.create(BuiltInRegistries.BLOCK.key(), spec.soilTagId()),
                     lookupItem(spec.soilItemId()),
                     Items.AIR,
                     lookupDistinctBlocks(spec.soilExcludeIds(), null),
                     lookupDistinctBlocks(spec.soilBlockIds(), null)
                  )
               );
            }
         } else {
            return Optional.empty();
         }
      }
   }

   public CropSpec spec() {
      return this.spec;
   }

   public String id() {
      return this.spec == null ? "" : this.spec.id();
   }

   public String sourceModId() {
      return this.spec == null ? "" : this.spec.sourceModId();
   }

   public Item seed() {
      return this.seed;
   }

   public Block plantBlock() {
      return this.plantBlock;
   }

   public Block produceBlock() {
      return this.produceBlock;
   }

   public boolean isTrellis() {
      return this.spec != null && this.spec.layout() == CropLayout.TRELLIS;
   }

   public Block fruitBlock() {
      return this.fruitBlock;
   }

   public Block supportBlock() {
      return this.supportBlock;
   }

   public TagKey<Block> soilTag() {
      return this.soilTag;
   }

   public Item soilItem() {
      return this.soilItem == null ? Items.AIR : this.soilItem;
   }

   /**
    * 这种藤架作物「可用的土壤方块」对应的全部物品。
    *
    * <p>作物定义里的 {@code soil_item} 只是首选材料（例如冰葡萄写的是 {@code minecraft:ice}），
    * 但宿主模组的土壤标签 {@code kaleidoscope_tavern:can_grow_ice_grape} 其实还包含
    * 浮冰 / 蓝冰 / 雪块（黄金葡萄一样有岩浆块）。只认 {@code soil_item} 会导致箱子里
    * 明明有这些方块、农民却站在那里等“冰块”，土壤一直填不上。</p>
    *
    * <p>所以这里直接从土壤标签推导可用物品：标签里每个方块连同 {@code soil_item} 一起返回，
    * 顺序上 {@code soil_item} 优先。标签是数据包内容，随宿主模组更新自动跟随。</p>
    */
   public List<Item> soilItems() {
      if (this.soilItems == null) {
         List<Item> items = new ArrayList<>();
         if (this.soilItem != null && this.soilItem != Items.AIR) {
            items.add(this.soilItem);
         }

         if (this.soilTag != null) {
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(this.soilTag)) {
               Block block = holder.value();
               if (block != Blocks.AIR && !this.soilExcludes.contains(block)) {
                  Item item = block.asItem();
                  if (item != null && item != Items.AIR && !items.contains(item)) {
                     items.add(item);
                  }
               }
            }
         }

         for (Block block : this.soilBlocks) {
            if (!this.soilExcludes.contains(block)) {
               Item item = block.asItem();
               if (item != null && item != Items.AIR && !items.contains(item)) {
                  items.add(item);
               }
            }
         }

         this.soilItems = List.copyOf(items);
      }

      return this.soilItems;
   }

   /**
    * 这个方块虽然写在土壤标签里，但被作物定义显式排除（例如黄金葡萄排除岩浆块：
    * 农民站上去会被烫死）。既不当作有效土壤，也不会被农民拿来铺。
    */
   /** 这种作物是否靠「自定义土壤」种植（藤架用藤架那套，普通作物则直接在土壤上种）。 */
   /** 是否要把作物直接种成成熟状态（见 {@link CropSpec.Builder#plantMature}）。 */
   public boolean plantMature() {
      return this.spec != null && this.spec.plantMature();
   }

   public boolean hasCustomSoil() {
      return this.soilTag != null || !this.soilBlocks.isEmpty();
   }

   /** 不走宿主「耕地」那一套的作物：藤架，或声明了自定义土壤的普通作物。 */
   public boolean usesCustomSoil() {
      return this.isTrellis() || this.hasCustomSoil();
   }

   /** 这个方块能不能当本作物的土壤：土壤标签命中，或直接写在 soil_blocks 里，且没被排除。 */
   public boolean matchesSoil(BlockState soil) {
      if (soil == null || this.isExcludedSoil(soil)) {
         return false;
      } else if (this.soilTag != null && soil.is(this.soilTag)) {
         return true;
      } else {
         for (Block block : this.soilBlocks) {
            if (soil.is(block)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isExcludedSoil(BlockState state) {
      if (state == null || this.soilExcludes.isEmpty()) {
         return false;
      } else {
         for (Block block : this.soilExcludes) {
            if (state.is(block)) {
               return true;
            }
         }

         return false;
      }
   }

   public Item fruitItem() {
      return this.fruitItem == null ? Items.AIR : this.fruitItem;
   }

   public boolean isFruit(BlockState state) {
      return state != null && this.fruitBlock != null && this.fruitBlock != Blocks.AIR && state.is(this.fruitBlock);
   }

   public boolean isVine(BlockState state) {
      return state != null && this.plantBlock != null && state.is(this.plantBlock);
   }

   public boolean isSupport(BlockState state) {
      return state != null && this.supportBlock != null && this.supportBlock != Blocks.AIR && state.is(this.supportBlock);
   }

   public BlockState plantState() {
      if (this.isMissing()) {
         return Blocks.AIR.defaultBlockState();
      } else {
         BlockState state = this.plantBlock.defaultBlockState();
         if (this.spec.waterlogOnPlant() && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, true);
         }

         return state;
      }
   }

   public boolean isStem() {
      return this.spec != null && this.spec.layout() == CropLayout.STEM;
   }

   public boolean shouldPlantAt(int x, int z) {
      return !this.isMissing() && this.spec != null ? this.spec.layout() == CropLayout.FULL || (Math.floorMod(x, 2) + Math.floorMod(z, 2)) % 2 == 0 : false;
   }

   public boolean isOwnPlant(BlockState state) {
      if (state == null) {
         return false;
      } else if (!this.isTrellis() || !this.isFruit(state) && !this.isVine(state)) {
         if (state.is(this.plantBlock)) {
            return true;
         } else {
            for (Block extra : this.extraHarvestBlocks) {
               if (state.is(extra)) {
                  return true;
               }
            }

            for (Block growth : this.growthBlocks) {
               if (state.is(growth)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public int harvestHeight() {
      if (this.spec != null && this.spec.harvestHeight() > 1) {
         return this.spec.harvestHeight();
      } else if (this.plantBlock != null && this.plantBlock != Blocks.AIR) {
         return looksTall(this.plantBlock.defaultBlockState()) ? 2 : 1;
      } else {
         return 1;
      }
   }

   public boolean isUpperSegment(BlockState state) {
      if (state == null) {
         return false;
      } else if (booleanProperty(state, "upper")) {
         return true;
      } else if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
         return true;
      } else {
         for (Block extra : this.extraHarvestBlocks) {
            if (state.is(extra)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean hasExtraHarvestBlocks() {
      return !this.extraHarvestBlocks.isEmpty();
   }

   public boolean isExtraHarvestBlock(BlockState state) {
      if (state == null) {
         return false;
      } else {
         for (Block extra : this.extraHarvestBlocks) {
            if (state.is(extra)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isMatureFull(BlockState state) {
      if (this.isTrellis()) {
         return this.isFruit(state) && CropAge.isMaxAge(state);
      } else if (state != null && this.isOwnPlant(state) && !this.isStem()) {
         return this.harvestHeight() > 1 && !this.isUpperSegment(state) ? false : this.isSegmentMature(state);
      } else {
         return false;
      }
   }

   public boolean isSegmentMature(BlockState state) {
      return state != null && this.isOwnPlant(state)
         ? CropRules.isHarvestableStage(
            this.harvestHeight(),
            !this.extraHarvestBlocks.isEmpty(),
            this.isExtraHarvestBlock(state),
            this.isUpperSegment(state),
            CropAge.hasAge(state),
            CropAge.isMaxAge(state),
            this.spec == null || this.spec.harvestMaxAge()
         )
         : false;
   }

   public String statusTranslationKey() {
      return "crop.kaleidoscopecropaddons." + this.id();
   }

   public String seedTranslationKey() {
      return this.seed != null && this.seed != Items.AIR ? this.seed.getDescriptionId() : this.statusTranslationKey();
   }

   public boolean isProduce(BlockState state) {
      return this.produceBlock != null && state != null && state.is(this.produceBlock);
   }

   public String translationKey() {
      if (this.isMissing()) {
         return "gui.simukraft.farmland_box.none";
      } else if (this.isTrellis()) {
         return this.statusTranslationKey();
      } else if (this.seed != null && this.seed != Items.AIR) {
         return this.seed.getDescriptionId();
      } else {
         return this.plantBlock != null && this.plantBlock != Blocks.AIR ? this.plantBlock.getDescriptionId() : "gui.simukraft.farmland_box.none";
      }
   }

   private static boolean looksTall(BlockState state) {
      if (state == null) {
         return false;
      } else {
         return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ? true : hasBooleanProperty(state, "upper");
      }
   }

   private static boolean booleanProperty(BlockState state, String name) {
      if (findProperty(state, name) instanceof BooleanProperty booleanProperty && state.hasProperty(booleanProperty)) {
         return (Boolean)state.getValue(booleanProperty);
      }

      return false;
   }

   private static boolean hasBooleanProperty(BlockState state, String name) {
      return findProperty(state, name) instanceof BooleanProperty;
   }

   private static Property<?> findProperty(BlockState state, String name) {
      if (state != null && name != null && !name.isBlank()) {
         for (Property<?> property : state.getProperties()) {
            if (name.equals(property.getName())) {
               return property;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<Block> lookupDistinctBlocks(List<ResourceLocation> ids, Block plantBlock) {
      List<Block> blocks = new ArrayList<>();
      if (ids == null) {
         return blocks;
      } else {
         for (ResourceLocation id : ids) {
            Block block = lookupBlock(id);
            if (block != Blocks.AIR && (plantBlock == null || block != plantBlock) && !blocks.contains(block)) {
               blocks.add(block);
            }
         }

         return blocks;
      }
   }

   private static Item lookupItem(ResourceLocation id) {
      if (id == null) {
         return Items.AIR;
      } else {
         Item item = (Item)BuiltInRegistries.ITEM.get(id);
         return item == null ? Items.AIR : item;
      }
   }

   private static Block lookupBlock(ResourceLocation id) {
      if (id == null) {
         return Blocks.AIR;
      } else {
         Block block = (Block)BuiltInRegistries.BLOCK.get(id);
         return block == null ? Blocks.AIR : block;
      }
   }
}
