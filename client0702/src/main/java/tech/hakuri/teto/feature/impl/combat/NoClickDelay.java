package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;

public class NoClickDelay extends Module {

    public NoClickDelay() {
        name = "点击无延迟";
        category = Category.combat;
        toggle();
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setField(Minecraft.class, Minecraft.getInstance(), "f_91078_", 0);//missTime f_91078_ 这逼玩意改名了我都不知道叫什么以为被mojang删了，感谢氯雷他定
        } catch (Exception ex) {
        }
    }
}
