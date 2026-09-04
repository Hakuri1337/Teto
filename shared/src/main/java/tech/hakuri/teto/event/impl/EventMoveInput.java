package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

/**
 * 移动输入事件。思路来自氯雷他定。
 * <p>
 * 字段类型是 {@code Object} 而不是具体的输入类：1.20.1 是
 * {@code net.minecraft.client.player.KeyboardInput}，1.21.x 换成了 {@code ClientInput}，
 * 两者互不兼容。消费方通过 {@code tech.hakuri.teto.compat.MoveInput} 读写前后/左右分量。
 */
public class EventMoveInput extends Event {
    public Object keyboardInput;

    public EventMoveInput(Object keyboardInput) {
        this.keyboardInput = keyboardInput;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onMoveInput(this);
        }
    }

    public static void moveInput(Object keyboardInput) {
        new EventMoveInput(keyboardInput);
    }
}
