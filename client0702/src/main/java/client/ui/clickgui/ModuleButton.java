package client.ui.clickgui;


import client.feature.Module;
import client.feature.Value;
import client.font.FontManager;
import client.ui.legacy.BindScreen;
import client.utils.Keyboard;
import client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;


public class ModuleButton extends ScreenLike {

    public Module module;

    public List<ScreenLike> valueButtons = new LinkedList<>();

    public ModuleButton(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        var valueY = this.y + this.height;
        for (Value value : module.values) {
            ScreenLike valueButton = null;
            if (value.isBooleanValue()) {
                valueButton = new CheckBox(value, this.x, valueY, 100, 15);
            }
            if (value.isModesValue()) {
                valueButton = new ModeBox(value, this.x, valueY, 100, 15);
            }
            if (value.isNumberValue()) {
                valueButton = new Slider(value, this.x, valueY, 100, 15);
            }
            if (valueButton == null) {
                throw new RuntimeException();
            }
            valueY += valueButton.height;
            valueButtons.add(valueButton);
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(guiGraphics, x, y, x + width, y + height, bgColor(mouseX, mouseY));
        String str = module.keyCode == 0 ? module.name : String.format("%s [ %s ] ", module.name, Keyboard.get(module.keyCode));
        FontManager.s.drawShadowString(guiGraphics, str, x * (float) mc.getWindow().getGuiScale(), y * (float) mc.getWindow().getGuiScale(), textColor(mouseX, mouseY));
        for (ScreenLike valueButton : valueButtons) {
            valueButton.render(guiGraphics, mouseX, mouseY, delta);
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        //mid-click
        if (isClicking(mouseX, mouseY, 2)) {
            Minecraft.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getInstance().setScreen(new BindScreen(module));
                }
            });
        }

        if (isClicking(mouseX, mouseY, 0)) {
            module.toggle();
        }

        for (ScreenLike valueButton : valueButtons) {
            valueButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public int textColor(double mouseX, double mouseY) {
        int r, g, b, a;

        if (module.enable) {
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

        if (module.enable) {
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

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        for (ScreenLike valueButton : valueButtons) {
            valueButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double scrolls) {
        super.mouseScrolled(mouseX, mouseY, scrolls);

        for (ScreenLike valueButton : valueButtons) {
            valueButton.mouseScrolled(mouseX, mouseY, scrolls);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        for (ScreenLike valueButton : valueButtons) {
            valueButton.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);
        for (ScreenLike valueButton : valueButtons) {
            valueButton.keyReleased(keyCode, scanCode, modifiers);
        }
    }
}
