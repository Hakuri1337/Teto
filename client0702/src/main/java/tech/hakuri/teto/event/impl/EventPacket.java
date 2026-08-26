package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
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
