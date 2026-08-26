package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

public class EventModuleToggle extends Event {
    public Module module;

    public EventModuleToggle(Module module) {
        this.module = module;
        for (Module m : ModuleManager.modules) {
            if (m.enable) m.onModuleToggle(this);
        }
    }
}
