package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

public class EventChamsPre extends Event {

    public EventChamsPre() {
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onChamsPre(this);
        }
    }

    public static void chamsPre() {
        new EventChamsPre();
    }
}
