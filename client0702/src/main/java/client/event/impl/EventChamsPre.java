package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;

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
