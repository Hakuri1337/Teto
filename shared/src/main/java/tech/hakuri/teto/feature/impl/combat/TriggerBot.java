package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.compat.Keys;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.impl.misc.ClickManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TriggerBot extends Module {

    public TriggerBot() {
        name = "瞄到就打";
        category = Category.combat;
    }

    @Override
    public void onTick(EventTick event) {
        if (ClickManager.timer.hasTimePassed(ClickManager.genRandomDelayMS())) {
            if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getType() == HitResult.Type.ENTITY) {
                if (!mc.gameMode.isDestroying()) {
                    KeyMapping.click(Keys.of(mc.options.keyAttack));
                    ClickManager.timer.reset();
                }
            }
        }
    }
}

