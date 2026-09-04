package tech.hakuri.teto.compat;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 快捷栏选中槽（1.20.1 Forge 实现）。
 * <p>
 * 1.20.1 上 {@code Inventory.selected} 是公开字段、{@code getSelected()} 取当前物品。
 * 1.21.8 把字段收成私有并改名为访问器 {@code getSelectedSlot()/setSelectedSlot()/getSelectedItem()}，
 * 所以共享代码统一走本类。
 */
public final class Inv {

    private Inv() {
    }

    public static int selectedSlot(Inventory inventory) {
        return inventory.selected;
    }

    public static void setSelectedSlot(Inventory inventory, int slot) {
        inventory.selected = slot;
    }

    public static ItemStack selectedItem(Inventory inventory) {
        return inventory.getSelected();
    }
}
