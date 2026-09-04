package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 1.21.8 版本专属副本：击杀瞬间放慢，然后逐步恢复。
 * <p>
 * 1.20.1 那份是反射写 {@code Minecraft.timer}（{@code f_90991_}）里的
 * {@code msPerTick}（{@code f_92521_}）。这条路在 1.21.8 走不通：
 * {@code Timer} 已被 {@code DeltaTracker} 取代，而 {@code DeltaTracker$Timer.msPerTick}
 * 是 <b>final</b> 字段 —— 反射改 final 实例字段在 Java 17+ 不可靠（还可能被 JIT 常量折叠）。
 * <p>
 * 改用 1.20.3 引入的公开 API {@code Level.tickRateManager().setTickRate(float)}。
 * 为了让上层逻辑一字不改，这里仍以「每刻毫秒数」为单位对外暴露
 * （{@code msPerTick = 1000 / tickrate}），所以 50 = 正常速度、250 = 五倍慢放，
 * 和 1.20.1 那份的数值含义完全一致。
 */
public class KillEffect extends Module {
    public MSTimer effectTimer = new MSTimer();

    public KillEffect() {
        name = "击杀效果";
        category = Category.misc;
    }

    public static void set(float msPerTick) {
        try {
            if (msPerTick <= 0F) return;
            Minecraft.getInstance().level.tickRateManager().setTickRate(1000F / msPerTick);
        } catch (Exception e) {
        }
    }

    public static Float get() {
        try {
            float rate = Minecraft.getInstance().level.tickRateManager().tickrate();
            return rate <= 0F ? null : 1000F / rate;
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
