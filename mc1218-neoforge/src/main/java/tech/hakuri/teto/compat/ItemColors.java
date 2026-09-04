package tech.hakuri.teto.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * 版本专属的物品颜色读取（1.21.8 NeoForge 实现）。
 * <p>
 * 1.20.5 的 data component 改造把 {@code DyeableLeatherItem} 接口删了，
 * 染色颜色变成挂在 ItemStack 上的 {@code DataComponents.DYED_COLOR} 组件。
 * 沿用「没染色就返回 null」的语义，所以先 has 再取，而不是直接用 getOrDefault。
 */
public final class ItemColors {

    private ItemColors() {
    }

    public static Integer getArmorColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR) ? DyedItemColor.getOrDefault(stack, 0) : null;
    }
}
