package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

//注：Grim已涵盖该模块的检测
public class SuperKB extends Module {

    //注入点来自氯雷他定
    public SuperKB() {
        name = "超级击退";
        category = Category.combat;
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setField(LocalPlayer.class, Minecraft.getInstance().player, "f_108603_", false);//wasSprinting f_108603_
        } catch (Exception ex) {
        }
    }
}
