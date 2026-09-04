package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

public class EventCamera extends Event {
    public float yaw;
    public float pitch;

    public EventCamera(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onCamera(this);
        }
    }

    public static float[] camera(float yaw, float pitch) {
        EventCamera eventCamera = new EventCamera(yaw, pitch);
        return new float[]{eventCamera.yaw, eventCamera.pitch};
    }

}
