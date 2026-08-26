package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
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
