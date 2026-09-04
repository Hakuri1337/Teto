package tech.hakuri.teto.ui.clickgui_old;

import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.LinkedList;
import java.util.List;

public class ClickGUI extends Screen {
    public List<CategoryButton> categoryButtons = new LinkedList<>();

    public ClickGUI() {
        super(Component.empty());
        Minecraft mc = Minecraft.getInstance();

        var mcw = mc.getWindow().getWidth();
        var empty = mcw - Category.getAll().size() * 100 * (int) mc.getWindow().getGuiScale();
        var space = empty / 2;
        space /= (int) mc.getWindow().getGuiScale();

        var tmp = space;
        for (String category : Category.getAll()) {
            CategoryButton categoryButton = new CategoryButton(category, tmp, 0, 100, 15);
            categoryButtons.add(categoryButton);
            tmp += categoryButton.width;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void resize(Minecraft p_96575_, int p_96576_, int p_96577_) {
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        //ClickGUI 走的是 Screen 的渲染路径，guiGraphics 来自 Minecraft 而不是 EventRender2D，
        //所以这里要单独把帧上下文交给 IRenderer 实现
        Platform.renderer().beginFrame2D(guiGraphics);
        for (ScreenLike category : categoryButtons) {
            category.render(guiGraphics, mouseX, mouseY, delta);
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ScreenLike category : categoryButtons) {
            category.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ScreenLike category : categoryButtons) {
            category.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //1.21.8 的 mouseScrolled 多了横向滚动量（scrollX）。ScreenLike 只关心纵向，
        //所以把 scrollY 传下去，与 1.20.1 的单参数版本行为一致。
        for (ScreenLike category : categoryButtons) {
            category.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ScreenLike category : categoryButtons) {
            category.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ScreenLike category : categoryButtons) {
            category.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
