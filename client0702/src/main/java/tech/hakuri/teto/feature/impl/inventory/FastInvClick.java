package tech.hakuri.teto.feature.impl.inventory;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FastInvClick extends Module {

    public FastInvClick() {
        name = "容器速点";
        category = Category.inventory;
        toggle();
    }

    @Override
    public void onTick(EventTick event) {
        //copy at net.minecraft.client.MouseHandler.class gui click
        if (mc.screen instanceof ContainerScreen screen && ContainerScreen.hasShiftDown()) {
            Slot slotUnderMouse = screen.getSlotUnderMouse();
            if (slotUnderMouse == null) return;

            //不要信傻逼idea的提示，这里必npe
            ItemStack item = slotUnderMouse.getItem();
            if (item == null) return;

            //点空格子也会发包，所以做个判断
            if (!item.isEmpty()) {
                var d0 = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
                var d1 = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
                screen.mouseClicked(d0, d1, 0);
            }
        }
    }
}
