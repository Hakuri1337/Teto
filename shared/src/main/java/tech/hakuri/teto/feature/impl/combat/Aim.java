package tech.hakuri.teto.feature.impl.combat;


import tech.hakuri.teto.compat.Timing;
import tech.hakuri.teto.event.impl.EventRender2D;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.utils.AsyncEntityFilter;
import tech.hakuri.teto.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Aim extends Module {

    public static Value speed = new Value("速度", 0.5f, 0.01f, 2f);
    public static Value needPressMouse = new Value("左键时开始", true);
    public static Value stopOnDigging = new Value("挖掘时停止", true);
    public static Value stopOnAimed = new Value("瞄准时停止", true);
    public static Value weapon = new Value("拿着武器时开始", true);

    public static Float yaw;
    public static Float pitch;
    public static Float prevYaw;
    public static Float prevPitch;

    public Aim() {
        name = "自瞄";
        category = Category.combat;
        addValues(speed, needPressMouse, stopOnDigging, stopOnAimed, weapon);
    }

    public static Entity aimedEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getType() == HitResult.Type.ENTITY) {
            return hitResult.getEntity();
        }
        return null;
    }

    public static boolean condition() {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        if (needPressMouse.enable && !mc.options.keyAttack.isDown()) {
            return false;
        }
        if (stopOnDigging.enable && mc.gameMode.isDestroying()) {
            return false;
        }
        if (stopOnAimed.enable && aimedEntity() != null) {
            return false;
        }
        if (weapon.enable && !KillAura.holdWeapon()) {
            return false;
        }
        if (AsyncEntityFilter.combatPick == null) {
            return false;
        }

        return true;
    }


    @Override
    public void onTick(EventTick event) {
        if (!condition()) {
            onDisable();
            return;
        }

        var myEye = mc.player.getEyePosition(Timing.partialTick());
        var tmp = RotationUtils.aimToPoint(myEye, RotationUtils.aabbConsider(myEye, AsyncEntityFilter.combatPick.getBoundingBox()));
        float shortestYaw = RotationUtils.shortestYaw(mc.player.getYRot(), tmp.x);
        float slowYaw = speed.numberValue * RotationUtils.toRelativeYaw(shortestYaw);
        yaw = RotationUtils.patchSensitivity(slowYaw, false);
        float slowPitch = speed.numberValue * RotationUtils.toRelativePitch(tmp.y);
        pitch = RotationUtils.patchSensitivity(slowPitch, false);

    }

    @Override
    public void onRender2D(EventRender2D event) {
        if (yaw != null && pitch != null) {
            mc.player.turn(yaw, pitch);
        }
    }

    @Override
    public void onEnable() {
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        prevYaw = mc.player.getYRot();
        prevPitch = mc.player.getXRot();
    }


    @Override
    public void onDisable() {
        yaw = null;
        pitch = null;
        prevYaw = null;
        prevPitch = null;
    }
}