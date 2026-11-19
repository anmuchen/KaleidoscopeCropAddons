package com.xiaolianganmuchen.kaleidoscopecropaddons;

import com.nsukcropapi.client.CropApiConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端入口：把本模组的「配置」按钮指到 API 提供的作物配置页。
 *
 * <p>配置页本身（逐来源开关、逐作物禁用）由 API 实现，本模组不再自带界面代码。</p>
 */
@Mod(value = KaleidoscopeCropAddons.MODID, dist = Dist.CLIENT)
public final class KaleidoscopeCropAddonsClient {

    public KaleidoscopeCropAddonsClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, parent) -> new CropApiConfigScreen(parent));
    }
}
