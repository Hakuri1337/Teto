package tech.hakuri.teto.ui.clickgui_old;

import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.Platform;
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
        Platform.renderer().rect(x, y, x + width, y + height, bgColor(mouseX, mouseY));
        Platform.renderer().textShadow(FontSize.S, String.format("    %s：%s", tech.hakuri.teto.i18n.I18n.value(value.name), value.currentMode), x, y, textColor(mouseX, mouseY));
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
