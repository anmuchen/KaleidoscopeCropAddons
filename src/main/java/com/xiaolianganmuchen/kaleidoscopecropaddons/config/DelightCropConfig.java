package com.xiaolianganmuchen.kaleidoscopecropaddons.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public final class DelightCropConfig {
   private static final Builder BUILDER = new Builder();
   private static final BooleanValue ENABLE_COOKERY = BUILDER.comment(
         "Allow Kaleidoscope Cookery farmland crops (tomato, chili, lettuce) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeCookery", true);
   private static final BooleanValue ENABLE_CHINESE_FOOD = BUILDER.comment(
         "Allow Kaleidoscope Chinese Food farmland crops (eggplant) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeChineseFood", true);
   private static final BooleanValue ENABLE_GRILLING = BUILDER.comment(
         "Allow Kaleidoscope Grilling farmland crops (canola, onion, sweet potato, houttuynia) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeGrilling", true);
   private static final BooleanValue ENABLE_DIM_WINE = BUILDER.comment("Allow Kaleidoscope Dimensions Wine farmland crops (hop) in the NSUK farmland box.")
      .define("enableKaleidoscopeDimWine", true);
   private static final BooleanValue ENABLE_TWILIGHT = BUILDER.comment("Allow Kaleidoscope Twilight farmland crops (fern) in the NSUK farmland box.")
      .define("enableKaleidoscopeTwilight", true);
   private static final BooleanValue ENABLE_ORCHARD = BUILDER.comment("Allow Kaleidoscope Orchard farmland crops (strawberry) in the NSUK farmland box.")
      .define("enableKaleidoscopeOrchard", true);
   private static final BooleanValue ENABLE_TAVERN = BUILDER.comment(
         "Allow Kaleidoscope Tavern grapes (grape, ice grape, gold grape) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeTavern", true);
   private static final BooleanValue ENABLE_END = BUILDER.comment(
         "Allow Kaleidoscope End crops (ender mint, grows on end stone) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeEnd", true);
   private static final BooleanValue ENABLE_NETHER = BUILDER.comment(
         "Allow Kaleidoscope Nether crops (soul pepper, poisonous fruit, grow on soul sand/soil) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeNether", true);
   private static final BooleanValue ENABLE_SCULK = BUILDER.comment(
         "Allow Kaleidoscope Sculk crops (echo crop, grows on sculk catalyst) in the NSUK farmland box."
      )
      .define("enableKaleidoscopeSculk", true);
   private static final BooleanValue AUTO_DISCOVERY = BUILDER.comment("Scan loaded mods for CropBlock seeds that were not listed in JSON.")
      .define("autoDiscovery", false);
   private static final ConfigValue<List<? extends String>> DISABLED_CROPS = BUILDER.comment("Catalog ids to hide, for example kc_tomato or kg_onion.")
      .defineListAllowEmpty("disabledCrops", List.of(), () -> "", DelightCropConfig::isString);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private DelightCropConfig() {
   }

   public static boolean configReady() {
      try {
         return SPEC.isLoaded();
      } catch (RuntimeException var1) {
         return false;
      }
   }

   public static boolean autoDiscovery() {
      return flag(AUTO_DISCOVERY, false);
   }

   public static boolean isSourceEnabled(String modId) {
      if (modId != null && !modId.isBlank()) {
         return switch (modId) {
            case "kaleidoscope_cookery" -> flag(ENABLE_COOKERY, true);
            case "kaleidoscope_chinesefood" -> flag(ENABLE_CHINESE_FOOD, true);
            case "kaleidoscope_grilling" -> flag(ENABLE_GRILLING, true);
            case "kaleidoscope_dim_wine" -> flag(ENABLE_DIM_WINE, true);
            case "kaleidoscope_twilight" -> flag(ENABLE_TWILIGHT, true);
            case "kaleidoscope_orchard" -> flag(ENABLE_ORCHARD, true);
            case "kaleidoscope_tavern" -> flag(ENABLE_TAVERN, true);
            case "kaleidoscope_end" -> flag(ENABLE_END, true);
            case "kaleidoscope_nether" -> flag(ENABLE_NETHER, true);
            case "kaleidoscope_sculk" -> flag(ENABLE_SCULK, true);
            case "custom" -> true;
            default -> true;
         };
      } else {
         return false;
      }
   }

   public static List<DelightCropConfig.SourceToggle> sourceToggles() {
      return List.of(
         new DelightCropConfig.SourceToggle("kaleidoscope_cookery", ENABLE_COOKERY),
         new DelightCropConfig.SourceToggle("kaleidoscope_chinesefood", ENABLE_CHINESE_FOOD),
         new DelightCropConfig.SourceToggle("kaleidoscope_grilling", ENABLE_GRILLING),
         new DelightCropConfig.SourceToggle("kaleidoscope_dim_wine", ENABLE_DIM_WINE),
         new DelightCropConfig.SourceToggle("kaleidoscope_twilight", ENABLE_TWILIGHT),
         new DelightCropConfig.SourceToggle("kaleidoscope_orchard", ENABLE_ORCHARD),
         new DelightCropConfig.SourceToggle("kaleidoscope_tavern", ENABLE_TAVERN),
         new DelightCropConfig.SourceToggle("kaleidoscope_end", ENABLE_END),
         new DelightCropConfig.SourceToggle("kaleidoscope_nether", ENABLE_NETHER),
         new DelightCropConfig.SourceToggle("kaleidoscope_sculk", ENABLE_SCULK)
      );
   }

   public static void setAutoDiscovery(boolean value) {
      AUTO_DISCOVERY.set(value);
   }

   public static boolean isCropEnabled(String cropId) {
      if (cropId != null && !cropId.isBlank()) {
         try {
            if (!configReady()) {
               return true;
            } else {
               Set<String> disabled = ((List<?>)DISABLED_CROPS.get())
                  .stream()
                  .map(value -> String.valueOf(value).toLowerCase(Locale.ROOT))
                  .collect(Collectors.toSet());
               return !disabled.contains(cropId.toLowerCase(Locale.ROOT));
            }
         } catch (RuntimeException var21) {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean flag(BooleanValue value, boolean fallback) {
      try {
         return !configReady() ? fallback : (Boolean)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static boolean isString(Object value) {
      return value instanceof String;
   }

   public static record SourceToggle(String modId, BooleanValue value) {
      public boolean enabled() {
         return DelightCropConfig.flag(this.value, true);
      }

      public void setEnabled(boolean enabled) {
         this.value.set(enabled);
      }

      public String labelKey() {
         return "source.kaleidoscopecropaddons." + this.modId;
      }
   }
}
