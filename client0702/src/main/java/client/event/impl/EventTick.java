package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;

public class EventTick extends Event {
    public EventTick() {
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onTick(this);
        }
    }

    public static void tick() {
        new EventTick();
    }
}
