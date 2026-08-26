package tech.hakuri.teto.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {
    public static void msg(Object msg) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.valueOf(msg)));
    }
}
