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

    // ===== NavenClickGUI 用的几何圆角（GuardLite RenderUtils.drawRoundedRect 移植）=====

    public static void drawRoundedRect(PoseStack poseStack, float x, float y, float width, float height, float edgeRadius, int color) {
        if ((color >>> 24) == 0) color |= 0xFF000000;
        if (edgeRadius < 0.0F) edgeRadius = 0.0F;
        if (edgeRadius > width / 2.0F) edgeRadius = width / 2.0F;
        if (edgeRadius > height / 2.0F) edgeRadius = height / 2.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        quad(buffer, matrix, x + edgeRadius, y + edgeRadius, width - edgeRadius * 2F, height - edgeRadius * 2F, r, g, b, a);
        quad(buffer, matrix, x + edgeRadius, y, width - edgeRadius * 2F, edgeRadius, r, g, b, a);
        quad(buffer, matrix, x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2F, edgeRadius, r, g, b, a);
        quad(buffer, matrix, x, y + edgeRadius, edgeRadius, height - edgeRadius * 2F, r, g, b, a);
        quad(buffer, matrix, x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2F, r, g, b, a);
        tesselator.end();

        //半径小时也要够多段数，否则小圆角看起来像方的；半径大时封顶 90 避免浪费顶点。
        //与 Renderer1218 的公式一致，保证两版小圆角同样平滑。
        int vertices = (int) Math.min(Math.max(edgeRadius * 2.5F, 12.0F), 90.0F);
        fan(tesselator, buffer, matrix, x + edgeRadius, y + edgeRadius, edgeRadius, vertices, 180, r, g, b, a);
        fan(tesselator, buffer, matrix, x + width - edgeRadius, y + edgeRadius, edgeRadius, vertices, 90, r, g, b, a);
        fan(tesselator, buffer, matrix, x + edgeRadius, y + height - edgeRadius, edgeRadius, vertices, 270, r, g, b, a);
        fan(tesselator, buffer, matrix, x + width - edgeRadius, y + height - edgeRadius, edgeRadius, vertices, 0, r, g, b, a);

        RenderSystem.disableBlend();
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix, float x, float y, float w, float h, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y + h, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + w, y, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, 0.0F).color(r, g, b, a).endVertex();
    }

    private static void fan(Tesselator tesselator, BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float radius, int vertices, int quadrant, float r, float g, float b, float a) {
        buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a).endVertex();
        for (int i = 0; i <= vertices; i++) {
            double angle = (Math.PI * 2.0) * (i + quadrant) / (vertices * 4.0);
            buffer.vertex(matrix, (float) (cx + Math.sin(angle) * radius), (float) (cy + Math.cos(angle) * radius), 0.0F).color(r, g, b, a).endVertex();
        }
        tesselator.end();
    }
}
