package tech.hakuri.teto.raster;

import tech.hakuri.teto.theme.MaterialTheme;
import tech.hakuri.teto.theme.ThemeMode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** 离线出图：不开游戏直接看圆角/阴影/bloom/亮暗主题的效果。 */
public class BakePreview {
    public static void main(String[] args) throws Exception {
        File outDir = new File(args.length > 0 ? args[0] : "core/build/bake-preview");
        outDir.mkdirs();

        // 一张对照图：两行（亮/暗），四列（圆角矩形 / +阴影 / +bloom / 描边）
        int cell = 160, pad = 20, cols = 4, rows = 2;
        BufferedImage sheet = new BufferedImage(cols * (cell + pad) + pad, rows * (cell + pad) + pad + 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float radius = 8;
        NinePatch card = new NinePatch(EffectBaker.roundedRect(48, radius), 12);
        NinePatch shadow = new NinePatch(EffectBaker.shadow(48, radius, 8, 4, 0.4f), 22);
        NinePatch glow = new NinePatch(EffectBaker.glow(48, radius, 14, MaterialTheme.PRIMARY, 0.8f), 26);
        NinePatch outline = new NinePatch(EffectBaker.roundedOutline(48, radius, 2), 12);

        ThemeMode[] modes = {ThemeMode.DARK, ThemeMode.LIGHT};
        for (int r = 0; r < rows; r++) {
            ThemeMode mode = modes[r];
            MaterialTheme.Surfaces s = MaterialTheme.of(mode);
            for (int c = 0; c < cols; c++) {
                int x = pad + c * (cell + pad);
                int y = pad + r * (cell + pad);
                // 背景
                g.setColor(MaterialTheme.awt(s.background));
                g.fillRect(x, y, cell, cell);

                float bx = x + 20, by = y + 40, bw = cell - 40, bh = cell - 80;
                switch (c) {
                    case 0 -> card.draw(g, bx, by, bw, bh, s.surface);
                    case 1 -> { shadow.draw(g, bx, by, bw, bh, null); card.draw(g, bx, by, bw, bh, s.surface); }
                    case 2 -> { shadow.draw(g, bx, by, bw, bh, null); glow.draw(g, bx, by, bw, bh, null); card.draw(g, bx, by, bw, bh, MaterialTheme.primaryContainer(mode)); }
                    case 3 -> { card.draw(g, bx, by, bw, bh, s.surface); outline.draw(g, bx, by, bw, bh, MaterialTheme.PRIMARY); }
                }
                // 标签
                g.setColor(MaterialTheme.awt(s.onSurface));
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String[] labels = {"rounded", "+shadow", "+bloom", "+outline"};
                g.drawString((mode == ThemeMode.DARK ? "dark " : "light ") + labels[c], x + 8, y + 16);
            }
        }
        g.dispose();
        File out = new File(outDir, "material-preview.png");
        ImageIO.write(sheet, "PNG", out);
        System.out.println("已输出: " + out.getAbsolutePath());
    }
}
