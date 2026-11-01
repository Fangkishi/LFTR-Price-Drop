package com.fangkishi.lftrpricedrop.lftr;

import com.fangkishi.lftrpricedrop.Config;

/**
 * 入口：配置开启时构造改动清单并交给执行器。
 *
 * <p>
 * 挂载点是 FMLLoadCompleteEvent → CommonProxy.loadComplete，调到这里的时机不早于 GT++ 的配方加载。
 */
public final class LftrPopularizer {

    private LftrPopularizer() {}

    public static void run() {
        if (!Config.enablePriceDrop) {
            LftrLog.disabled();
            return;
        }
        RecipeExecutor.apply(RecipeChanges.standard());
    }
}
