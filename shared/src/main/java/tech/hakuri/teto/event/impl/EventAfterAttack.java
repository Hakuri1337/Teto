package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import net.minecraft.world.entity.Entity;

public class EventAfterAttack extends Event {
    public Entity entity;

    public EventAfterAttack(Entity entity) {
        this.entity = entity;
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onAfterAttack(this);
        }
    }

    public static void attack(Entity entity) {
        new EventAfterAttack(entity);
    }
}
