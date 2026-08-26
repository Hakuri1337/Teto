package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

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
