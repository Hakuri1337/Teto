package client.feature.impl.combat;


import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.impl.misc.ClickManager;
import client.feature.impl.move.Eagle;
import client.utils.ReflectBridge;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;


public class AutoClicker extends Module {

    public AutoClicker() {
        name = "连点";
        category = Category.combat;
        toggle();
    }

    @Override
    public void onTick(EventTick event) {
        if (ClickManager.timer.hasTimePassed(ClickManager.genRandomDelayMS())) {
            try {
                ReflectBridge.setField(Minecraft.class, Minecraft.getInstance(), "f_91078_", 0);//f_91078_ missTime
            } catch (Exception ex) {
            }
            if (mc.options.keyAttack.isDown()) {
                if (!mc.gameMode.isDestroying()) {
                    KeyMapping.click(mc.options.keyAttack.getKey());
                    ClickManager.timer.reset();
                }
            }
            if (mc.options.keyUse.isDown()) {
                if (!mc.player.isUsingItem()) {
                    if (Eagle.holdingBlock() && Eagle.canPlace()) {
                        KeyMapping.click(mc.options.keyUse.getKey());
                        ClickManager.timer.reset();
                    }
                }
            }
        }
    }
}
