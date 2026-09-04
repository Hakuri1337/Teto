package tech.hakuri.teto.feature.impl.render;

import tech.hakuri.teto.compat.Timing;
import tech.hakuri.teto.event.impl.EventRender3D;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.platform.Platform;
import tech.hakuri.teto.utils.AsyncEntityFilter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class ESP extends Module {

    public ESP() {
        name = "碰撞箱透视";
        category = Category.render;
        toggle();
    }

    @Override
    public void onRender3D(EventRender3D event) {
        for (Entity entity : AsyncEntityFilter.render) {
            AABB aabb = entity.getBoundingBox();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            //平滑
            aabb = aabb.move((entity.getX() - entity.xOld) * Timing.partialTick(), (entity.getY() - entity.yOld) * Timing.partialTick(), (entity.getZ() - entity.zOld) * Timing.partialTick());
            //相机
            aabb = aabb.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (entity instanceof LivingEntity le) {
                if (le.hurtTime != 0) {
                    box(aabb, Color.red.getRGB());
                } else {
                    box(aabb, Color.white.getRGB());
                    if (entity == AsyncEntityFilter.tpAuraPick) {
                        box(aabb, Color.blue.getRGB());
                    }
                    if (entity == AsyncEntityFilter.combatPick) {
                        box(aabb, Color.green.getRGB());
                    }
                }
            }
        }
    }

    private static void box(AABB aabb, int argb) {
        Platform.renderer().box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, argb);
    }
}