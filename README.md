# 森罗物语：作物附属（Kaleidoscope Crop Addons）

让 NSUK（New-Sim-U-Kraft）的雇佣农民通过农田工作盒种植**森罗物语系列**模组的作物 —— 既包括能种在原版耕地上的，也包括需要特殊土壤的，以及带藤架的葡萄类。

## 环境要求

| 项目 | 要求 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 或更高（21.2 之前） |
| New-Sim-U-Kraft（`simukraft`） | 1.0.0 或更高，**必需前置** |
| 森罗物语各模组 | 可选，装了才会出现对应作物 |

## 从源码构建

```bash
gradlew build
```

> ⚠️ **注意**：`libs/` 目录下的第三方依赖**不随仓库分发**，本地构建前请自行放入：
>
> | 文件 | 说明 |
> |---|---|
> | `New-Sim-U-Kraft-2.2.0-neoforge-1.21.1.jar` | NSUK 本体（必需） |
> | `ldlib2-neoforge-1.21.1-2.2.40-all.jar` | LDLib2（森罗物语前置） |
> | `taffy-1.1.4.jar` | Taffy（森罗物语前置） |
> | `yoga-1.0.0.jar` | Yoga（森罗物语前置） |
> | `ftb-ultimine-stub.jar` | FTB Ultimine 的 stub |
>
> 这些是别人的模组，请按各自许可自行获取。

## 支持的作物

| 森罗模组 | 作物 | 需要的土壤 |
|---|---|---|
| 厨房 `kaleidoscope_cookery` | 番茄、辣椒、生菜 | 耕地（泥土类） |
| 国味 `kaleidoscope_chinesefood` | 茄子 | 耕地（泥土类） |
| 炙烤 `kaleidoscope_grilling` | 油菜、洋葱、红薯、折耳根 | 耕地（泥土类） |
| 次元酒窖 `kaleidoscope_dim_wine` | 啤酒花 | 耕地（泥土类） |
| 次元酒窖 `kaleidoscope_dim_wine` | 绯红葡萄、诡异葡萄 | 绯红菌岩 / 诡异菌岩，加酒馆藤架方块 |
| 暮色 `kaleidoscope_twilight` | 蕨菜 | 耕地（泥土类） |
| 果园 `kaleidoscope_orchard` | 草莓、蓝莓 | 耕地（泥土类） |
| 酒馆 `kaleidoscopetavern` | 绿葡萄 | 耕地（泥土类），加酒馆藤架方块 |
| 酒馆 `kaleidoscopetavern` | 冰葡萄 | 冰 / 浮冰 / 蓝冰 / 雪块 |
| 酒馆 `kaleidoscopetavern` | 黄金葡萄 | 下界岩（岩浆块会烫伤 NPC，已排除） |
| 末地 `kaleidoscope_end` | 末影薄荷 | 末地石 |
| 下界 `kaleidoscope_nether` | 灵魂辣椒、迷毒果 | 灵魂沙 / 灵魂土 |
| 幽匿 `kaleidoscope_sculk` | 回响作物 | 幽匿催发体 |

箱子里备好对应种子与土壤，农民就会自己动手。

## 功能特性

- **自动铺土**：需要特殊土壤的作物由农民自行铺土、播种、施骨粉、收割；四角立柱下方不铺土（那里不结藤蔓）
- **葡萄藤架**：先把整片藤架铺满，再插葡萄藤；成熟后按「右键收割」掉落并复位为 0% 继续生长，而不是把果实方块拆掉
- **形状自愈**：藤架方块放下后按当前邻居重算一次形状，修正角落立柱缺失 / 横梁朝外支棱的问题
- **状态文字与动作一致**：翻地→「铺设中：土壤」、立柱横梁→「搭建中：藤架」、插藤→「播种中：葡萄藤」、缺料→「等待藤架 / 等待种子：xxx」
- **作物列表分组折叠**：同一来源有多个作物时默认收起，点击展开（选择会记住）
- **按来源开关**：配置页可逐模组启用/关闭，也可单独禁用某个作物 id

## 新增其它森罗模组的作物

在 `src/main/resources/data/kaleidoscopecropaddons/kaleidoscope_crops/` 放一份 JSON 即可，例如：

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

- `soil_tag`（方块标签）与 `soil_blocks`（直接列方块 id）**二选一**即可；可用的土壤物品会自动由标签推导
- `layout` 为 `trellis` 时还需 `fruit_block` / `fruit_item` / `support_block`，必要时加 `plant_mature`
- `soil_exclude` 可把标签里不想用的方块排除掉（例如黄金葡萄排除岩浆块）
- 语言键 `source.kaleidoscopecropaddons.kaleidoscope_xxx` 决定列表里显示的名字

## 许可

GPL-3.0，见 [LICENSE](./LICENSE)。
