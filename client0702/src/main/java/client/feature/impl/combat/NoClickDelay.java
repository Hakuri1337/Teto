package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.utils.ReflectBridge;
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
