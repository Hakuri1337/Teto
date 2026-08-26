package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.font.FontManager;
import tech.hakuri.teto.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;


public class CheckBox extends ScreenLike {

    public Value value;

    public CheckBox(Value value, int x, int y, int width, int height) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(guiGraphics, x, y, x + width, y + height, bgColor(mouseX, mouseY));
        FontManager.s.drawShadowString(guiGraphics, String.format("    %s", value.name), x * (float) mc.getWindow().getGuiScale(), y * (float) mc.getWindow().getGuiScale(), textColor(mouseX, mouseY));
        super.render(guiGraphics, mouseX, mouseY, delta);
    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isClicking(mouseX, mouseY, 0)) {
            value.enable = !value.enable;
        }
    }

    @Override
    public int textColor(double mouseX, double mouseY) {
        int r, g, b, a;

        if (value.enable) {
            if (isClicking(mouseX, mouseY, null)) {
                r = g = b = 255;
                a = 255;
                return new Color(r, g, b, a).getRGB();
            } else {
                r = g = b = 0;
                a = 255;
                return new Color(r, g, b, a).getRGB();
            }
        }

        return super.textColor(mouseX, mouseY);
    }

    @Override
    public int bgColor(double mouseX, double mouseY) {
        int r, g, b, a;

        if (value.enable) {
            if (isClicking(mouseX, mouseY, null)) {
                r = g = b = 64;
                a = 128;
                return new Color(r, g, b, a).getRGB();
            } else if (isHovering(mouseX, mouseY)) {
                r = g = b = 255;
                a = 192;
                return new Color(r, g, b, a).getRGB();
            } else {
                r = g = b = 255;
                a = 255;
                return new Color(r, g, b, a).getRGB();
            }
        }

        return super.bgColor(mouseX, mouseY);
    }
}