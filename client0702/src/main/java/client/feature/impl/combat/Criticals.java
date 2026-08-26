package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;

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
