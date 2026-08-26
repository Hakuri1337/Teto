package a;

import client.hook.*;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import java.security.ProtectionDomain;

//这个类不要移动和改名，也不要混类名
public class a {

    public static native void a(Class<?> cls);

    //这玩意一但选定一个位置，想要修改就必须重新编译dll，得放在相对一个固定的地方
    //这个方法不要混淆名字和参数，方法体可以混
    public static byte[] a(Class<?> capturedClass, ClassLoader itsClassLoader, String itsName, ProtectionDomain itsProtectionDomain, byte[] itsData) {
        if (capturedClass == Gui.class) return Render2DHook.transform(itsData);
        if (capturedClass == Camera.class) return CameraHook.transform(itsData);
        if (capturedClass == LocalPlayer.class) return TickHook.transform(itsData);
        if (capturedClass == KeyMapping.class) return InvMoveHook.transform(itsData);
        if (capturedClass == Player.class) return AfterAttackHook.transform(itsData);
        if (capturedClass == KeyboardHandler.class) return KeyHook.transform(itsData);
        if (capturedClass == GameRenderer.class) return Render3DHook.transform(itsData);
        if (capturedClass == KeyboardInput.class) return MoveInputHook.transform(itsData);
        if (capturedClass == Connection.class) return PacketReceiveHook.transform(itsData);
        if (capturedClass == EntityRenderDispatcher.class) return ChamsHook.transform(itsData);
        if (capturedClass == ClientPacketListener.class) return PacketSendHook.transform(KickHook.transform(itsData));
        return itsData;
    }
}
