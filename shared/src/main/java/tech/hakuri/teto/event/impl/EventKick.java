package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.compat.Compat;
import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import net.minecraft.network.chat.Component;

/**
 * 被踢下线事件。
 * <p>
 * 工厂方法收 {@code Object} 而不是 {@code Component}：被挂钩的
 * {@code onDisconnect} 参数类型两版不同 —— 1.20.1 是 {@code Component}，
 * 1.21.8 换成了 {@code DisconnectionDetails}（一个包着 Component 的 record）。
 * 钩子只管 {@code ALOAD 1} 把原始参数推进来，具体怎么取出理由由
 * {@link Compat#kickReason(Object)} 按版本处理。
 * 若让钩子直接传 {@code Component}，1.21.8 上会因为类型不符触发 VerifyError。
 */
public class EventKick extends Event {
    public Component reason;
    public boolean cancel;

    public EventKick(Component reason) {
        this.reason = reason;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onKick(this);
        }
    }

    public static boolean kick(Object rawReason) {
        return new EventKick(Compat.kickReason(rawReason)).cancel;
    }
}
