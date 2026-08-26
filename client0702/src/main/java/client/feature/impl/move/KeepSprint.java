package client.feature.impl.move;

import client.event.impl.EventAfterAttack;
import client.feature.Category;
import client.feature.Module;
import client.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

//注：Grim已涵盖该模块的检测
//眼珠也干了
public class KeepSprint extends Module {

    //打开此功能会导致Attack Reduce消失，两个机制均在原版冲突，非客户端问题
    public KeepSprint() {
        name = "击中不减速";
        category = Category.move;
    }

    public static Boolean can() {
        try {
            return ReflectBridge.invoke(Boolean.class, LocalPlayer.class, Minecraft.getInstance().player, "m_264082_");//canStartSprinting m_264082_
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void onAfterAttack(EventAfterAttack event) {
        if (can() == null) return;//idea debug uses mcp name
        if (!can()) return;//然而玩家因其他原因如后退导致没有在疾跑，下面这行也会触发，所以我们要假设玩家只要能疾跑都在疾跑

        //在Player.class.attack()里，只有一行setSprinting()，为setSprinting(false)，且在这行以后，没有任何的isSprinting()，所以这是一个切入点
        //我们只要在Player.class.attack()方法尾部插入event，可以在其被其他代码读取之前修改
        if (!mc.player.isSprinting()) {
            //加回减去的速度
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(1.0 / 0.6, 1.0F, 1.0 / 0.6));
            mc.player.setSprinting(true);
        }
    }
}
