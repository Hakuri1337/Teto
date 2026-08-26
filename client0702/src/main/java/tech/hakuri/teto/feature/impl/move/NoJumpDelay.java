package tech.hakuri.teto.feature.impl.move;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        name = "无跳跃冷却";
        category = Category.move;
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setField(LivingEntity.class, Minecraft.getInstance().player, "f_20954_", 0);//noJumpDelay f_20954_
        } catch (Exception ex) {
        }
    }
}
