package tech.hakuri.teto.feature.impl.combat;


import tech.hakuri.teto.compat.Keys;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.impl.misc.ClickManager;
import tech.hakuri.teto.feature.impl.move.Eagle;
import tech.hakuri.teto.utils.ReflectBridge;
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
                ReflectBridge.setFieldAny(Minecraft.class, Minecraft.getInstance(), 0, "f_91078_", "missTime");//f_91078_ missTime
            } catch (Exception ex) {
            }
            if (mc.options.keyAttack.isDown()) {
                if (!mc.gameMode.isDestroying()) {
                    KeyMapping.click(Keys.of(mc.options.keyAttack));
                    ClickManager.timer.reset();
                }
            }
            if (mc.options.keyUse.isDown()) {
                if (!mc.player.isUsingItem()) {
                    if (Eagle.holdingBlock() && Eagle.canPlace()) {
                        KeyMapping.click(Keys.of(mc.options.keyUse));
                        ClickManager.timer.reset();
                    }
                }
            }
        }
    }
}
