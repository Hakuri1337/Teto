package tech.hakuri.teto.compat;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 快捷栏选中槽（1.21.8 NeoForge 实现）。
 * <p>
 * 1.21.4 把 {@code Inventory.selected} 收成私有字段，对外只留访问器。
 * 这边不需要反射 —— 三个访问器都是公开的。
 */
public final class Inv {

    private Inv() {
    }

    public static int selectedSlot(Inventory inventory) {
        return inventory.getSelectedSlot();
    }

    public static void setSelectedSlot(Inventory inventory, int slot) {
        inventory.setSelectedSlot(slot);
    }

    public static ItemStack selectedItem(Inventory inventory) {
        return inventory.getSelectedItem();
    }
}
