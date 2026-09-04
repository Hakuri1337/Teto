package tech.hakuri.teto.theme;

import java.awt.Color;

/**
 * Material 3 色板（ARGB int）。分亮 / 暗两套，按 Material 3 官方 token 取值。
 * <p>
 * 设计要点（Material 3 的视觉语言）：
 * <ul>
 *   <li><b>表面层级</b>：背景 → 卡片 → 弹层，一层比一层略亮（暗色）或略深（亮色），
 *       靠明度差而不是边框来区分层级。</li>
 *   <li><b>柔和阴影</b>：用 elevation 投射的柔和投影表达层级，不用硬边投影。</li>
 *   <li><b>单一强调色</b>：交互元素（开关、选中、滑块）统一用 primary，
 *       其余都是中性色，bloom 只允许出现在 primary 元素上。</li>
 *   <li><b>圆角</b>：卡片用大圆角，小控件用中圆角。</li>
 * </ul>
 * 颜色全部是不可变静态字段，组件在渲染时按当前 {@link ThemeMode} 取。
 */
public final class MaterialTheme {

    /** 圆角半径（px）。用户定的起步值。 */
    public static final float RADIUS_CARD = 8f;
    public static final float RADIUS_CONTROL = 6f;

    /** 主色（Material 3 紫）。 */
    public static final int PRIMARY = argb(0x6750A4);
    public static final int ON_PRIMARY = 0xFFFFFFFF;

    /** 亮 / 暗两套表面色。 */
    public static final class Surfaces {
        public final int background;
        public final int surface;      // 卡片
        public final int surfaceHigh;  // 弹层 / 悬浮
        public final int outline;
        public final int onSurface;    // 主文字
        public final int onSurfaceVar; // 次要文字

        Surfaces(int background, int surface, int surfaceHigh, int outline, int onSurface, int onSurfaceVar) {
            this.background = background;
            this.surface = surface;
            this.surfaceHigh = surfaceHigh;
            this.outline = outline;
            this.onSurface = onSurface;
            this.onSurfaceVar = onSurfaceVar;
        }
    }

    public static final Surfaces DARK = new Surfaces(
            argb(0x141218),  // background
            argb(0x1D1B20),  // surface
            argb(0x2B2930),  // surfaceHigh
            argb(0x49454F),  // outline
            argb(0xE6E0E9),  // onSurface
            argb(0xCAC4D0)   // onSurfaceVariant
    );

    public static final Surfaces LIGHT = new Surfaces(
            argb(0xFEF7FF),
            argb(0xF3EDF7),
            argb(0xECE6F0),
            argb(0x79747E),
            argb(0x1D1B20),
            argb(0x49454F)
    );

    public static Surfaces of(ThemeMode mode) {
        return mode == ThemeMode.LIGHT ? LIGHT : DARK;
    }

    /** 选中态（开关开 / 模块启用）用的 primary 容器色。 */
    public static int primaryContainer(ThemeMode mode) {
        return mode == ThemeMode.LIGHT ? argb(0xEADDFF) : argb(0x4F378B);
    }

    /** primary 在表面上的文字/图标色。 */
    public static int onPrimaryContainer(ThemeMode mode) {
        return mode == ThemeMode.LIGHT ? argb(0x21005D) : argb(0xEADDFF);
    }

    private static int argb(int rgb) {
        return 0xFF000000 | rgb;
    }

    public static Color awt(int argb) {
        return new Color(argb, true);
    }

    private MaterialTheme() {
    }
}
