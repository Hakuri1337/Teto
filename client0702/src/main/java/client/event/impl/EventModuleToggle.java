package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;

public class EventModuleToggle extends Event {
    public Module module;

    public EventModuleToggle(Module module) {
        this.module = module;
        for (Module m : ModuleManager.modules) {
            if (m.enable) m.onModuleToggle(this);
        }
    }
}
