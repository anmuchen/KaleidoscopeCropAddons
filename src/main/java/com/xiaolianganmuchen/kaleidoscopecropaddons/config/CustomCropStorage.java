package com.xiaolianganmuchen.kaleidoscopecropaddons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaolianganmuchen.kaleidoscopecropaddons.KaleidoscopeCropAddons;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropIds;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropLangIndex;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropOrigin;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public final class CustomCropStorage {
   public static final String SOURCE_MOD = "custom";
   public static final String PREFIX = "cu";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final List<CustomCropStorage.Entry> ENTRIES = new CopyOnWriteArrayList<>();

   private CustomCropStorage() {
   }

   public static List<CustomCropStorage.Entry> entries() {
      return List.copyOf(ENTRIES);
   }

   public static void loadAndApply() {
      ENTRIES.clear();
      ENTRIES.addAll(readFile());
      apply();
   }

   public static void apply() {
      List<CropSpec> specs = new ArrayList<>();

      for (CustomCropStorage.Entry entry : ENTRIES) {
         CropSpec spec = entry.toSpec();
         if (spec != null) {
            specs.add(spec);
         }
      }

      CropCatalog.replaceOrigin(CropOrigin.CUSTOM, specs);
   }

   public static boolean add(CustomCropStorage.Entry entry) {
      if (entry != null && entry.seed != null && !entry.seed.isBlank()) {
         CustomCropStorage.Entry stored = entry.withId(uniqueId(entry));
         ENTRIES.add(stored);
         save();
         apply();
         return true;
      } else {
         return false;
      }
   }

   public static void remove(String id) {
      ENTRIES.removeIf(entry -> entry.id.equals(id));
      save();
      apply();
   }

   public static Component displayName(String cropId) {
      CustomCropStorage.Entry entry = find(cropId);
      if (entry == null) {
         return null;
      } else {
         String preferred = preferredName(entry);
         return preferred.isBlank() ? null : Component.literal(preferred);
      }
   }

   public static String searchText(String cropId) {
      CustomCropStorage.Entry entry = find(cropId);
      return entry == null ? "" : CropLangIndex.haystack(entry.id, entry.seed, entry.plant, entry.nameZh, entry.nameEn);
   }

   private static CustomCropStorage.Entry find(String id) {
      if (id == null) {
         return null;
      } else {
         for (CustomCropStorage.Entry entry : ENTRIES) {
            if (id.equals(entry.id)) {
               return entry;
            }
         }

         return null;
      }
   }

   private static String preferredName(CustomCropStorage.Entry entry) {
      String language = "";
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.options != null && minecraft.options.languageCode != null) {
         language = minecraft.options.languageCode;
      }

      boolean chinese = language.toLowerCase(Locale.ROOT).startsWith("zh");
      if (chinese && !entry.nameZh.isBlank()) {
         return entry.nameZh;
      } else if (!chinese && !entry.nameEn.isBlank()) {
         return entry.nameEn;
      } else {
         return !entry.nameZh.isBlank() ? entry.nameZh : entry.nameEn;
      }
   }

   private static String uniqueId(CustomCropStorage.Entry entry) {
      String base = entry.id != null && !entry.id.isBlank()
         ? CropIds.compose("cu", entry.id)
         : CropIds.compose(
            "cu", CropIds.keyFromSeedPath(ResourceLocation.tryParse(entry.seed) == null ? entry.seed : ResourceLocation.tryParse(entry.seed).getPath())
         );
      String id = base;
      int suffix = 2;

      while (CropCatalog.contains(id) || hasId(id)) {
         String extra = String.valueOf(suffix++);
         id = base.length() + extra.length() > 32 ? base.substring(0, Math.max(1, 32 - extra.length())) + extra : base + extra;
      }

      return id;
   }

   private static boolean hasId(String id) {
      for (CustomCropStorage.Entry entry : ENTRIES) {
         if (entry.id.equals(id)) {
            return true;
         }
      }

      return false;
   }

   private static List<CustomCropStorage.Entry> readFile() {
      Path path = file();
      if (!Files.isRegularFile(path)) {
         return List.of();
      } else {
         try {
            JsonElement element = (JsonElement)GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonElement.class);
            JsonArray array = element != null && element.isJsonObject() && element.getAsJsonObject().has("crops")
               ? element.getAsJsonObject().getAsJsonArray("crops")
               : (element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray());
            List<CustomCropStorage.Entry> loaded = new ArrayList<>();

            for (JsonElement crop : array) {
               if (crop.isJsonObject()) {
                  CustomCropStorage.Entry entry = CustomCropStorage.Entry.fromJson(crop.getAsJsonObject());
                  if (entry != null) {
                     loaded.add(entry);
                  }
               }
            }

            return loaded;
         } catch (Exception var71) {
            KaleidoscopeCropAddons.LOGGER.error("Failed to read custom delight crops", var71);
            return List.of();
         }
      }
   }

   private static void save() {
      JsonObject root = new JsonObject();
      JsonArray crops = new JsonArray();

      for (CustomCropStorage.Entry entry : ENTRIES) {
         crops.add(entry.toJson());
      }

      root.add("crops", crops);
      Path path = file();

      try {
         Files.createDirectories(path.getParent());
         Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (IOException var41) {
         KaleidoscopeCropAddons.LOGGER.error("Failed to save custom delight crops", var41);
      }
   }

   private static Path file() {
      return FMLPaths.CONFIGDIR.get().resolve("kaleidoscopecropaddons-custom-crops.json");
   }

   public static record Entry(String id, String seed, String plant, List<String> harvestBlocks, boolean allowNonCropBlock, String nameZh, String nameEn) {
      public CustomCropStorage.Entry withId(String nextId) {
         return new CustomCropStorage.Entry(nextId, this.seed, this.plant, this.harvestBlocks, this.allowNonCropBlock, this.nameZh, this.nameEn);
      }

      public CropSpec toSpec() {
         ResourceLocation seedId = CropIds.parse(this.seed, "minecraft");
         if (seedId == null) {
            return null;
         } else {
            List<ResourceLocation> extra = new ArrayList<>();
            if (this.harvestBlocks != null) {
               for (String harvest : this.harvestBlocks) {
                  ResourceLocation extraId = CropIds.parse(harvest, seedId.getNamespace());
                  if (extraId != null) {
                     extra.add(extraId);
                  }
               }
            }

            return CropSpec.builder(this.id)
               .sourceModId("custom")
               .sourcePrefix("cu")
               .seedId(seedId)
               .plantBlockId(CropIds.parse(this.plant, seedId.getNamespace()))
               .extraHarvestBlockIds(extra)
               .allowNonCropBlock(this.allowNonCropBlock)
               .origin(CropOrigin.CUSTOM)
               .build();
         }
      }

      public JsonObject toJson() {
         JsonObject json = new JsonObject();
         json.addProperty("id", this.id);
         json.addProperty("seed", this.seed);
         if (this.plant != null && !this.plant.isBlank()) {
            json.addProperty("plant", this.plant);
         }

         if (this.harvestBlocks != null && !this.harvestBlocks.isEmpty()) {
            JsonArray extra = new JsonArray();
            this.harvestBlocks.forEach(extra::add);
            json.add("harvest_blocks", extra);
         }

         json.addProperty("allow_non_crop_block", this.allowNonCropBlock);
         if (this.nameZh != null && !this.nameZh.isBlank()) {
            json.addProperty("name_zh", this.nameZh);
         }

         if (this.nameEn != null && !this.nameEn.isBlank()) {
            json.addProperty("name_en", this.nameEn);
         }

         return json;
      }

      public static CustomCropStorage.Entry fromJson(JsonObject json) {
         String seed = string(json, "seed");
         if (seed.isBlank()) {
            return null;
         } else {
            List<String> extra = new ArrayList<>();
            if (json.has("harvest_blocks") && json.get("harvest_blocks").isJsonArray()) {
               for (JsonElement element : json.getAsJsonArray("harvest_blocks")) {
                  extra.add(element.getAsString());
               }
            }

            String id = string(json, "id");
            return new CustomCropStorage.Entry(
               id,
               seed,
               string(json, "plant"),
               List.copyOf(extra),
               json.has("allow_non_crop_block") && json.get("allow_non_crop_block").getAsBoolean(),
               string(json, "name_zh"),
               string(json, "name_en")
            );
         }
      }

      private static String string(JsonObject json, String key) {
         if (json.has(key) && !json.get(key).isJsonNull()) {
            String value = json.get(key).getAsString();
            return value == null ? "" : value.trim();
         } else {
            return "";
         }
      }
   }
}
