package client.ui.legacy;

import client.feature.Module;
import client.utils.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class BindScreen extends Screen {
    public Module module;

    public BindScreen(Module module) {
        super(Component.empty());
        this.module = module;
        this.minecraft = Minecraft.getInstance();//神秘勿删
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float tick) {
        Minecraft mc = Minecraft.getInstance();
        var mcw = mc.getWindow().getGuiScaledWidth();
        var mch = mc.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, mcw, mch, new Color(0, 0, 0, 128).getRGB());
        guiGraphics.drawCenteredString(mc.font, "请按下...", mcw / 2, mch / 2, Color.white.getRGB());
        super.render(guiGraphics, x, y, tick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == Keyboard.get("ESCAPE")) {
            this.module.keyCode = 0;
        } else {
            this.module.keyCode = keyCode;
        }
        this.onClose();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
