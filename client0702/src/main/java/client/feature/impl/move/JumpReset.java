package client.feature.impl.move;


import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;


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
        if (mc.player.isInFluidType()) return;
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }
    }

}