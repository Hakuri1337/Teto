package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.font.FontManager;
import tech.hakuri.teto.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedList;
import java.util.List;

public class CategoryButton extends ScreenLike {

    public String category;

    public List<ModuleButton> moduleButtons = new LinkedList<>();

    public CategoryButton(String category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        var moduleY = this.y + this.height;
        for (Module module : ModuleManager.getModules(category)) {
            ModuleButton moduleButton = new ModuleButton(module, this.x, moduleY, 100, 15);
            moduleY += moduleButton.height + moduleButton.height * moduleButton.valueButtons.size();
            moduleButtons.add(moduleButton);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(guiGraphics, x, y, x + width, y + height, bgColor(mouseX, mouseY));
        FontManager.s.drawShadowString(guiGraphics, category, x * (float) mc.getWindow().getGuiScale(), y * (float) mc.getWindow().getGuiScale(), textColor(mouseX, mouseY));
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.render(guiGraphics, mouseX, mouseY, delta);
        }
        super.render(guiGraphics, mouseX, mouseY, delta);

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.mouseReleased(mouseX, mouseY, button);
        }
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double scrolls) {
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.mouseScrolled(mouseX, mouseY, scrolls);
        }
        super.mouseScrolled(mouseX, mouseY, scrolls);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.keyReleased(keyCode, scanCode, modifiers);
        }
    }
}