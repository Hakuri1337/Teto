package client.feature.impl.misc;

import client.event.impl.EventKey;
import client.feature.Category;
import client.feature.Module;
import client.feature.ModuleManager;
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
        mc.options.keyUp.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyUp.getKey().getValue()));
    }

    public static void resetS() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyDown.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyDown.getKey().getValue()));
    }

    public static void resetA() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyLeft.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyLeft.getKey().getValue()));
    }

    public static void resetD() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyRight.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyRight.getKey().getValue()));
    }

    public static void resetShift() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyShift.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyShift.getKey().getValue()));
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
