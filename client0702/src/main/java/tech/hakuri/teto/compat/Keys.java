package tech.hakuri.teto.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * 按键绑定的当前实际按键（1.20.1 Forge 实现）。
 * <p>
 * 1.20.1 上 {@code KeyMapping.getKey()} 是公开方法，直接调即可。
 * 1.21.8 把 {@code key} 字段收成了私有、只留 {@code setKey()} 与 {@code getDefaultKey()}，
 * 所以那边得反射。共享代码统一走本类，两版各给一份实现。
 */
public final class Keys {

    private Keys() {
    }

    public static InputConstants.Key of(KeyMapping mapping) {
        return mapping.getKey();
    }
}
