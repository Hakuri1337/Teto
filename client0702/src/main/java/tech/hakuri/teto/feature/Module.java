package tech.hakuri.teto.feature;


import tech.hakuri.teto.event.impl.*;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;
import java.util.List;

public class Module {
    public Minecraft mc = Minecraft.getInstance();
    public String name;
    public String category;
    public List<Value> values = new LinkedList<>();
    public boolean enable;
    public int keyCode;

    public void addValues(Value... vls) {
        values.addAll(List.of(vls));
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onAfterAttack(EventAfterAttack event) {
    }

    public void onCamera(EventCamera event) {
    }

    public void onChamsPre(EventChamsPre event) {
    }

    public void onChamsAfter(EventChamsAfter event) {
    }

    public void onInvMove(EventInvMove event) {
    }

    public void onKey(EventKey event) {
    }

    public void onModuleToggle(EventModuleToggle event) {
    }

    public void onMoveInput(EventMoveInput event) {
    }

    public void onPacket(EventPacket event) {
    }

    public void onRender2D(EventRender2D event) {
    }

    public void onRender3D(EventRender3D event) {
    }

    public void onTick(EventTick event) {
    }

    public void onKick(EventKick event) {
    }

    public void enable() {
        if (enable) return;
        enable = true;
        onEnable();

        new EventModuleToggle(this);
    }

    public void disable() {
        if (!enable) return;
        enable = false;
        onDisable();

        new EventModuleToggle(this);
    }

    public void toggle() {
        if (enable) {
            disable();
        } else {
            enable();
        }
    }

}
