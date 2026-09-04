package tech.hakuri.teto.compat;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import tech.hakuri.teto.utils.ReflectBridge;

/**
 * 移动输入（1.21.8 NeoForge 实现）。
 * <p>
 * 1.21.x 把 {@code Input} 改名成 {@code ClientInput}，并把
 * {@code forwardImpulse}/{@code leftImpulse} 两个 float 合并成一个
 * {@code Vec2 moveVector}（x = 左右，y = 前后）。该字段是 protected 且 Vec2 不可变，
 * 所以写入要反射整体替换。
 */
public final class MoveInput {

    private MoveInput() {
    }

    private static Vec2 vec(Object input) {
        return ((ClientInput) input).getMoveVector();
    }

    private static void setVec(Object input, Vec2 value) {
        try {
            ReflectBridge.setFieldAny(ClientInput.class, input, value, "moveVector");
        } catch (Exception ignored) {
            // 拿不到字段就放弃修改，不要让转头逻辑因此抛异常
        }
    }

    public static float forward(Object input) {
        return vec(input).y;
    }

    public static float left(Object input) {
        return vec(input).x;
    }

    /**
     * 一次写入前后与左右两个分量。
     * <p>
     * <b>必须归一化</b>：1.21.8 的 {@code KeyboardInput.tick()} 结尾会对 moveVector 调
     * {@code normalized()}，而我们的钩子插在 {@code RETURN} 之前（也就是归一化<b>之后</b>），
     * 所以写回时要自己补上，否则斜向移动会是 √2 的模长 —— 比原版快约 41%。
     * <p>
     * 也正因如此不能提供「只改一个分量」的写法：分别归一化两次会把中间状态算错，
     * 必须两个分量一起定下来再归一化。
     */
    public static void set(Object input, float forward, float left) {
        setVec(input, new Vec2(left, forward).normalized());
    }
}
