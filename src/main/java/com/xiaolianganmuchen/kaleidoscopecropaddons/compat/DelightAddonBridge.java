package com.xiaolianganmuchen.kaleidoscopecropaddons.compat;

import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropLangIndex;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class DelightAddonBridge {
   private static final String MOD_ID = "nsukdelightcropaddons";
   private static final String CATALOG_CLASS = "com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog";
   private static final String RESOLVED_CLASS = "com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop";
   private static boolean probed;
   private static boolean available;
   private static Method selectableBySource;
   private static Method cropId;
   private static Method sourceModId;
   private static Method seed;

   private DelightAddonBridge() {
   }

   public static boolean isPresent() {
      probe();
      return available;
   }

   public static Map<String, List<DelightAddonBridge.BridgedCrop>> groupedBySource() {
      probe();
      if (!available) {
         return Map.of();
      } else {
         Map<String, List<DelightAddonBridge.BridgedCrop>> result = new LinkedHashMap<>();

         try {
            if (!(selectableBySource.invoke(null) instanceof Map<?, ?> map)) {
               return Map.of();
            } else {
               for (Entry<?, ?> entry : map.entrySet()) {
                  String modId = String.valueOf(entry.getKey());
                  List<DelightAddonBridge.BridgedCrop> converted = (List<DelightAddonBridge.BridgedCrop>)entry.getValue();
                  if (converted instanceof List) {
                     List<?> crops = converted;
                     converted = new ArrayList<>(crops.size());

                     for (Object crop : crops) {
                        DelightAddonBridge.BridgedCrop bridged = convert(crop);
                        if (bridged != null) {
                           converted.add(bridged);
                        }
                     }

                     if (!converted.isEmpty()) {
                        result.put(modId, converted);
                     }
                  }
               }

               return result;
            }
         } catch (Throwable var11) {
            KaleidoscopeCropAddons.LOGGER.warn("Failed to read NSUK Delight Crop Addons catalog; showing only Kaleidoscope crops", var11);
            available = false;
            return Map.of();
         }
      }
   }

   public static Component sourceName(String modId) {
      if (modId == null || modId.isBlank()) {
         return null;
      } else if (CropLangIndex.hasTranslation("source.kaleidoscopecropaddons." + modId)) {
         return Component.translatable("source.kaleidoscopecropaddons." + modId);
      } else {
         try {
            Class<?> displayClass = Class.forName("com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropDisplayNames");
            Method source = displayClass.getMethod("source", String.class);
            if (source.invoke(null, modId) instanceof Component component && !component.getString().startsWith("source.")) {
               return component;
            }
         } catch (Throwable var5) {
         }

         return null;
      }
   }

   public static Component cropName(String cropId) {
      if (cropId != null && !cropId.isBlank()) {
         probe();
         if (!available) {
            return null;
         } else {
            try {
               Class<?> displayClass = Class.forName("com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropDisplayNames");
               Method resolve = displayClass.getMethod("of", String.class);
               if (resolve.invoke(null, cropId) instanceof Component component) {
                  String text = component.getString();
                  if (text != null && !text.isBlank() && !text.equals(cropId)) {
                     return component;
                  }
               }
            } catch (Throwable var6) {
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean isBridgedCrop(String targetId) {
      if (targetId != null && !targetId.isBlank()) {
         probe();
         if (!available) {
            return false;
         } else {
            try {
               if (!(selectableBySource.invoke(null) instanceof Map<?, ?> map)) {
                  return false;
               }

               for (Object value : map.values()) {
                  if (value instanceof List) {
                     for (Object crop : (List)value) {
                        if (targetId.equals(String.valueOf(cropId.invoke(crop)))) {
                           return true;
                        }
                     }
                  }
               }
            } catch (Throwable var8) {
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static DelightAddonBridge.BridgedCrop convert(Object crop) {
      if (crop == null) {
         return null;
      } else {
         try {
            String id = String.valueOf(cropId.invoke(crop));
            String modId = String.valueOf(sourceModId.invoke(crop));
            Item seedItem = (Item)seed.invoke(crop);
            return id != null && !id.isBlank() && !id.equals("null") ? new DelightAddonBridge.BridgedCrop(id, modId, seedItem) : null;
         } catch (Throwable var4) {
            return null;
         }
      }
   }

   private static synchronized void probe() {
      if (!probed) {
         probed = true;
         if (ModList.get().isLoaded("nsukdelightcropaddons")) {
            try {
               Class<?> catalog = Class.forName("com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog");
               Class<?> resolved = Class.forName("com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop");
               selectableBySource = catalog.getMethod("selectableBySource");
               cropId = resolved.getMethod("id");
               sourceModId = resolved.getMethod("sourceModId");
               seed = resolved.getMethod("seed");
               available = true;
               KaleidoscopeCropAddons.LOGGER.info("Detected NSUK Delight Crop Addons; its crops will be merged into the Kaleidoscope crop list.");
            } catch (Throwable var2) {
               available = false;
               KaleidoscopeCropAddons.LOGGER.warn("NSUK Delight Crop Addons is loaded but its catalog could not be bridged", var2);
            }
         }
      }
   }

   public static record BridgedCrop(String id, String sourceModId, Item seed) {
      public ItemStack icon() {
         return this.seed == null ? ItemStack.EMPTY : new ItemStack(this.seed);
      }
   }
}
