package tech.hakuri.teto.hook;

import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * 用 java.lang.instrument 重转换 Minecraft 的类，取代原先的 hook.dll（JVMTI ClassFileLoadHook）。
 * <p>
 * 分发方式与原先的 native 回调 a.a(...) 完全一致：按 classBeingRedefined 的身份判断，
 * 转发给 hook/ 下的 ASM transformer。那些 transformer 一行未改 —— 它们只在既有方法体内插指令，
 * 不增删字段/方法、不改签名和继承，正好落在 retransform 的合法范围内。
 */
public final class Transformer implements ClassFileTransformer {

    /** Agent 发布 Instrumentation 用的 System.getProperties() 键，需与 injector/src/Agent.java 保持一致。 */
    private static final String INSTRUMENTATION_KEY = "teto.instrumentation";

    /** 需要挂钩的 Minecraft 类，顺序与原先 11 次 a.a(...) 调用一致。 */
    private static Class<?>[] targets() {
        return new Class<?>[]{
                Gui.class,
                Camera.class,
                Player.class,
                Connection.class,
                KeyMapping.class,
                LocalPlayer.class,
                LevelRenderer.class,
                KeyboardInput.class,
                KeyboardHandler.class,
                ClientCommonPacketListenerImpl.class,
                EntityRenderDispatcher.class,
        };
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // 只处理重转换；普通类加载（classBeingRedefined 为 null）直接放过，
        // 返回 null 表示不修改，JVM 连重新解析都省了。
        if (classBeingRedefined == null) {
            return null;
        }
        try {
            return dispatch(classBeingRedefined, classfileBuffer);
        } catch (Throwable throwable) {
            // 单个类转换失败不应波及其他类：返回 null 保留原字节码。
            // 原先的 native 回调在这里没有 ExceptionCheck，异常会让 GetArrayLength(NULL) 直接崩掉游戏。
            System.err.println("[teto] 转换失败 " + className + "：" + throwable);
            throwable.printStackTrace(System.err);
            return null;
        }
    }

    /** 与原 a.a(Class, ClassLoader, String, ProtectionDomain, byte[]) 的分支一一对应。 */
    private static byte[] dispatch(Class<?> captured, byte[] data) {
        if (captured == Gui.class) return Render2DHook.transform(data);
        if (captured == Camera.class) return CameraHook.transform(data);
        if (captured == LocalPlayer.class) return TickHook.transform(data);
        if (captured == KeyMapping.class) return InvMoveHook.transform(data);
        if (captured == Player.class) return AfterAttackHook.transform(data);
        if (captured == KeyboardHandler.class) return KeyHook.transform(data);
        if (captured == LevelRenderer.class) return Render3DHook.transform(data);
        if (captured == KeyboardInput.class) return MoveInputHook.transform(data);
        if (captured == Connection.class) return PacketReceiveHook.transform(data);
        if (captured == EntityRenderDispatcher.class) return ChamsHook.transform(data);
        if (captured == ClientCommonPacketListenerImpl.class) return PacketSendHook.transform(KickHook.transform(data));
        return null;
    }

    /**
     * 安装钩子：取得 Instrumentation，注册转换器，逐个重转换目标类。
     * 逐个而不是一次全传，是为了保留原实现的容错性 —— 一个类失败不影响其余的。
     */
    public static void install() {
        Instrumentation inst = resolveInstrumentation();
        if (inst == null) {
            throw new IllegalStateException(
                    "未取得 Instrumentation。客户端需要由注入器热注入启动（teto-injector.jar 会以 agent 形式提供）；"
                            + "作为普通 Forge mod 放入 mods 目录时没有这个能力。");
        }
        if (!inst.isRetransformClassesSupported()) {
            throw new IllegalStateException("目标 JVM 不支持类重转换，无法挂钩。");
        }

        Transformer transformer = new Transformer();
        inst.addTransformer(transformer, true);
        int done = 0;
        try {
            for (Class<?> target : targets()) {
                if (!inst.isModifiableClass(target)) {
                    System.err.println("[teto] 不可修改的类，跳过：" + target.getName());
                    continue;
                }
                try {
                    inst.retransformClasses(target);
                    done++;
                } catch (Throwable throwable) {
                    System.err.println("[teto] 重转换失败 " + target.getName() + "：" + throwable);
                }
            }
        } finally {
            inst.removeTransformer(transformer);
        }
        System.out.println("[teto] 钩子安装完成：" + done + "/" + targets().length);
    }

    /** 通道一：系统 ClassLoader 上 Agent 的静态字段；通道二：System.getProperties()。 */
    private static Instrumentation resolveInstrumentation() {
        try {
            Class<?> agentClass = Class.forName("Agent", false, ClassLoader.getSystemClassLoader());
            Object value = agentClass.getField("INSTRUMENTATION").get(null);
            if (value instanceof Instrumentation inst) {
                return inst;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // 落到通道二。java.lang.instrument.Instrumentation 由 bootstrap 加载，
            // 所以跨 ClassLoader 传递同一个对象不会 ClassCastException。
        }
        Object value = System.getProperties().get(INSTRUMENTATION_KEY);
        if (value instanceof Instrumentation inst) {
            return inst;
        }
        return null;
    }
}
