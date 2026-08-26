package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;

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
