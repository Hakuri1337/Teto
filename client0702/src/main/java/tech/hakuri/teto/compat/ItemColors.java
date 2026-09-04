package tech.hakuri.teto.compat;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

/**
 * 版本专属的物品颜色读取（1.20.1 Forge 实现）。
 * <p>
 * 抽出来只为了让 {@code TargetManager} 保持共享：那个文件两百多行逻辑完全通用，
 * 唯一不兼容的就是取染色护甲颜色这一句。1.20.1 用 {@code DyeableLeatherItem}
 * 接口，1.21.8 改成了 data component，两边各给一份这个小类即可。
 */
public final class ItemColors {

    private ItemColors() {
    }

    public static Integer getArmorColor(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem item ? item.getColor(stack) : null;
    }
}
