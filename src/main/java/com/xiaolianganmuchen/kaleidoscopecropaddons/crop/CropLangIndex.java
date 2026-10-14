package com.xiaolianganmuchen.kaleidoscopecropaddons.crop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class CropLangIndex {
   private static final Gson GSON = new GsonBuilder().setLenient().create();
   private static final Map<String, String> EN = new HashMap<>();
   private static final Map<String, String> ZH = new HashMap<>();

   private CropLangIndex() {
   }

   public static void refresh() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null) {
         refresh(minecraft.getResourceManager());
      }
   }

   public static void refresh(ResourceManager resourceManager) {
      EN.clear();
      ZH.clear();
      if (resourceManager != null) {
         try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources("lang", location -> {
               String path = location.getPath();
               return path.endsWith("en_us.json") || path.endsWith("zh_cn.json");
            });

            for (Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
               boolean english = entry.getKey().getPath().endsWith("en_us.json");

               try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                  JsonElement element = (JsonElement)GSON.fromJson(reader, JsonElement.class);
                  if (element != null && element.isJsonObject()) {
                     JsonObject object = element.getAsJsonObject();
                     Map<String, String> target = english ? EN : ZH;

                     for (Entry<String, JsonElement> line : object.entrySet()) {
                        if (line.getValue() != null && line.getValue().isJsonPrimitive()) {
                           target.put(line.getKey(), line.getValue().getAsString());
                        }
                     }
                  }
               } catch (Exception var13) {
               }
            }
         } catch (Exception var14) {
         }
      }
   }

   public static String en(String key) {
      return EN.getOrDefault(key, "");
   }

   public static String zh(String key) {
      return ZH.getOrDefault(key, "");
   }

   public static String current(String key) {
      if (key != null && !key.isBlank()) {
         String value = Language.getInstance().getOrDefault(key);
         return value != null && !value.equals(key) ? value : "";
      } else {
         return "";
      }
   }

   public static boolean hasTranslation(String key) {
      return !current(key).isBlank();
   }

   public static String haystack(String... parts) {
      StringBuilder builder = new StringBuilder();

      for (String part : parts) {
         if (part != null && !part.isBlank()) {
            if (!builder.isEmpty()) {
               builder.append(' ');
            }

            builder.append(part.toLowerCase(Locale.ROOT));
         }
      }

      return builder.toString();
   }
}
