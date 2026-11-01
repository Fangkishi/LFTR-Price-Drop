package com.fangkishi.lftrpricedrop.lftr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.BuildCost;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.FluidPath;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Form;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Group;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Ingredient;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.MapKey;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Selector;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Timing;

import gregtech.api.util.GTRecipe;

/**
 * 执行器：把改动清单落到 GT 的合成表、装配机、装配线与运行配方表，逐条打一行日志、每组打一行汇总。
 *
 * <p>
 * 失败语义是这一层唯一的硬要求：替换先构造新配方，构造成功才删旧、加新；目标未命中、命中多条、重建失败、
 * 任何异常都只记一行警告并跳过，旧配方原封不动，加载绝不因此失败。写入后的复核失败时，先回滚再记未命中：
 * 日志说未命中，NEI 里就该什么都没变。
 */
public final class RecipeExecutor {

    private RecipeExecutor() {}

    public static void apply(RecipeChanges changes) {
        run(Group.BUILD_COST, changes.buildCost, RecipeExecutor::buildCost);
        run(Group.FLUID_PATH, changes.fluidPath, RecipeExecutor::fluidPath);
        run(Group.TIMING, changes.timing, RecipeExecutor::timing);
    }

    private interface Operation<T> {

        boolean apply(T entry);
    }

    /** 替换写入之后必须成立的状态。写操作没有返回值，只能靠选择器复核。 */
    private interface Postcondition {

        boolean holds();
    }

    private static <T extends RecipeChanges.Entry> void run(Group group, List<T> entries, Operation<T> operation) {
        LftrLog.begin(group, entries.size());
        int hits = 0;
        for (T entry : entries) {
            boolean applied;
            try {
                applied = operation.apply(entry);
            } catch (Throwable throwable) {
                LftrLog.miss(group, entry.description(), "执行失败：" + throwable);
                applied = false;
            }
            // 未命中已经由 miss 记过一行；这里只补命中，避免一条失败出现两行日志。
            if (applied) {
                LftrLog.result(group, entry.description());
                hits++;
            }
        }
        LftrLog.summary(group, hits, entries.size());
    }

    // region A组：新增方块造价配方

    private static boolean buildCost(BuildCost entry) {
        ItemStack result = GtRecipes.item(entry.target);
        if (result == null) {
            LftrLog.miss(Group.BUILD_COST, entry.description(), "目标物品无法解析");
            return false;
        }
        for (Form form : entry.forms) {
            if (!form(entry, form, result)) {
                return false;
            }
        }
        return true;
    }

    private static boolean form(BuildCost entry, Form form, ItemStack result) {
        switch (form.kind) {
            case SHAPED:
                return shaped(entry, form, result);
            case ASSEMBLER:
                return machine(entry, form, result, false);
            case ASSEMBLY_LINE:
                return machine(entry, form, result, true);
            default:
                LftrLog.miss(Group.BUILD_COST, entry.description(), "未知的配方形式 " + form.kind);
                return false;
        }
    }

    private static boolean shaped(BuildCost entry, Form form, ItemStack result) {
        Object[] recipe = new Object[form.shape.length + form.pattern.size() * 2];
        int index = 0;
        for (String row : form.shape) {
            recipe[index++] = row;
        }
        for (Map.Entry<Character, Ingredient> slot : form.pattern.entrySet()) {
            ItemStack ingredient = GtRecipes.item(slot.getValue());
            if (ingredient == null) {
                LftrLog.miss(Group.BUILD_COST, entry.description(), "材料无法解析：" + slot.getValue());
                return false;
            }
            recipe[index++] = slot.getKey();
            recipe[index++] = ingredient;
        }
        if (!GtRecipes.addCrafting(result, recipe)) {
            LftrLog.miss(Group.BUILD_COST, entry.description(), "GTModHandler 拒绝了这条合成配方");
            return false;
        }
        return true;
    }

    private static boolean machine(BuildCost entry, Form form, ItemStack result, boolean assemblyLine) {
        ItemStack[] items = new ItemStack[form.items.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = GtRecipes.item(form.items[i]);
            if (items[i] == null) {
                LftrLog.miss(Group.BUILD_COST, entry.description(), "材料无法解析：" + form.items[i]);
                return false;
            }
        }
        FluidStack[] fluids = new FluidStack[form.fluids.length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = GtRecipes.fluid(form.fluids[i]);
            if (fluids[i] == null) {
                LftrLog.miss(Group.BUILD_COST, entry.description(), "流体无法解析：" + form.fluids[i]);
                return false;
            }
        }
        if (!assemblyLine) {
            if (!GtRecipes.addAssembler(result, items, fluids, form.duration, form.eut)) {
                LftrLog.miss(Group.BUILD_COST, entry.description(), "GTRecipeBuilder 没有产出配方");
                return false;
            }
            return true;
        }
        if (!GtRecipes.hasAssemblyLineSource(result)) {
            LftrLog.miss(Group.BUILD_COST, entry.description(), "找不到同产物的装配线原配方，无法沿用研究信息");
            return false;
        }
        if (!GtRecipes.addAssemblyLine(result, items, fluids, form.duration, form.eut)) {
            LftrLog.miss(Group.BUILD_COST, entry.description(), "装配线配方没有加入装配线");
            return false;
        }
        return true;
    }

    // endregion

    // region B组：新增或替换流体变体

    private static boolean fluidPath(FluidPath entry) {
        switch (entry.action) {
            case ADD:
                return addVariants(entry);
            case REPLACE:
                return replaceVariants(entry);
            default:
                LftrLog.miss(Group.FLUID_PATH, entry.description(), "未知的动作 " + entry.action);
                return false;
        }
    }

    /**
     * 新增流体变体：多表条目（B2 的两张表）要整条成功才留下，中途失败就删掉已经加进去的变体，
     * 否则日志说未命中、NEI 里却变了一条。
     */
    private static boolean addVariants(FluidPath entry) {
        // 已写入的 (map, variant) 一一对应；失败时按插入顺序的逆序原样删掉。
        List<MapKey> addedMaps = new ArrayList<>();
        List<GTRecipe> addedVariants = new ArrayList<>();
        for (MapKey map : entry.maps) {
            GTRecipe source = locate(Group.FLUID_PATH, entry.description(), map, entry.selector);
            if (source == null) {
                continue;
            }
            GTRecipe variant = GtRecipes.variant(source, entry);
            if (variant == null) {
                LftrLog.miss(Group.FLUID_PATH, entry.description(), "重建配方失败：" + map.mapName);
                continue;
            }
            GtRecipes.add(map, variant);
            addedMaps.add(map);
            addedVariants.add(variant);
        }
        if (addedMaps.size() == entry.maps.length) {
            return true;
        }
        for (int i = addedMaps.size() - 1; i >= 0; i--) {
            GtRecipes.remove(addedMaps.get(i), addedVariants.get(i));
        }
        return false;
    }

    private static boolean replaceVariants(FluidPath entry) {
        int replaced = 0;
        for (MapKey map : entry.maps) {
            if (replaceVariant(entry, map)) {
                replaced++;
            }
        }
        return replaced == entry.maps.length;
    }

    /**
     * 替换一条流体变体：定位唯一旧配方 → 构造新配方 → 校验新配方本身 → 交给 {@link #swap} 删旧、加新、复核。
     *
     * <p>
     * 前三步任何一步失败都不动旧配方；写入后的复核由 swap 统一处理，失败时回滚。
     */
    private static boolean replaceVariant(FluidPath entry, MapKey map) {
        GTRecipe source = locate(Group.FLUID_PATH, entry.description(), map, entry.selector);
        if (source == null) {
            return false;
        }
        GTRecipe replacement = GtRecipes.variant(source, entry);
        if (replacement == null) {
            LftrLog.miss(Group.FLUID_PATH, entry.description(), "重建配方失败：" + map.mapName);
            return false;
        }
        if (!GtRecipes.variantHolds(source, replacement, entry)) {
            LftrLog.miss(Group.FLUID_PATH, entry.description(), "新配方校验失败：" + map.mapName);
            return false;
        }
        // 副产侧的选择器（流体输入、时长、电压）在替换前后不变，所以"唯一命中且正是刚加入的那条"
        // 恰好等价于"旧版本已消失、新版本唯一存在"。
        return swap(
            map,
            Group.FLUID_PATH,
            entry.description(),
            entry.selector,
            source,
            replacement,
            () -> uniquely(replacement, map, entry.selector));
    }

    // endregion

    // region C组：替换时长与电压

    private static boolean timing(Timing entry) {
        GTRecipe source = locate(Group.TIMING, entry.description(), entry.map, entry.selector);
        if (source == null) {
            return false;
        }
        GTRecipe replacement = GtRecipes.withTiming(source, entry.newDuration, entry.newEut);
        if (replacement == null) {
            LftrLog.miss(Group.TIMING, entry.description(), "重建配方失败");
            return false;
        }
        // 删旧之前先核对构造结果：builder 若悄悄改了时长、电压，后面"新配方唯一存在"的复核就是假象。
        if (replacement.mDuration != entry.newDuration || replacement.mEUt != entry.newEut) {
            LftrLog.miss(
                Group.TIMING,
                entry.description(),
                "新配方时长/电压与目标不符：实际 " + replacement.mDuration
                    + " ticks / "
                    + replacement.mEUt
                    + " EU/t，期望 "
                    + entry.newDuration
                    + " ticks / "
                    + entry.newEut
                    + " EU/t");
            return false;
        }
        // 删除接口没有返回值：用选择器复核"旧版本已消失、新版本唯一存在"，否则命中数会说谎。
        return swap(
            entry.map,
            Group.TIMING,
            entry.description(),
            entry.selector,
            source,
            replacement,
            () -> RecipeFinder.find(entry.map, entry.selector).count == 0
                && RecipeFinder.find(entry.map, entry.selector.withTiming(entry.newDuration, entry.newEut))
                    .isUnique());
    }

    // endregion

    /**
     * 替换的公共流程：删旧 → 加新 → 复核；复核失败就把旧配方放回去。B 组与 C 组共用。
     *
     * <p>
     * 调用方必须先定位旧配方，并构造、校验好替换配方；到这里时两边都已就绪。复核失败先
     * remove(replacement) 再 add(source)，然后按定位用的选择器确认旧配方真的回来了：回来才只记未命中，
     * 没回来再记一行 error 级警告说明配方表状态已不一致——加载仍然照常继续。
     *
     * @return 替换是否成立
     */
    private static boolean swap(MapKey map, Group group, String description, Selector selector, GTRecipe source,
        GTRecipe replacement, Postcondition postcondition) {
        GtRecipes.remove(map, source);
        GtRecipes.add(map, replacement);
        String failure;
        try {
            if (postcondition.holds()) {
                return true;
            }
            failure = "替换后校验失败";
        } catch (Throwable throwable) {
            // 复核本身抛异常也按复核失败处理：先保证旧配方回来，加载照常继续。
            failure = "替换后校验异常：" + throwable;
        }
        GtRecipes.remove(map, replacement);
        GtRecipes.add(map, source);
        if (uniquely(source, map, selector)) {
            LftrLog.miss(group, description, failure + "，已回滚原配方");
            return false;
        }
        LftrLog.miss(group, description, failure + "，回滚也未能恢复原配方");
        LftrLog.inconsistent(group, description, "配方表状态已不一致：旧配方未回到 " + map.mapName);
        return false;
    }

    /** 选择器唯一命中，且命中的就是 expected。 */
    private static boolean uniquely(GTRecipe expected, MapKey map, Selector selector) {
        RecipeFinder.Match match = RecipeFinder.find(map, selector);
        return match.isUnique() && match.recipe == expected;
    }

    private static GTRecipe locate(Group group, String description, MapKey map, Selector selector) {
        RecipeFinder.Match match = RecipeFinder.find(map, selector);
        if (match.isUnique()) {
            return match.recipe;
        }
        LftrLog.miss(group, description, match.missReason(map.mapName));
        return null;
    }
}
