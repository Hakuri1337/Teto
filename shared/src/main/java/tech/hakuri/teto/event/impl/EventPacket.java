package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.net.NetworkFreeze;
import net.minecraft.network.protocol.Packet;

public class EventPacket extends Event {
    public Packet<?> packet;
    public boolean cancel;

    public EventPacket(Packet<?> packet) {
        this.packet = packet;
        //网络冻结的驻留点。放在这里而不是某个模块里，是因为 Criticals / StopEvading
        //也会调 PingPongKB.toggle(true)，那时反击退模块本身可能是关着的。
        //只有网络读线程会真的驻留；渲染线程（出站包）直接放过。
        NetworkFreeze.holdIfFrozen();
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onPacket(this);
        }
    }

    public static boolean packet(Packet<?> packet) {
        return new EventPacket(packet).cancel;
    }
}
