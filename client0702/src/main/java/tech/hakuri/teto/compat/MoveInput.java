package tech.hakuri.teto.compat;

import net.minecraft.client.player.Input;

/**
 * 移动输入（1.20.1 Forge 实现）。
 * <p>
 * 参数类型故意用 {@code Object}：1.20.1 是 {@code net.minecraft.client.player.Input}，
 * 1.21.8 换成了 {@code ClientInput}，两个类互不兼容，共享代码没法直接写死类型。
 * 1.20.1 这边 {@code forwardImpulse}/{@code leftImpulse} 是公开的 float 字段，可直接读写。
 */
public final class MoveInput {

    private MoveInput() {
    }

    public static float forward(Object input) {
        return ((Input) input).forwardImpulse;
    }

    public static float left(Object input) {
        return ((Input) input).leftImpulse;
    }

    /**
     * 一次写入前后与左右两个分量。
     * 1.20.1 的 {@code KeyboardInput} 不做归一化（两个 float 字段独立），
     * 所以这里直接赋值即可；归一化是 1.21.8 那边才需要处理的事。
     */
    public static void set(Object input, float forward, float left) {
        ((Input) input).forwardImpulse = forward;
        ((Input) input).leftImpulse = left;
    }
}
