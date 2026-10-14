package com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk;

import common.cn.kafei.simukraft.citizen.CitizenData;
import common.cn.kafei.simukraft.citizen.CitizenService;
import common.cn.kafei.simukraft.citizen.CitizenWorkStatus;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandBoxService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class FarmlandBoxCleanup {
   private FarmlandBoxCleanup() {
   }

   public static void purge(ServerLevel level, BlockPos boxPos) {
      if (level != null && boxPos != null) {
         FarmlandBoxManager manager = FarmlandBoxManager.get(level);
         FarmlandBoxData data = manager.get(boxPos);
         if (data != null) {
            FarmlandBoxDataAccess.clearSelection(data);
            data.setPlot(null);
            data.setRunning(false);
         }

         clearFarmerFarmStatus(level, boxPos);
         manager.remove(boxPos);
         FarmlandBoxReconcile.forget(level, boxPos);
      }
   }

   public static void clearFarmerFarmStatus(ServerLevel level, BlockPos boxPos) {
      CitizenData farmer = FarmlandBoxService.findAssignedWorker(level, boxPos);
      if (farmer != null) {
         farmer.setWorkStatus(CitizenWorkStatus.IDLE);
         farmer.setStatusLabel("");
         farmer.setWorkNeedDetail("");
         CitizenService.save(level, farmer.uuid());
      }
   }
}
