# 森罗物语：作物附属（Kaleidoscope Crop Addons）

> 本说明提供中英文双语。中文在上，英文紧随其后。
> This document is bilingual (Chinese / English). Chinese text first, followed by its English translation.

让 NSUK（New-Sim-U-Kraft）的雇佣农民通过农田工作盒种植**森罗物语系列**模组的作物 —— 既包括能种在原版耕地上的，也包括需要特殊土壤的，以及带藤架的葡萄类。

Lets the hired farmers of NSUK (New-Sim-U-Kraft) grow crops from the **Kaleidoscope (森罗物语) series** mods through the Farm Work Box — covering those that grow on vanilla farmland, those requiring special soils, and trellis-grown grape varieties.

## 版本说明 / Version Note

**2.0.0 起本模组改为纯数据模组。** 作业逻辑（铺土、播种、骨粉、收割、补种、存档字段、选择界面）全部上移到 [NSUK 作物 API](https://github.com/anmuchen/NSUK-CropAddons-API)；本模组只剩两个类与一叠作物 JSON，不再自带 mixin。

1.3.x 及更早版本是独立实现，内含 20 余个类与 12 个 mixin。两者**存档不通用**，请勿混装。

> **Since 2.0.0 this is a data-only mod.** All work logic (soiling, sowing, bonemealing, harvesting, replanting, save fields, selection UI) moved into [NSUK Crop Addons API](https://github.com/anmuchen/NSUK-CropAddons-API). This mod now contains just two classes and a set of crop JSONs, with no mixins of its own.
>
> 1.3.x and earlier were standalone implementations with 20+ classes and 12 mixins. **Save data is not compatible between the two** — do not mix them.

## 环境要求 / Environment Requirements

| 项目 / Item | 要求 / Requirement |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 或更高（21.2 之前 / before 21.2） |
| NSUK 作物 API（`nsukcropapi`） | 1.0.0 或更高，必需前置（or newer, **required**） |
| NSUK 本体（`simukraft`） | 1.0.0 或更高，必需前置（or newer, **required**） |
| 森罗物语各模组 Mods | 可选，装了才会出现对应作物（optional; crops appear only if installed） |

## 支持的作物 / Supported Crops

| 森罗模组 / Mod | 作物 / Crops | 需要的土壤 / Required Soil |
|---|---|---|
| 厨房 `kaleidoscope_cookery` | 番茄、辣椒、生菜 / Tomato, Chili, Lettuce | 耕地（泥土类 / Farmland, dirt-type） |
| 国味 `kaleidoscope_chinesefood` | 茄子 / Eggplant | 耕地（泥土类 / Farmland, dirt-type） |
| 炙烤 `kaleidoscope_grilling` | 油菜、洋葱、红薯、折耳根 / Rapeseed, Onion, Sweet Potato, Fish Mint | 耕地（泥土类 / Farmland, dirt-type） |
| 次元酒窖 `kaleidoscope_dim_wine` | 啤酒花 / Hops | 耕地（泥土类 / Farmland, dirt-type） |
| 次元酒窖 `kaleidoscope_dim_wine` | 绯红葡萄、诡异葡萄 / Crimson Grape, Warped Grape | 绯红菌岩 / 诡异菌岩，加酒馆藤架方块（Crimson/Warped Nylium + Tavern trellis block） |
| 暮色 `kaleidoscope_twilight` | 蕨菜 / Fern | 耕地（泥土类 / Farmland, dirt-type） |
| 果园 `kaleidoscope_orchard` | 草莓、蓝莓 / Strawberry, Blueberry | 耕地（泥土类 / Farmland, dirt-type） |
| 酒馆 `kaleidoscopetavern` | 绿葡萄 / Green Grape | 耕地（泥土类 / Farmland, dirt-type）+ 酒馆藤架方块（Tavern trellis block） |
| 酒馆 `kaleidoscopetavern` | 冰葡萄 / Ice Grape | 冰 / 浮冰 / 蓝冰 / 雪块（Ice / Packed Ice / Blue Ice / Snow Block） |
| 酒馆 `kaleidoscopetavern` | 黄金葡萄 / Gold Grape | 下界岩（岩浆块会烫伤 NPC，已排除 / Netherrack; magma block excluded — it burns NPCs） |
| 末地 `kaleidoscope_end` | 末影薄荷 / Ender Mint | 末地石 / End Stone |
| 下界 `kaleidoscope_nether` | 灵魂辣椒、迷毒果 / Soul Chili, Toxic Berry | 灵魂沙 / 灵魂土（Soul Sand / Soul Soil） |
| 幽匿 `kaleidoscope_sculk` | 回响作物 / Echo Crop | 幽匿催发体 / Sculk Catalyst |

共 10 个来源、21 种作物。箱子里备好对应种子与土壤，农民就会自己动手。

10 sources, 21 crops. Stock the matching seeds and soil in the box, and the farmer will get to work.

## 功能特性 / Features

- **自动铺土 / Auto-soiling**：需要特殊土壤的作物由农民自行铺土、播种、施骨粉、收割；四角立柱下方不铺土（那里不结藤蔓）
  - *Crops needing special soil are automatically tilled, seeded, bonemealed, and harvested by the farmer. The four corner posts are not tilled (no vines grow there).*
- **葡萄藤架 / Grape trellis**：先把整片藤架铺满，再插葡萄藤；成熟后按「右键收割」掉落并复位为 0% 继续生长，而不是把果实方块拆掉
  - *The trellis is filled first, then grape vines are planted. When ripe, a right-click harvest drops the fruit and resets growth to 0% to regrow — the fruit block itself is not removed.*
- **形状自愈 / Self-healing shape**：藤架方块放下后按当前邻居重算一次形状，修正角落立柱缺失 / 横梁朝外支棱的问题
  - *Once placed, trellis blocks recompute their shape from current neighbors, fixing missing corner posts or outward-jutting crossbeams.*
- **状态文字与动作一致 / Status text matches action**：翻地→「铺设中：土壤」、立柱横梁→「搭建中：藤架」、插藤→「播种中：葡萄藤」、缺料→「等待藤架 / 等待种子：xxx」
  - *Status text matches the action: tilling → "Laying: soil", posts/beams → "Building: trellis", planting vines → "Sowing: grape vine", missing materials → "Waiting for trellis / Waiting for seed: xxx".*
- **作物列表分组折叠 / Grouped, collapsible crop list**：同一来源有多个作物时默认收起，点击展开（选择会记住）
  - *When a source has multiple crops, the list is collapsed by default; click to expand (selection is remembered).*
- **按来源开关 / Per-source toggles**：配置页可逐模组启用/关闭，也可单独禁用某个作物 id
  - *The config screen lets you enable/disable each mod, or disable an individual crop id.*

## 从源码构建 / Building from Source

```bash
gradlew build
```

> ⚠️ **注意 / Note**：`libs/` 目录下的第三方依赖**不随仓库分发**，本地构建前请自行放入：
>
> ⚠️ Third-party dependencies in `libs/` are **not distributed with this repository**. Place them locally before building:
>
> | 文件 / File | 说明 / Description |
> |---|---|
> | `nsukcropapi-1.1.1.jar` | NSUK 作物 API（必需 / required），见 [Releases](https://github.com/anmuchen/NSUK-CropAddons-API/releases) |

产物 / Output：`build/libs/kaleidoscopecropaddons-2.0.0.jar`

## 新增其它森罗模组的作物 / Adding Crops from Other Kaleidoscope Mods

在 `src/main/resources/data/kaleidoscopecropaddons/kaleidoscope_crops/` 放一份 JSON 即可，例如：

Drop a JSON file under `src/main/resources/data/kaleidoscopecropaddons/kaleidoscope_crops/`, for example:

```json
{
  "mod": "kaleidoscope_xxx",
  "prefix": "kx",
  "label": "source.kaleidoscopecropaddons.kaleidoscope_xxx",
  "discover": false,
  "crops": [
    {
      "id": "example",
      "seed": "kaleidoscope_xxx:example_seed",
      "plant": "kaleidoscope_xxx:example_crop",
      "soil_tag": "kaleidoscope_xxx:can_grow_example",
      "soil_item": "minecraft:dirt"
    }
  ]
}
```

- `soil_tag`（方块标签 / block tag）与 `soil_blocks`（直接列方块 id / list block ids directly）**二选一**即可；可用的土壤物品会自动由标签推导 / choose one; valid soil items are auto-derived from the tag
- `layout` 为 `trellis` 时还需 `fruit_block` / `fruit_item` / `support_block`，必要时加 `plant_mature` / when `layout` is `trellis`, you also need `fruit_block` / `fruit_item` / `support_block`, plus `plant_mature` if required
- `soil_exclude` 可把标签里不想用的方块排除掉（例如黄金葡萄排除岩浆块）/ `soil_exclude` removes unwanted blocks from the tag (e.g. gold grapes exclude magma blocks)
- 语言键 `source.kaleidoscopecropaddons.kaleidoscope_xxx` 决定列表里显示的名字；也可在 JSON 里用 `name_key` 直接指定 / the language key `source.kaleidoscopecropaddons.kaleidoscope_xxx` sets the name shown in the list; alternatively set `name_key` in the JSON
- 字段的完整定义与显示名解析顺序见 [NSUK 作物 API 的 README](https://github.com/anmuchen/NSUK-CropAddons-API#readme) / for full field definitions and display-name resolution order, see the API's README

## 许可 / License

GPL-3.0，见 [LICENSE](./LICENSE)。

GPL-3.0, see [LICENSE](./LICENSE).
