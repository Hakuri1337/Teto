package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.compat.Keys;
import tech.hakuri.teto.event.impl.EventKey;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public class KeyListener extends Module {

    public KeyListener() {
        name = "按键管理器";
        category = Category.misc;
        toggle();
    }

    public static void resetMove() {
        resetW();
        resetS();
        resetA();
        resetD();
    }

    public static void resetAll() {
        resetMove();
        resetShift();
    }

    public static void resetW() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyUp.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), Keys.of(mc.options.keyUp).getValue()));
    }

    public static void resetS() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyDown.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), Keys.of(mc.options.keyDown).getValue()));
    }

    public static void resetA() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyLeft.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), Keys.of(mc.options.keyLeft).getValue()));
    }

    public static void resetD() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyRight.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), Keys.of(mc.options.keyRight).getValue()));
    }

    public static void resetShift() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyShift.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), Keys.of(mc.options.keyShift).getValue()));
    }

    @Override
    public void onKey(EventKey it) {
        if (mc.screen instanceof ChatScreen) return;

        for (Module module : ModuleManager.modules) {
            if (it.key == module.keyCode) {
                if (it.action == 1) {
                    module.toggle();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        toggle();
    }
}
