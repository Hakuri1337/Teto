package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
import net.minecraft.client.KeyMapping;

public class EventInvMove extends Event {
    public KeyMapping keyMapping;
    public boolean handle;//这个变量同时兼顾钩子开关和修改结果

    public EventInvMove(KeyMapping keyMapping) {
        this.keyMapping = keyMapping;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onInvMove(this);
        }
    }

    public static boolean invMove(KeyMapping keyMapping) {
        return new EventInvMove(keyMapping).handle;
    }
}
