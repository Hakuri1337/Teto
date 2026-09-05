package tech.hakuri.teto.anim;

/**
 * 帧间隔（毫秒），GuardLite 的 AnimationUtils.delta 的等价物。
 * 原版 1.20.1 用 60fps 固定的逻辑帧概念，1.21.8 起 deltaFrameTime 直接是真实帧时间。
 * 这里统一用真实系统时钟算，与渲染帧率无关。
 */
public final class AnimationClock {

    public static float delta = 16F;

    private static long lastNanos;

    private AnimationClock() {
    }

    /** 每渲染帧调用一次（在 ClickGUI.render 与 HUD 绘制入口）。 */
    public static void tick() {
        long now = System.nanoTime();
        if (lastNanos != 0) {
            delta = (now - lastNanos) / 1_000_000F;
            // 掉帧时钳制，避免动画一大步跳过去
            if (delta > 50F) delta = 50F;
            if (delta < 1F) delta = 1F;
        }
        lastNanos = now;
    }
}
