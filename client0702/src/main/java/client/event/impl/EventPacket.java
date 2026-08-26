package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
import net.minecraft.network.protocol.Packet;

public class EventPacket extends Event {
    public Packet<?> packet;
    public boolean cancel;

    public EventPacket(Packet<?> packet) {
        this.packet = packet;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onPacket(this);
        }
    }

    public static boolean packet(Packet<?> packet) {
        return new EventPacket(packet).cancel;
    }
}
