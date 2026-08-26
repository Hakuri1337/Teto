package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.impl.misc.ClickManager;
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
                    KeyMapping.click(mc.options.keyAttack.getKey());
                    ClickManager.timer.reset();
                }
            }
        }
    }
}

