package tech.hakuri.teto.feature.impl.move;

import tech.hakuri.teto.compat.Keys;
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
        w = Keys.of(mc.options.keyUp).getValue();
        s = Keys.of(mc.options.keyDown).getValue();
        a = Keys.of(mc.options.keyLeft).getValue();
        d = Keys.of(mc.options.keyRight).getValue();
        jump = Keys.of(mc.options.keyJump).getValue();
        shift = Keys.of(mc.options.keyShift).getValue();
    }

    @Override
    public void onInvMove(EventInvMove e) {
        if (mc.screen instanceof ChatScreen) return;
        //又改了一下，现在支持跳跃了
        int key = Keys.of(e.keyMapping).getValue();
        if (key == w || key == s || key == a || key == d || key == jump || key == shift) {
            e.handle = InputConstants.isKeyDown(mc.getWindow().getWindow(), key);
        }
    }
}
