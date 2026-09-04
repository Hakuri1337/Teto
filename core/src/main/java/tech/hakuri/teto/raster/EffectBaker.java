package tech.hakuri.teto.raster;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * 廉价路径的效果烘焙器：把圆角矩形、投影、辉光一次性画成 BufferedImage。
 * <p>
 * <b>为什么叫「廉价」</b>：高斯模糊只在这里发生（一次性 CPU 卷积），
 * 运行时永远只是贴九宫格纹理，没有任何实时模糊。
 * <p>
 * 全部用 Java2D 软件光栅化，不碰 Minecraft，所以可以离线跑、离线出 PNG 对照图。
 */
public final class EffectBaker {

    private EffectBaker() {
    }

    /** 高斯模糊（两次一维卷积近似，比二维卷积快得多）。 */
    public static BufferedImage gaussianBlur(BufferedImage src, float radius) {
        if (radius < 0.5f) return src;
        int size = (int) Math.ceil(radius) * 2 + 1;
        float[] kernel = new float[size];
        float sigma = radius / 3f;
        float sum = 0;
        for (int i = 0; i < size; i++) {
            int x = i - size / 2;
            kernel[i] = (float) Math.exp(-(x * x) / (2 * sigma * sigma));
            sum += kernel[i];
        }
        for (int i = 0; i < size; i++) kernel[i] /= sum;

        BufferedImage tmp = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        new ConvolveOp(new Kernel(size, 1, kernel), ConvolveOp.EDGE_NO_OP, null)
                .filter(src, tmp);
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        new ConvolveOp(new Kernel(1, size, kernel), ConvolveOp.EDGE_NO_OP, null)
                .filter(tmp, dst);
        return dst;
    }

    /** 一张纯圆角矩形（白色，后续用 tint 染色）。边距留白给九宫格裁切。 */
    public static BufferedImage roundedRect(int size, float radius) {
        BufferedImage img = transparent(size, size);
        Graphics2D g = img.createGraphics();
        enableAA(g);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(1, 1, size - 2, size - 2, radius * 2, radius * 2));
        g.dispose();
        return img;
    }

    /** 投影：一个被高斯模糊的圆角矩形，带偏移与透明度。 */
    public static BufferedImage shadow(int size, float radius, float blur, float offsetY, float alpha) {
        int pad = (int) Math.ceil(blur * 2);
        BufferedImage img = transparent(size + pad * 2, size + pad * 2);
        BufferedImage shape = transparent(img.getWidth(), img.getHeight());
        Graphics2D g = shape.createGraphics();
        enableAA(g);
        g.setColor(new Color(0f, 0f, 0f, alpha));
        g.fill(new RoundRectangle2D.Float(pad, pad + offsetY, size, size, radius * 2, radius * 2));
        g.dispose();
        gaussianBlurTo(shape, img, blur);
        return img;
    }

    /** 辉光（bloom）：主色的高斯模糊，用于加色混合叠在强调元素下。 */
    public static BufferedImage glow(int size, float radius, float blur, int argbColor, float strength) {
        int pad = (int) Math.ceil(blur * 2);
        BufferedImage img = transparent(size + pad * 2, size + pad * 2);
        BufferedImage shape = transparent(img.getWidth(), img.getHeight());
        Graphics2D g = shape.createGraphics();
        enableAA(g);
        Color c = new Color(argbColor, true);
        g.setColor(new Color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, strength));
        g.fill(new RoundRectangle2D.Float(pad, pad, size, size, radius * 2, radius * 2));
        g.dispose();
        gaussianBlurTo(shape, img, blur);
        return img;
    }

    /** 描边圆角矩形（用于未选中的边框态）。 */
    public static BufferedImage roundedOutline(int size, float radius, float stroke) {
        BufferedImage img = transparent(size, size);
        Graphics2D g = img.createGraphics();
        enableAA(g);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(stroke));
        g.draw(new RoundRectangle2D.Float(stroke, stroke, size - stroke * 2, size - stroke * 2, radius * 2, radius * 2));
        g.dispose();
        return img;
    }

    private static void gaussianBlurTo(BufferedImage src, BufferedImage dst, float radius) {
        BufferedImage blurred = gaussianBlur(src, radius);
        Graphics2D g = dst.createGraphics();
        g.drawImage(blurred, 0, 0, null);
        g.dispose();
    }

    private static BufferedImage transparent(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
