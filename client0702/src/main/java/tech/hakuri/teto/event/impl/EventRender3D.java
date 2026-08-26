package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import com.mojang.blaze3d.vertex.PoseStack;

public class EventRender3D extends Event {
    public PoseStack poseStack;

    public EventRender3D(PoseStack poseStack) {
        this.poseStack = poseStack;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onRender3D(this);
        }
    }

    public static void r3d(PoseStack poseStack) {
        new EventRender3D(poseStack);
    }
}
