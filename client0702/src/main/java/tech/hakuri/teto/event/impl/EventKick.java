package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import net.minecraft.network.chat.Component;

public class EventKick extends Event {
    public Component reason;
    public boolean cancel;

    public EventKick(Component reason) {
        this.reason = reason;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onKick(this);
        }
    }

    public static boolean kick(Component reason) {
        return new EventKick(reason).cancel;
    }
}
