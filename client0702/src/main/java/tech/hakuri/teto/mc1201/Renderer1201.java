package tech.hakuri.teto.mc1201;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.AABB;
import tech.hakuri.teto.font.FontManager;
import tech.hakuri.teto.font.TrueTypeFont;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.IRenderer;
import tech.hakuri.teto.utils.RenderUtils;

/**
 * {@link IRenderer} 的 Minecraft 1.20.1 / Forge 实现。
 * <p>
 * 内部仍然走 {@code RenderUtils}（Tesselator + getPositionColorShader）和
 * {@code TrueTypeFont}（每字符一张纹理）—— 这一步只做"把 MC API 挡在接口后面"，
 * 不改渲染方式，所以 1.20.1 的画面与迁移前逐像素一致。
 * 渲染质量的改进（字体图集、圆角、批量绘制）留到 UI 重写那一步。
 * <p>
 * <b>坐标约定</b>：接口对外统一用 GUI 坐标。而 {@code TrueTypeFont.drawString} 内部会把
 * pose 缩放 1/guiScale，所以文字坐标必须先乘回 guiScale —— 这个换算收敛在本类里，
 * 调用方不再各自处理（迁移前 UI 各处手动乘、HUD 又不乘，正是画面错位的根因）。
 */
public final class Renderer1201 implements IRenderer {

    private static final Renderer1201 INSTANCE = new Renderer1201();

    /** 帧上下文需要被事件类直接设置，所以单例暴露具体类型而不是接口。 */
    public static Renderer1201 get() {
        return INSTANCE;
    }

    /** 当前帧的 2D 上下文。由 EventRender2D 与 ClickGUI 在绘制入口设置。 */
    private GuiGraphics gui;

    /** 当前帧的 3D 变换栈。由 EventRender3D 设置。 */
    private PoseStack pose;

    @Override
    public void beginFrame2D(Object context) {
        this.gui = (GuiGraphics) context;
    }

    @Override
    public void beginFrame3D(Object context) {
        this.pose = (PoseStack) context;
    }

    private static TrueTypeFont font(FontSize size) {
        return switch (size) {
            case XS -> FontManager.xs;
            case S -> FontManager.s;
            case M -> FontManager.m;
            case L -> FontManager.l;
            case XL -> FontManager.xl;
        };
    }

    @Override
    public void rect(float left, float top, float right, float bottom, int argb) {
        if (gui == null) return;
        RenderUtils.drawRect(gui, left, top, right, bottom, argb);
    }

    /**
     * 几何细分圆角（GuardLite RenderUtils.drawRoundedRect 同款）：
     * 中心十字 5 个矩形 + 4 个 TRIANGLE_FAN 扇形角，顶点数随半径走，任何尺寸都不糊。
     */
    @Override
    public void roundedRect(float x, float y, float w, float h, float radius, int argb) {
        if (gui == null) return;
        RenderUtils.drawRoundedRect(gui.pose(), x, y, w, h, radius, argb);
    }

    @Override
    public void image(java.awt.image.BufferedImage image, float x, float y, float w, float h, int tint) {
        if (gui == null || image == null) return;
        net.minecraft.resources.ResourceLocation location = BufferedImageTexture.of(image);
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, location);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        gui.blit(location, Math.round(x), Math.round(y), 0f, 0f,
                Math.round(w), Math.round(h), image.getWidth(), image.getHeight());
    }

    @Override
    public float text(FontSize size, String str, float x, float y, int argb) {
        if (gui == null) return x;
        float s = guiScale();
        return font(size).drawString(gui, str, x * s, y * s, argb) / s;
    }

    @Override
    public float textShadow(FontSize size, String str, float x, float y, int argb) {
        if (gui == null) return x;
        float s = guiScale();
        return font(size).drawShadowString(gui, str, x * s, y * s, argb) / s;
    }

    @Override
    public float textCentered(FontSize size, String str, float minX, float minY, float maxX, float maxY, int argb) {
        if (gui == null) return minX;
        float s = guiScale();
        return font(size).drawCenteredShadowString(gui, str, minX * s, minY * s, maxX * s, maxY * s, argb) / s;
    }

    @Override
    public float textWidth(FontSize size, String str) {
        return font(size).getStringWidth(str) / guiScale();
    }

    @Override
    public float textHeight(FontSize size) {
        return font(size).getStringHeight("") / guiScale();
    }

    @Override
    public void box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int argb) {
        if (pose == null) return;
        RenderUtils.drawAABBBox(pose, new AABB(minX, minY, minZ, maxX, maxY, maxZ), argb);
    }

    @Override
    public float screenWidth() {
        return (float) (Minecraft.getInstance().getWindow().getWidth() / guiScale());
    }

    @Override
    public float screenHeight() {
        return (float) (Minecraft.getInstance().getWindow().getHeight() / guiScale());
    }

    @Override
    public float guiScale() {
        //不要用 mc.options.guiScale，自动档时它是 0
        return (float) Minecraft.getInstance().getWindow().getGuiScale();
    }

    @Override
    public void beginScissor(float x, float y, float w, float h) {
        if (gui == null) return;
        gui.enableScissor(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
    }

    @Override
    public void endScissor() {
        if (gui == null) return;
        gui.disableScissor();
    }

    /**
     * 面板背景模糊（1.20.1 only）。委托 {@link BlurRenderer1201}：把主渲染目标颜色纹理过一遍
     * 可分离高斯，再以圆角几何贴回面板区域。必须在面板其它绘制之前调。
     */
    @Override
    public void blurRect(float x, float y, float w, float h, float radius) {
        if (gui == null) return;
        BlurRenderer1201.get().renderBlur(gui, x, y, w, h, radius);
    }
}
