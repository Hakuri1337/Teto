package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class EventRender2D extends Event {
    public GuiGraphics guiGraphics;

    public EventRender2D() {
        this.guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onRender2D(this);
        }
    }

    public static void r2d() {
        new EventRender2D();
    }
}
