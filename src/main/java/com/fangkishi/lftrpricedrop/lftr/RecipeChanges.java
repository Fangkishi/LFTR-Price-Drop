package com.fangkishi.lftrpricedrop.lftr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 三组改动的清单：只有数据与词汇表，不引用任何 Minecraft / GT 类型。
 *
 * <p>
 * 别和「配方表（recipe map）」混：后者是 GT 按机器类型分组的运行配方集合（{@link MapKey} 中的名字指的就是它），
 * 这里装的是"这一票要改哪些配方"。
 *
 * <p>
 * A 组一条 = 目标方块 + 一组形式（工作台 / 装配机 / 装配线），日志按方块计一条而不是按配方条数；
 * B 组一条 = 动作 + 若干配方表 + 选择器 + 要改成什么；C 组一条 = 配方表 + 选择器 + 新的时长与 EU/t。
 * 要找"这一票改了什么"，只需要看这一个文件。
 *
 * <p>
 * 本包内的值对象只暴露包内可见的 final 字段，不再写一排 getter：这些类型只在 lftr 包内流转，
 * getter 只是把字段名重复一遍。
 */
public final class RecipeChanges {

    /** 一条改动；日志只用到它的描述。 */
    public interface Entry {

        String description();
    }

    /** 三组改动。组头与汇总行用 display()，每条日志用 tag。 */
    public enum Group {

        BUILD_COST("A组", "方块造价"),
        FLUID_PATH("B组", "工业氢氟酸"),
        TIMING("C组", "时长与电压");

        /** 每条日志里的组标识，例如「A组」。 */
        final String tag;
        /** 组头与汇总行里的组名，例如「方块造价」。 */
        private final String heading;

        Group(String tag, String heading) {
            this.tag = tag;
            this.heading = heading;
        }

        public String display() {
            return tag + " " + heading;
        }
    }

    /** A 组的目标方块。description 直接进日志。 */
    public enum BlockKey {

        LFTR_CONTROLLER("LFTR 主方块"),
        REACTOR_CASING_I("反应堆外壳 I"),
        REACTOR_CASING_II("反应堆外壳 II"),
        RPU_I("反应堆处理单元 I"),
        RPU_II("反应堆处理单元 II"),
        COLD_TRAP_I("冷阱 I"),
        COLD_TRAP_II("冷阱 II"),
        SPARGE_TOWER_CONTROLLER("喷淋塔主方块"),
        SPARGE_TOWER_CASING("喷淋塔外墙"),
        FUEL_REFINERY("燃料处理厂"),
        REFINERY_CASING_INTERNAL("燃料处理厂内壁"),
        REFINERY_CASING_EXTERNAL("燃料处理厂外壳"),
        REFINERY_CASING_STRUCTURAL("燃料处理厂结构方块");

        final String description;

        BlockKey(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    /** 主线材料，由 GtRecipes 映射到 GT 的 Materials。 */
    public enum MaterialKey {
        TITANIUM,
        STAINLESS_STEEL,
        TUNGSTEN_STEEL,
        STEEL,
        NAQUADAH,
        YTTRIUM_BARIUM_CUPRATE,
        PLATINUM
    }

    /** 材料形态，由 GtRecipes 映射到 GT 的 OrePrefixes。 */
    public enum MaterialForm {
        PLATE,
        PLATE_DOUBLE,
        PLATE_DENSE,
        FRAME,
        GEAR,
        SCREW,
        RING,
        PIPE_HUGE,
        CABLE_12X,
        WIRE_FINE,
        FOIL
    }

    /** 配方里的 GT5U 主线物品，由 GtRecipes 映射到 ItemList。 */
    public enum ItemKey {
        HULL_IV,
        FIELD_GENERATOR_LV,
        FIELD_GENERATOR_HV,
        FIELD_GENERATOR_IV,
        ROBOT_ARM_IV,
        ROBOT_ARM_ZPM,
        MACHINE_IV_CHEMICAL_REACTOR,
        MACHINE_IV_DISTILLERY,
        CASING_EV,
        CASING_IV,
        CASING_FROSTPROOF,
        SUPER_TANK_IV,
        ELITE_CIRCUIT,
        LFTR_CONTROL_CIRCUIT,
        COMPUTER_CUBE,
        THORIUM_232_PLATE,
        GTPP_CASING_HV,
        GTPP_CASING_EV,
        REACTOR_PLATING_HEAT,
        CELL_EMPTY,
        HF_CELLS
    }

    /** 配方里用到的流体，由 GtRecipes 映射到 FluidStack。 */
    public enum FluidKey {
        MOLTEN_STAINLESS_STEEL,
        MOLTEN_TUNGSTEN_STEEL,
        MOLTEN_TITANIUM,
        INDUSTRIAL_HF,
        HF,
        AMMONIUM_TETRAFLUOROBERYLLATE,
        URANIUM_HEXAFLUORIDE,
        LIF_BEF2,
        HYDROGEN,
        LITHIUM_FLUORIDE,
        BERYLLIUM_FLUORIDE,
        ZIRCONIUM_TETRAFLUORIDE,
        THORIUM_TETRAFLUORIDE,
        URANIUM_TETRAFLUORIDE,
        URANIUM_235_MOLTEN,
        LIF_THF4,
        LIF_BEF2_THF4,
        SPARGED_LIF_THF4,
        SPARGED_LIF_BEF2_THF4,
        LIF_BEF2_UF4,
        LIF_BEF2_UF4FP,
        SPARGED_LIF_BEF2_UF4FP,
        UF6F2,
        UF6F2_FP,
        IMPURE_MOLTEN_SALT_BASE,
        HELIUM,
        FLUORINE
    }

    /** 改动涉及的 GT 配方表。mapName 是 GT 侧的名字，只用于未命中日志。 */
    public enum MapKey {

        CHEMICAL_BATH("chemicalBathRecipes"),
        CHEMICAL_REACTOR("chemicalReactorRecipes"),
        MULTIBLOCK_CHEMICAL_REACTOR("multiblockChemicalReactorRecipes"),
        MIXER("mixerRecipes"),
        BLAST_FURNACE("blastFurnaceRecipes"),
        CHEMICAL_PLANT("chemicalPlantRecipes"),
        FISSION_FUEL_PROCESSING("fissionFuelProcessingRecipes"),
        REACTOR_PROCESSING_UNIT("reactorProcessingUnitRecipes"),
        COLD_TRAP("coldTrapRecipes"),
        DISTILLATION_TOWER("distillationTowerRecipes"),
        SPARGE_TOWER("spargeTowerRecipes");

        final String mapName;

        MapKey(String mapName) {
            this.mapName = mapName;
        }
    }

    /** B 组的动作：输入不同的变体只新增，副产不同的变体替换原配方。 */
    public enum Action {
        ADD,
        REPLACE
    }

    /** A 组的配方形式：原配方在哪里，新配方就放在哪里。 */
    public enum CraftKind {
        SHAPED,
        ASSEMBLER,
        ASSEMBLY_LINE
    }

    /** 配方里的一个物品槽：主线材料 + 形态、主线物品、集成芯片或另一台方块。 */
    public static final class Ingredient {

        final MaterialKey material;
        final MaterialForm form;
        final ItemKey item;
        final BlockKey block;
        final int circuit;
        final int amount;

        private Ingredient(MaterialKey material, MaterialForm form, ItemKey item, BlockKey block, int circuit,
            int amount) {
            this.material = material;
            this.form = form;
            this.item = item;
            this.block = block;
            this.circuit = circuit;
            this.amount = amount;
        }

        public static Ingredient part(MaterialKey material, MaterialForm form, int amount) {
            return new Ingredient(material, form, null, null, 0, amount);
        }

        public static Ingredient item(ItemKey item, int amount) {
            return new Ingredient(null, null, item, null, 0, amount);
        }

        public static Ingredient block(BlockKey block, int amount) {
            return new Ingredient(null, null, null, block, 0, amount);
        }

        /** GT 集成芯片，number 即电路编号。 */
        public static Ingredient circuit(int number, int amount) {
            return new Ingredient(null, null, null, null, number, amount);
        }

        @Override
        public String toString() {
            if (material != null) return amount + "x " + form + " " + material;
            if (item != null) return amount + "x " + item;
            if (block != null) return amount + "x " + block.description;
            return amount + "x integrated circuit " + circuit;
        }
    }

    /** 物品 + 数量：B 组替换文案里的"追加空单元 / 换成普通氢氟酸单元"。 */
    public static final class ItemAmount {

        final ItemKey item;
        final int amount;

        private ItemAmount(ItemKey item, int amount) {
            this.item = item;
            this.amount = amount;
        }

        public static ItemAmount of(ItemKey item, int amount) {
            return new ItemAmount(item, amount);
        }

        @Override
        public String toString() {
            return amount + "x " + item;
        }
    }

    /** 流体 + 数量（mB）。 */
    public static final class FluidAmount {

        final FluidKey fluid;
        final int amount;

        private FluidAmount(FluidKey fluid, int amount) {
            this.fluid = fluid;
            this.amount = amount;
        }

        public static FluidAmount of(FluidKey fluid, int amount) {
            return new FluidAmount(fluid, amount);
        }

        @Override
        public String toString() {
            return amount + " mB " + fluid;
        }
    }

    /**
     * 在配方表里定位一条既有运行配方：精确的流体输入集合 + 原时长 + 原 EU/t。
     *
     * <p>
     * 时长与电压也一起比对，目标写错时才会变成"未命中"，而不是改错配方。
     */
    public static final class Selector {

        final FluidAmount[] fluidInputs;
        final int duration;
        final int eut;

        private Selector(FluidAmount[] fluidInputs, int duration, int eut) {
            this.fluidInputs = fluidInputs;
            this.duration = duration;
            this.eut = eut;
        }

        public static Selector of(int duration, int eut, FluidAmount... fluidInputs) {
            return new Selector(fluidInputs, duration, eut);
        }

        public Selector withTiming(int duration, int eut) {
            return new Selector(fluidInputs, duration, eut);
        }
    }

    /** A 组的一种形式：工作台图案、装配机输入或装配线输入，附带时长与 EU/t。 */
    public static final class Form {

        final CraftKind kind;
        final String[] shape;
        final Map<Character, Ingredient> pattern;
        final Ingredient[] items;
        final FluidAmount[] fluids;
        final int duration;
        final int eut;

        private Form(CraftKind kind, String[] shape, Map<Character, Ingredient> pattern, Ingredient[] items,
            FluidAmount[] fluids, int duration, int eut) {
            this.kind = kind;
            this.shape = shape;
            this.pattern = pattern;
            this.items = items;
            this.fluids = fluids;
            this.duration = duration;
            this.eut = eut;
        }

        /** 有序合成：rows 是 3 行图案，charIngredientPairs 是 (字符, Ingredient) 的平铺列表。 */
        public static Form shaped(String[] rows, Object[] charIngredientPairs) {
            if (charIngredientPairs.length % 2 != 0) {
                throw new IllegalArgumentException("character/ingredient pairs must be even");
            }
            Map<Character, Ingredient> pattern = new LinkedHashMap<>();
            for (int i = 0; i < charIngredientPairs.length; i += 2) {
                pattern.put((Character) charIngredientPairs[i], (Ingredient) charIngredientPairs[i + 1]);
            }
            return new Form(CraftKind.SHAPED, rows, pattern, new Ingredient[0], new FluidAmount[0], 0, 0);
        }

        public static Form assembler(int duration, int eut, Ingredient[] items, FluidAmount[] fluids) {
            return new Form(CraftKind.ASSEMBLER, new String[0], null, items, fluids, duration, eut);
        }

        public static Form assemblyLine(int duration, int eut, Ingredient[] items, FluidAmount[] fluids) {
            return new Form(CraftKind.ASSEMBLY_LINE, new String[0], null, items, fluids, duration, eut);
        }
    }

    /** A 组一条：一个目标方块可以带多种形式（ticket 05 会给两个方块各加一种），日志仍按方块计一条。 */
    public static final class BuildCost implements Entry {

        final BlockKey target;
        final Form[] forms;

        private BuildCost(BlockKey target, Form[] forms) {
            this.target = target;
            this.forms = forms;
        }

        public static BuildCost of(BlockKey target, Form... forms) {
            return new BuildCost(target, forms);
        }

        @Override
        public String description() {
            return target.description;
        }
    }

    /**
     * B 组一条：新增或替换一条流体变体。maps 通常只有一个；经 UniversalChemical 加入的配方同时存在于单方块与 LCR
     * 两张表，这时两张表都要补一份，日志仍按一条计。
     *
     * <p>
     * 替换时还可以动物品：itemInputsToAdd 追加到原物品输入之后，itemOutputs 非空时整体替换物品输出。
     * B4/B6 只换流体，两项都是空数组；B5 要补 2 个空单元、把副产换成 4 个普通氢氟酸单元。
     */
    public static final class FluidPath implements Entry {

        final String description;
        final Action action;
        final MapKey[] maps;
        final Selector selector;
        final FluidKey source;
        final FluidKey replacement;
        final int factor;
        final ItemAmount[] itemInputsToAdd;
        final ItemAmount[] itemOutputs;

        private FluidPath(String description, Action action, MapKey[] maps, Selector selector, FluidKey source,
            FluidKey replacement, int factor, ItemAmount[] itemInputsToAdd, ItemAmount[] itemOutputs) {
            this.description = description;
            this.action = action;
            this.maps = maps;
            this.selector = selector;
            this.source = source;
            this.replacement = replacement;
            this.factor = factor;
            this.itemInputsToAdd = itemInputsToAdd;
            this.itemOutputs = itemOutputs;
        }

        /** 输入不同 → 新增，原配方保留。 */
        public static FluidPath of(String description, MapKey[] maps, Selector selector, FluidKey source,
            FluidKey replacement, int factor) {
            return new FluidPath(
                description,
                Action.ADD,
                maps,
                selector,
                source,
                replacement,
                factor,
                new ItemAmount[0],
                new ItemAmount[0]);
        }

        /** 只有副产不同 → 替换，原配方删除（B4/B6 用）。 */
        public static FluidPath replace(String description, MapKey[] maps, Selector selector, FluidKey source,
            FluidKey replacement, int factor) {
            return replace(
                description,
                maps,
                selector,
                source,
                replacement,
                factor,
                new ItemAmount[0],
                new ItemAmount[0]);
        }

        /** 替换流体，同时追加物品输入、整体替换物品输出（B5 用）。 */
        public static FluidPath replace(String description, MapKey[] maps, Selector selector, FluidKey source,
            FluidKey replacement, int factor, ItemAmount[] itemInputsToAdd, ItemAmount[] itemOutputs) {
            return new FluidPath(
                description,
                Action.REPLACE,
                maps,
                selector,
                source,
                replacement,
                factor,
                itemInputsToAdd,
                itemOutputs);
        }

        @Override
        public String description() {
            return description;
        }
    }

    /** C 组一条：时长减半、EU/t 除以 4，输入输出不变，所以直接替换原配方。 */
    public static final class Timing implements Entry {

        final String description;
        final MapKey map;
        final Selector selector;
        final int newDuration;
        final int newEut;

        private Timing(String description, MapKey map, Selector selector, int newDuration, int newEut) {
            this.description = description;
            this.map = map;
            this.selector = selector;
            this.newDuration = newDuration;
            this.newEut = newEut;
        }

        public static Timing of(String description, MapKey map, Selector selector, int newDuration, int newEut) {
            return new Timing(description, map, selector, newDuration, newEut);
        }

        @Override
        public String description() {
            return description;
        }
    }

    private static final int SECONDS = 20;
    private static final int MINUTES = 60 * SECONDS;
    private static final int HOURS = 60 * MINUTES;
    private static final int INGOTS = 144;
    /** 与 GT 的 TierEU 对齐；本类不引用 GT 类型，所以这里用同值常量。 */
    private static final int EUT_HV = 480;
    private static final int EUT_EV = 1920;
    private static final int EUT_IV = 7680;
    private static final int EUT_LuV = 30720;
    /** 与 GT++ 既有的 2:1 换算一致：1 mB 工业氢氟酸 = 2 mB 普通氢氟酸。 */
    private static final int HF_RATIO = 2;

    final List<BuildCost> buildCost = new ArrayList<>();
    final List<FluidPath> fluidPath = new ArrayList<>();
    final List<Timing> timing = new ArrayList<>();

    private RecipeChanges() {}

    public static RecipeChanges standard() {
        RecipeChanges changes = new RecipeChanges();
        addBuildCost(changes.buildCost);
        addFluidPath(changes.fluidPath);
        addTiming(changes.timing);
        return changes;
    }

    // 三张表按"一条一块"手排：Eclipse 会把每个参数拆成一行，表会散成几百行，反而看不出改了什么。
    // spotless:off
    // region A组：方块造价（13 条，只换单点合金）

    private static void addBuildCost(List<BuildCost> entries) {
        add(entries, BlockKey.LFTR_CONTROLLER, Form.shaped(new String[] { "ABA", "CDC", "EFE" }, new Object[] {
            'A', item(ItemKey.LFTR_CONTROL_CIRCUIT), 'B', part(MaterialKey.NAQUADAH, MaterialForm.CABLE_12X),
            'C', part(MaterialKey.TITANIUM, MaterialForm.PLATE_DOUBLE), 'D', item(ItemKey.COMPUTER_CUBE),
            'E', item(ItemKey.THORIUM_232_PLATE), 'F', item(ItemKey.HULL_IV) }));

        add(entries, BlockKey.REACTOR_CASING_I, Form.shaped(new String[] { "PIP", "IFI", "PIP" }, new Object[] {
            'P', part(MaterialKey.TITANIUM, MaterialForm.PLATE_DOUBLE),
            'I', item(ItemKey.REACTOR_PLATING_HEAT),
            'F', part(MaterialKey.STAINLESS_STEEL, MaterialForm.FRAME) }));

        add(entries, BlockKey.REACTOR_CASING_II, Form.shaped(new String[] { "PdP", "GFG", "PhP" }, new Object[] {
            'P', part(MaterialKey.STAINLESS_STEEL, MaterialForm.PLATE_DOUBLE),
            'G', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.GEAR),
            'F', item(ItemKey.FIELD_GENERATOR_LV) }),
            Form.assembler(5 * SECONDS, EUT_HV, new Ingredient[] {
                part(MaterialKey.STAINLESS_STEEL, MaterialForm.PLATE_DOUBLE, 4),
                part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.GEAR, 2), item(ItemKey.FIELD_GENERATOR_LV, 1) },
                new FluidAmount[0]));

        add(entries, BlockKey.RPU_I, Form.shaped(new String[] { "FRP", "DCD", "PDF" }, new Object[] {
            'F', item(ItemKey.FIELD_GENERATOR_HV), 'R', item(ItemKey.ROBOT_ARM_IV),
            'P', part(MaterialKey.TITANIUM, MaterialForm.PLATE),
            'D', part(MaterialKey.TITANIUM, MaterialForm.PLATE_DOUBLE),
            'C', item(ItemKey.MACHINE_IV_CHEMICAL_REACTOR) }));

        add(entries, BlockKey.RPU_II, Form.shaped(new String[] { "FRP", "DCD", "PDF" }, new Object[] {
            'F', item(ItemKey.FIELD_GENERATOR_IV), 'R', item(ItemKey.ROBOT_ARM_ZPM),
            'P', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE),
            'D', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE_DOUBLE),
            'C', block(BlockKey.RPU_I, 1) }));

        add(entries, BlockKey.COLD_TRAP_I, Form.shaped(new String[] { "PDP", "PCP", "RFR" }, new Object[] {
            'P', part(MaterialKey.TITANIUM, MaterialForm.PLATE),
            'D', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE_DOUBLE),
            'C', item(ItemKey.CASING_IV), 'R', item(ItemKey.ROBOT_ARM_IV), 'F', item(ItemKey.CASING_FROSTPROOF) }));

        add(entries, BlockKey.COLD_TRAP_II, Form.shaped(new String[] { "PDP", "PCP", "RFR" }, new Object[] {
            'P', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE),
            'D', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE_DOUBLE),
            'C', block(BlockKey.COLD_TRAP_I, 1), 'R', item(ItemKey.ROBOT_ARM_ZPM),
            'F', item(ItemKey.CASING_FROSTPROOF) }));

        add(entries, BlockKey.FUEL_REFINERY, Form.shaped(new String[] { "CiC", "PXP", "GHG" }, new Object[] {
            'C', item(ItemKey.ELITE_CIRCUIT), 'P', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE_DENSE),
            'X', item(ItemKey.COMPUTER_CUBE), 'G', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.GEAR),
            'H', item(ItemKey.HULL_IV) }));

        add(entries, BlockKey.REFINERY_CASING_INTERNAL, Form.shaped(new String[] { "PHP", "GTG", "PHP" }, new Object[] {
            'P', part(MaterialKey.STAINLESS_STEEL, MaterialForm.PLATE),
            'H', part(MaterialKey.STAINLESS_STEEL, MaterialForm.PIPE_HUGE),
            'G', part(MaterialKey.STAINLESS_STEEL, MaterialForm.GEAR), 'T', item(ItemKey.SUPER_TANK_IV) }));

        add(entries, BlockKey.REFINERY_CASING_EXTERNAL, Form.shaped(new String[] { "ABA", "BFB", "ABA" }, new Object[] {
            'A', part(MaterialKey.STAINLESS_STEEL, MaterialForm.PLATE),
            'B', part(MaterialKey.TITANIUM, MaterialForm.PLATE),
            'F', part(MaterialKey.STAINLESS_STEEL, MaterialForm.FRAME) }),
            Form.assembler(5 * SECONDS, EUT_HV, new Ingredient[] {
                part(MaterialKey.TITANIUM, MaterialForm.PLATE, 4),
                part(MaterialKey.STAINLESS_STEEL, MaterialForm.PLATE, 4),
                part(MaterialKey.STAINLESS_STEEL, MaterialForm.FRAME, 1) }, new FluidAmount[0]));

        add(entries, BlockKey.REFINERY_CASING_STRUCTURAL,
            Form.shaped(new String[] { "RGP", "hFw", "PHR" }, new Object[] {
            'R', part(MaterialKey.STAINLESS_STEEL, MaterialForm.RING),
            'G', part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.GEAR),
            'P', part(MaterialKey.STEEL, MaterialForm.PLATE),
            'F', part(MaterialKey.STAINLESS_STEEL, MaterialForm.FRAME),
            'H', item(ItemKey.CASING_EV) }));

        add(entries, BlockKey.SPARGE_TOWER_CASING, Form.assembler(2 * MINUTES, EUT_IV, new Ingredient[] {
            item(ItemKey.GTPP_CASING_HV, 1), part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE, 2),
            part(MaterialKey.TITANIUM, MaterialForm.RING, 4),
            part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE_DOUBLE, 4),
            part(MaterialKey.TITANIUM, MaterialForm.SCREW, 4), Ingredient.circuit(8, 1) },
            new FluidAmount[] { fl(FluidKey.MOLTEN_STAINLESS_STEEL, 8 * INGOTS) }));

        add(entries, BlockKey.SPARGE_TOWER_CONTROLLER, Form.assemblyLine(1 * MINUTES, EUT_LuV, new Ingredient[] {
            block(BlockKey.SPARGE_TOWER_CASING, 4), item(ItemKey.GTPP_CASING_EV, 4),
            item(ItemKey.MACHINE_IV_DISTILLERY, 1),
            item(ItemKey.ELITE_CIRCUIT, 8), part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.GEAR, 8),
            part(MaterialKey.TUNGSTEN_STEEL, MaterialForm.PLATE, 32),
            part(MaterialKey.TITANIUM, MaterialForm.PLATE_DOUBLE, 16),
            part(MaterialKey.TITANIUM, MaterialForm.SCREW, 64),
            part(MaterialKey.YTTRIUM_BARIUM_CUPRATE, MaterialForm.WIRE_FINE, 64),
            part(MaterialKey.YTTRIUM_BARIUM_CUPRATE, MaterialForm.WIRE_FINE, 64),
            part(MaterialKey.PLATINUM, MaterialForm.FOIL, 64) }, new FluidAmount[] {
                fl(FluidKey.MOLTEN_STAINLESS_STEEL, 16 * INGOTS), fl(FluidKey.MOLTEN_TUNGSTEN_STEEL, 32 * INGOTS),
                fl(FluidKey.MOLTEN_TITANIUM, 32 * INGOTS) }));
    }

    // endregion

    // region B组：工业氢氟酸通路（6 条：消耗侧 3 条新增，副产侧 3 条替换）

    private static void addFluidPath(List<FluidPath> entries) {
        addPath(entries, "碳酸锂制氟化锂", new MapKey[] { MapKey.CHEMICAL_BATH },
            Selector.of(9 * SECONDS, 480, fl(FluidKey.INDUSTRIAL_HF, 500)));

        addPath(entries, "氢氧化锂制氟化锂",
            new MapKey[] { MapKey.CHEMICAL_REACTOR, MapKey.MULTIBLOCK_CHEMICAL_REACTOR },
            Selector.of(2 * MINUTES, 30, fl(FluidKey.INDUSTRIAL_HF, 500)));

        addPath(entries, "U-233 与 U-235 制四氟化铀", new MapKey[] { MapKey.MIXER },
            Selector.of(2 * MINUTES + 30 * SECONDS, 480, fl(FluidKey.INDUSTRIAL_HF, 10_000)));

        replacePath(entries, "四氟铍酸铵制氟化铍", new MapKey[] { MapKey.MULTIBLOCK_CHEMICAL_REACTOR },
            Selector.of(5 * MINUTES, 120, fl(FluidKey.AMMONIUM_TETRAFLUOROBERYLLATE, 1_000)));

        // 原配方把副产单元当作"氢氟酸单元"输出，但不在物品输出里指明是哪种氢氟酸；这里不猜原物品，
        // 直接把物品输出整体换成 4 个普通氢氟酸单元，并在输入里补上被占用的 2 个空单元。
        replacePath(entries, "燃料基（工业高炉）", new MapKey[] { MapKey.BLAST_FURNACE },
            Selector.of(2 * MINUTES + 30 * SECONDS, 7680, fl(FluidKey.HYDROGEN, 2_000)),
            new ItemAmount[] { ItemAmount.of(ItemKey.CELL_EMPTY, 2) },
            new ItemAmount[] { ItemAmount.of(ItemKey.HF_CELLS, 4) });

        replacePath(entries, "化工厂制 LiFBeF2-UF4", new MapKey[] { MapKey.CHEMICAL_PLANT },
            Selector.of(2 * MINUTES + 30 * SECONDS, 7680, fl(FluidKey.URANIUM_HEXAFLUORIDE, 1_000),
                fl(FluidKey.LIF_BEF2, 1_000), fl(FluidKey.HYDROGEN, 2_000)));
    }

    // endregion

    // region C组：时长与电压（17 条，全部替换）

    private static void addTiming(List<Timing> entries) {
        // 燃料合成（5 条）
        timing(entries, "燃料合成 LiFBeF2-ZrF4-U235", MapKey.FISSION_FUEL_PROCESSING, 30 * MINUTES, EUT_EV,
            new FluidAmount[] { fl(FluidKey.LITHIUM_FLUORIDE, 550), fl(FluidKey.BERYLLIUM_FLUORIDE, 150),
                fl(FluidKey.ZIRCONIUM_TETRAFLUORIDE, 60), fl(FluidKey.URANIUM_235_MOLTEN, 240) });
        timing(entries, "燃料合成 LiFBeF2-ZrF4-UF4", MapKey.FISSION_FUEL_PROCESSING, 40 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.LITHIUM_FLUORIDE, 600), fl(FluidKey.BERYLLIUM_FLUORIDE, 250),
                fl(FluidKey.ZIRCONIUM_TETRAFLUORIDE, 80), fl(FluidKey.URANIUM_TETRAFLUORIDE, 70) });
        timing(entries, "燃料合成 LiFBeF2-ThF4-UF4", MapKey.FISSION_FUEL_PROCESSING, 50 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.LITHIUM_FLUORIDE, 580), fl(FluidKey.BERYLLIUM_FLUORIDE, 270),
                fl(FluidKey.THORIUM_TETRAFLUORIDE, 80), fl(FluidKey.URANIUM_TETRAFLUORIDE, 70) });
        timing(entries, "燃料再配制 LiFBeF2-ZrF4-UF4", MapKey.FISSION_FUEL_PROCESSING, 12 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.ZIRCONIUM_TETRAFLUORIDE, 100), fl(FluidKey.LIF_BEF2_UF4, 900) });
        timing(entries, "燃料再配制 LiFBeF2-ThF4-UF4", MapKey.FISSION_FUEL_PROCESSING, 15 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.THORIUM_TETRAFLUORIDE, 100), fl(FluidKey.LIF_BEF2_UF4, 900) });

        // 乏燃料处理：反应堆处理单元（6 条）
        timing(entries, "乏燃料氟化 LiF-ThF4", MapKey.REACTOR_PROCESSING_UNIT, 10 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.LIF_THF4, 10_000) });
        timing(entries, "乏燃料氟化 LiFBeF2-ThF4", MapKey.REACTOR_PROCESSING_UNIT, 10 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.LIF_BEF2_THF4, 10_000) });
        timing(entries, "乏燃料氟化 喷淋后 LiF-ThF4", MapKey.REACTOR_PROCESSING_UNIT, 5 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.SPARGED_LIF_THF4, 10_000) });
        timing(entries, "乏燃料氟化 喷淋后 LiFBeF2-ThF4", MapKey.REACTOR_PROCESSING_UNIT, 5 * MINUTES, EUT_IV,
            new FluidAmount[] { fl(FluidKey.SPARGED_LIF_BEF2_THF4, 10_000) });
        timing(entries, "乏燃料氟化 LiFBeF2-UF4-FP", MapKey.REACTOR_PROCESSING_UNIT, 2 * HOURS, EUT_HV,
            new FluidAmount[] { fl(FluidKey.LIF_BEF2_UF4FP, 1_000) });
        timing(entries, "乏燃料氟化 喷淋后 UF4-FP", MapKey.REACTOR_PROCESSING_UNIT, 60 * MINUTES, EUT_HV,
            new FluidAmount[] { fl(FluidKey.SPARGED_LIF_BEF2_UF4FP, 1_000) });

        // 乏燃料处理：冷阱（2 条）
        timing(entries, "冷阱 UF6F2", MapKey.COLD_TRAP, 10 * MINUTES, EUT_HV,
            new FluidAmount[] { fl(FluidKey.UF6F2, 3_000) });
        timing(entries, "冷阱 UF6F2-FP", MapKey.COLD_TRAP, 10 * MINUTES, EUT_EV,
            new FluidAmount[] { fl(FluidKey.UF6F2_FP, 2_000) });

        // 乏燃料处理：蒸馏（1 条）
        timing(entries, "蒸馏 不纯熔盐基", MapKey.DISTILLATION_TOWER, 7 * MINUTES + 30 * SECONDS, EUT_IV,
            new FluidAmount[] { fl(FluidKey.IMPURE_MOLTEN_SALT_BASE, 1_000) });

        // 喷淋塔（3 条）
        timing(entries, "喷淋 LiFBeF2-UF4-FP", MapKey.SPARGE_TOWER, 25 * SECONDS, EUT_IV,
            new FluidAmount[] { fl(FluidKey.HELIUM, 1_000), fl(FluidKey.LIF_BEF2_UF4FP, 50) });
        timing(entries, "喷淋 LiF-ThF4", MapKey.SPARGE_TOWER, 25 * SECONDS, EUT_IV,
            new FluidAmount[] { fl(FluidKey.FLUORINE, 100), fl(FluidKey.LIF_THF4, 50) });
        timing(entries, "喷淋 LiFBeF2-ThF4", MapKey.SPARGE_TOWER, 25 * SECONDS, EUT_IV,
            new FluidAmount[] { fl(FluidKey.FLUORINE, 100), fl(FluidKey.LIF_BEF2_THF4, 50) });
    }

    // endregion
    // spotless:on

    private static void timing(List<Timing> entries, String description, MapKey map, int duration, int eut,
        FluidAmount[] fluids) {
        entries.add(Timing.of(description, map, Selector.of(duration, eut, fluids), duration / 2, eut / 4));
    }

    private static void addPath(List<FluidPath> entries, String description, MapKey[] maps, Selector selector) {
        entries.add(FluidPath.of(description, maps, selector, FluidKey.INDUSTRIAL_HF, FluidKey.HF, HF_RATIO));
    }

    private static void replacePath(List<FluidPath> entries, String description, MapKey[] maps, Selector selector) {
        entries.add(FluidPath.replace(description, maps, selector, FluidKey.INDUSTRIAL_HF, FluidKey.HF, HF_RATIO));
    }

    private static void replacePath(List<FluidPath> entries, String description, MapKey[] maps, Selector selector,
        ItemAmount[] itemInputsToAdd, ItemAmount[] itemOutputs) {
        entries.add(
            FluidPath.replace(
                description,
                maps,
                selector,
                FluidKey.INDUSTRIAL_HF,
                FluidKey.HF,
                HF_RATIO,
                itemInputsToAdd,
                itemOutputs));
    }

    private static void add(List<BuildCost> entries, BlockKey target, Form... forms) {
        entries.add(BuildCost.of(target, forms));
    }

    private static Ingredient part(MaterialKey material, MaterialForm form) {
        return Ingredient.part(material, form, 1);
    }

    private static Ingredient part(MaterialKey material, MaterialForm form, int amount) {
        return Ingredient.part(material, form, amount);
    }

    private static Ingredient item(ItemKey item) {
        return Ingredient.item(item, 1);
    }

    private static Ingredient item(ItemKey item, int amount) {
        return Ingredient.item(item, amount);
    }

    private static Ingredient block(BlockKey block, int amount) {
        return Ingredient.block(block, amount);
    }

    private static FluidAmount fl(FluidKey fluid, int amount) {
        return FluidAmount.of(fluid, amount);
    }
}
