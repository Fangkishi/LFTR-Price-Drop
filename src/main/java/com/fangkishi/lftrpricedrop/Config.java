package com.fangkishi.lftrpricedrop;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** 模组的配置项。配置文件是 {@code config/lftrpricedrop.cfg}。 */
public final class Config {

    /** 配方改动的总开关。关闭时不构造改动清单，配方表一条都不碰。 */
    public static boolean enablePriceDrop = true;

    private Config() {}

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        enablePriceDrop = configuration.getBoolean(
            "enablePriceDrop",
            Configuration.CATEGORY_GENERAL,
            true,
            "Cheaper LFTR build costs from mainline materials, a hydrofluoric-acid-free fuel path, "
                + "and faster fuel and blanket processing. Set to false to keep every GT++ recipe untouched.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
