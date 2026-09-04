package tech.hakuri.teto.mc1218;

import net.neoforged.fml.common.Mod;

/**
 * NeoForge 入口，对应 1.20.1 侧的 {@code ForgeEntry}。
 * <p>
 * 继承 Thread 的理由同 1.20.1：Minecraft 的渲染和逻辑是同一个线程，
 * 初始化里有 sleep，跑在渲染线程上会卡住游戏。
 * <p>
 * {@code @Mod} 注解来自 {@code net.neoforged.fml.common.Mod}（Forge 那边是
 * {@code net.minecraftforge.fml.common.Mod}）。保留它是为了让客户端也能作为
 * 普通 mod 放进 mods 目录被 NeoForge 发现 —— 但注意热注入才是主用法，
 * mod 路径拿不到 Instrumentation，钩子不会生效。
 */
@Mod("teto")
public class NeoEntry extends Thread {

    public NeoEntry() {
        start();
    }

    @Override
    public void run() {
        ClientEntry1218.init();
    }
}
