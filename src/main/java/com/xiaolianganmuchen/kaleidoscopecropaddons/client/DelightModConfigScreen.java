package com.xiaolianganmuchen.kaleidoscopecropaddons.client;

import com.xiaolianganmuchen.kaleidoscopecropaddons.config.CustomCropStorage;
import com.xiaolianganmuchen.kaleidoscopecropaddons.config.DelightCropConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class DelightModConfigScreen extends Screen {
   private static final int PANEL_WIDTH = 340;
   private static final int PANEL_HEIGHT = 220;
   private static final int ROW_HEIGHT = 20;
   private final Screen parent;
   private int tab;
   private int scroll;
   private int panelX;
   private int panelY;
   private Button tabSources;
   private Button tabCustom;
   private Button addCrop;
   private Button done;
   private final List<Button> dynamicButtons = new ArrayList<>();

   public DelightModConfigScreen(Screen parent) {
      super(Component.translatable("gui.kaleidoscopecropaddons.config.title"));
      this.parent = parent;
   }

   protected void init() {
      this.panelX = (this.width - 340) / 2;
      this.panelY = (this.height - 220) / 2;
      this.clearWidgets();
      this.dynamicButtons.clear();
      this.tabSources = (Button)this.addRenderableWidget(Button.builder(Component.translatable("gui.kaleidoscopecropaddons.config.tab.sources"), button -> {
         this.tab = 0;
         this.scroll = 0;
         this.rebuild();
      }).bounds(this.panelX + 12, this.panelY + 28, 150, 18).build());
      this.tabCustom = (Button)this.addRenderableWidget(Button.builder(Component.translatable("gui.kaleidoscopecropaddons.config.tab.custom"), button -> {
         this.tab = 1;
         this.scroll = 0;
         this.rebuild();
      }).bounds(this.panelX + 340 - 162, this.panelY + 28, 150, 18).build());
      this.done = (Button)this.addRenderableWidget(
         Button.builder(Component.translatable("gui.done"), button -> this.onClose()).bounds(this.panelX + 340 - 88, this.panelY + 220 - 26, 76, 18).build()
      );
      this.addCrop = (Button)this.addRenderableWidget(Button.builder(Component.translatable("gui.kaleidoscopecropaddons.config.add"), button -> {
         if (this.minecraft != null) {
            this.minecraft.setScreen(new DelightAddCropScreen(this));
         }
      }).bounds(this.panelX + 12, this.panelY + 220 - 26, 88, 18).build());
      this.rebuild();
   }

   private void rebuild() {
      for (Button button : this.dynamicButtons) {
         this.removeWidget(button);
      }

      this.dynamicButtons.clear();
      this.addCrop.visible = this.tab == 1;
      this.tabSources.active = this.tab != 0;
      this.tabCustom.active = this.tab != 1;
      int contentTop = this.panelY + 52;
      int contentBottom = this.panelY + 220 - 34;
      int visible = Math.max(1, (contentBottom - contentTop) / 20);
      if (this.tab == 0) {
         List<DelightModConfigScreen.Row> rows = this.sourceRows();
         this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, rows.size() - visible));
         int y = contentTop;

         for (int index = this.scroll; index < rows.size() && y + 20 <= contentBottom; index++) {
            DelightModConfigScreen.Row row = rows.get(index);
            Button toggle = Button.builder(row.right, button -> {
               row.action.run();
               this.rebuild();
            }).bounds(this.panelX + 340 - 86, y + 1, 70, 18).build();
            this.dynamicButtons.add((Button)this.addRenderableWidget(toggle));
            y += 20;
         }
      } else {
         List<CustomCropStorage.Entry> entries = CustomCropStorage.entries();
         this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, entries.size() - visible));
         int y = contentTop;

         for (int index = this.scroll; index < entries.size() && y + 20 <= contentBottom; index++) {
            CustomCropStorage.Entry entry = entries.get(index);
            Button delete = Button.builder(Component.translatable("gui.kaleidoscopecropaddons.config.remove"), button -> {
               CustomCropStorage.remove(entry.id());
               this.rebuild();
            }).bounds(this.panelX + 340 - 86, y + 1, 70, 18).build();
            this.dynamicButtons.add((Button)this.addRenderableWidget(delete));
            y += 20;
         }
      }
   }

   private List<DelightModConfigScreen.Row> sourceRows() {
      List<DelightModConfigScreen.Row> rows = new ArrayList<>();
      rows.add(
         new DelightModConfigScreen.Row(
            Component.translatable("kaleidoscopecropaddons.configuration.autoDiscovery"),
            toggleLabel(DelightCropConfig.autoDiscovery()),
            () -> DelightCropConfig.setAutoDiscovery(!DelightCropConfig.autoDiscovery())
         )
      );

      for (DelightCropConfig.SourceToggle toggle : DelightCropConfig.sourceToggles()) {
         rows.add(
            new DelightModConfigScreen.Row(Component.translatable(toggle.labelKey()), toggleLabel(toggle.enabled()), () -> toggle.setEnabled(!toggle.enabled()))
         );
      }

      return rows;
   }

   private static Component toggleLabel(boolean enabled) {
      return Component.translatable(enabled ? "gui.kaleidoscopecropaddons.config.on" : "gui.kaleidoscopecropaddons.config.off");
   }

   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(0, 0, this.width, this.height, -1072689128);
      graphics.fill(this.panelX, this.panelY, this.panelX + 340, this.panelY + 220, -266198488);
      drawBorder(graphics, this.panelX, this.panelY, 340, 220, -2047904);
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelY + 10, -1518454);
      int contentTop = this.panelY + 52;
      int contentBottom = this.panelY + 220 - 34;
      graphics.fill(this.panelX + 10, contentTop - 4, this.panelX + 340 - 10, contentBottom, -15329764);
      drawBorder(graphics, this.panelX + 10, contentTop - 4, 320, contentBottom - (contentTop - 4), -11908526);
      graphics.enableScissor(this.panelX + 12, contentTop, this.panelX + 340 - 12, contentBottom);
      if (this.tab == 0) {
         List<DelightModConfigScreen.Row> rows = this.sourceRows();
         int y = contentTop;
         int visible = Math.max(1, (contentBottom - contentTop) / 20);
         this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, rows.size() - visible));

         for (int index = this.scroll; index < rows.size() && y + 20 <= contentBottom; index++) {
            int var10003 = this.panelX + 18;
            graphics.drawString(this.font, rows.get(index).left, var10003, y + 6, -1250068, false);
            y += 20;
         }
      } else {
         List<CustomCropStorage.Entry> entries = CustomCropStorage.entries();
         if (entries.isEmpty()) {
            graphics.drawWordWrap(
               this.font, Component.translatable("gui.kaleidoscopecropaddons.config.custom.empty"), this.panelX + 18, contentTop + 8, 300, -4342339
            );
         } else {
            int y = contentTop;
            int visible = Math.max(1, (contentBottom - contentTop) / 20);
            this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, entries.size() - visible));

            for (int index = this.scroll; index < entries.size() && y + 20 <= contentBottom; index++) {
               CustomCropStorage.Entry entry = entries.get(index);
               String name = !entry.nameZh().isBlank() ? entry.nameZh() : (!entry.nameEn().isBlank() ? entry.nameEn() : entry.seed());
               graphics.drawString(this.font, name, this.panelX + 18, y + 3, -1250068, false);
               graphics.drawString(this.font, entry.seed(), this.panelX + 18, y + 12, -6645094, false);
               y += 20;
            }
         }
      }

      graphics.disableScissor();
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (mouseX >= (double)this.panelX && mouseX <= (double)(this.panelX + 340) && mouseY >= (double)this.panelY && mouseY <= (double)(this.panelY + 220)) {
         this.scroll = this.scroll - (int)Math.signum(scrollY);
         this.rebuild();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   public boolean isPauseScreen() {
      return true;
   }

   private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private static record Row(Component left, Component right, Runnable action) {
   }
}
