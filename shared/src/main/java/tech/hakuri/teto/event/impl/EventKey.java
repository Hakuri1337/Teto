package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

public class EventKey extends Event {

    public int key;
    public int scanCode;
    public int action;
    public int modifiers;

    public EventKey(int key, int scanCode, int action, int modifiers) {
        this.key = key;
        this.scanCode = scanCode;
        this.action = action;
        this.modifiers = modifiers;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onKey(this);
        }
    }

    public static void key(int key, int scanCode, int action, int modifiers) {
        new EventKey(key, scanCode, action, modifiers);
    }
}
