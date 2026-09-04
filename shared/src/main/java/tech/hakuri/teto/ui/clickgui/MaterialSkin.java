package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.platform.Platform;
import tech.hakuri.teto.raster.EffectBaker;
import tech.hakuri.teto.raster.NinePatch;
import tech.hakuri.teto.theme.MaterialTheme;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Material 组件的纹理底座：把圆角矩形 / 阴影 / 辉光烘成 NinePatch 缓存，
 * 组件按需取用并按当前主题染色。
 * <p>
 * 烘焙只发生一次（首次使用时），之后所有绘制都是九宫格拉伸 + tint，
 * 运行时零模糊、零重算 —— 这就是「廉价路径」在组件层的落点。
 */
public final class MaterialSkin {

    private static final int SHAPE = 48;

    private static NinePatch card;
    private static NinePatch cardShadow;
    private static NinePatch control;
    private static NinePatch controlOutline;
    private static NinePatch primaryGlow;
    private static final Map<String, NinePatch> cache = new HashMap<>();

    private MaterialSkin() {
    }

    /** 卡片底（大圆角）。 */
    public static NinePatch card() {
        if (card == null) card = new NinePatch(EffectBaker.roundedRect(SHAPE, MaterialTheme.RADIUS_CARD), 14);
        return card;
    }

    /** 卡片投影（柔和）。 */
    public static NinePatch cardShadow() {
        if (cardShadow == null) cardShadow = new NinePatch(EffectBaker.shadow(SHAPE, MaterialTheme.RADIUS_CARD, 9, 4, 0.35f), 24);
        return cardShadow;
    }

    /** 控件底（小圆角）。 */
    public static NinePatch control() {
        if (control == null) control = new NinePatch(EffectBaker.roundedRect(SHAPE, MaterialTheme.RADIUS_CONTROL), 12);
        return control;
    }

    /** 控件描边（未选中态的边框）。 */
    public static NinePatch controlOutline() {
        if (controlOutline == null) controlOutline = new NinePatch(EffectBaker.roundedOutline(SHAPE, MaterialTheme.RADIUS_CONTROL, 2), 12);
        return controlOutline;
    }

    /** 主色辉光（bloom，用于强调元素）。 */
    public static NinePatch primaryGlow() {
        if (primaryGlow == null) primaryGlow = new NinePatch(EffectBaker.glow(SHAPE, MaterialTheme.RADIUS_CONTROL, 16, MaterialTheme.PRIMARY, 0.8f), 28);
        return primaryGlow;
    }

    /** 任意半径的圆角矩形（滑块轨道这类非标准尺寸用）。 */
    public static NinePatch rounded(float radius, int inset) {
        return cache.computeIfAbsent("r" + radius + "/" + inset,
                k -> new NinePatch(EffectBaker.roundedRect(SHAPE, radius), inset));
    }

    /** 把缓存的 NinePatch 导出为 BufferedImage（需要直接上传纹理时用）。 */
    public static BufferedImage image(NinePatch patch, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        patch.draw(g, 0, 0, w, h, null);
        g.dispose();
        return img;
    }

    /** 当前辉光是否该画（读 Settings 开关）。 */
    public static boolean bloomOn() {
        return tech.hakuri.teto.feature.impl.misc.Settings.bloomEnabled();
    }
}
