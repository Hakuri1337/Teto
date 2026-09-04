package tech.hakuri.teto.feature.impl.move;

import tech.hakuri.teto.compat.Compat;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;


public class JumpReset extends Module {

    public JumpReset() {
        name = "跳跃重置";
        category = Category.move;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.hurtTime == 10) {
            jump();
        }
    }

    public void jump() {
        if (Compat.inFluid(mc.player)) return;
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }
    }

}