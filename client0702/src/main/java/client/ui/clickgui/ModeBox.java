package client.ui.clickgui;

import client.feature.Value;
import client.font.FontManager;
import client.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;


public class ModeBox extends ScreenLike {

    public Value value;

    public ModeBox(Value value, int x, int y, int width, int height) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(guiGraphics, x, y, x + width, y + height, bgColor(mouseX, mouseY));
        FontManager.s.drawShadowString(guiGraphics, String.format("    %s：%s", value.name, value.currentMode), x * (float) mc.getWindow().getGuiScale(), y * (float) mc.getWindow().getGuiScale(), textColor(mouseX, mouseY));
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isClicking(mouseX, mouseY, 0)) {
            value.nextMode();
        }
    }
}
