package tech.hakuri.teto.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class RenderUtils {

    //by 氯雷他定
    //also see LevelRenderer.renderLineBox() and EntityRenderDispatcher.renderHitbox()
    public static void drawAABBBox(PoseStack poseStack, AABB aabb, int color) {

        RenderSystem.setShader(new Supplier<>() {
            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buildBoxLines(poseStack.last().pose(), buffer, aabb, r, g, b, a);
        Tesselator.getInstance().end();
    }

    //by 氯雷他定
    public static void buildBoxLines(Matrix4f matrix, BufferBuilder buffer, AABB bb, float r, float g, float b, float a) {
        // 底部四边形
        addLine(matrix, buffer, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, r, g, b, a);
        addLine(matrix, buffer, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.minY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.minY, bb.maxZ, bb.minX, bb.minY, bb.maxZ, r, g, b, a);

        // 顶部四边形
        addLine(matrix, buffer, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, r, g, b, a);
        addLine(matrix, buffer, bb.minX, bb.maxY, bb.maxZ, bb.minX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.maxY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, r, g, b, a);

        // 垂直边
        addLine(matrix, buffer, bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(matrix, buffer, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, r, g, b, a);
        addLine(matrix, buffer, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, r, g, b, a);
    }

    //by 氯雷他定
    public static void addLine(Matrix4f matrix, BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
    }

    //see GuiGraphics.fill()
    public static void drawRect(GuiGraphics guiGraphics, float left, float top, float right, float bottom, int color) {
        RenderSystem.enableBlend();

        RenderSystem.setShader(new Supplier<>() {
            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        //GL坐标系，左下角是原点
        bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, right, top, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, left, top, 0.0F).color(r, g, b, a).endVertex();

        Tesselator.getInstance().end();

        RenderSystem.disableBlend();
    }


}