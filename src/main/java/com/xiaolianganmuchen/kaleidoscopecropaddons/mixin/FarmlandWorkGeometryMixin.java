package com.xiaolianganmuchen.kaleidoscopecropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropContext;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.TrellisCropWork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 藤架作物的农夫站位修正。
 *
 * <p>目标是 NSUK 里包内可见的 {@code FarmlandWorkGeometry}，编译期无法直接引用，
 * 所以用 {@code targets} 字符串指定；该方法的参数与返回类型都是公开类型，签名可以正常书写。</p>
 *
 * <p>宿主原逻辑只在作物所在那一层找落脚点；藤架被抬高两格，土壤还没铺时那一层全是空气，
 * 找不到站位就会退回农田盒周围的停车点，农夫于是盒子与田块之间来回跑。这里换成
 * {@link TrellisCropWork#standAnchor} 的多层搜索，结果与「玩家能站在哪」一致。</p>
 */
@Mixin(targets = "common.cn.kafei.simukraft.farmland.FarmlandWorkGeometry", remap = false)
public abstract class FarmlandWorkGeometryMixin {
   @WrapMethod(
      method = {"workAnchorFor"}
   )
   private static Vec3 kaleidoscope$trellisStandAnchor(ServerLevel level, BlockPos boxPos, BlockPos cropPos, Operation<Vec3> original) {
      ResolvedCrop crop = CropContext.current().orElse(null);
      if (crop != null && crop.isTrellis()) {
         Vec3 anchor = TrellisCropWork.standAnchor(level, cropPos);
         if (anchor != null) {
            return anchor;
         }
      }

      return (Vec3)original.call(new Object[]{level, boxPos, cropPos});
   }
}
