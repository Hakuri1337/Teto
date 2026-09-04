package tech.hakuri.teto.feature.impl.render;

import tech.hakuri.teto.ClientPaths;
import tech.hakuri.teto.event.impl.EventModuleToggle;
import tech.hakuri.teto.event.impl.EventRender2D;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.Platform;
import tech.hakuri.teto.utils.ColorUtils;

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
        //屏幕宽度与文字宽度现在同为 GUI 坐标，右对齐才真正成立（迁移前一个是物理像素、一个是字形像素，对不齐）
        float w2 = Platform.renderer().textWidth(FontSize.XS, s2);
        Platform.renderer().textShadow(FontSize.XS, s2, Platform.renderer().screenWidth() - w2, 0, ColorUtils.rainbow(0));
    }

    public void logo(EventRender2D event) {
        Platform.renderer().text(FontSize.M, ClientPaths.getClientName(), random.nextFloat(-1, 1), random.nextFloat(-1, 1), Color.red.getRGB());
        Platform.renderer().text(FontSize.M, ClientPaths.getClientName(), random.nextFloat(-1, 1), random.nextFloat(-1, 1), Color.cyan.getRGB());
        Platform.renderer().textShadow(FontSize.M, ClientPaths.getClientName(), 0, 0, Color.white.getRGB());
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
        float startY = Platform.renderer().textHeight(FontSize.M);

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
            element.render();
            colorStep++;
        }
    }

    /**
     * @author 豆包
     */
    public static class HUDElement {
        /** 模块身份（中文键），显示时按当前语言翻译，所以切语言不用重建元素。 */
        public String identity;
        public float targetX, targetY;
        public float currentX, currentY;
        public int color = Color.white.getRGB();

        public HUDElement(String identity) {
            this.identity = identity;
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

        public void render() {
            if (maybeCantSee()) return;
            Platform.renderer().textShadow(FontSize.M, tech.hakuri.teto.i18n.I18n.module(identity), currentX, currentY, color);
        }

        public float width() {
            return Platform.renderer().textWidth(FontSize.M, tech.hakuri.teto.i18n.I18n.module(identity));
        }

        public float height() {
            return Platform.renderer().textHeight(FontSize.M);
        }

        public boolean moveFinish() {
            return currentX == targetX && currentY == targetY;
        }

        public boolean maybeCantSee() {
            return currentX <= -200 || currentY <= -200;
        }
    }
}