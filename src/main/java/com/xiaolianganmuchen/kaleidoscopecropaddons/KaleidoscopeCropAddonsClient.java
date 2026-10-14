package com.xiaolianganmuchen.kaleidoscopecropaddons;

import com.xiaolianganmuchen.kaleidoscopecropaddons.client.DelightModConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(
   value = "kaleidoscopecropaddons",
   dist = {Dist.CLIENT}
)
public class KaleidoscopeCropAddonsClient {
   public KaleidoscopeCropAddonsClient(ModContainer container) {
      container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory)(modContainer, parent) -> new DelightModConfigScreen(parent));
   }
}
