package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;


public class NightVision extends Module {

    public NightVision() {
        name = "夜视";
        category = Category.render;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.tickCount % 20 == 0) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 60, 0));
        }
    }
}
