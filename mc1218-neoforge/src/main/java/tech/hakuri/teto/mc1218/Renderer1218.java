package tech.hakuri.teto.mc1218;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.IRenderer;

/**
 * {@link IRenderer} 的 Minecraft 1.21.8 / NeoForge 实现。
 * <p>
 * 与 1.20.1 实现的关键差别：
 * <ul>
 *   <li>1.21.6 起 GUI 绘制是两阶段的 —— {@code GuiGraphics} 的方法只往
 *       {@code GuiRenderState} 提交，真正出图在 {@code GuiRenderer}。所以这里
 *       只管调 {@code fill}/{@code drawString}，不再自己 begin/end Tesselator。</li>
 *   <li>文字直接用原版 {@code Font}。它是图集化、批量提交的，比 1.20.1 那套
 *       "每字符一张纹理 + 每字符一个 draw call" 的实现好得多，所以这边不移植它。</li>
 *   <li>坐标本来就是 GUI 空间（{@code guiWidth()}/{@code guiHeight()}），
 *       不需要像 1.20.1 那样手动处理 guiScale。</li>
 *   <li><b>颜色必须带 alpha</b>：1.21.6 起 {@code drawString} 按 ARGB 解释 int，
 *       alpha 为 0 会完全不渲染。接口约定本来就要求 ARGB，这里再兜一层。</li>
 * </ul>
 */
public final class Renderer1218 implements IRenderer {

    private static final Renderer1218 INSTANCE = new Renderer1218();

    public static Renderer1218 get() {
        return INSTANCE;
    }

    private GuiGraphics gui;
    private PoseStack pose;

    /** 五档字号各一张图集。懒加载 —— 构造时游戏可能还没准备好 GL 上下文。 */
    private final java.util.EnumMap<FontSize, AtlasFont> fonts = new java.util.EnumMap<>(FontSize.class);
    private boolean fontsUnavailable;

    /** 与 1.20.1 的 TrueTypeFont 字号一致，保证两版布局可比。 */
    private static int pixelSize(FontSize size) {
        return switch (size) {
            case XS -> 10;
            case S -> 20;
            case M -> 25;
            case L -> 30;
            case XL -> 100;
        };
    }

    /**
     * 取对应字号的图集字体。
     * <p>
     * <b>必须在渲染线程上创建</b>：构造函数里会建 {@code NativeImage} 并注册
     * {@code DynamicTexture}，那是 GL 调用。而 {@code textHeight()} 会被
     * {@code HUD.onModuleToggle} 调到，那条路径来自 {@code ClientEntry1218.init()}
     * 里的 {@code hud.toggle()} —— 跑在注入器的 loader 线程上，不是渲染线程。
     * 早先版本没做这个判断，于是首次调用就在错误的线程上失败，
     * 把 {@code fontsUnavailable} 永久置真，结果永远退回原版字体（表现就是「字体没变化」）。
     * <p>
     * 所以：不在渲染线程时直接返回 null（本次退回原版字体，但<b>不</b>标记为不可用），
     * 只有真正在渲染线程上失败才永久放弃。
     */
    private AtlasFont font(FontSize size) {
        if (fontsUnavailable) {
            return null;
        }
        AtlasFont cached = fonts.get(size);
        if (cached != null) {
            return cached;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            return null;
        }
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(System.getenv("WINDIR"), "Fonts", "msyh.ttc");
            if (!java.nio.file.Files.isRegularFile(file)) {
                file = java.nio.file.Paths.get(System.getenv("WINDIR"), "Fonts", "Deng.ttf");
            }
            AtlasFont created = new AtlasFont(file, pixelSize(size), size.name().toLowerCase());
            fonts.put(size, created);
            System.out.println("[teto] 字体图集就绪：" + size + " " + pixelSize(size) + "px " + file);
            return created;
        } catch (Throwable t) {
            System.err.println("[teto] 字体图集初始化失败，退回原版字体：" + t);
            t.printStackTrace(System.err);
            fontsUnavailable = true;
            return null;
        }
    }

    @Override
    public void beginFrame2D(Object context) {
        this.gui = (GuiGraphics) context;
        //把本帧（以及上一帧末尾）新烘的字形推到 GPU。
        //必须放在这里而不是某个只有旧代码会调的方法上 —— 事件层统一走 beginFrame2D。
        for (AtlasFont f : fonts.values()) {
            f.flush();
        }
    }

    @Override
    public void beginFrame3D(Object context) {
        this.pose = (PoseStack) context;
    }

    /** 原版 Font 只有一个字号，用缩放模拟档位；缩放通过 pose 施加。 */
    private static float scaleOf(FontSize size) {
        return switch (size) {
            case XS -> 0.75F;
            case S -> 1.0F;
            case M -> 1.25F;
            case L -> 1.5F;
            case XL -> 5.0F;
        };
    }

    /** 补上不透明 alpha —— 1.21.6 起 alpha=0 的颜色完全不渲染。 */
    private static int opaque(int argb) {
        return (argb >>> 24) == 0 ? (argb | 0xFF000000) : argb;
    }

    @Override
    public void rect(float left, float top, float right, float bottom, int argb) {
        if (gui == null) return;
        gui.fill(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom), opaque(argb));
    }

    // ===== CPU 栅格化图的贴图（Material UI 的唯一贴图原语）=====

    /** 已上传的 UI 纹理缓存：源图 identity + 缩放后尺寸 → 纹理位置。 */
    private final Map<ImageKey, ResourceLocation> uiTextures = new LinkedHashMap<>();

    @Override
    public void image(java.awt.image.BufferedImage image, float x, float y, float w, float h, int tint) {
        if (gui == null || image == null) return;
        int dw = Math.max(1, Math.round(w));
        int dh = Math.max(1, Math.round(h));
        ResourceLocation loc = uiTexture(image, dw, dh);
        //tint==0 表示不染色（阴影/辉光自带颜色），用纯白
        int color = tint == 0 ? 0xFFFFFFFF : opaque(tint);
        gui.blit(RenderPipelines.GUI_TEXTURED, loc,
                Math.round(x), Math.round(y), 0F, 0F, dw, dh, dw, dh, color);
    }

    /**
     * 把 BufferedImage 按目标尺寸缩放成 NativeImage 并注册成纹理。
     * 缓存键是「源图 identity + 目标尺寸」，所以同一形状同一尺寸只上传一次。
     * 缩放用 Graphics2D 平滑插值 —— 这是九宫格拉伸的地方，CPU 做，每帧最多一次新尺寸。
     */
    private ResourceLocation uiTexture(java.awt.image.BufferedImage src, int dw, int dh) {
        ImageKey key = new ImageKey(System.identityHashCode(src), dw, dh);
        ResourceLocation cached = uiTextures.get(key);
        if (cached != null) return cached;

        java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(dw, dh, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, dw, dh, null);
        g.dispose();

        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, dw, dh, false);
        for (int px = 0; px < dw; px++) {
            for (int py = 0; py < dh; py++) {
                int argb = scaled.getRGB(px, py);
                //NativeImage 存 ABGR
                nativeImage.setPixel(px, py, (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >> 16) & 0xFF));
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> "teto-ui-" + key.hash, nativeImage);
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("teto", "ui/" + key.hash);
        Minecraft.getInstance().getTextureManager().register(loc, texture);
        uiTextures.put(key, loc);
        return loc;
    }

    private static final class ImageKey {
        final int hash;
        private final int srcId, w, h;
        ImageKey(int srcId, int w, int h) {
            this.srcId = srcId; this.w = w; this.h = h;
            this.hash = 31 * (31 * srcId + w) + h;
        }
        @Override public boolean equals(Object o) {
            return o instanceof ImageKey k && k.srcId == srcId && k.w == w && k.h == h;
        }
        @Override public int hashCode() { return hash; }
    }

    @Override
    public void image(java.awt.image.BufferedImage image, float x, float y, float w, float h, int tint) {
        if (gui == null || image == null) return;
        net.minecraft.resources.ResourceLocation location = BufferedImageTexture.of(image);
        //tint==0 表示不染色（阴影/辉光本身带色），否则按 tint 染（白底圆角染成表面色/primary）
        int color = tint == 0 ? 0xFFFFFFFF : opaque(tint);
        gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, location,
                Math.round(x), Math.round(y), 0f, 0f,
                Math.round(w), Math.round(h), image.getWidth(), image.getHeight(), color);
    }

    @Override
    public float text(FontSize size, String str, float x, float y, int argb) {
        return draw(size, str, x, y, argb, false);
    }

    @Override
    public float textShadow(FontSize size, String str, float x, float y, int argb) {
        return draw(size, str, x, y, argb, true);
    }

    private float draw(FontSize size, String str, float x, float y, int argb, boolean shadow) {
        if (gui == null) return x;
        AtlasFont atlas = font(size);
        if (atlas != null) {
            //把 pose 缩到 1/guiScale，再把 GUI 坐标乘回 guiScale 传进去 ——
            //这样一个烘焙纹素 = 一个物理像素，字形既不放大也不被采样糊掉。
            float s = guiScale();
            gui.pose().pushMatrix();
            gui.pose().scale(1F / s, 1F / s);
            if (shadow) {
                //阴影就是同一串字偏移 1 物理像素画一遍半透明黑
                atlas.drawPx(gui, str, x * s + 1F, y * s + 1F, 0x80000000);
            }
            atlas.drawPx(gui, str, x * s, y * s, opaque(argb));
            gui.pose().popMatrix();
            return x + atlas.widthPx(str) / s;
        }
        //退路：原版 Font + pose 缩放模拟字号
        float s = scaleOf(size);
        gui.pose().pushMatrix();
        gui.pose().scale(s, s);
        gui.drawString(Minecraft.getInstance().font, str, Math.round(x / s), Math.round(y / s), opaque(argb), shadow);
        gui.pose().popMatrix();
        return x + textWidth(size, str);
    }

    @Override
    public float textCentered(FontSize size, String str, float minX, float minY, float maxX, float maxY, int argb) {
        float w = textWidth(size, str);
        float h = textHeight(size);
        return textShadow(size, str, minX + (maxX - minX) / 2F - w / 2F, minY + (maxY - minY) / 2F - h / 2F, argb);
    }

    @Override
    public float textWidth(FontSize size, String str) {
        AtlasFont atlas = font(size);
        //图集是物理像素空间的，除回 guiScale 才是接口约定的 GUI 坐标
        return atlas != null ? atlas.widthPx(str) / guiScale()
                : Minecraft.getInstance().font.width(str) * scaleOf(size);
    }

    @Override
    public float textHeight(FontSize size) {
        AtlasFont atlas = font(size);
        return atlas != null ? atlas.lineHeightPx() / guiScale()
                : Minecraft.getInstance().font.lineHeight * scaleOf(size);
    }

    @Override
    public void box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int argb) {
        if (pose == null) return;
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        float a = (argb >>> 24 & 0xFF) / 255F;
        float r = (argb >> 16 & 0xFF) / 255F;
        float g = (argb >> 8 & 0xFF) / 255F;
        float b = (argb & 0xFF) / 255F;
        //ShapeRenderer 是 1.21.x 的原版线框助手（1.20.1 时这套逻辑在 LevelRenderer 里）
        ShapeRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.lines()),
                minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a == 0F ? 1F : a);
    }

    @Override
    public float screenWidth() {
        return gui == null ? 0F : gui.guiWidth();
    }

    @Override
    public float screenHeight() {
        return gui == null ? 0F : gui.guiHeight();
    }

    @Override
    public float guiScale() {
        return (float) Minecraft.getInstance().getWindow().getGuiScale();
    }
}
