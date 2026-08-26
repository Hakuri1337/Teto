package client.utils;

import java.awt.*;

public class ColorUtils {
    public static int rainbow(int step) {
        var cur = System.currentTimeMillis();
        int big = step * 100;
        var mix = cur - big;
        var res = mix % 5000 / 5000f;
        return Color.HSBtoRGB(res, 0.5f, 1);
    }
}
