package client.feature.impl.inventory;


import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoTool extends Module {

    public AutoTool() {
        name = "自动工具";
        category = Category.inventory;
        toggle();
    }

    public static float getItemDestroySpeed(ItemStack itemStack, BlockState blockState) {
        float baseSpeed = itemStack.getDestroySpeed(blockState);
        int enchantLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, itemStack);
        if (enchantLevel == 0) {
            return baseSpeed;
        } else {
            return baseSpeed * enchantLevel;
        }
    }

    @Override
    public void onTick(EventTick event) {
        if (!mc.gameMode.isDestroying()) return;

        if (mc.hitResult instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {

            var bestSpeed = Float.MIN_VALUE;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    var speed = getItemDestroySpeed(stack, mc.level.getBlockState(hitResult.getBlockPos()));
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        mc.player.getInventory().selected = i;
                    }
                }
            }
        }
    }
}