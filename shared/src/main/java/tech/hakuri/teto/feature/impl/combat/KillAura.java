package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.compat.Inv;


import tech.hakuri.teto.compat.Timing;
import tech.hakuri.teto.compat.Keys;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.impl.block.Scaffold;
import tech.hakuri.teto.feature.impl.misc.ClickManager;
import tech.hakuri.teto.feature.impl.misc.RotationManager;
import tech.hakuri.teto.utils.AsyncEntityFilter;
import tech.hakuri.teto.utils.RotationUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class KillAura extends Module {
    public KillAura() {
        name = "杀戮光环";
        category = Category.combat;
    }

    public static boolean holdWeapon() {
        Minecraft mc = Minecraft.getInstance();
        ItemStack handItem = Inv.selectedItem(mc.player.getInventory());
        if (handItem.isEmpty()) return false;
        boolean damageable = handItem.isDamageableItem();
        boolean stackable = handItem.isStackable();
        boolean enchanted = handItem.isEnchanted();
        return damageable && !stackable || enchanted;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.mouseHandler.isMouseGrabbed()) {
            if (AsyncEntityFilter.combatPick != null) {
                if (ClickManager.timer.hasTimePassed(ClickManager.genRandomDelayMS())) {
                    if (!mc.gameMode.isDestroying()) {
                        KeyMapping.click(Keys.of(mc.options.keyAttack));
                        ClickManager.timer.reset();
                    }
                }

                var myEye = mc.player.getEyePosition(Timing.partialTick());
                var smoothAABB = AsyncEntityFilter.combatPick.getBoundingBox().move((AsyncEntityFilter.combatPick.getX() - AsyncEntityFilter.combatPick.xOld) * Timing.partialTick(), (AsyncEntityFilter.combatPick.getY() - AsyncEntityFilter.combatPick.yOld) * Timing.partialTick(), (AsyncEntityFilter.combatPick.getZ() - AsyncEntityFilter.combatPick.zOld) * Timing.partialTick());
                var basic = RotationUtils.aimToPoint(myEye, RotationUtils.aabbConsider(myEye, smoothAABB));

                RotationManager.setRotation(basic.x, basic.y);
            } else {
                onDisable();
            }
        }
    }

    @Override
    public void onEnable() {
        ModuleManager.getModule(Scaffold.class).disable();
    }

    @Override
    public void onDisable() {
        RotationManager.resetRotation();
    }
}


















