package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.utils.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

/**
 * @author 手淫
 * @since 2025/6/4
 * R.I.P. people dies in 1986/6/4 (joke)
 * 注意，如果没有把握，请不要随意移动super调用的位置，或是删除空的Override方法
 */
public class ScreenLike {
    public Minecraft mc = Minecraft.getInstance();

    public int x, y, width, height, prevMouseX, prevMouseY;
    public Integer mousePressing, pressingKey;


    public int textColor(double mouseX, double mouseY) {
        int r, g, b, a;

        if (isClicking(mouseX, mouseY, null)) {
            r = g = b = 255;
            a = 255;
        } else if (isHovering(mouseX, mouseY)) {
            r = g = b = a = 255;
        } else {
            r = g = b = a = 255;
        }

        return new Color(r, g, b, a).getRGB();
    }

    public int bgColor(double mouseX, double mouseY) {
        int r, g, b, a;

        if (isClicking(mouseX, mouseY, null)) {
            r = g = b = 192;
            a = 128;
        } else if (isHovering(mouseX, mouseY)) {
            r = g = b = a = 192;
        } else {
            r = g = b = 0;
            a = 128;
        }

        return new Color(r, g, b, a).getRGB();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public boolean isClicking(double mouseX, double mouseY, Integer button) {
        if (isHovering(mouseX, mouseY)) {
            if (button == null) {
                return mousePressing != null;
            } else {
                return mousePressing != null && mousePressing.equals(button);
            }
        } else {
            return false;
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY)) {
            mousePressing = button;
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        mousePressing = null;
    }

    public void mouseScrolled(double mouseX, double mouseY, double scrolls) {
        if (!isKeyPressing(Keyboard.get("LCONTROL")) && !isKeyPressing(Keyboard.get("RCONTROL"))) {
            if (isKeyPressing(Keyboard.get("LSHIFT")) || isKeyPressing(Keyboard.get("RSHIFT"))) {
                x += scrolls * 10;
            } else {
                y += scrolls * 10;
            }
        }
    }

    public boolean isKeyPressing(int keyCode) {
        return pressingKey != null && pressingKey == keyCode;
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        pressingKey = keyCode;
    }

    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        pressingKey = null;
    }
}
