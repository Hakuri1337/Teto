package tech.hakuri.teto.mc1218;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 STBTruetype 的字体图集渲染器（1.21.8）。
 * <p>
 * <b>为什么不用 1.20.1 那套 TrueTypeFont</b>：那份实现给<b>每个字符</b>建一张独立的
 * {@code DynamicTexture}，并且每字符做一次完整的 Tesselator begin/end —— 一个 47 项的
 * 功能列表每帧要几百个 draw call，字形度量还是整数取整（没有 kerning、基线固定用 ascent
 * 导致垂直居中永远偏）。那是 UI 又慢又糊的头号原因，不该移植过来。
 * <p>
 * <b>本实现</b>：所有字形烘进<b>一张</b> {@value #ATLAS} 见方的图集，按需增量烘焙
 * （中文字符太多，不可能预先全烤），用 shelf 装箱摆放；绘制走
 * {@code GuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ...)}，同一张纹理让
 * 1.21.6 的两阶段 GUI 系统自己合批。字形度量直接取 STB 的 hmetrics/vmetrics，
 * 所以字距和基线是准的。
 * <p>
 * lwjgl-stb 本来就在 Minecraft 的 classpath 上（1.21.8 用的是 3.3.3），
 * 所以这个方案<b>不引入任何新依赖</b>，也不需要发额外的 native。
 */
public final class AtlasFont implements AutoCloseable {

    /** 图集边长。1024² RGBA = 4MB，20px 字号下可容纳约 2500 个字形，中文够用。 */
    private static final int ATLAS = 1024;

    /** 字形之间留 1px，避免线性采样时相邻字形渗色。 */
    private static final int PADDING = 1;

    private final STBTTFontinfo info;
    private final ByteBuffer fontData;
    private final float scale;
    private final float ascent;
    private final float lineHeight;
    private final ResourceLocation location;
    private final NativeImage image;
    private final DynamicTexture texture;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    private int penX = PADDING;
    private int penY = PADDING;
    private int rowHeight;
    private boolean dirty;

    private record Glyph(float u, float v, float w, float h, float xOff, float yOff, float advance) {
    }

    /**
     * @param fontFile  TTF 或 TTC 文件路径
     * @param pixelSize 像素字号
     * @param id        纹理注册用的标识，同一个客户端里多个字号要互不相同
     */
    public AtlasFont(Path fontFile, float pixelSize, String id) throws Exception {
        byte[] bytes = Files.readAllBytes(fontFile);
        //STB 要求字体数据在 native 内存里，且生命周期覆盖整个 STBTTFontinfo 的使用期，
        //所以这里不能用 heap ByteBuffer
        this.fontData = org.lwjgl.system.MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
        this.info = STBTTFontinfo.create();

        //msyh.ttc 是 TrueType Collection（多字体打包），必须取子字体偏移，
        //直接 InitFont(data, 0) 对 ttc 会失败
        int offset = STBTruetype.stbtt_GetFontOffsetForIndex(fontData, 0);
        if (!STBTruetype.stbtt_InitFont(info, fontData, offset < 0 ? 0 : offset)) {
            throw new IllegalStateException("STB 无法解析字体：" + fontFile);
        }

        this.scale = STBTruetype.stbtt_ScaleForPixelHeight(info, pixelSize);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var a = stack.mallocInt(1);
            var d = stack.mallocInt(1);
            var g = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(info, a, d, g);
            this.ascent = a.get(0) * scale;
            this.lineHeight = (a.get(0) - d.get(0) + g.get(0)) * scale;
        }

        this.image = new NativeImage(NativeImage.Format.RGBA, ATLAS, ATLAS, true);
        this.texture = new DynamicTexture(() -> "teto-atlas-" + id, image);
        //关掉双线性过滤：图集是按 1:1 纹素对物理像素绘制的，线性采样只会把边缘糊掉
        this.texture.setFilter(false, false);
        this.texture.setClamp(true);
        this.location = ResourceLocation.fromNamespaceAndPath("teto", "font/" + id);
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    /** 行高，物理像素。 */
    public float lineHeightPx() {
        return lineHeight;
    }

    /** 取字形，没烘过就现烘。只在渲染线程调用（绘制路径本来就在渲染线程上）。 */
    private Glyph glyph(int codePoint) {
        Glyph cached = glyphs.get(codePoint);
        if (cached != null) {
            return cached;
        }
        Glyph baked = bake(codePoint);
        glyphs.put(codePoint, baked);
        return baked;
    }

    private Glyph bake(int codePoint) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var adv = stack.mallocInt(1);
            var lsb = stack.mallocInt(1);
            STBTruetype.stbtt_GetCodepointHMetrics(info, codePoint, adv, lsb);
            float advance = adv.get(0) * scale;

            var w = stack.mallocInt(1);
            var h = stack.mallocInt(1);
            var xo = stack.mallocInt(1);
            var yo = stack.mallocInt(1);
            ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(info, scale, scale, codePoint, w, h, xo, yo);
            if (bitmap == null) {
                //空白字形（空格等），只有推进量
                return new Glyph(0, 0, 0, 0, 0, 0, advance);
            }
            int gw = w.get(0);
            int gh = h.get(0);
            try {
                if (penX + gw + PADDING > ATLAS) {
                    penX = PADDING;
                    penY += rowHeight + PADDING;
                    rowHeight = 0;
                }
                if (penY + gh + PADDING > ATLAS) {
                    //图集满了。不做重建（会让已发出的 UV 全部失效），退化为不可见字形。
                    System.err.println("[teto] 字体图集已满，字形 U+" + Integer.toHexString(codePoint) + " 无法烘焙");
                    return new Glyph(0, 0, 0, 0, 0, 0, advance);
                }
                //STB 给的是单通道覆盖度；写成白色 + alpha=覆盖度，
                //这样 blit 时用 color 参数染色就能得到任意颜色的文字
                for (int y = 0; y < gh; y++) {
                    for (int x = 0; x < gw; x++) {
                        int coverage = bitmap.get(y * gw + x) & 0xFF;
                        image.setPixel(penX + x, penY + y, (coverage << 24) | 0x00FFFFFF);
                    }
                }
                Glyph glyph = new Glyph(penX, penY, gw, gh, xo.get(0), yo.get(0), advance);
                penX += gw + PADDING;
                rowHeight = Math.max(rowHeight, gh);
                dirty = true;
                return glyph;
            } finally {
                STBTruetype.stbtt_FreeBitmap(bitmap);
            }
        }
    }

    /** 文本宽度，单位与绘制坐标一致。 */
    /** 字符串宽度，物理像素。 */
    public float widthPx(String text) {
        float total = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            total += glyph(cp).advance();
            i += Character.charCount(cp);
        }
        return total;
    }

    /**
     * 绘制文本，返回结束时的 x。
     * <p>
     * {@code y} 是文本顶部（与 1.20.1 的调用约定一致），内部加 ascent 换成基线。
     */
    /**
     * 绘制一行文字，坐标与返回值都在<b>物理像素</b>空间。
     * <p>
     * 调用方（{@link Renderer1218}）负责先把 pose 缩放 1/guiScale 并把 GUI 坐标乘回
     * guiScale 再传进来。这样一个烘焙纹素恰好对应一个物理像素，字形才是锐的 ——
     * 早先直接把烘焙像素当 GUI 单位用，于是在 guiScale=3 的机器上字被放大三倍、
     * 同时被线性采样糊掉，就是「字体过大且模糊」的原因。
     */
    public float drawPx(GuiGraphics gui, String text, float x, float y, int argb) {
        float penXf = x;
        float baseline = y + ascent;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            Glyph g = glyph(cp);
            if (g.w() > 0 && g.h() > 0) {
                //xOff/yOff 是 STB 给的相对基线偏移；yOff 为负（字形在基线之上）
                int gx = Math.round(penXf + g.xOff());
                int gy = Math.round(baseline + g.yOff());
                gui.blit(RenderPipelines.GUI_TEXTURED, location,
                        gx, gy, g.u(), g.v(), (int) g.w(), (int) g.h(), ATLAS, ATLAS, argb);
            }
            penXf += g.advance();
            i += Character.charCount(cp);
        }
        return penXf;
    }

    /**
     * 把本帧新烘的字形推到 GPU。整张图集一次性上传，所以只在真有新字形时做 ——
     * 新字符只在首次出现时烘焙，热身几帧后这里就不再触发了。
     * 由 {@code Renderer1218} 在帧开始时调用。
     */
    public void flush() {
        if (!dirty) {
            return;
        }
        texture.upload();
        dirty = false;
    }

    @Override
    public void close() {
        Minecraft.getInstance().getTextureManager().release(location);
        texture.close();
        org.lwjgl.system.MemoryUtil.memFree(fontData);
    }
}
