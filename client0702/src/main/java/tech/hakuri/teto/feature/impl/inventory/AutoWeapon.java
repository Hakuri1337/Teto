package tech.hakuri.teto.feature.impl.inventory;

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
        var baseDamage = 0.0;
        var modifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        var attackModifiers = modifiers.get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : attackModifiers) {
            baseDamage += modifier.getAmount();
        }
        return entity == null ? baseDamage : baseDamage + EnchantmentHelper.getDamageBonus(itemStack, entity.getMobType());
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
                        mc.player.getInventory().selected = i;
                    }
                }
            }
        }
    }
}