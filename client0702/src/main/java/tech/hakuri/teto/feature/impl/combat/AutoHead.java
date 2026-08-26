package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.impl.move.NoFall;
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
