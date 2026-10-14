package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.nsuk.FarmlandBoxDataAccess;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class CropContext {
   private static final ThreadLocal<Deque<CropContext.Frame>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

   private CropContext() {
   }

   public static void push(FarmlandBoxData data) {
      STACK.get().push(new CropContext.Frame(resolveOrMissing(data), data));
   }

   public static void pop() {
      Deque<CropContext.Frame> stack = STACK.get();
      if (!stack.isEmpty()) {
         stack.pop();
      }

      if (stack.isEmpty()) {
         STACK.remove();
      }
   }

   public static void clear() {
      STACK.remove();
   }

   public static Optional<ResolvedCrop> current() {
      CropContext.Frame frame = peek();
      return frame != null && frame.crop != null && !frame.crop.isMissing() ? Optional.of(frame.crop) : Optional.empty();
   }

   public static Optional<FarmlandBoxData> currentBox() {
      CropContext.Frame frame = peek();
      return frame != null && frame.box != null ? Optional.of(frame.box) : Optional.empty();
   }

   private static CropContext.Frame peek() {
      Deque<CropContext.Frame> stack = STACK.get();
      return stack.isEmpty() ? null : stack.peek();
   }

   private static ResolvedCrop resolveOrMissing(FarmlandBoxData data) {
      try {
         String addonId = FarmlandBoxDataAccess.addonCropId(data);
         return addonId != null && !addonId.isBlank() ? CropCatalog.resolve(addonId).orElse(ResolvedCrop.missing()) : ResolvedCrop.missing();
      } catch (Exception var21) {
         KaleidoscopeCropAddons.LOGGER.error("Failed to bind delight crop context for farmland box", var21);
         return ResolvedCrop.missing();
      }
   }

   private static record Frame(ResolvedCrop crop, FarmlandBoxData box) {
   }
}
