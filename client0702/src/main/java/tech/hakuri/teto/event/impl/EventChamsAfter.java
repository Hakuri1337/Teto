package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

public class EventChamsAfter extends Event {

    public EventChamsAfter() {
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onChamsAfter(this);
        }
    }

    public static void chamsAfter() {
        new EventChamsAfter();
    }
}
