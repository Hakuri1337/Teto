package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.feature.impl.misc.KeyListener;
import client.utils.RotationUtils;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class KeepRange extends Module {
    public Value min = new Value("最小距离", 1.0f, 0.1f, 5);
    public Value max = new Value("最大距离", 3.5f, 0.1f, 5);
    public Value leftAndRight = new Value("包括斜向移动", false);

    public KeepRange() {
        name = "自动控距";
        category = Category.combat;
        addValues(min, max, leftAndRight);
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getType() == HitResult.Type.ENTITY) {
            var v = RotationUtils.distanceToEntityAABBNearest(mc.player, hitResult.getEntity());
            if (v > min.numberValue && v < max.numberValue) {
                mc.options.keyUp.setDown(false);
                if (leftAndRight.enable) {
                    mc.options.keyLeft.setDown(false);
                    mc.options.keyRight.setDown(false);
                }
            } else {
                KeyListener.resetMove();
            }
        } else {
            KeyListener.resetMove();
        }
    }
}
