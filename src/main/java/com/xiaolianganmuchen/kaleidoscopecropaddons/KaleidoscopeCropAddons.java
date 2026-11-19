package com.xiaolianganmuchen.kaleidoscopecropaddons;

import com.nsukcropapi.CropAddons;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 森罗物语：作物附属。
 *
 * <p>本模组不含任何作业逻辑，只提供作物定义数据。农田盒的作物注册表、五阶段作业流程、
 * 存档字段与选择界面全部由 NSUK 作物 API（{@code nsukcropapi}）统一提供。</p>
 */
@Mod(KaleidoscopeCropAddons.MODID)
public class KaleidoscopeCropAddons {

    public static final String MODID = "kaleidoscopecropaddons";

    /** 作物 JSON 所在目录名，对应 {@code data/kaleidoscopecropaddons/kaleidoscope_crops/}。 */
    private static final String CROP_JSON_DIR = "kaleidoscope_crops";

    public KaleidoscopeCropAddons(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // anchor 用本模组的类，API 借它的类加载器读取 jar 内资源；
        // 不给 builtinFiles，API 会自动扫描上述目录下的全部 JSON。
        event.enqueueWork(() -> CropAddons.bootstrap(KaleidoscopeCropAddons.class, MODID, CROP_JSON_DIR));
    }
}
