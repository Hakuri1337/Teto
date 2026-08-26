package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.impl.move.NoFall;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.Items;

public class AutoSoup extends Module {
    public AutoSoup() {
        name = "自动汤";
        category = Category.combat;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.getHealth() < mc.player.getMaxHealth() / 2) {
            Integer soup = NoFall.findItemHotbar(Items.MUSHROOM_STEW);
            if (soup != null) {
                mc.player.getInventory().selected = soup;
                KeyMapping.click(mc.options.keyUse.getKey());
            }
        }
    }
}
