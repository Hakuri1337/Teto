package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.font.FontManager;
import tech.hakuri.teto.utils.Keyboard;
import tech.hakuri.teto.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class Slider extends ScreenLike {

    public Value value;

    public Slider(Value value, int x, int y, int width, int height) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {

        RenderUtils.drawRect(guiGraphics, x, y, x + width, y + height, bgColor(mouseX, mouseY));

        //Lefraudeur Mujina
        if (isClicking(mouseX, mouseY, null)) {
            float diff = mouseX - x;//int->float(!important!)
            float percent = diff / width;
            value.numberValue = value.min + percent * (value.max - value.min);
            if (value.numberValue < value.min) value.numberValue = value.min;
            if (value.numberValue > value.max) value.numberValue = value.max;
        }

        var renderWidth = width * (value.numberValue - value.min) / (value.max - value.min);

        RenderUtils.drawRect(guiGraphics, x, y, x + renderWidth, y + height, bgColor(mouseX, mouseY));

        FontManager.s.drawShadowString(guiGraphics, String.format("    %s：%.2f", value.name, value.numberValue), x * (float) mc.getWindow().getGuiScale(), y * (float) mc.getWindow().getGuiScale(), textColor(mouseX, mouseY));

        super.render(guiGraphics, mouseX, mouseY, delta);
    }


    @Override
    public void mouseScrolled(double mouseX, double mouseY, double scrolls) {
        super.mouseScrolled(mouseX, mouseY, scrolls);

        if (isKeyPressing(Keyboard.get("LCONTROL")) || isKeyPressing(Keyboard.get("RCONTROL"))) {
            if (isHovering(mouseX, mouseY)) {
                value.numberValue += scrolls;
            }
        }
    }
}
