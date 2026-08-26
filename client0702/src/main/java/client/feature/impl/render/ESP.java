package client.feature.impl.render;

import client.event.impl.EventRender3D;
import client.feature.Category;
import client.feature.Module;
import client.utils.AsyncEntityFilter;
import client.utils.RenderUtils;
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
            aabb = aabb.move((entity.getX() - entity.xOld) * mc.getFrameTime(), (entity.getY() - entity.yOld) * mc.getFrameTime(), (entity.getZ() - entity.zOld) * mc.getFrameTime());
            //相机
            aabb = aabb.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (entity instanceof LivingEntity le) {
                if (le.hurtTime != 0) {
                    RenderUtils.drawAABBBox(event.poseStack, aabb, Color.red.getRGB());
                } else {
                    RenderUtils.drawAABBBox(event.poseStack, aabb, Color.white.getRGB());
                    if (entity == AsyncEntityFilter.tpAuraPick) {
                        RenderUtils.drawAABBBox(event.poseStack, aabb, Color.blue.getRGB());
                    }
                    if (entity == AsyncEntityFilter.combatPick) {
                        RenderUtils.drawAABBBox(event.poseStack, aabb, Color.green.getRGB());
                    }
                }
            }
        }
    }
}