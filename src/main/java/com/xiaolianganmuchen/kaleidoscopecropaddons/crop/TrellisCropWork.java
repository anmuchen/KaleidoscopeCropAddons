package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxService;
import common.cn.kafei.simukraft.farmland.FarmlandPlot;
import common.cn.kafei.simukraft.material.WorkContainerService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public final class TrellisCropWork {
   public static final int TRELLIS_HEIGHT = 3;
   private static final String TYPE = "type";
   private static final String SINGLE = "single";
   private static final String WAXED = "waxed";
   private static final ResourceLocation GREEN_GRAPE = ResourceLocation.parse("kaleidoscope_tavern:green_grape");

   private TrellisCropWork() {
   }

   public static Item statusItem(ServerLevel level, ResolvedCrop crop, FarmlandBoxData data) {
      if (crop == null) {
         return Items.AIR;
      } else if (level != null && data != null && data.plot() != null && data.boxPos() != null) {
         FarmlandPlot plot = data.plot();
         List<BlockPos> chests = FarmlandBoxService.resolveAdjacentChests(level, data.boxPos());
         BlockPos boxPos = data.boxPos();
         if (!crop.isTrellis()) {
            return soilCropStatusItem(level, crop, plot, boxPos, chests);
         }

         Item next = Items.AIR;
         int cells = Math.max(1, plot.cellCount());

         for (int index = 0; index < cells; index++) {
            BlockPos fruitPos = plot.cellAt(index);
            if ((fruitPos.getX() != boxPos.getX() || fruitPos.getZ() != boxPos.getZ()) && level.isLoaded(fruitPos)) {
               Item needed = nextItem(level, crop, chests, fruitPos);
               if (needed != Items.AIR) {
                  if (!WorkContainerService.hasItem(level, chests, needed)) {
                     return needed;
                  }

                  if (next == Items.AIR) {
                     next = needed;
                  }
               }
            }
         }

         return next != Items.AIR ? next : crop.fruitItem();
      } else {
         return crop.seed();
      }
   }

   /**
    * 按「当前正在执行的阶段」报出对应的材料，让农民头顶的文字和它手上的活一致。
    *
    * <p>宿主把阶段以状态键传进来：{@code .till} 只会要土壤；{@code .plant} 要么在立柱子 / 铺横梁，
    * 要么在插葡萄藤 —— 两者需要的东西不同；只有没有目标（{@code missing_seed} 等）时，
    * 才退回全局优先级扫描去猜「下一件缺的东西」。</p>
    */
   public static Item statusItemForPhase(ServerLevel level, ResolvedCrop crop, FarmlandBoxData data, String phaseKey) {
      if (crop == null) {
         return Items.AIR;
      } else if (level == null || data == null || data.plot() == null || data.boxPos() == null) {
         return crop.seed();
      } else {
         FarmlandPlot plot = data.plot();
         List<BlockPos> chests = FarmlandBoxService.resolveAdjacentChests(level, data.boxPos());
         boolean tillPhase = phaseKey != null && phaseKey.endsWith(".till");
         boolean plantPhase = phaseKey != null && phaseKey.endsWith(".plant");
         if (tillPhase) {
            Item soil = availableSoilItem(level, chests, crop);
            return soil != Items.AIR ? soil : crop.soilItem();
         } else if (plantPhase) {
            if (crop.isTrellis() && hasFrameWork(level, crop, plot, data.boxPos())) {
               return supportItem(crop);
            }

            return crop.seed() != Items.AIR ? crop.seed() : crop.soilItem();
         } else {
            return crop.isTrellis()
               ? statusItem(level, crop, data)
               : soilCropStatusItem(level, crop, plot, data.boxPos(), chests);
         }
      }
   }

   /** 这片田里还有没有任何一根立柱 / 一条横梁没铺好。 */
   private static boolean hasFrameWork(ServerLevel level, ResolvedCrop crop, FarmlandPlot plot, BlockPos boxPos) {
      int cells = Math.max(1, plot.cellCount());

      for (int index = 0; index < cells; index++) {
         BlockPos cell = plot.cellAt(index);
         if ((cell.getX() != boxPos.getX() || cell.getZ() != boxPos.getZ()) && level.isLoaded(cell)) {
            if (isCorner(cell)) {
               if (missingPost(level, crop, cell) != null) {
                  return true;
               }
            } else if (missingBeam(level, crop, cell) != null) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * 自定义土壤作物（末影薄荷 / 灵魂辣椒 / 迷毒果 / 回响作物）该报什么。
    *
    * <p>优先级固定：<b>先土壤、后种子</b>。箱子里一种可用的土壤都没有时先报土壤方块
    * （显示成「等待中：灵魂沙」），土壤齐了才轮到种子（「等待种子：红辣椒」）。
    * 这样农民头上的提示不会一会儿缺土、一会儿缺种子地来回跳。</p>
    */
   private static Item soilCropStatusItem(ServerLevel level, ResolvedCrop crop, FarmlandPlot plot, BlockPos boxPos, List<BlockPos> chests) {
      Item seed = crop.seed();
      boolean needsSoil = false;
      int cells = Math.max(1, plot.cellCount());

      for (int index = 0; index < cells; index++) {
         BlockPos cell = plot.cellAt(index);
         if ((cell.getX() != boxPos.getX() || cell.getZ() != boxPos.getZ()) && level.isLoaded(cell) && SoilCropWork.needsTill(level, crop, cell)) {
            needsSoil = true;
            break;
         }
      }

      if (needsSoil && !hasAnySoilItem(level, chests, crop)) {
         return crop.soilItem();
      } else if (seed != Items.AIR && !WorkContainerService.hasItem(level, chests, seed)) {
         return seed;
      } else {
         return seed != Items.AIR ? seed : crop.soilItem();
      }
   }

   /** 箱子里有没有任意一种本作物可用的土壤材料。 */
   private static boolean hasAnySoilItem(ServerLevel level, List<BlockPos> chests, ResolvedCrop crop) {
      for (Item item : crop.soilItems()) {
         if (item != null && item != Items.AIR && WorkContainerService.hasItem(level, chests, item)) {
            return true;
         }
      }

      return false;
   }

   private static Item nextItem(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos fruitPos) {
      if (isCorner(fruitPos)) {
         return missingPost(level, crop, fruitPos) != null ? supportItem(crop) : Items.AIR;
      } else if (needsTill(level, crop, fruitPos)) {
         return availableSoilItem(level, chests, crop);
      } else if (!matchesSoil(crop, level.getBlockState(fruitPos.below()))) {
         return Items.AIR;
      } else if (beamMissing(level, crop, fruitPos)) {
         return supportItem(crop);
      } else {
         return firstEmptyVine(level, crop, fruitPos) != null ? crop.seed() : Items.AIR;
      }
   }

   /**
    * 箱子里现成可用的土壤材料：按 {@link ResolvedCrop#soilItems()} 的顺序取第一个箱子里有的。
    * 一个都没有时退回作物定义里的首选材料（让状态栏能显示“缺什么”），而不是直接判为没活干。
    */
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

   /**
    * 需不需要铺土壤（把作物格下方换成本作物的土壤方块）。
    *
    * <p>四角例外：角落那一列只立蜡封立柱、不结葡萄藤，土壤铺了也永远没有东西长在上面，
    * 所以直接跳过，省掉玩家在角落里白填一块冰 / 下界岩。</p>
    */
   public static boolean needsTill(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      if (!ready(level, crop, fruitPos) || isCorner(fruitPos)) {
         return false;
      } else {
         BlockState soil = level.getBlockState(fruitPos.below());
         // 被排除的方块（例如岩浆块）视同需要换掉：农民会把它们替换成本作物真正可用的土壤，
         // 免得站在上面被烫死。
         return matchesSoil(crop, soil)
            ? false
            : soil.isAir() || soil.canBeReplaced() || isLooseSoil(soil) || crop.isExcludedSoil(soil);
      }
   }

   public static boolean applyTill(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos fruitPos) {
      if (!needsTill(level, crop, fruitPos)) {
         return false;
      } else {
         Item soilItem = availableSoilItem(level, chests, crop);
         if (soilItem instanceof BlockItem blockItem && WorkContainerService.consumeItem(level, chests, soilItem)) {
            BlockPos soilPos = fruitPos.below();
            BlockState previous = level.getBlockState(soilPos);
            if (!previous.isAir()) {
               List<ItemStack> drops = Block.getDrops(previous, level, soilPos, level.getBlockEntity(soilPos));
               level.setBlock(soilPos, Blocks.AIR.defaultBlockState(), 3);
               WorkContainerService.depositDropsOrDrop(level, chests, drops, soilPos);
            }

            level.setBlock(soilPos, blockItem.getBlock().defaultBlockState(), 3);
            return true;
         }

         return false;
      }
   }

   public static boolean needsPlant(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      if (!ready(level, crop, fruitPos)) {
         return false;
      } else if (isCorner(fruitPos)) {
         return missingPost(level, crop, fruitPos) != null;
      } else if (lowestClutter(level, crop, fruitPos) != null) {
         return true;
      } else if (!matchesSoil(crop, level.getBlockState(fruitPos.below()))) {
         return false;
      } else {
         BlockState fruit = level.getBlockState(fruitPos);
         return !fruit.isAir() && !fruit.canBeReplaced() ? false : beamMissing(level, crop, fruitPos) || firstEmptyVine(level, crop, fruitPos) != null;
      }
   }

   /**
    * 执行一次「建造」动作。
    *
    * @return 是否真的做了事（放置了立柱 / 横梁、清掉杂物、插下葡萄藤）。
    *         返回 false 只代表这一次没能推进——通常是箱子里的材料不够，
    *         调用方据此报「等材料」，不要再用「还缺不缺下一步」来判断，
    *         否则放下横梁后（下一步才插藤）会被误报成“缺葡萄藤”。
    */
   public static boolean applyPlant(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos fruitPos) {
      if (!needsPlant(level, crop, fruitPos)) {
         return false;
      } else if (isCorner(fruitPos)) {
         return placeSupport(level, crop, chests, fruitPos, missingPost(level, crop, fruitPos), true);
      } else {
         BlockPos clutter = lowestClutter(level, crop, fruitPos);
         if (clutter != null) {
            breakStructure(level, chests, clutter);
            return true;
         } else {
            BlockPos missingBeam = missingBeam(level, crop, fruitPos);
            if (missingBeam != null) {
               return placeSupport(level, crop, chests, fruitPos, missingBeam, false);
            } else {
               BlockPos bare = firstEmptyVine(level, crop, fruitPos);
               if (bare == null) {
                  return false;
               } else if (!WorkContainerService.consumeItem(level, chests, crop.seed())) {
                  return false;
               } else {
                  BlockState above = level.getBlockState(bare);
                  BlockState vine = crop.plantBlock().defaultBlockState();
                  if (above.hasProperty(BlockStateProperties.WATERLOGGED) && vine.hasProperty(BlockStateProperties.WATERLOGGED)) {
                     vine = (BlockState)vine.setValue(BlockStateProperties.WATERLOGGED, (Boolean)above.getValue(BlockStateProperties.WATERLOGGED));
                  }

                  if (crop.plantMature()) {
                     // 个别模组的葡萄藤必须直接种成成熟状态（次元酒窖的绯红/诡异葡萄：
                     // 它们的随机生长要求正下方是菌岩，而这里正下方要留给果实）。
                     vine = CropAge.resetToMaxAge(vine);
                  }

                  level.setBlock(bare, vine, 3);
                  refreshShape(level, bare);
                  return true;
               }
            }
         }
      }
   }

   public static boolean needsBonemeal(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      if (ready(level, crop, fruitPos) && !isCorner(fruitPos)) {
         BlockPos hang = hangPos(fruitPos);
         BlockState fruit = level.getBlockState(hang);
         if (crop.isFruit(fruit)) {
            return !CropAge.isMaxAge(fruit) && canBonemeal(level, hang, fruit);
         } else if ((fruit.isAir() || fruit.canBeReplaced()) && !beamMissing(level, crop, fruitPos)) {
            BlockPos supportPos = beamPos(fruitPos);
            BlockState above = level.getBlockState(supportPos);
            return crop.isVine(above) && canBonemeal(level, supportPos, above);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean applyBonemeal(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      if (!needsBonemeal(level, crop, fruitPos)) {
         return false;
      } else {
         BlockPos hang = hangPos(fruitPos);
         BlockState fruit = level.getBlockState(hang);
         if (crop.isFruit(fruit)) {
            return bonemeal(level, hang, fruit);
         } else {
            BlockPos supportPos = beamPos(fruitPos);
            return bonemeal(level, supportPos, level.getBlockState(supportPos));
         }
      }
   }

   public static boolean isReadyToHarvest(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      return ready(level, crop, fruitPos) && !isCorner(fruitPos) && crop.isMatureFull(level.getBlockState(hangPos(fruitPos)));
   }

   /**
    * 成熟的收割：先按玩家右键的方式来一遍（手持剪刀右键葡萄串，掉落物走原版逻辑），
    * 然后把果实方块复位回 0% 生长状态，而不是把它破坏掉。
    *
    * <p>不复位而是拆掉的坏处：藤架上会留下一个缺口，而且必须等头顶的藤蔓重新长一串；
    * 复位成 0% 之后藤架始终是完整的，农民下一轮会继续给同一串葡萄催熟。</p>
    */
   public static void harvest(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos fruitPos) {
      if (!isReadyToHarvest(level, crop, fruitPos)) {
         return;
      }

      BlockPos hang = hangPos(fruitPos);
      BlockState mature = level.getBlockState(hang);
      List<ItemStack> drops = new ArrayList<>();
      if (!CropHarvest.rightClickHarvest(level, chests, hang, new ItemStack(Items.SHEARS))) {
         drops.addAll(handPickedDrops(level, crop));
      }

      BlockState reset = resetFruit(crop, mature);
      BlockState current = level.getBlockState(hang);
      if (reset != null && (current.isAir() || current.canBeReplaced() || crop.isFruit(current))) {
         level.setBlock(hang, reset, 3);
      }

      if (!drops.isEmpty()) {
         WorkContainerService.depositDropsOrDrop(level, chests, drops, hang);
      }
   }

   /**
    * 本体掉落表：3 颗葡萄 + 30% 概率 1~2 颗绿葡萄，与酒馆「剪刀右键」的结果保持一致。
    * 只有在右键收割没能产出任何掉落物时才会用到。
    */
   private static List<ItemStack> handPickedDrops(ServerLevel level, ResolvedCrop crop) {
      Item fruit = crop.fruitItem();
      if (fruit == Items.AIR) {
         return List.of();
      } else {
         List<ItemStack> drops = new ArrayList<>();
         drops.add(new ItemStack(fruit, 3));
         Item green = (Item)BuiltInRegistries.ITEM.get(GREEN_GRAPE);
         // 30% 绿葡萄是酒馆绿葡萄自己的规则，别套到别的模组的葡萄上
         if (green != null && green != Items.AIR && fruit == green && level.getRandom().nextFloat() < 0.3F) {
            drops.add(new ItemStack(green, 1 + level.getRandom().nextInt(2)));
         }

         return drops;
      }
   }

   /**
    * 把成熟果实换算成 0% 生长状态的状态：优先在现有状态上把 age 归零，
    * 保不住（例如右键已经把方块拿走了）时退回果实方块的默认状态。
    */
   private static BlockState resetFruit(ResolvedCrop crop, BlockState mature) {
      BlockState reset = crop.isFruit(mature) ? CropAge.resetToMinAge(mature) : null;
      if (reset == null || !crop.isFruit(reset)) {
         Block fruitBlock = crop.fruitBlock();
         reset = fruitBlock == null || fruitBlock == Blocks.AIR ? null : fruitBlock.defaultBlockState();
      }

      return reset;
   }

   private static boolean ready(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      return level != null && crop != null && crop.isTrellis() && !crop.isMissing() && fruitPos != null && level.isLoaded(fruitPos);
   }

   private static boolean matchesSoil(ResolvedCrop crop, BlockState soil) {
      return crop.matchesSoil(soil);
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

   /**
    * 放一根立柱或横梁。
    *
    * @return 是否真的放成了（材料够、方块已落地）。材料不够返回 false，调用方据此等待补货。
    */
   private static boolean placeSupport(ServerLevel level, ResolvedCrop crop, List<BlockPos> chests, BlockPos fruitPos, BlockPos pos, boolean post) {
      if (pos == null) {
         return false;
      } else {
         Item supportItem = supportItem(crop);
         if (supportItem != Items.AIR && WorkContainerService.consumeItem(level, chests, supportItem)) {
            BlockState previous = level.getBlockState(pos);
            if (!previous.isAir() && previous.canBeReplaced()) {
               level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }

            BlockState placed = placedSupport(level, crop, fruitPos, pos);
            if (post) {
               placed = withFlag(placed, "waxed", true);
            }

            level.setBlock(pos, placed, 3);
            refreshShape(level, pos);
            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * 放下方块之后立刻按当前邻居重算一次它自己的形状。
    *
    * <p>藤架的 {@code type}（立柱 / 东西向横梁 / 十字 / 六向）完全由邻居推导，而
    * {@code setBlock} 只会去通知“邻居”重新计算，方块本身保持放下时的初始状态。
    * 于是当一格的所有邻居都早就放好时（典型的最后建成的那个角落立柱），
    * 它会永远停在放下时的 {@code east_west} —— 表现得就像“角落没有立柱、横梁朝外支棱”，
    * 而不是玩家手动摆放时会收敛出的立柱+四向横梁。</p>
    *
    * <p>这里把原版「邻居变动」触发的 {@code updateShape} 主动补跑一遍，收敛结果与手动摆放一致。</p>
    *
    * @return 形状是否真的发生了改变
    */
   private static boolean refreshShape(ServerLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      BlockState updated = state;

      for (Direction direction : Direction.values()) {
         BlockPos neighbour = pos.relative(direction);
         updated = updated.updateShape(direction, level.getBlockState(neighbour), level, pos, neighbour);
      }

      if (updated != state) {
         level.setBlock(pos, updated, 3);
         return true;
      } else {
         return false;
      }
   }

   /**
    * 一次性补正整座藤架的方块形状，用于修老存档。
    *
    * <p>上面 {@link #refreshShape} 只能保证“之后放下”的方块是对的；存档里已经卡在初始 type 的
    * 角落立柱不会自己恢复（它被 {@link #missingPost} 判定为已建好，农民永远不会再碰它）。
    * 这个方法在箱子被认领/刷新时把它们按当前邻居统一重算一遍，玩家不必拆掉重搭。</p>
    *
    * @return 被补正的方块数量，仅用于日志
    */
   public static int settleFrame(ServerLevel level, ResolvedCrop crop, FarmlandPlot plot) {
      if (level == null || crop == null || plot == null || !crop.isTrellis() || crop.isMissing()) {
         return 0;
      } else {
         int settled = 0;
         int cells = Math.max(1, plot.cellCount());

         for (int index = 0; index < cells; index++) {
            BlockPos cell = plot.cellAt(index);
            if (!level.isLoaded(cell)) {
               continue;
            }

            if (isCorner(plot, cell)) {
               for (int dy = 0; dy < TRELLIS_HEIGHT; dy++) {
                  BlockPos pos = cell.above(dy);
                  if (level.isLoaded(pos) && isWaxedPost(crop, level.getBlockState(pos)) && refreshShape(level, pos)) {
                     settled++;
                  }
               }
            } else {
               BlockPos beam = beamPos(cell);
               if (level.isLoaded(beam) && crop.isSupport(level.getBlockState(beam)) && refreshShape(level, beam)) {
                  settled++;
               }
            }
         }

         return settled;
      }
   }

   /**
    * 找出挡在藤架建造路径上、需要先清理掉的“杂物”（残留的藤蔓 / 果实）。
    *
    * <p>三格藤架布局：cell-1 土壤 · cell+0 作物格（应为空）· cell+1 挂果位 · cell+2 横梁（藤蔓长在其上，
    * 果实挂在藤蔓正下方一格）。其中 cell+1 正是本作物果实正常的生长位置，若把它当成杂物，
    * {@link #applyPlant} 会在果实刚结出（age=0）时就用 {@code breakStructure} 把它拆掉；
    * 又因为 NSUK 的工作阶段顺序是 TILL → PLANT → BONEMEAL → HARVEST，PLANT 早于 HARVEST，
    * 果实每次结出都会被立刻清掉，玩家看到的现象就是“葡萄刚长出来 0% 就被农民收掉了”。
    *
    * <p>因此：cell+0 上的藤蔓/果实一律视为杂物（旧版两格布局的残留）；
    * cell+1 上只清理错位的藤蔓，本作物的果实一律放过，等它长到满熟再交给 HARVEST 阶段收割。</p>
    */
   private static BlockPos lowestClutter(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      for (int dy = 0; dy < 2; dy++) {
         BlockPos pos = fruitPos.above(dy);
         if (!level.isLoaded(pos)) {
            return null;
         }

         BlockState state = level.getBlockState(pos);
         if (dy == 1 && crop.isFruit(state)) {
            continue;
         }

         if (crop.isVine(state) || crop.isFruit(state)) {
            return pos;
         }
      }

      return null;
   }

   private static void breakStructure(ServerLevel level, List<BlockPos> chests, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
      WorkContainerService.depositDropsOrDrop(level, chests, drops, pos);
   }

   private static BlockPos missingPost(ServerLevel level, ResolvedCrop crop, BlockPos corner) {
      for (int dy = 0; dy < 3; dy++) {
         BlockPos pos = corner.above(dy);
         if (!level.isLoaded(pos)) {
            return null;
         }

         BlockState state = level.getBlockState(pos);
         if (!isWaxedPost(crop, state)) {
            if (!state.isAir() && !state.canBeReplaced() && !crop.isSupport(state) && !crop.isVine(state) && !crop.isFruit(state)) {
               return null;
            }

            return pos;
         }
      }

      return null;
   }

   private static boolean isWaxedPost(ResolvedCrop crop, BlockState state) {
      return crop.isSupport(state) && booleanValue(state, "waxed");
   }

   private static boolean isCorner(BlockPos pos) {
      FarmlandBoxData data = CropContext.currentBox().orElse(null);
      return isCorner(data == null ? null : data.plot(), pos);
   }

   private static boolean isCorner(FarmlandPlot plot, BlockPos pos) {
      if (plot != null && pos != null) {
         boolean edgeX = pos.getX() == plot.min().getX() || pos.getX() == plot.max().getX();
         boolean edgeZ = pos.getZ() == plot.min().getZ() || pos.getZ() == plot.max().getZ();
         return edgeX && edgeZ;
      } else {
         return false;
      }
   }

   /** 站位搜索顺序：先本格，再四正，再四斜。2 格以外的位置基本超出 2.4 格行动距离，不取。 */
   private static final int[][] STAND_OFFSETS = new int[][]{
      {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
   };
   private static final double ACTION_REACH_SQR = 5.76;

   /**
    * 给农夫找一个「能站住、又在行动距离内够得着这一格」的位置，供宿主的站位计算使用。
    *
    * <p>宿主原本只在作物所在的<b>那一层</b>找脚下有实心方块的位置。藤架是抬高的：
    * 作物格 cell 上方才是果实与藤蔓，cell-1 才是待铺的土壤。土壤还没铺时那一层全是空气，
    * 宿主一格都找不到，于是退回到农田盒周围停车点 —— 农夫就在盒子与田块之间来回跑，
    * 每趟只铺得下一根，头顶状态文字也跟着跳。</p>
    *
    * <p>这里往下多找几层（土壤坑 / 地面），先把能站的位置找出来，并且校验离作物格不超过
    * 2.4 格，避免「走到了却够不着」导致原地打转。</p>
    *
    * @return 站位坐标；找不到可站之处时返回 null，由调用方退回宿主原逻辑
    */
   public static Vec3 standAnchor(ServerLevel level, BlockPos cropPos) {
      if (level == null || cropPos == null) {
         return null;
      } else {
         for (int dy = 0; dy >= -3; dy--) {
            for (int[] offset : STAND_OFFSETS) {
               BlockPos feet = cropPos.offset(offset[0], dy, offset[1]);
               Vec3 anchor = Vec3.atBottomCenterOf(feet);
               if (isStandable(level, feet) && anchor.distanceToSqr(Vec3.atCenterOf(cropPos)) <= ACTION_REACH_SQR) {
                  return anchor;
               }
            }
         }

         return null;
      }
   }

   private static boolean isStandable(ServerLevel level, BlockPos feet) {
      if (!level.isLoaded(feet)) {
         return false;
      } else {
         BlockPos belowPos = feet.below();
         BlockState below = level.getBlockState(belowPos);
         if (below.is(Blocks.FARMLAND) || below.getCollisionShape(level, belowPos).isEmpty()) {
            return false;
         } else {
            return isBodyPassable(level, feet, level.getBlockState(feet))
               && isBodyPassable(level, feet.above(), level.getBlockState(feet.above()));
         }
      }
   }

   private static boolean isBodyPassable(ServerLevel level, BlockPos pos, BlockState state) {
      return state.isAir() || state.canBeReplaced() || state.getCollisionShape(level, pos).isEmpty();
   }


   private static BlockPos hangPos(BlockPos fruitPos) {
      return fruitPos.above(1);
   }

   private static BlockPos firstEmptyVine(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos) {
      BlockPos pos = beamPos(fruitPos);
      if (!level.isLoaded(pos)) {
         return null;
      } else {
         BlockState state = level.getBlockState(pos);
         if (crop.isVine(state)) {
            return null;
         } else {
            return crop.isSupport(state) && plantableSupport(state) ? pos : null;
         }
      }
   }

   private static boolean beamMissing(ServerLevel level, ResolvedCrop crop, BlockPos aislePos) {
      return missingBeam(level, crop, aislePos) != null;
   }

   private static BlockPos missingBeam(ServerLevel level, ResolvedCrop crop, BlockPos aislePos) {
      BlockPos pos = beamPos(aislePos);
      if (!level.isLoaded(pos)) {
         return null;
      } else {
         BlockState state = level.getBlockState(pos);
         if (!crop.isSupport(state) && !crop.isVine(state)) {
            return !state.isAir() && !state.canBeReplaced() ? null : pos;
         } else {
            return null;
         }
      }
   }

   private static BlockPos beamPos(BlockPos fruitPos) {
      return fruitPos.above(2);
   }

   private static BlockState placedSupport(ServerLevel level, ResolvedCrop crop, BlockPos fruitPos, BlockPos pos) {
      boolean beam = pos.getY() == fruitPos.getY() + 3 - 1;
      String type = beam ? "east_west" : "single";
      BlockState state = crop.supportBlock().defaultBlockState();

      for (Property<?> property : state.getProperties()) {
         if ("type".equals(property.getName())) {
            state = withValue(state, property, type);
         }
      }

      return state;
   }

   private static BlockState withFlag(BlockState state, String name, boolean value) {
      for (Property<?> property : state.getProperties()) {
         if (name.equals(property.getName()) && property instanceof BooleanProperty flag) {
            return (BlockState)state.setValue(flag, value);
         }
      }

      return state;
   }

   private static BlockState withValue(BlockState state, Property property, String serialized) {
      for (Object value : property.getPossibleValues()) {
         Comparable comparable = (Comparable)value;
         if (serialized.equals(property.getName(comparable))) {
            return (BlockState)state.setValue(property, comparable);
         }
      }

      return state;
   }

   private static boolean plantableSupport(BlockState state) {
      return !booleanValue(state, "waxed");
   }

   private static boolean canBonemeal(ServerLevel level, BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, pos, state)) {
         return true;
      }

      return false;
   }

   private static boolean bonemeal(ServerLevel level, BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, pos, state)) {
         growable.performBonemeal(level, level.getRandom(), pos, state);
         return true;
      }

      return false;
   }

   private static Item supportItem(ResolvedCrop crop) {
      Block support = crop.supportBlock();
      if (support != null && support != Blocks.AIR) {
         Item item = support.asItem();
         return item == null ? Items.AIR : item;
      } else {
         return Items.AIR;
      }
   }

   private static boolean booleanValue(BlockState state, String name) {
      for (Property<?> property : state.getProperties()) {
         if (name.equals(property.getName()) && property instanceof BooleanProperty flag) {
            return Boolean.TRUE.equals(state.getValue(flag));
         }
      }

      return false;
   }
}
