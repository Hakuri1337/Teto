package tech.hakuri.teto.compat;

import net.minecraft.client.Minecraft;

/**
 * 帧内插值系数（1.20.1 Forge 实现）。
 * <p>
 * 这个值用于把实体位置按「当前帧处于两个 tick 之间的哪个位置」插值，
 * 瞄准、ESP 平滑、脚手架落点都要用。1.20.1 叫 {@code Minecraft.getFrameTime()}，
 * 1.21.x 改成了 {@code DeltaTracker}，所以共享代码统一走本类。
 */
public final class Timing {

    private Timing() {
    }

    public static float partialTick() {
        return Minecraft.getInstance().getFrameTime();
    }
}
