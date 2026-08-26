package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
import net.minecraft.client.player.KeyboardInput;

//思路来自氯雷他定
public class EventMoveInput extends Event {
    public KeyboardInput keyboardInput;

    public EventMoveInput(KeyboardInput keyboardInput) {
        this.keyboardInput = keyboardInput;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onMoveInput(this);
        }
    }

    public static void moveInput(KeyboardInput keyboardInput) {
        new EventMoveInput(keyboardInput);
    }
}
