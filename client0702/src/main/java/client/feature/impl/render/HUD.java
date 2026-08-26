package client.feature.impl.render;

import client.ClientEntry;
import client.event.impl.EventModuleToggle;
import client.event.impl.EventRender2D;
import client.feature.Category;
import client.feature.Module;
import client.feature.ModuleManager;
import client.font.FontManager;
import client.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author 豆包
 * @author 手淫
 */
public class HUD extends Module {

    public static float easeSpeed = 0.1f;
    public Map<Module, HUDElement> elements = new LinkedHashMap<>();
    public Random random = new Random();

    public HUD() {
        name = "功能列表";
        category = Category.render;
    }

    @Override
    public void onDisable() {
        elements.clear();
    }

    public void specialLogo(EventRender2D e) {
        if (!mc.options.keyShift.isDown()) return;
        if (mc.options.keyPlayerList.isDown() || mc.options.keyJump.isDown()) {
        } else return;
        String s2 = "爱来自小驴，Nimo，张德帅，手淫，北京深盾科技股份有限公司，Zelix Pty Ltd，LiquidBounce，github.com/x4e，cookiedragon24，StarTear(Goose)，GishCodeReloaded，Lefraudeur，radioegor146，TheQmaks，Ghsta，jeopardization，snowflak3，豆包，老氯雷他定团队全体成员，cherish团队，lzmk团队，风横（涅槃科技），以及屏幕前看这行字的你";
        float w2 = FontManager.xs.getStringWidth(s2);
        float h2 = FontManager.xs.getStringHeight(s2);
        FontManager.xs.drawShadowString(e.guiGraphics, s2, mc.getWindow().getWidth() - w2, 0, ColorUtils.rainbow(0));
    }

    public void logo(EventRender2D event) {
        FontManager.m.drawString(event.guiGraphics, ClientEntry.getClientName(), random.nextFloat(-1, 1), random.nextFloat(-1, 1), Color.red.getRGB());
        FontManager.m.drawString(event.guiGraphics, ClientEntry.getClientName(), random.nextFloat(-1, 1), random.nextFloat(-1, 1), Color.cyan.getRGB());
        FontManager.m.drawShadowString(event.guiGraphics, ClientEntry.getClientName(), 0, 0, Color.white.getRGB());
    }

    @Override
    public void onRender2D(EventRender2D it) {
        logo(it);
        specialLogo(it);
        r2d(it);
    }

    @Override
    public void onEnable() {
        for (Module module : ModuleManager.modules) {
            elements.put(module, new HUDElement(module.name));
        }
    }

    @Override
    public void onModuleToggle(EventModuleToggle event) {
        int startY = FontManager.m.getStringHeight(ClientEntry.getClientName());

        for (Module module : elements.keySet()) {
            HUDElement element = elements.get(module);

            if (module.enable) {
                element.setTargetPosition(0, startY);
                startY += element.height();
            } else {
                element.setTargetPosition(-200, startY);
            }
        }
    }

    public void r2d(EventRender2D event) {
        int colorStep = 1;
        for (HUDElement element : elements.values()) {
            element.updatePosition();
            element.color = ColorUtils.rainbow(colorStep);
            element.render(event.guiGraphics);
            colorStep++;
        }
    }

    /**
     * @author 豆包
     */
    public static class HUDElement {
        public String text;
        public float targetX, targetY;
        public float currentX, currentY;
        public int color = Color.white.getRGB();

        public HUDElement(String text) {
            this.text = text;
            this.targetX = -100;
            this.targetY = -100;
            this.currentX = targetX;
            this.currentY = targetY;
        }

        public void updatePosition() {
            if (moveFinish()) return;

            float deltaX = targetX - currentX;
            float deltaY = targetY - currentY;

            if (Math.abs(deltaX) > 1) {
                currentX += deltaX * easeSpeed;
            } else {
                currentX = targetX;
            }

            if (Math.abs(deltaY) > 1) {
                currentY += deltaY * easeSpeed;
            } else {
                currentY = targetY;
            }
        }

        public void setTargetPosition(float x, float y) {
            this.targetX = x;
            this.targetY = y;
        }

        public void render(GuiGraphics g) {
            if (maybeCantSee()) return;
            FontManager.m.drawShadowString(g, text, currentX, currentY, color);
        }

        public float width() {
            return FontManager.m.getStringWidth(text);
        }

        public int height() {
            return FontManager.m.getStringHeight(text);
        }

        public boolean moveFinish() {
            return currentX == targetX && currentY == targetY;
        }

        public boolean maybeCantSee() {
            return currentX <= -200 || currentY <= -200;
        }
    }
}