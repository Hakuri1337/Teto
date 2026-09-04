package tech.hakuri.teto.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import tech.hakuri.teto.utils.ReflectBridge;

/**
 * 按键绑定的当前实际按键（1.21.8 NeoForge 实现）。
 * <p>
 * 1.21.8 移除了 {@code KeyMapping.getKey()}：{@code key} 字段变成私有，
 * 对外只剩 {@code setKey()} 和 {@code getDefaultKey()}。这里反射读那个私有字段，
 * 因为我们要的是<b>玩家当前绑定</b>，用 {@code getDefaultKey()} 会在玩家改过键位后拿错。
 * 读不到时退回默认键，至少不会 NPE。
 */
public final class Keys {

    private Keys() {
    }

    public static InputConstants.Key of(KeyMapping mapping) {
        try {
            return ReflectBridge.getFieldAny(InputConstants.Key.class, KeyMapping.class, mapping, "key", "f_90818_");
        } catch (Exception e) {
            return mapping.getDefaultKey();
        }
    }
}
