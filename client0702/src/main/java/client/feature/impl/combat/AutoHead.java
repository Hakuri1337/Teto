package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.impl.move.NoFall;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.Items;

public class AutoHead extends Module {
    public AutoHead() {
        name = "自动金头";
        category = Category.combat;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.tickCount % 10 != 0) return;
        if (mc.player.getHealth() < mc.player.getMaxHealth() / 2) {
            Integer head = NoFall.findItemHotbar(Items.PLAYER_HEAD);
            if (head != null && mc.player.getAbsorptionAmount() == 0) {
                mc.player.getInventory().selected = head;
                KeyMapping.click(mc.options.keyUse.getKey());
            }
        }
    }
}
