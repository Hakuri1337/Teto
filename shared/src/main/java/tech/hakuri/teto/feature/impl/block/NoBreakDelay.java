package tech.hakuri.teto.feature.impl.block;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

//cant bypass grim
public class NoBreakDelay extends Module {

    public NoBreakDelay() {
        name = "破坏无冷却";
        category = Category.block;
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setFieldAny(MultiPlayerGameMode.class, Minecraft.getInstance().gameMode, 0, "f_105195_", "destroyDelay");//destroyDelay f_105195_
        } catch (Exception ex) {
        }
    }
}
