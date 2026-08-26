package client.feature.impl.combat;

import client.event.impl.EventAfterAttack;
import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.utils.AsyncEntityFilter;
import client.utils.RotationUtils;

public class StopEvading extends Module {

    public StopEvading() {
        name = "回溯";
        category = Category.combat;
    }

    @Override
    public void onDisable() {
        PingPongKB.toggle(false);
    }

    @Override
    public void onTick(EventTick event) {
        if (AsyncEntityFilter.combatPick != null) {
            if (RotationUtils.fovCalc(AsyncEntityFilter.combatPick, mc.player) > 90) {
                if (RotationUtils.distanceToEntityAABBNearest(mc.player, AsyncEntityFilter.combatPick) > mc.player.getEntityReach()) {
                    if (mc.player.swinging) {
                        PingPongKB.toggle(true);
                        return;
                    }
                }
            }
        }
        PingPongKB.toggle(false);
    }

    @Override
    public void onAfterAttack(EventAfterAttack event) {
        PingPongKB.toggle(false);
    }
}
