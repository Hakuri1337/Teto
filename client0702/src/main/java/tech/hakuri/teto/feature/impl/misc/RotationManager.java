package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.event.impl.EventCamera;
import tech.hakuri.teto.event.impl.EventMoveInput;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.utils.RotationUtils;
import net.minecraft.client.Minecraft;

/**
 * @author 手淫
 */
public class RotationManager extends Module {
    public static Float startYaw;
    public static Float startPitch;
    public static Float camYaw;
    public static Float camPitch;
    public static Float curYaw;
    public static Float curPitch;
    public static Double startMouseX;
    public static Double startMouseY;
    public static Double prevX;
    public static Double prevY;

    public RotationManager() {
        name = "转头管理器";
        category = Category.misc;
        toggle();
    }

    public static void setRotation(float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();


        if (curYaw == null || curPitch == null) {
            startYaw = mc.player.getYRot();
            startPitch = mc.player.getXRot();
            startMouseX = mc.mouseHandler.xpos();
            startMouseY = mc.mouseHandler.ypos();
            prevX = startMouseX;
            prevY = startMouseY;
            camYaw = startYaw;
            camPitch = startPitch;
        }


        curYaw = RotationUtils.patchSensitivity(RotationUtils.shortestYaw(mc.player.getYRot(), yaw), true);
        curPitch = RotationUtils.patchSensitivity(pitch, true);
        mc.player.setYRot(curYaw);
        mc.player.setXRot(curPitch);
    }

    public static void resetRotation() {
        Minecraft mc = Minecraft.getInstance();

        if (camYaw != null) mc.player.setYRot(RotationUtils.shortestYaw(mc.player.getYRot(), camYaw));
        if (camPitch != null) mc.player.setXRot(RotationUtils.limitPitch90(camPitch));
        curYaw = null;
        curPitch = null;
        startYaw = null;
        startPitch = null;
        startMouseX = null;
        startMouseY = null;
        camYaw = null;
        camPitch = null;
    }

    //氯雷他定
    public static float direction(float rotationYaw, float moveForward, float moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float tmp = 1F;

        if (moveForward < 0F) tmp = -0.5F;
        else if (moveForward > 0F) tmp = 0.5F;

        if (moveStrafing < 0F) rotationYaw += 90F * tmp;
        else if (moveStrafing > 0F) rotationYaw -= 90F * tmp;

        return rotationYaw;
    }

    @Override
    public void onCamera(EventCamera e) {
        Minecraft mc = Minecraft.getInstance();

        if (camYaw != null) e.yaw = camYaw;
        if (camPitch != null) e.pitch = RotationUtils.limitPitch90(camPitch);

        if (mc.mouseHandler.isMouseGrabbed()) {
            if (camYaw == null) return;
            if (camPitch == null) return;

            if (prevX == null) prevX = mc.mouseHandler.xpos();
            if (prevY == null) prevY = mc.mouseHandler.ypos();

            double dx = mc.mouseHandler.xpos() - prevX;
            double dy = mc.mouseHandler.ypos() - prevY;

            var slowX = dx * RotationUtils.getGCD(true);
            var slowY = dy * RotationUtils.getGCD(true);

            e.yaw = camYaw += (float) slowX;
            e.pitch = RotationUtils.limitPitch90(camPitch += (float) slowY);

            prevX = mc.mouseHandler.xpos();
            prevY = mc.mouseHandler.ypos();

        }
    }

    //氯雷他定
    @Override
    public void onMoveInput(EventMoveInput event) {
        if (camYaw == null) return;
        if (camPitch == null) return;

        float originForward = event.keyboardInput.forwardImpulse;
        float originLeft = event.keyboardInput.leftImpulse;

        if (originForward == 0 && originLeft == 0) return;

        float originDirection = RotationUtils.limitYaw180(direction(RotationUtils.limitYaw180(camYaw), originForward, originLeft));

        float calcForward = 0, calcLeft = 0, calcDiff = Float.MAX_VALUE;

        for (float tryForward = -1F; tryForward <= 1F; tryForward += 1F) {
            for (float tryLeft = -1F; tryLeft <= 1F; tryLeft += 1F) {
                if (tryLeft == 0 && tryForward == 0) continue;

                float tryDirection = RotationUtils.limitYaw180(direction(RotationUtils.limitYaw180(Minecraft.getInstance().player.getYRot()), tryForward, tryLeft));
                float directionDiff = Math.abs(originDirection - tryDirection);

                if (directionDiff < calcDiff) {
                    calcDiff = directionDiff;
                    calcForward = tryForward;
                    calcLeft = tryLeft;
                }
            }
        }


        event.keyboardInput.forwardImpulse = calcForward;
        event.keyboardInput.leftImpulse = calcLeft;
    }

    @Override
    public void onEnable() {
        prevX = mc.mouseHandler.xpos();
        prevY = mc.mouseHandler.ypos();
    }

    @Override
    public void onDisable() {
        resetRotation();
    }
}
