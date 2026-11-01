package com.fangkishi.lftrpricedrop.lftr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.FluidAmount;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.MapKey;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Selector;

import gregtech.api.util.GTRecipe;

/**
 * 选择器：按"配方表 + 流体输入集合 + 时长 + EU/t"定位一条既有运行配方。
 *
 * <p>
 * 时长与电压也一起比对，目标写错时才会变成"未命中"而不是删错配方；命中多条同样只算未命中，
 * 因为无法判断该删哪一条。
 */
public final class RecipeFinder {

    private RecipeFinder() {}

    public static Match find(MapKey key, Selector selector) {
        Collection<GTRecipe> recipes = GtRecipes.all(key);
        if (recipes == null) {
            return Match.missingMap();
        }
        List<GTRecipe> matches = new ArrayList<>();
        for (GTRecipe recipe : recipes) {
            if (recipe.mDuration == selector.duration && recipe.mEUt == selector.eut
                && matchesFluidInputs(recipe.mFluidInputs, selector.fluidInputs)) {
                matches.add(recipe);
            }
        }
        if (matches.size() == 1) {
            return Match.unique(matches.get(0));
        }
        return Match.ambiguous(matches.size());
    }

    /**
     * 输入集合按"多重集"比对：流体种类与数量都要对上，顺序无关。GT 查询配方只看输入、并允许多余输入，
     * 这里故意收紧成完全相等，宁可未命中也不改错配方。
     */
    private static boolean matchesFluidInputs(FluidStack[] actual, FluidAmount[] expected) {
        List<FluidStack> remaining = new ArrayList<>();
        for (FluidStack fluid : actual) {
            if (fluid != null) {
                remaining.add(fluid);
            }
        }
        if (remaining.size() != expected.length) {
            return false;
        }
        for (FluidAmount want : expected) {
            FluidStack wantStack = GtRecipes.fluid(want);
            if (wantStack == null) {
                return false;
            }
            boolean found = false;
            for (Iterator<FluidStack> iterator = remaining.iterator(); iterator.hasNext();) {
                FluidStack got = iterator.next();
                if (got.getFluid() == wantStack.getFluid() && got.amount == wantStack.amount) {
                    iterator.remove();
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    /** 一次定位的结果：唯一命中、命中多条，或整张配方表不存在。 */
    public static final class Match {

        final GTRecipe recipe;
        final int count;
        final boolean mapFound;

        private Match(GTRecipe recipe, int count, boolean mapFound) {
            this.recipe = recipe;
            this.count = count;
            this.mapFound = mapFound;
        }

        private static Match unique(GTRecipe recipe) {
            return new Match(recipe, 1, true);
        }

        private static Match ambiguous(int count) {
            return new Match(null, count, true);
        }

        private static Match missingMap() {
            return new Match(null, 0, false);
        }

        public boolean isUnique() {
            return recipe != null;
        }

        /**
         * 未命中时给执行器的一句话：整张表不存在、表里 0 条、表里多条，三种情况措辞不同。
         * 0 条不是"匹配到 0 条、期望 1 条"——那是把"没写这条"说成了"选择器太宽"。
         */
        String missReason(String mapName) {
            if (!mapFound) {
                return "找不到配方表 " + mapName;
            }
            if (count == 0) {
                return mapName + " 中未找到目标配方";
            }
            return mapName + " 中匹配到 " + count + " 条目标配方，期望 1 条";
        }
    }
}
