package com.xiaolianganmuchen.kaleidoscopecropaddons.client;

import client.cn.kafei.simukraft.client.ui.SimuKraftUiTheme;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.xiaolianganmuchen.kaleidoscopecropaddons.compat.DelightAddonBridge;
import com.xiaolianganmuchen.kaleidoscopecropaddons.config.CropSelectUiStorage;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropDisplayNames;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.CropLangIndex;
import com.xiaolianganmuchen.kaleidoscopecropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenRequestPacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxSetCropPacket;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public final class DelightCropSelectScreen {
   private static final int ITEM_HEIGHT = 24;
   private static final int HEADER_HEIGHT = 14;
   private static final int HEADER_COLOR = -658016;
   private static final int ICON_SIZE = 18;
   private static final int PANEL_MAX_WIDTH = 320;
   private static final int PANEL_HEIGHT = 236;
   private static final int SEARCH_HEIGHT = 22;
   private static final int RANK_DELIGHT_MAIN = 10;
   private static final int RANK_DELIGHT_SINGLE = 15;
   private static final int RANK_DELIGHT_ADDON = 20;
   private static final int RANK_DELIGHT_TRAILING = 25;
   private static final int RANK_KALEIDO_COOKERY = 30;
   private static final int RANK_KALEIDO_ADDON = 40;
   private static final int RANK_CUSTOM = 50;
   private static final int RANK_OTHER = 60;
   private static final Set<String> DELIGHT_TRAILING = Set.of("croptopia");
   private static final Set<String> DELIGHT_FAMILY = Set.of(
      "farmersdelight",
      "croptopia",
      "culturaldelights",
      "dumplings_delight",
      "expandeddelight",
      "gan_delight_reborn",
      "immortalers_delight",
      "pineapple_delight",
      "rusticdelight",
      "trailandtales_delight",
      "ubesdelight",
      "veggiesdelight",
      "youkaisfeasts"
   );

   private DelightCropSelectScreen() {
   }

   public static void open(FarmlandBoxOpenResponsePacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null) {
         CropLangIndex.refresh();
         minecraft.execute(() -> minecraft.setScreen(new DelightCropSelectScreen.CropScreen(createUi(packet), Component.empty())));
      }
   }

   private static ModularUI createUi(FarmlandBoxOpenResponsePacket packet) {
      int screenWidth = Math.max(320, Minecraft.getInstance().getWindow().getGuiScaledWidth());
      int screenHeight = Math.max(240, Minecraft.getInstance().getWindow().getGuiScaledHeight());
      UIElement root = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.heightPercent(100.0F);
         layout.alignItems(AlignItems.CENTER);
         layout.justifyContent(AlignContent.CENTER);
         layout.paddingAll(8.0F);
      });
      root.addChild(SimuKraftUiTheme.createShellPanel(screenWidth, screenHeight));
      root.addChild(topButton("gui.button.back", () -> back(packet.boxPos())));
      UIElement panel = new UIElement().layout(layout -> {
         layout.widthPercent(90.0F);
         layout.maxWidth(320.0F);
         layout.height((float)Math.min(236, screenHeight - 48));
         layout.flexDirection(FlexDirection.COLUMN);
         layout.alignItems(AlignItems.STRETCH);
         layout.paddingAll(10.0F);
         layout.gapAll(6.0F);
      }).addClass("simukraft_panel");
      panel.addChild(label(Component.translatable("gui.simukraft.farmland_box.select_crop_title"), Horizontal.CENTER, -1, 16));
      panel.addChild(label(Component.translatable("gui.kaleidoscopecropaddons.select_crop.hint"), Horizontal.CENTER, -4342339, 12));
      UIElement list = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.flexDirection(FlexDirection.COLUMN);
         layout.alignItems(AlignItems.STRETCH);
         layout.gapAll(4.0F);
      });
      DelightCropSelectScreen.CropListState state = new DelightCropSelectScreen.CropListState(packet, list);
      panel.addChild(searchBox(state));
      ScrollerView scroller = new ScrollerView();
      scroller.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
      scroller.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.flex(1.0F);
         layout.minHeight(80.0F);
      });
      scroller.addScrollViewChild(list);
      panel.addChild(scroller);
      state.rebuild();
      root.addChild(panel);
      return new ModularUI(SimuKraftUiTheme.createUi(root)).shouldCloseOnEsc(true).shouldCloseOnKeyInventory(false);
   }

   private static UIElement searchBox(DelightCropSelectScreen.CropListState state) {
      UIElement wrap = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(22.0F);
         layout.minHeight(22.0F);
         layout.maxHeight(22.0F);
         layout.flexShrink(0.0F);
      }).style(style -> style.backgroundTexture(new GuiTextureGroup(new IGuiTexture[]{new ColorRectTexture(-15461352), new ColorBorderTexture(1, -7697808)})));
      TextField field = new TextField();
      field.setAnyString();
      field.setTextResponder(state::setQuery);
      field.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(22.0F);
         layout.paddingLeft(6.0F);
         layout.paddingRight(6.0F);
         layout.paddingTop(5.0F);
         layout.paddingBottom(5.0F);
      });
      field.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
      field.textFieldStyle(
         style -> style.fontSize(9.0F)
               .textColor(-1)
               .cursorColor(-1)
               .textShadow(false)
               .placeholder(Component.translatable("gui.kaleidoscopecropaddons.select_crop.search"))
               .focusOverlay(IGuiTexture.EMPTY)
      );
      wrap.addChild(field);
      return wrap;
   }

   private static UIElement vanillaButton(FarmlandBoxOpenResponsePacket packet, FarmCrop crop) {
      boolean selected = crop.id().equals(packet.cropId());
      Component name = Component.translatable(crop.translationKey());
      return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), name, selected);
   }

   private static UIElement delightButton(FarmlandBoxOpenResponsePacket packet, ResolvedCrop crop) {
      boolean selected = crop.id().equals(packet.cropId());
      return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), CropDisplayNames.of(crop), selected);
   }

   private static UIElement bridgedButton(FarmlandBoxOpenResponsePacket packet, DelightAddonBridge.BridgedCrop crop) {
      boolean selected = crop.id().equals(packet.cropId());
      Component name = CropDisplayNames.of(crop.id());
      return cropRow(packet.boxPos(), crop.id(), crop.icon(), name, selected);
   }

   private static boolean bridgedMatches(String query, DelightAddonBridge.BridgedCrop crop) {
      if (query != null && !query.isBlank()) {
         String needle = query.toLowerCase(Locale.ROOT).trim();
         String ourKey = "crop.kaleidoscopecropaddons." + crop.id();
         String seedKey = crop.seed() == null ? "" : crop.seed().getDescriptionId();
         return CropLangIndex.haystack(
               crop.id(),
               crop.sourceModId(),
               CropDisplayNames.of(crop.id()).getString(),
               seedKey,
               CropLangIndex.en(seedKey),
               CropLangIndex.zh(seedKey),
               CropLangIndex.current(seedKey),
               CropLangIndex.en(ourKey),
               CropLangIndex.zh(ourKey)
            )
            .contains(needle);
      } else {
         return true;
      }
   }

   private static UIElement cropRow(BlockPos boxPos, String cropId, ItemStack icon, Component name, boolean selected) {
      UIElement row = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(24.0F);
         layout.flexDirection(FlexDirection.ROW);
         layout.alignItems(AlignItems.CENTER);
         layout.gapAll(6.0F);
      });
      row.addChild(new UIElement().layout(layout -> {
         layout.width(18.0F);
         layout.height(18.0F);
         layout.flexShrink(0.0F);
      }).style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack[]{icon}))));
      Button button = new Button();
      Component text = (Component)(selected ? Component.translatable("gui.simukraft.farmland_box.crop_selected", new Object[]{name}) : name);
      button.setText(text);
      button.setOnClick(event -> select(boxPos, cropId));
      button.layout(layout -> {
         layout.height(24.0F);
         layout.flex(1.0F);
      });
      row.addChild(button);
      return row;
   }

   private static UIElement sectionTitle(Component text) {
      return label(text, Horizontal.LEFT, -658016, 14);
   }

   private static UIElement foldHeader(String modId, boolean expanded, Runnable toggle) {
      String key = expanded ? "gui.kaleidoscopecropaddons.select_crop.group_expanded" : "gui.kaleidoscopecropaddons.select_crop.group_collapsed";
      Button header = new Button().noText();
      header.buttonStyle(style -> style.baseTexture(IGuiTexture.EMPTY).hoverTexture(new ColorRectTexture(586544544)).pressedTexture(IGuiTexture.EMPTY));
      header.setOnClick(event -> toggle.run());
      header.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(14.0F);
         layout.flexDirection(FlexDirection.ROW);
         layout.alignItems(AlignItems.CENTER);
      });
      header.addChild(label(Component.translatable(key, new Object[]{CropDisplayNames.source(modId)}), Horizontal.LEFT, -658016, 14));
      return header;
   }

   private static boolean isDelightFamily(String modId) {
      return modId != null && !modId.isBlank() ? DELIGHT_FAMILY.contains(modId) || modId.endsWith("_delight") || modId.endsWith("delight") : false;
   }

   private static int sourceRank(String modId, int cropCount) {
      if (modId == null || modId.isBlank()) {
         return 60;
      } else if ("farmersdelight".equals(modId)) {
         return 10;
      } else if ("custom".equals(modId)) {
         return 50;
      } else if ("kaleidoscope_cookery".equals(modId)) {
         return 30;
      } else if (isDelightFamily(modId)) {
         if (DELIGHT_TRAILING.contains(modId)) {
            return 25;
         } else {
            return cropCount <= 1 ? 15 : 20;
         }
      } else {
         return modId.startsWith("kaleidoscope_") ? 40 : 60;
      }
   }

   private static boolean needsFold(String modId, int cropCount) {
      return !"kaleidoscope_cookery".equals(modId) && !"farmersdelight".equals(modId) ? cropCount > 1 : false;
   }

   private static Button topButton(String key, Runnable action) {
      Button button = new Button();
      button.setText(Component.translatable(key));
      button.setOnClick(event -> action.run());
      button.layout(layout -> {
         layout.positionType(TaffyPosition.ABSOLUTE);
         layout.left(5.0F);
         layout.top(5.0F);
         layout.width(50.0F);
         layout.height(22.0F);
      });
      return button;
   }

   private static Label label(Component text, Horizontal horizontal, int color, int height) {
      Label label = new Label();
      label.setText(text);
      label.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height((float)height);
      });
      label.textStyle(style -> style.textColor(color).textShadow(true).textAlignHorizontal(horizontal).textAlignVertical(Vertical.CENTER));
      return label;
   }

   private static void select(BlockPos boxPos, String cropId) {
      PacketDistributor.sendToServer(new FarmlandBoxSetCropPacket(boxPos, cropId), new CustomPacketPayload[0]);
   }

   private static void back(BlockPos boxPos) {
      PacketDistributor.sendToServer(new FarmlandBoxOpenRequestPacket(boxPos), new CustomPacketPayload[0]);
   }

   private static final class CropListState {
      private final FarmlandBoxOpenResponsePacket packet;
      private final UIElement list;
      private String query = "";

      private CropListState(FarmlandBoxOpenResponsePacket packet, UIElement list) {
         this.packet = packet;
         this.list = list;
      }

      private void setQuery(String text) {
         String next = text == null ? "" : text.trim();
         if (!next.equals(this.query)) {
            this.query = next;
            this.rebuild();
         }
      }

      private void toggle(String modId) {
         CropSelectUiStorage.toggle(modId);
         this.rebuild();
      }

      private void rebuild() {
         this.list.clearAllChildren();
         boolean searching = !this.query.isBlank();
         boolean any = false;
         boolean vanillaHeader = false;

         for (FarmCrop crop : FarmCrop.values()) {
            if (CropDisplayNames.matches(this.query, crop)) {
               if (!vanillaHeader) {
                  this.list.addChild(DelightCropSelectScreen.sectionTitle(Component.translatable("source.kaleidoscopecropaddons.vanilla")));
                  vanillaHeader = true;
               }

               this.list.addChild(DelightCropSelectScreen.vanillaButton(this.packet, crop));
               any = true;
            }
         }

         List<DelightCropSelectScreen.RenderGroup> groups = new ArrayList<>();

         for (Entry<String, List<ResolvedCrop>> entry : CropCatalog.selectableBySource().entrySet()) {
            groups.add(DelightCropSelectScreen.RenderGroup.ofResolved(entry.getKey(), entry.getValue()));
         }

         if (DelightAddonBridge.isPresent()) {
            for (Entry<String, List<DelightAddonBridge.BridgedCrop>> entry : DelightAddonBridge.groupedBySource().entrySet()) {
               if (!CropCatalog.selectableBySource().containsKey(entry.getKey())) {
                  groups.add(DelightCropSelectScreen.RenderGroup.ofBridged(entry.getKey(), entry.getValue()));
               }
            }
         }

         if (!groups.isEmpty()) {
            groups.sort(
               Comparator.<DelightCropSelectScreen.RenderGroup>comparingInt(group -> DelightCropSelectScreen.sourceRank(group.modId(), group.total()))
                  .thenComparing(group -> group.displayName().getString())
            );
         }

         for (DelightCropSelectScreen.RenderGroup group : groups) {
            String modId = group.modId();
            boolean fold = DelightCropSelectScreen.needsFold(modId, group.total());
            int matchCount = group.matchedCount(this.query);
            if (matchCount != 0) {
               boolean open = searching || !fold || CropSelectUiStorage.isExpanded(modId);
               if (fold && !searching) {
                  this.list.addChild(DelightCropSelectScreen.foldHeader(modId, open, () -> this.toggle(modId)));
               } else {
                  this.list.addChild(DelightCropSelectScreen.sectionTitle(group.displayName()));
               }

               if (open) {
                  group.emit(this.packet, this.list, this.query);
               }

               any = true;
            }
         }

         if (!any && !searching) {
            this.list
               .addChild(DelightCropSelectScreen.label(Component.translatable("gui.kaleidoscopecropaddons.select_crop.empty"), Horizontal.LEFT, -4342339, 14));
         } else if (!any) {
            this.list
               .addChild(
                  DelightCropSelectScreen.label(Component.translatable("gui.kaleidoscopecropaddons.select_crop.no_match"), Horizontal.LEFT, -4342339, 14)
               );
         }
      }
   }

   private static final class CropScreen extends ModularUIScreen {
      private CropScreen(ModularUI modularUI, Component title) {
         super(modularUI, title);
      }
   }

   private static record RenderGroup(String modId, Component displayName, List<ResolvedCrop> ownCrops, List<DelightAddonBridge.BridgedCrop> bridgedCrops) {
      static DelightCropSelectScreen.RenderGroup ofResolved(String modId, List<ResolvedCrop> crops) {
         return new DelightCropSelectScreen.RenderGroup(modId, CropDisplayNames.source(modId), List.copyOf(crops), List.of());
      }

      static DelightCropSelectScreen.RenderGroup ofBridged(String modId, List<DelightAddonBridge.BridgedCrop> crops) {
         Component name = DelightAddonBridge.sourceName(modId);
         return new DelightCropSelectScreen.RenderGroup(modId, name != null ? name : CropDisplayNames.source(modId), List.of(), List.copyOf(crops));
      }

      int total() {
         return this.ownCrops.size() + this.bridgedCrops.size();
      }

      int matchedCount(String query) {
         int count = 0;

         for (ResolvedCrop crop : this.ownCrops) {
            if (CropDisplayNames.matches(query, crop)) {
               count++;
            }
         }

         for (DelightAddonBridge.BridgedCrop cropx : this.bridgedCrops) {
            if (DelightCropSelectScreen.bridgedMatches(query, cropx)) {
               count++;
            }
         }

         return count;
      }

      void emit(FarmlandBoxOpenResponsePacket packet, UIElement list, String query) {
         for (ResolvedCrop crop : this.ownCrops) {
            if (CropDisplayNames.matches(query, crop)) {
               list.addChild(DelightCropSelectScreen.delightButton(packet, crop));
            }
         }

         for (DelightAddonBridge.BridgedCrop cropx : this.bridgedCrops) {
            if (DelightCropSelectScreen.bridgedMatches(query, cropx)) {
               list.addChild(DelightCropSelectScreen.bridgedButton(packet, cropx));
            }
         }
      }
   }
}
