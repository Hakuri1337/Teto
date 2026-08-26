package client.feature.impl.block;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.utils.ReflectBridge;
import net.minecraft.client.Minecraft;

public class FastPlace extends Module {

    public FastPlace() {
        name = "放置无冷却";
        category = Category.block;
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setField(Minecraft.class, Minecraft.getInstance(), "f_91011_", 0);//rightClickDelay f_91011_
        } catch (Exception ex) {
        }
    }
}
