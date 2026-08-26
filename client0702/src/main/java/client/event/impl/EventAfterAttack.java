package client.event.impl;

import client.event.Event;
import client.feature.Module;
import client.feature.ModuleManager;
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
