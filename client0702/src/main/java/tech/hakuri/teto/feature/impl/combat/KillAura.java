package tech.hakuri.teto.feature.impl.combat;


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
        ItemStack handItem = mc.player.getInventory().getSelected();
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
                        KeyMapping.click(mc.options.keyAttack.getKey());
                        ClickManager.timer.reset();
                    }
                }

                var myEye = mc.player.getEyePosition(mc.getFrameTime());
                var smoothAABB = AsyncEntityFilter.combatPick.getBoundingBox().move((AsyncEntityFilter.combatPick.getX() - AsyncEntityFilter.combatPick.xOld) * mc.getFrameTime(), (AsyncEntityFilter.combatPick.getY() - AsyncEntityFilter.combatPick.yOld) * mc.getFrameTime(), (AsyncEntityFilter.combatPick.getZ() - AsyncEntityFilter.combatPick.zOld) * mc.getFrameTime());
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


















