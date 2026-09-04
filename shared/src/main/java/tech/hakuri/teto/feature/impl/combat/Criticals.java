package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;

public class Criticals extends Module {

    public Criticals() {
        name = "刀爆";
        category = Category.combat;
    }

    @Override
    public void onDisable() {
        PingPongKB.toggle(false);
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.onGround()) return;

        if (mc.player.getDeltaMovement().y > 0) {
            PingPongKB.toggle(true);
        }
        if (mc.player.getDeltaMovement().y < 0) {
            PingPongKB.toggle(false);
        }
    }
}
