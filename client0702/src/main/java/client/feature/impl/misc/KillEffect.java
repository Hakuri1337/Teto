package client.feature.impl.misc;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.utils.MSTimer;
import client.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

//f_90991_ timer
//f_92521_ msPerTick
public class KillEffect extends Module {
    public MSTimer effectTimer = new MSTimer();

    public KillEffect() {
        name = "击杀效果";
        category = Category.misc;
    }

    public static void set(float i) {
        try {
            Timer timer = ReflectBridge.getField(Timer.class, Minecraft.class, Minecraft.getInstance(), "f_90991_");
            ReflectBridge.setField(Timer.class, timer, "f_92521_", i);
        } catch (Exception e) {
        }
    }

    public static Float get() {
        try {
            Timer timer = ReflectBridge.getField(Timer.class, Minecraft.class, Minecraft.getInstance(), "f_90991_");
            return ReflectBridge.getField(Float.class, Timer.class, timer, "f_92521_");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onTick(EventTick event) {
        if (get() == null) return;

        if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getType() == HitResult.Type.ENTITY && hitResult.getEntity() instanceof LivingEntity it && it.isDeadOrDying() && get() == 50) {
            effectTimer.reset();
            set(250);
        }
        if (effectTimer.hasTimePassed(500)) {
            if (get() > 50) {
                set(get() * 0.8f);
            } else {
                set(50);
            }
        }
    }
}
