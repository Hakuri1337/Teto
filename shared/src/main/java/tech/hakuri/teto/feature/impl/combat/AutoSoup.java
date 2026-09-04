package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.compat.Inv;

import tech.hakuri.teto.compat.Keys;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.impl.move.NoFall;
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
                Inv.setSelectedSlot(mc.player.getInventory(), soup);
                KeyMapping.click(Keys.of(mc.options.keyUse));
            }
        }
    }
}
