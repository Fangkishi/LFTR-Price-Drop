package com.fangkishi.lftrpricedrop.lftr;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.BlockKey;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.FluidAmount;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.FluidPath;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Ingredient;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.ItemAmount;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.ItemKey;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.MapKey;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.MaterialForm;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.MaterialKey;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.GTUtility;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.fluids.GTPPFluids;
import gtPlusPlus.core.material.MaterialsElements;
import gtPlusPlus.core.material.nuclear.MaterialsFluorides;
import gtPlusPlus.core.material.nuclear.MaterialsNuclides;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

/**
 * 本模组接触 GT 配方表的唯一位置：符号解析（枚举 → ItemStack / FluidStack）、GTRecipe 重建、增删，
 * 以及装配机与装配线两条特殊入口。这里只回答成功与否，"为什么没成功"由执行器记进日志。
 */
public final class GtRecipes {

    /**
     * 新配方带 NOT_REMOVABLE：FMLLoadCompleteEvent 的分发顺序并不严格跟随依赖声明（开发回路日志里 lftrpricedrop
     * 早于 gregtech），而 GT 的 load-complete flush 会执行 bulkRemoveRecipeByOutput / bulkRemoveByRecipe。
     * NOT_REMOVABLE 保证这些批量删除不会误删新配方。不带 BUFFERED：直接进合成表，命中数才是真的。
     */
    private static final long CRAFTING_BITS = GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
        | GTModHandler.RecipeBits.NOT_REMOVABLE;

    private GtRecipes() {}

    public static ItemStack item(BlockKey key) {
        switch (key) {
            case LFTR_CONTROLLER:
                return GregtechItemList.ThoriumReactor.get(1);
            case REACTOR_CASING_I:
                return GregtechItemList.Casing_Reactor_I.get(1);
            case REACTOR_CASING_II:
                return GregtechItemList.Casing_Reactor_II.get(1);
            case RPU_I:
                return GregtechItemList.ReactorProcessingUnit_IV.get(1);
            case RPU_II:
                return GregtechItemList.ReactorProcessingUnit_ZPM.get(1);
            case COLD_TRAP_I:
                return GregtechItemList.ColdTrap_IV.get(1);
            case COLD_TRAP_II:
                return GregtechItemList.ColdTrap_ZPM.get(1);
            case SPARGE_TOWER_CONTROLLER:
                return GregtechItemList.Controller_Sparge_Tower.get(1);
            case SPARGE_TOWER_CASING:
                return GregtechItemList.Casing_Sparge_Tower_Exterior.get(1);
            case FUEL_REFINERY:
                return GregtechItemList.Industrial_FuelRefinery.get(1);
            case REFINERY_CASING_INTERNAL:
                return GregtechItemList.Casing_Refinery_Internal.get(1);
            case REFINERY_CASING_EXTERNAL:
                return GregtechItemList.Casing_Refinery_External.get(1);
            case REFINERY_CASING_STRUCTURAL:
                return GregtechItemList.Casing_Refinery_Structural.get(1);
            default:
                return null;
        }
    }

    public static ItemStack item(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }
        if (ingredient.material != null) {
            return part(ingredient.material, ingredient.form, ingredient.amount);
        }
        if (ingredient.item != null) {
            return item(ingredient.item, ingredient.amount);
        }
        if (ingredient.block != null) {
            ItemStack stack = item(ingredient.block);
            if (stack != null) {
                stack.stackSize = ingredient.amount;
            }
            return stack;
        }
        ItemStack circuit = GTUtility.getIntegratedCircuit(ingredient.circuit);
        if (circuit != null) {
            circuit.stackSize = ingredient.amount;
        }
        return circuit;
    }

    public static FluidStack fluid(FluidAmount fluid) {
        if (fluid == null) {
            return null;
        }
        int amount = fluid.amount;
        switch (fluid.fluid) {
            case MOLTEN_STAINLESS_STEEL:
                return Materials.StainlessSteel.getMolten(amount);
            case MOLTEN_TUNGSTEN_STEEL:
                return Materials.TungstenSteel.getMolten(amount);
            case MOLTEN_TITANIUM:
                return Materials.Titanium.getMolten(amount);
            case INDUSTRIAL_HF:
                return new FluidStack(GTPPFluids.IndustrialStrengthHydrofluoricAcid, amount);
            case HF:
                return Materials.HydrofluoricAcid.getFluid(amount);
            case AMMONIUM_TETRAFLUOROBERYLLATE:
                return MaterialsFluorides.AMMONIUM_TETRAFLUOROBERYLLATE.getFluidStack(amount);
            case URANIUM_HEXAFLUORIDE:
                return MaterialsFluorides.URANIUM_HEXAFLUORIDE.getFluidStack(amount);
            case LIF_BEF2:
                return MaterialsNuclides.LiFBeF2.getFluidStack(amount);
            case HYDROGEN:
                return Materials.Hydrogen.getGas(amount);
            case LITHIUM_FLUORIDE:
                return MaterialsFluorides.LITHIUM_FLUORIDE.getFluidStack(amount);
            case BERYLLIUM_FLUORIDE:
                return MaterialsFluorides.BERYLLIUM_FLUORIDE.getFluidStack(amount);
            case ZIRCONIUM_TETRAFLUORIDE:
                return MaterialsFluorides.ZIRCONIUM_TETRAFLUORIDE.getFluidStack(amount);
            case THORIUM_TETRAFLUORIDE:
                return MaterialsFluorides.THORIUM_TETRAFLUORIDE.getFluidStack(amount);
            case URANIUM_TETRAFLUORIDE:
                return MaterialsFluorides.URANIUM_TETRAFLUORIDE.getFluidStack(amount);
            case URANIUM_235_MOLTEN:
                return Materials.Uranium235.getMolten(amount);
            case LIF_THF4:
                return MaterialsNuclides.LiFThF4.getFluidStack(amount);
            case LIF_BEF2_THF4:
                return MaterialsNuclides.LiFBeF2ThF4.getFluidStack(amount);
            case SPARGED_LIF_THF4:
                return MaterialsNuclides.Sparged_LiFThF4.getFluidStack(amount);
            case SPARGED_LIF_BEF2_THF4:
                return MaterialsNuclides.Sparged_LiFBeF2ThF4.getFluidStack(amount);
            case LIF_BEF2_UF4:
                return MaterialsNuclides.LiFBeF2UF4.getFluidStack(amount);
            case LIF_BEF2_UF4FP:
                return MaterialsNuclides.LiFBeF2UF4FP.getFluidStack(amount);
            case SPARGED_LIF_BEF2_UF4FP:
                return MaterialsNuclides.Sparged_LiFBeF2UF4FP.getFluidStack(amount);
            case UF6F2:
                return MaterialsNuclides.UF6F2.getFluidStack(amount);
            case UF6F2_FP:
                return MaterialsNuclides.UF6F2FP.getFluidStack(amount);
            case IMPURE_MOLTEN_SALT_BASE:
                return new FluidStack(GTPPFluids.ImpureMoltenSaltBase, amount);
            case HELIUM:
                return Materials.Helium.getGas(amount);
            case FLUORINE:
                return Materials.Fluorine.getGas(amount);
            default:
                return null;
        }
    }

    public static Collection<GTRecipe> all(MapKey key) {
        RecipeMap<?> map = map(key);
        return map == null ? null : map.getAllRecipes();
    }

    public static void add(MapKey key, GTRecipe recipe) {
        RecipeMap<?> map = map(key);
        if (map != null) {
            map.add(recipe);
        }
    }

    public static void remove(MapKey key, GTRecipe recipe) {
        RecipeMap<?> map = map(key);
        if (map != null) {
            map.getBackend()
                .removeRecipe(recipe);
        }
    }

    public static boolean addCrafting(ItemStack result, Object[] recipe) {
        return GTModHandler.addCraftingRecipe(result, CRAFTING_BITS, recipe);
    }

    public static boolean addAssembler(ItemStack result, ItemStack[] items, FluidStack[] fluids, int duration,
        int eut) {
        GTRecipeBuilder builder = GTValues.RA.stdBuilder();
        builder.itemInputs(items)
            .itemOutputs(result)
            .duration(duration)
            .eut(eut)
            .ignoreCollision();
        if (fluids.length > 0) {
            builder.fluidInputs(fluids);
        }
        Optional<GTRecipe> built = builder.build();
        if (!built.isPresent()) {
            return false;
        }
        RecipeMaps.assemblerRecipes.add(built.get());
        return true;
    }

    public static boolean hasAssemblyLineSource(ItemStack result) {
        return assemblyLineSource(result) != null;
    }

    public static boolean addAssemblyLine(ItemStack result, ItemStack[] items, FluidStack[] fluids, int duration,
        int eut) {
        GTRecipe.RecipeAssemblyLine source = assemblyLineSource(result);
        if (source == null) {
            return false;
        }
        GTRecipeBuilder builder = GTValues.RA.stdBuilder();
        builder.itemInputs(items)
            .itemOutputs(result)
            .duration(duration)
            .eut(eut)
            .ignoreCollision();
        if (fluids.length > 0) {
            builder.fluidInputs(fluids);
        }
        builder.metadata(GTRecipeConstants.RESEARCH_ITEM, source.mResearchItem)
            .metadata(GTRecipeConstants.SCANNING, new Scanning(source.mResearchTime, source.mResearchVoltage));
        return !builder.addTo(GTRecipeConstants.AssemblyLine)
            .isEmpty();
    }

    public static GTRecipe withTiming(GTRecipe source, int duration, int eut) {
        GTRecipeBuilder builder = builderFrom(source);
        builder.duration(duration)
            .eut(eut);
        Optional<GTRecipe> built = builder.build();
        return built.isPresent() ? built.get() : null;
    }

    /**
     * 以原配方为底构造 B 组的流体变体：把旧氢氟酸换成 factor 倍的新氢氟酸；entry 还声明了物品改动时，
     * 追加物品输入、整体替换物品输出。任何一步解析失败都返回 null，调用方据此保留原配方。
     */
    public static GTRecipe variant(GTRecipe source, FluidPath path) {
        FluidStack fromStack = fluid(FluidAmount.of(path.source, 1));
        FluidStack toStack = fluid(FluidAmount.of(path.replacement, 1));
        if (fromStack == null || toStack == null) {
            return null;
        }
        GTRecipeBuilder builder = builderFrom(source);
        builder
            .fluidInputs(
                substitute(source.mFluidInputs, fromStack.getFluid(), toStack, path.factor),
                source.mFluidInputChances)
            .fluidOutputs(
                substitute(source.mFluidOutputs, fromStack.getFluid(), toStack, path.factor),
                source.mFluidOutputChances);
        if (!items(builder, source, path)) {
            return null;
        }
        Optional<GTRecipe> built = builder.build();
        return built.isPresent() ? built.get() : null;
    }

    /**
     * 删旧配方之前的自检：entry 声明了什么，就要求新配方真的变成了什么。
     *
     * <p>
     * 流体：原配方里出现的旧氢氟酸必须全部消失，换成 factor 倍的新氢氟酸（B4/B6）；物品：追加的空单元与
     * 替换后的氢氟酸单元数量必须对得上（B5）。两类改动至少要声明一类，否则说明表里的流体写错了，直接判失败。
     */
    public static boolean variantHolds(GTRecipe source, GTRecipe variant, FluidPath path) {
        FluidStack from = fluid(FluidAmount.of(path.source, 1));
        FluidStack to = fluid(FluidAmount.of(path.replacement, 1));
        if (from == null || to == null) {
            return false;
        }
        boolean fluidWork = totalFluid(source.mFluidInputs, from.getFluid()) > 0
            || totalFluid(source.mFluidOutputs, from.getFluid()) > 0;
        boolean itemWork = path.itemInputsToAdd.length > 0 || path.itemOutputs.length > 0;
        if (!fluidWork && !itemWork) {
            return false;
        }
        if (fluidWork && !fluidReplaced(source, variant, from.getFluid(), to.getFluid(), path.factor)) {
            return false;
        }
        return !itemWork || itemsHeld(source, variant, path);
    }

    /** 追加物品输入、整体替换物品输出；entry 没声明物品改动时什么也不做。 */
    private static boolean items(GTRecipeBuilder builder, GTRecipe source, FluidPath path) {
        if (path.itemInputsToAdd.length > 0) {
            if (source.mInputs == null) {
                return false;
            }
            ItemStack[] inputs = new ItemStack[source.mInputs.length + path.itemInputsToAdd.length];
            System.arraycopy(source.mInputs, 0, inputs, 0, source.mInputs.length);
            for (int i = 0; i < path.itemInputsToAdd.length; i++) {
                ItemStack added = item(path.itemInputsToAdd[i]);
                if (added == null) {
                    return false;
                }
                inputs[source.mInputs.length + i] = added;
            }
            // 与 builderFrom 一致：这些物品已经统一过，不再走一遍矿石字典统一。
            builder.itemInputsUnified(inputs, (int[]) null);
        }
        if (path.itemOutputs.length > 0) {
            ItemStack[] outputs = new ItemStack[path.itemOutputs.length];
            for (int i = 0; i < path.itemOutputs.length; i++) {
                outputs[i] = item(path.itemOutputs[i]);
                if (outputs[i] == null) {
                    return false;
                }
            }
            builder.itemOutputs(outputs, (int[]) null);
        }
        return true;
    }

    private static boolean fluidReplaced(GTRecipe source, GTRecipe variant, Fluid from, Fluid to, int factor) {
        return fluidArrayReplaced(source.mFluidInputs, variant.mFluidInputs, from, to, factor)
            && fluidArrayReplaced(source.mFluidOutputs, variant.mFluidOutputs, from, to, factor);
    }

    private static boolean fluidArrayReplaced(FluidStack[] before, FluidStack[] after, Fluid from, Fluid to,
        int factor) {
        int replaced = totalFluid(before, from);
        if (replaced == 0) {
            return true;
        }
        return totalFluid(after, from) == 0 && totalFluid(after, to) == totalFluid(before, to) + replaced * factor;
    }

    private static int totalFluid(FluidStack[] stacks, Fluid fluid) {
        int total = 0;
        if (stacks == null) {
            return total;
        }
        for (FluidStack stack : stacks) {
            if (stack != null && stack.getFluid() == fluid) {
                total += stack.amount;
            }
        }
        return total;
    }

    private static boolean itemsHeld(GTRecipe source, GTRecipe variant, FluidPath path) {
        for (ItemAmount added : path.itemInputsToAdd) {
            ItemStack kind = item(added.item, 1);
            if (kind == null || totalItems(variant.mInputs, kind) != totalItems(source.mInputs, kind) + added.amount) {
                return false;
            }
        }
        for (ItemAmount output : path.itemOutputs) {
            ItemStack kind = item(output.item, 1);
            if (kind == null || totalItems(variant.mOutputs, kind) != output.amount) {
                return false;
            }
        }
        return true;
    }

    private static int totalItems(ItemStack[] stacks, ItemStack kind) {
        int total = 0;
        if (stacks == null) {
            return total;
        }
        for (ItemStack stack : stacks) {
            if (stack != null && GTUtility.areStacksEqual(stack, kind)) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    private static ItemStack item(ItemAmount amount) {
        return item(amount.item, amount.amount);
    }

    private static GTRecipe.RecipeAssemblyLine assemblyLineSource(ItemStack result) {
        for (GTRecipe.RecipeAssemblyLine recipe : GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes) {
            if (recipe.mOutput != null && GTUtility.areStacksEqual(recipe.mOutput, result)) {
                return recipe;
            }
        }
        return null;
    }

    private static ItemStack part(MaterialKey key, MaterialForm form, int amount) {
        Materials material = material(key);
        OrePrefixes prefix = prefix(form);
        if (material == null || prefix == null) {
            return null;
        }
        return GTOreDictUnificator.get(prefix, material, amount);
    }

    private static ItemStack item(ItemKey key, int amount) {
        switch (key) {
            case HULL_IV:
                return ItemList.Hull_IV.get(amount);
            case FIELD_GENERATOR_LV:
                return ItemList.Field_Generator_LV.get(amount);
            case FIELD_GENERATOR_HV:
                return ItemList.Field_Generator_HV.get(amount);
            case FIELD_GENERATOR_IV:
                return ItemList.Field_Generator_IV.get(amount);
            case ROBOT_ARM_IV:
                return ItemList.Robot_Arm_IV.get(amount);
            case ROBOT_ARM_ZPM:
                return ItemList.Robot_Arm_ZPM.get(amount);
            case MACHINE_IV_CHEMICAL_REACTOR:
                return ItemList.Machine_IV_ChemicalReactor.get(amount);
            case MACHINE_IV_DISTILLERY:
                return ItemList.Machine_IV_Distillery.get(amount);
            case CASING_EV:
                return ItemList.Casing_EV.get(amount);
            case CASING_IV:
                return ItemList.Casing_IV.get(amount);
            case CASING_FROSTPROOF:
                return ItemList.Casing_FrostProof.get(amount);
            case SUPER_TANK_IV:
                return ItemList.Super_Tank_IV.get(amount);
            case ELITE_CIRCUIT:
                return GTOreDictUnificator.get(OrePrefixes.circuit, Materials.IV, amount);
            case LFTR_CONTROL_CIRCUIT:
                return GregtechItemList.LFTRControlCircuit.get(amount);
            case COMPUTER_CUBE:
                return GregtechItemList.Gregtech_Computer_Cube.get(amount);
            case THORIUM_232_PLATE:
                return MaterialsElements.getInstance().THORIUM232.getPlate(amount);
            case GTPP_CASING_HV:
                return GregtechItemList.GTPP_Casing_HV.get(amount);
            case GTPP_CASING_EV:
                return GregtechItemList.GTPP_Casing_EV.get(amount);
            case REACTOR_PLATING_HEAT:
                return GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "reactorPlatingHeat", amount);
            case CELL_EMPTY:
                return ItemList.Cell_Empty.get(amount);
            case HF_CELLS:
                return Materials.HydrofluoricAcid.getCells(amount);
            default:
                return null;
        }
    }

    private static Materials material(MaterialKey key) {
        switch (key) {
            case TITANIUM:
                return Materials.Titanium;
            case STAINLESS_STEEL:
                return Materials.StainlessSteel;
            case TUNGSTEN_STEEL:
                return Materials.TungstenSteel;
            case STEEL:
                return Materials.Steel;
            case NAQUADAH:
                return Materials.Naquadah;
            case YTTRIUM_BARIUM_CUPRATE:
                return Materials.YttriumBariumCuprate;
            case PLATINUM:
                return Materials.Platinum;
            default:
                return null;
        }
    }

    private static OrePrefixes prefix(MaterialForm form) {
        switch (form) {
            case PLATE:
                return OrePrefixes.plate;
            case PLATE_DOUBLE:
                return OrePrefixes.plateDouble;
            case PLATE_DENSE:
                return OrePrefixes.plateDense;
            case FRAME:
                return OrePrefixes.frameGt;
            case GEAR:
                return OrePrefixes.gearGt;
            case SCREW:
                return OrePrefixes.screw;
            case RING:
                return OrePrefixes.ring;
            case PIPE_HUGE:
                return OrePrefixes.pipeHuge;
            case CABLE_12X:
                return OrePrefixes.cableGt12;
            case WIRE_FINE:
                return OrePrefixes.wireFine;
            case FOIL:
                return OrePrefixes.foil;
            default:
                return null;
        }
    }

    private static RecipeMap<?> map(MapKey key) {
        switch (key) {
            case CHEMICAL_BATH:
                return RecipeMaps.chemicalBathRecipes;
            case CHEMICAL_REACTOR:
                return RecipeMaps.chemicalReactorRecipes;
            case MULTIBLOCK_CHEMICAL_REACTOR:
                return RecipeMaps.multiblockChemicalReactorRecipes;
            case MIXER:
                return RecipeMaps.mixerRecipes;
            case BLAST_FURNACE:
                return RecipeMaps.blastFurnaceRecipes;
            case CHEMICAL_PLANT:
                return RecipeMaps.chemicalPlantRecipes;
            case FISSION_FUEL_PROCESSING:
                return RecipeMaps.fissionFuelProcessingRecipes;
            case REACTOR_PROCESSING_UNIT:
                return RecipeMaps.reactorProcessingUnitRecipes;
            case COLD_TRAP:
                return RecipeMaps.coldTrapRecipes;
            case DISTILLATION_TOWER:
                return RecipeMaps.distillationTowerRecipes;
            case SPARGE_TOWER:
                return RecipeMaps.spargeTowerRecipes;
            default:
                return null;
        }
    }

    /**
     * 以既有配方为底重建一份新配方：保留输入输出、概率、特殊槽、标记与全部 metadata，只改需要改的字段。
     *
     * <p>
     * {@code GTRecipe.copy()} 会丢掉 metadata，所以这里走 GTRecipeBuilder 的"配方改写"重载。
     */
    private static GTRecipeBuilder builderFrom(GTRecipe source) {
        GTRecipeBuilder builder = GTValues.RA.stdBuilder();
        builder.itemInputsUnified(source.mInputs, source.mInputChances)
            .itemOutputs(source.mOutputs, source.mOutputChances)
            .fluidInputs(source.mFluidInputs, source.mFluidInputChances)
            .fluidOutputs(source.mFluidOutputs, source.mFluidOutputChances)
            .special(source.mSpecialItems)
            .specialValue(source.mSpecialValue)
            .duration(source.mDuration)
            .eut(source.mEUt)
            .ignoreCollision();
        if (!source.mCanBeBuffered) {
            builder.noBuffer();
        }
        if (source.mNeedsEmptyOutput) {
            builder.needsEmptyOutput();
        }
        if (source.isNBTSensitive) {
            builder.nbtSensitive();
        }
        if (source.mHidden) {
            builder.hidden();
        }
        if (source.mFakeRecipe) {
            builder.fake();
        }
        if (!source.mEnabled) {
            builder.disabled();
        }
        if (source.getRecipeCategory() != null) {
            builder.recipeCategory(source.getRecipeCategory());
        }
        for (Map.Entry<RecipeMetadataKey<?>, Object> entry : source.getMetadataStorage()
            .getEntries()) {
            copyMetadata(builder, entry.getKey(), entry.getValue());
        }
        return builder;
    }

    private static FluidStack[] substitute(FluidStack[] fluids, Fluid from, FluidStack to, int factor) {
        FluidStack[] result = new FluidStack[fluids.length];
        for (int i = 0; i < fluids.length; i++) {
            FluidStack fluid = fluids[i];
            if (fluid == null) {
                result[i] = null;
            } else if (fluid.getFluid() == from) {
                result[i] = new FluidStack(to.getFluid(), fluid.amount * factor);
            } else {
                result[i] = fluid.copy();
            }
        }
        return result;
    }

    private static <T> void copyMetadata(GTRecipeBuilder builder, RecipeMetadataKey<T> key, Object raw) {
        builder.metadata(key, key.cast(raw));
    }
}
