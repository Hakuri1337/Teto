package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.theme.MaterialTheme;
import tech.hakuri.teto.theme.ThemeMode;

/**
 * 当前生效的主题。统一从 Settings 读，组件不各自判断亮暗。
 * 放在 clickgui 包是因为只有 UI 层关心主题；烘焙层（raster）只接受颜色参数。
 */
public final class UiTheme {

    private UiTheme() {
    }

    public static ThemeMode mode() {
        return tech.hakuri.teto.feature.impl.misc.Settings.isLight() ? ThemeMode.LIGHT : ThemeMode.DARK;
    }

    public static MaterialTheme.Surfaces surfaces() {
        return MaterialTheme.of(mode());
    }

    public static int background() { return surfaces().background; }
    public static int surface() { return surfaces().surface; }
    public static int surfaceHigh() { return surfaces().surfaceHigh; }
    public static int outline() { return surfaces().outline; }
    public static int onSurface() { return surfaces().onSurface; }
    public static int onSurfaceVariant() { return surfaces().onSurfaceVar; }
    public static int primary() { return MaterialTheme.PRIMARY; }
    public static int primaryContainer() { return MaterialTheme.primaryContainer(mode()); }
    public static int onPrimaryContainer() { return MaterialTheme.onPrimaryContainer(mode()); }

    /** 带透明度的颜色（用于 hover / 分隔线）。 */
    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    /** 在两种颜色间插值（hover 过渡）。t∈[0,1]。 */
    public static int lerp(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (aa + (ba - aa) * t) << 24)
                | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }
}
