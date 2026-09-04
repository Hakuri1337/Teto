package tech.hakuri.teto.raster;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * 九宫格（nine-patch）：把一张带圆角/阴影的源图按 3×3 切开，
 * 拉伸中间无内容的区域，让任意尺寸的目标都保持圆角与阴影边缘清晰。
 * <p>
 * 这是「廉价路径」的核心：形状烘一次，尺寸靠拉伸，运行时零模糊、零重算。
 */
public final class NinePatch {

    private final BufferedImage src;
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    /**
     * @param src    源图（正方形或近正方形，内容是居中的形状 + 周围留白）
     * @param inset  四个方向不拉伸的边缘厚度（px）。阴影/辉光图要取「留白 + 圆角」的总厚度。
     */
    public NinePatch(BufferedImage src, int inset) {
        this(src, inset, inset, inset, inset);
    }

    public NinePatch(BufferedImage src, int left, int top, int right, int bottom) {
        this.src = src;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * 画到目标 Graphics2D 的给定矩形里，并用 {@code tint} 给整张贴图染色。
     * 白底源图 + tint 染色 = 一份纹理全色通用（表面色、primary、半透明都行）。
     * 传 {@code null} 表示不染色（阴影/辉光这类本身带颜色的用）。
     */
    public void draw(Graphics2D g, float x, float y, float w, float h, Integer tint) {
        if (tint != null) {
            g = (Graphics2D) g.create();
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(new Color(tint, true));
            //用一幅临时图染色：先按原样画，再用 SrcIn 乘上目标色
            BufferedImage tmp = new BufferedImage((int) Math.ceil(w), (int) Math.ceil(h), BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tmp.createGraphics();
            tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            draw(tg, 0, 0, w, h, null);
            tg.setComposite(AlphaComposite.SrcIn);
            tg.setColor(new Color(tint, true));
            tg.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
            tg.dispose();
            g.drawImage(tmp, (int) x, (int) y, null);
            g.dispose();
            return;
        }
        draw(g, x, y, w, h);
    }

    /** 画到目标 Graphics2D 的给定矩形里。 */
    public void draw(Graphics2D g, float x, float y, float w, float h) {
        int sw = src.getWidth();
        int sh = src.getHeight();
        int cw = Math.max(1, sw - left - right);   // 源中心宽
        int ch = Math.max(1, sh - top - bottom);   // 源中心高
        float dw = Math.max(0, w - left - right);  // 目标中心宽
        float dh = Math.max(0, h - top - bottom);  // 目标中心高

        // 四角：原样
        blit(g, 0, 0, left, top, x, y, left, top);
        blit(g, sw - right, 0, right, top, x + left + dw, y, right, top);
        blit(g, 0, sh - bottom, left, bottom, x, y + top + dh, left, bottom);
        blit(g, sw - right, sh - bottom, right, bottom, x + left + dw, y + top + dh, right, bottom);
        // 四边：单向拉伸
        blit(g, left, 0, cw, top, x + left, y, dw, top);
        blit(g, left, sh - bottom, cw, bottom, x + left, y + top + dh, dw, bottom);
        blit(g, 0, top, left, ch, x, y + top, left, dh);
        blit(g, sw - right, top, right, ch, x + left + dw, y + top, right, dh);
        // 中心：双向拉伸
        blit(g, left, top, cw, ch, x + left, y + top, dw, dh);
    }

    private void blit(Graphics2D g, int sx, int sy, int sw, int sh, float dx, float dy, float dw, float dh) {
        if (sw <= 0 || sh <= 0 || dw <= 0 || dh <= 0) return;
        g.drawImage(src,
                (int) dx, (int) dy, (int) (dx + dw), (int) (dy + dh),
                sx, sy, sx + sw, sy + sh, null);
    }
}
