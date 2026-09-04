package tech.hakuri.teto.feature.impl.block;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;

public class FastPlace extends Module {

    public FastPlace() {
        name = "放置无冷却";
        category = Category.block;
    }

    @Override
    public void onTick(EventTick event) {
        try {
            ReflectBridge.setFieldAny(Minecraft.class, Minecraft.getInstance(), 0, "f_91011_", "rightClickDelay");//rightClickDelay f_91011_
        } catch (Exception ex) {
        }
    }
}
