package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;

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
