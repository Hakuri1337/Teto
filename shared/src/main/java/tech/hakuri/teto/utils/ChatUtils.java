package tech.hakuri.teto.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {
    public static void msg(Object msg) {
        tech.hakuri.teto.compat.Compat.chat(Component.literal(String.valueOf(msg)));
    }
}
