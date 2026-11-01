# AGENTS.md

## 这个仓库是什么

GTNH 1.7.10 的 Forge 模组 `LFTR: Price Drop`（modid `lftrpricedrop`）。它不改 GT++ 的源码，只在加载结束时用 GT5U 的配方 API 增删/替换 LFTR 生产线上的一批运行配方。

## 构建

需要 JDK 17+ 启动 wrapper；daemon 由 `gradle/gradle-daemon-jvm.properties` 钉在 JDK 25。

```bash
export JAVA_HOME="<你的 JDK 25 路径>"
./gradlew build          # 编译 + Spotless/Checkstyle
./gradlew runClient      # 开发回路：日志里看 36 条命中
```

网络受限时用仓库内的 `GRADLE_USER_HOME`：

```bash
GRADLE_USER_HOME="$PWD/.gradle-home" ./gradlew build --offline
```

## 改动的结构

`src/main/java/com/fangkishi/lftrpricedrop/lftr/` 下四个部件，各自只做一件事（另有两个辅助：`LftrPopularizer` 是入口、`LftrLog` 是唯一的日志出口）：

- `RecipeChanges`：三组改动的清单，纯数据，不引用任何 Minecraft / GT 类型。**要看"这票改了什么"，只看这一个文件。**
- `RecipeFinder`：按"配方表 + 流体输入集合 + 时长 + EU/t"定位一条既有配方。
- `RecipeExecutor`：新增与替换，失败即保持原样。
- `GtRecipes`：唯一接触 GT 配方表的地方（符号解析、重建、增删）。

## 硬规则

1. **改 `RecipeChanges` 就要同步改 `README.md` 与 `README.en.md` 的改动表**。那份表是模组唯一的产品说明，过期比没有更糟。
2. `LftrLog` 的格式串是验收缝，只允许原样搬运，不允许"顺手改好看"。
3. 替换失败必须保持原配方，加载绝不失败。
4. 不引入单元测试源集：这个模组的产物是配方数据，唯一能断言的缝是加载日志。

## 已知缺口（1.0.0，未修）

规则 3 在**异常路径**上还没有完全成立，1.0.0 的三处缺口都是「已经改了一部分，日志却记未命中」：

- `RecipeExecutor.swap`：`remove(source)` 之后 `add(replacement)` 若抛异常，旧配方不会自动放回（`run()` 的 catch 只记一行未命中）。
- `RecipeExecutor.replaceVariants`：B2 覆盖两张配方表，逐表替换且不整体回滚；`addVariants` 有回滚，这里没有。
- `RecipeExecutor.buildCost`：两个方块各有工作台 + 装配机两种形式，第二种形式失败时不撤销第一种。

判据：这些都是异常/部分失败路径，正常加载（36/36 命中）不经过它们。修它们要重跑 `runClient` 验收，所以留到 1.0.1，不要在没有重跑验收的情况下顺手改。

