package client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * @author 手淫
 * 冷知识：玩家的yaw范围是负无穷到正无穷，并不限制在[-180,180]，你只是需要给你的转头算法限制，不要让他一次性转好几圈，或者本来转2度非要转-178度
 */
public class RotationUtils {

    public static Vec3 aabbCenter(Vec3 from, AABB box) {
        var x = box.minX + (box.maxX - box.minX) / 2;
        var y = box.minY + (box.maxY - box.minY) / 2;
        var z = box.minZ + (box.maxZ - box.minZ) / 2;
        return new Vec3(x, y, z);
    }

    public static Vec3 aabbConsider(Vec3 from, AABB box) {
        var x = box.minX + (box.maxX - box.minX) / 2;
        var y = Mth.clamp(from.y, box.minY, box.maxY);
        var z = box.minZ + (box.maxZ - box.minZ) / 2;
        return new Vec3(x, y, z);
    }

    public static Vec3 aabbNearest(Vec3 from, AABB box) {
        var x = Mth.clamp(from.x, box.minX, box.maxX);
        var y = Mth.clamp(from.y, box.minY, box.maxY);
        var z = Mth.clamp(from.z, box.minZ, box.maxZ);
        return new Vec3(x, y, z);
    }

    public static Vec2 aimToPoint(Vec3 from, Vec3 to) {//FPS游戏经典公式了属于是
        Vec3 diff = to.subtract(from);
        var distance = Math.hypot(diff.x, diff.z);
        var yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;
        var pitch = (float) -Math.toDegrees(Mth.atan2(diff.y, distance));
        return new Vec2(yaw, pitch);
    }

    //bypass grim aim360
    //yeah easy like this
    public static float shortestYaw(float from, float to) {
        return from + limitYaw180(to - from);
    }

    public static float limitYaw180(float yaw) {
        return Mth.wrapDegrees(yaw);
    }

    public static float limitPitch90(float pitch) {
        return Mth.clamp(pitch, -90, 90);
    }

    //hellospace3
    //知识+1
    public static double fovCalc(Entity from, Entity to) {
        Minecraft mc = Minecraft.getInstance();

        Vec3 fromPos = from.position();
        Vec3 toPos = to.position();

        Vec3 fromDirection = from.getViewVector(mc.getFrameTime()).normalize();
        Vec3 toDirection = toPos.subtract(fromPos).normalize();

        return Math.toDegrees(Math.acos(fromDirection.dot(toDirection)));
    }

    public static float toAbsoluteYaw(float in) {
        return Minecraft.getInstance().player.getYRot() + in;
    }

    public static float toAbsolutePitch(float in) {
        return Minecraft.getInstance().player.getXRot() + in;
    }

    public static float toRelativeYaw(float in) {
        return in - Minecraft.getInstance().player.getYRot();
    }

    public static float toRelativePitch(float in) {
        return in - Minecraft.getInstance().player.getXRot();
    }

    /**
     * also see MouseHandler.turnPlayer()
     */
    public static float patchSensitivity(float in, boolean include0_15) {
        var gcd = getGCD(include0_15);
        return Math.round(in / gcd) * gcd;
    }

    /**
     * also see MouseHandler.turnPlayer()
     */
    public static float getGCD(boolean include0_15) {
        var sensitivity = Minecraft.getInstance().options.sensitivity().get().floatValue() * 0.6f + 0.2f;
        var tmp = sensitivity * sensitivity * sensitivity * 8f;
        if (include0_15) tmp *= 0.15f;//0.15在mc.player.turn()里，其他都在MouseHandler.turnPlayer()里
        return tmp;
    }

    public static double distanceToEntityAABBNearest(Entity from, Entity to) {
        Minecraft mc = Minecraft.getInstance();
        return from.getEyePosition(mc.getFrameTime()).distanceTo(aabbNearest(from.getEyePosition(mc.getFrameTime()), to.getBoundingBox()));
    }

}