package tech.hakuri.teto.compat;

import net.minecraft.client.Minecraft;

/**
 * 帧内插值系数（1.21.8 NeoForge 实现）。
 * <p>
 * {@code Minecraft.getFrameTime()} 在 1.21.x 已移除，改由 {@code DeltaTracker} 提供。
 * 参数 {@code false} 表示要「游戏时间」的插值系数而不是不受暂停影响的实时值 ——
 * 与 1.20.1 的 {@code getFrameTime()} 语义一致（暂停时不推进）。
 */
public final class Timing {

    private Timing() {
    }

    public static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
