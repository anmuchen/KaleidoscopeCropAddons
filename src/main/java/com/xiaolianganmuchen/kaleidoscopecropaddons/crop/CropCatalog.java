package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.config.DelightCropConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

public final class CropCatalog {
   private static final Map<String, CropSpec> SPECS = new ConcurrentHashMap<>();
   private static final Map<String, String> SOURCE_LABELS = new ConcurrentHashMap<>();

   private CropCatalog() {
   }

   public static void registerSourceLabel(String modId, String translationKey) {
      if (modId != null && !modId.isBlank() && translationKey != null && !translationKey.isBlank()) {
         SOURCE_LABELS.put(modId, translationKey);
      }
   }

   public static boolean register(CropSpec spec) {
      if (spec == null) {
         return false;
      } else {
         CropSpec existing = SPECS.get(spec.id());
         if (existing != null && (existing.origin() != CropOrigin.DISCOVERY || spec.origin() == CropOrigin.DISCOVERY)) {
            return false;
         } else {
            SPECS.put(spec.id(), spec);
            KaleidoscopeCropAddons.LOGGER.debug("Registered delight crop {} -> {}", spec.id(), spec.seedId());
            return true;
         }
      }
   }

   public static void replaceOrigin(CropOrigin origin, Collection<CropSpec> next) {
      SPECS.entrySet().removeIf(entry -> entry.getValue().origin() == origin);
      if (next != null) {
         for (CropSpec spec : next) {
            register(spec);
         }
      }
   }

   public static void clear() {
      SPECS.clear();
      SOURCE_LABELS.clear();
   }

   public static boolean contains(String id) {
      return id != null && SPECS.containsKey(id);
   }

   public static boolean isPopulated() {
      return !SPECS.isEmpty();
   }

   public static Optional<CropSpec> spec(String id) {
      return id != null && !id.isBlank() ? Optional.ofNullable(SPECS.get(id)) : Optional.empty();
   }

   public static Optional<ResolvedCrop> resolve(String id) {
      return spec(id).flatMap(ResolvedCrop::resolve).filter(CropCatalog::isEnabled);
   }

   public static boolean isSelectable(String id) {
      return resolve(id).isPresent();
   }

   public static List<ResolvedCrop> selectable() {
      List<ResolvedCrop> crops = new ArrayList<>();

      for (CropSpec spec : SPECS.values()) {
         ResolvedCrop.resolve(spec).filter(CropCatalog::isEnabled).ifPresent(crops::add);
      }

      crops.sort(
         Comparator.<ResolvedCrop>comparingInt(crop -> "custom".equals(crop.sourceModId()) ? 1 : 0)
            .thenComparing(ResolvedCrop::sourceModId)
            .thenComparing(ResolvedCrop::id)
      );
      return List.copyOf(crops);
   }

   public static Optional<ResolvedCrop> resolveByBlock(BlockState state) {
      if (state != null && !state.isAir()) {
         for (CropSpec spec : SPECS.values()) {
            Optional<ResolvedCrop> resolved = ResolvedCrop.resolve(spec).filter(CropCatalog::isEnabled);
            if (resolved.isPresent() && resolved.get().isOwnPlant(state)) {
               return resolved;
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public static Map<String, List<ResolvedCrop>> selectableBySource() {
      Map<String, List<ResolvedCrop>> grouped = new LinkedHashMap<>();

      for (ResolvedCrop crop : selectable()) {
         grouped.computeIfAbsent(crop.sourceModId(), ignored -> new ArrayList<>()).add(crop);
      }

      return grouped;
   }

   public static String sourceLabelKey(String modId) {
      return modId != null && !modId.isBlank()
         ? SOURCE_LABELS.getOrDefault(modId, "source.kaleidoscopecropaddons." + modId)
         : "source.kaleidoscopecropaddons.unknown";
   }

   public static boolean isSeedRegistered(ResourceLocation seedId) {
      if (seedId == null) {
         return false;
      } else {
         for (CropSpec spec : SPECS.values()) {
            if (seedId.equals(spec.seedId())) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isModLoaded(String modId) {
      return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
   }

   public static Item seedItemOrAir(ResourceLocation seedId) {
      return (Item)BuiltInRegistries.ITEM.get(seedId);
   }

   private static boolean isEnabled(ResolvedCrop crop) {
      return DelightCropConfig.isSourceEnabled(crop.sourceModId()) && DelightCropConfig.isCropEnabled(crop.id());
   }
}
