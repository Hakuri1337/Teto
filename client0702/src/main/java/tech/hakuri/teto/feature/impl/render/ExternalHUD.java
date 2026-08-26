package tech.hakuri.teto.feature.impl.render;

import tech.hakuri.teto.event.impl.EventModuleToggle;
import tech.hakuri.teto.event.impl.EventRender2D;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author nimo
 * @author 手淫
 * @author 豆包
 */
public class ExternalHUD extends Module {

    public static float easeSpeed = 0.1f;
    public Map<Module, ExternalElement> elements = new LinkedHashMap<>();

    public ExternalHUD() {
        name = "外置功能列表";
        category = Category.render;
    }

    @Override
    public void onDisable() {
        for (ExternalElement element : elements.values()) {
            element.dispose();
        }
        elements.clear();
    }

    @Override
    public void onEnable() {
        for (Module module : ModuleManager.modules) {
            elements.put(module, new ExternalElement(module.name));
        }
    }

    @Override
    public void onModuleToggle(EventModuleToggle event) {
        int yOffset = 0;
        for (Module module : elements.keySet()) {
            ExternalElement element = elements.get(module);

            if (module.enable) {
                element.setTargetPosition(0, yOffset);
                yOffset += element.getHeight();
            } else {
                element.setTargetPosition(-200, yOffset);
            }
        }
    }

    @Override
    public void onRender2D(EventRender2D event) {
        for (ExternalElement element : elements.values()) {
            element.updatePosition();
        }
    }

    public static class ExternalElement extends JWindow {
        public String text;
        public float targetX, targetY;
        public float currentX, currentY;

        public ExternalElement(String text) {
            this.text = text;
            this.targetX = -100;
            this.targetY = -100;
            this.currentX = targetX;
            this.currentY = targetY;
            JLabel label = new JLabel(text);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 24));
            add(label);
            setSize(150, 25);
            setLocation((int) this.targetX, (int) this.targetY);
            setAlwaysOnTop(true);
            setVisible(true);
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

            setLocation((int) currentX, (int) currentY);
        }

        public void setTargetPosition(float x, float y) {
            this.targetX = x;
            this.targetY = y;
        }

        public boolean moveFinish() {
            return currentX == targetX && currentY == targetY;
        }

        public boolean maybeCantSee() {
            return currentX <= -200 || currentY <= -200;
        }
    }
}