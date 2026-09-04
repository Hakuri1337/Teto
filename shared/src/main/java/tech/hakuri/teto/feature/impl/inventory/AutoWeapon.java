package tech.hakuri.teto.feature.impl.inventory;

import tech.hakuri.teto.compat.Compat;

import tech.hakuri.teto.compat.Inv;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;

public class AutoWeapon extends Module {

    public AutoWeapon() {
        name = "自动武器";
        category = Category.inventory;
        toggle();
    }

    public static double getWeaponDamage(ItemStack itemStack, LivingEntity entity) {
        double baseDamage = Compat.baseAttackDamage(itemStack);
        return entity == null ? baseDamage : baseDamage + Compat.damageBonus(itemStack, entity);
    }

    @Override
    public void onTick(EventTick event) {
        if (!mc.player.swinging) return;

        if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof LivingEntity entity) {

            var bestDamage = Double.MIN_VALUE;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    double damage = getWeaponDamage(stack, entity);
                    if (damage > bestDamage) {
                        bestDamage = damage;
                        Inv.setSelectedSlot(mc.player.getInventory(), i);
                    }
                }
            }
        }
    }
}