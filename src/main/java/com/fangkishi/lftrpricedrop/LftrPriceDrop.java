package com.fangkishi.lftrpricedrop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * LFTR 配方改动的入口。
 *
 * <p>
 * 改动挂在 {@link FMLLoadCompleteEvent} 上：GT 与 GT++ 的配方在 postInit 只进缓冲，加载结束才可用。
 *
 * <p>
 * 依赖写 {@code required-after:miscutils} 而不是 {@code after:}：本模组直接引用 {@code gtPlusPlus.*} 的类，
 * 缺了它要的是启动期一行「missing dependency」，而不是进游戏后崩在 FMLLoadCompleteEvent 里。
 */
@Mod(
    modid = LftrPriceDrop.MODID,
    version = Tags.VERSION,
    name = "LFTR: Price Drop",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:gregtech;required-after:miscutils")
public class LftrPriceDrop {

    public static final String MODID = "lftrpricedrop";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.fangkishi.lftrpricedrop.ClientProxy",
        serverSide = "com.fangkishi.lftrpricedrop.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }
}
