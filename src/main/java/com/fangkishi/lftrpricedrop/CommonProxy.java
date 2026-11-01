package com.fangkishi.lftrpricedrop;

import com.fangkishi.lftrpricedrop.lftr.LftrPopularizer;

import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
    }

    /** 加载阶段的最后一个节点：GT 与 GT++ 的配方到这里才可用，改动在这里应用。 */
    public void loadComplete(FMLLoadCompleteEvent event) {
        LftrPopularizer.run();
    }
}
