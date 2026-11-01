package com.fangkishi.lftrpricedrop.lftr;

import com.fangkishi.lftrpricedrop.LftrPriceDrop;
import com.fangkishi.lftrpricedrop.lftr.RecipeChanges.Group;

/**
 * 唯一的日志出口。这些格式串就是验收缝：改这里等于改产品行为，所以只允许原样搬运。
 */
public final class LftrLog {

    private LftrLog() {}

    public static void begin(Group group, int count) {
        LftrPriceDrop.LOG.info("[LFTR] {}：{} 项", group.display(), count);
    }

    /** 只记命中：未命中已经由 miss 记过一行，不再补一行「命中 0」。 */
    public static void result(Group group, String target) {
        LftrPriceDrop.LOG.info("[LFTR] [{}] 目标 {} / 期望 1 / 命中 {}", group.tag, target, 1);
    }

    public static void miss(Group group, String target, String reason) {
        LftrPriceDrop.LOG.warn("[LFTR] [{}] 目标 {} 未命中：{}", group.tag, target, reason);
    }

    /** 回滚也失败时的醒目告警：配方表状态已不一致，但加载照常继续。 */
    public static void inconsistent(Group group, String target, String reason) {
        LftrPriceDrop.LOG.error("[LFTR] [{}] 目标 {} 状态异常：{}", group.tag, target, reason);
    }

    public static void summary(Group group, int hits, int total) {
        LftrPriceDrop.LOG.info("[LFTR] {}：命中 {} / 期望 {}", group.display(), hits, total);
    }

    public static void disabled() {
        LftrPriceDrop.LOG.info("[LFTR] 配置关闭（enablePriceDrop=false），跳过配方改动");
    }
}
