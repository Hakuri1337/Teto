package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventAfterAttack;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.AsyncEntityFilter;
import tech.hakuri.teto.utils.RotationUtils;

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
