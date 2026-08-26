package client.feature.impl.inventory;


import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.utils.MSTimer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

public class ChestStealer extends Module {

    public MSTimer timer = new MSTimer();
    public Value delay = new Value("间隔延迟", 50, 1, 1000);

    public ChestStealer() {
        name = "拿箱";
        category = Category.inventory;
        addValues(delay);
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.containerMenu instanceof ChestMenu chestMenu) {
            if (timer.hasTimePassed(delay.numberValue)) {
                if (chestMenu.getContainer().isEmpty()) {
                    mc.player.closeContainer();
                }

                for (int i = 0; i < chestMenu.getContainer().getContainerSize(); i++) {
                    if (chestMenu.getSlot(i).hasItem()) {
                        if (timer.hasTimePassed(delay.numberValue)) {
                            mc.gameMode.handleInventoryMouseClick(chestMenu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                            timer.reset();
                        }
                    }
                }
            }
        }
    }
}