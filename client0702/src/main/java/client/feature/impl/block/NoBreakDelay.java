package client.feature.impl.block;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.utils.ReflectBridge;
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
            ReflectBridge.setField(MultiPlayerGameMode.class, Minecraft.getInstance().gameMode, "f_105195_", 0);//destroyDelay f_105195_
        } catch (Exception ex) {
        }
    }
}
