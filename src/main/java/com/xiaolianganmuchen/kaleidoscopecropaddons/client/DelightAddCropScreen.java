package com.xiaolianganmuchen.kaleidoscopecropaddons.client;

import com.xiaolianganmuchen.kaleidoscopecropaddons.config.CustomCropStorage;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class DelightAddCropScreen extends Screen {
   private static final int PANEL_WIDTH = 320;
   private static final int PANEL_HEIGHT = 196;
   private final Screen parent;
   private EditBox seedBox;
   private EditBox plantBox;
   private EditBox nameZhBox;
   private EditBox nameEnBox;
   private Checkbox allowNonCrop;
   private Component status = Component.empty();
   private int panelX;
   private int panelY;

   public DelightAddCropScreen(Screen parent) {
      super(Component.translatable("gui.kaleidoscopecropaddons.config.add.title"));
      this.parent = parent;
   }

   protected void init() {
      this.panelX = (this.width - 320) / 2;
      this.panelY = (this.height - 196) / 2;
      this.seedBox = this.field(this.panelY + 40, "gui.kaleidoscopecropaddons.config.field.seed", "modid:crop_seeds");
      this.plantBox = this.field(this.panelY + 68, "gui.kaleidoscopecropaddons.config.field.plant", "");
      this.nameZhBox = this.field(this.panelY + 96, "gui.kaleidoscopecropaddons.config.field.name_zh", "");
      this.nameEnBox = this.field(this.panelY + 124, "gui.kaleidoscopecropaddons.config.field.name_en", "");
      this.allowNonCrop = (Checkbox)this.addRenderableWidget(
         Checkbox.builder(Component.translatable("gui.kaleidoscopecropaddons.config.field.allow_non_crop"), this.font)
            .pos(this.panelX + 16, this.panelY + 150)
            .selected(false)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.translatable("gui.kaleidoscopecropaddons.config.save"), button -> this.save())
            .bounds(this.panelX + 320 - 168, this.panelY + 196 - 26, 72, 18)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.translatable("gui.cancel"), button -> this.onClose()).bounds(this.panelX + 320 - 88, this.panelY + 196 - 26, 72, 18).build()
      );
      this.setInitialFocus(this.seedBox);
   }

   private EditBox field(int y, String hintKey, String value) {
      EditBox box = new EditBox(this.font, this.panelX + 108, y, 196, 18, Component.translatable(hintKey));
      box.setMaxLength(128);
      box.setValue(value);
      box.setHint(Component.translatable(hintKey));
      return (EditBox)this.addRenderableWidget(box);
   }

   private void save() {
      String seed = this.seedBox.getValue().trim();
      if (!seed.isBlank() && ResourceLocation.tryParse(seed) != null) {
         String plant = this.plantBox.getValue().trim();
         if (!plant.isBlank() && ResourceLocation.tryParse(plant) == null) {
            this.status = Component.translatable("gui.kaleidoscopecropaddons.config.error.plant");
         } else {
            boolean added = CustomCropStorage.add(
               new CustomCropStorage.Entry(
                  "", seed, plant, List.of(), this.allowNonCrop.selected(), this.nameZhBox.getValue().trim(), this.nameEnBox.getValue().trim()
               )
            );
            if (!added) {
               this.status = Component.translatable("gui.kaleidoscopecropaddons.config.error.seed");
            } else {
               this.onClose();
            }
         }
      } else {
         this.status = Component.translatable("gui.kaleidoscopecropaddons.config.error.seed");
      }
   }

   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(0, 0, this.width, this.height, -1072689128);
      graphics.fill(this.panelX, this.panelY, this.panelX + 320, this.panelY + 196, -266198488);
      graphics.fill(this.panelX, this.panelY, this.panelX + 320, this.panelY + 1, -2047904);
      graphics.fill(this.panelX, this.panelY + 196 - 1, this.panelX + 320, this.panelY + 196, -2047904);
      graphics.fill(this.panelX, this.panelY, this.panelX + 1, this.panelY + 196, -2047904);
      graphics.fill(this.panelX + 320 - 1, this.panelY, this.panelX + 320, this.panelY + 196, -2047904);
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelY + 10, -1518454);
      graphics.drawString(
         this.font, Component.translatable("gui.kaleidoscopecropaddons.config.field.seed"), this.panelX + 16, this.panelY + 45, -1250068, false
      );
      graphics.drawString(
         this.font, Component.translatable("gui.kaleidoscopecropaddons.config.field.plant"), this.panelX + 16, this.panelY + 73, -1250068, false
      );
      graphics.drawString(
         this.font, Component.translatable("gui.kaleidoscopecropaddons.config.field.name_zh"), this.panelX + 16, this.panelY + 101, -1250068, false
      );
      graphics.drawString(
         this.font, Component.translatable("gui.kaleidoscopecropaddons.config.field.name_en"), this.panelX + 16, this.panelY + 129, -1250068, false
      );
      graphics.drawCenteredString(this.font, this.status, this.width / 2, this.panelY + 196 - 42, -30584);
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   public boolean isPauseScreen() {
      return true;
   }
}
