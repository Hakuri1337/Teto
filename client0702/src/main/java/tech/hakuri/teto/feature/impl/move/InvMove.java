package tech.hakuri.teto.feature.impl.move;

import tech.hakuri.teto.event.impl.EventInvMove;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.ChatScreen;

//注：Grim已涵盖该模块的检测
public class InvMove extends Module {

    public static int w;
    public static int s;
    public static int a;
    public static int d;
    public static int jump;
    public static int shift;

    //注入点来自氯雷他定
    public InvMove() {
        name = "背包行走";
        category = Category.move;
    }

    @Override
    public void onEnable() {
        w = mc.options.keyUp.getKey().getValue();
        s = mc.options.keyDown.getKey().getValue();
        a = mc.options.keyLeft.getKey().getValue();
        d = mc.options.keyRight.getKey().getValue();
        jump = mc.options.keyJump.getKey().getValue();
        shift = mc.options.keyShift.getKey().getValue();
    }

    @Override
    public void onInvMove(EventInvMove e) {
        if (mc.screen instanceof ChatScreen) return;
        //又改了一下，现在支持跳跃了
        int key = e.keyMapping.getKey().getValue();
        if (key == w || key == s || key == a || key == d || key == jump || key == shift) {
            e.handle = InputConstants.isKeyDown(mc.getWindow().getWindow(), key);
        }
    }
}
