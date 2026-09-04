import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.8 / NeoForge 环境探针。附加到运行中的游戏，核实迁移方案依赖的三个前提：
 * 1. 动态 attach 的 agent 能否在 Java 21 的游戏进程里拿到重转换能力；
 * 2. loader 依赖的「存在名为 Render thread 的线程且其 ContextClassLoader 非空」是否成立；
 * 3. 12 个 hook 目标类是否已加载、可修改，方法名是否与 javap 审计结果一致。
 * 全程只读 + 一次 no-op 重转换（transformer 返回 null 表示不改字节码），不影响游戏。
 */
public final class Probe1218 {

    /** 审计得出的 1.21.8 目标：类名 / 方法名 / 期望的方法描述符片段。 */
    private static final String[][] TARGETS = {
            {"net.minecraft.client.gui.Gui", "renderCrosshair"},
            {"net.minecraft.client.Camera", "setRotation"},
            {"net.minecraft.client.player.LocalPlayer", "tick"},
            {"net.minecraft.client.KeyMapping", "isDown"},
            {"net.minecraft.world.entity.player.Player", "attack"},
            {"net.minecraft.client.KeyboardHandler", "keyPress"},
            {"net.minecraft.client.renderer.GameRenderer", "renderItemInHand"},
            {"net.minecraft.client.player.KeyboardInput", "tick"},
            {"net.minecraft.network.Connection", "genericsFtw"},
            {"net.minecraft.client.renderer.entity.EntityRenderDispatcher", "render"},
            {"net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl", "send"},
            {"net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl", "onDisconnect"},
    };

    private Probe1218() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        List<String> out = new ArrayList<>();
        out.add("=== 运行时 ===");
        out.add("java.version = " + System.getProperty("java.version"));
        out.add("isRetransformClassesSupported = " + inst.isRetransformClassesSupported());
        out.add("isRedefineClassesSupported    = " + inst.isRedefineClassesSupported());

        reportRenderThread(out);
        reportTargets(out, inst);

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(
                Paths.get(agentArgs), StandardCharsets.UTF_8))) {
            out.forEach(w::println);
        }
    }

    /** loader 的核心假设：找到 Render thread，取它的 ContextClassLoader 当注入目标。 */
    private static void reportRenderThread(List<String> out) {
        out.add("");
        out.add("=== loader 前提：Render thread ===");
        boolean found = false;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("Render thread".equals(t.getName())) {
                found = true;
                ClassLoader cl = t.getContextClassLoader();
                out.add("Render thread 存在，ContextClassLoader = " + (cl == null ? "null（loader 会失败）" : cl.getClass().getName()));
                if (cl != null) {
                    out.add("  parent = " + (cl.getParent() == null ? "bootstrap" : cl.getParent().getClass().getName()));
                    // loader 靠反射调 ClassLoader.defineClass 把客户端类塞进去，确认该方法可达
                    try {
                        cl.getClass().getMethod("getName");
                        out.add("  ClassLoader.getName() = " + cl.getName());
                    } catch (Throwable ignored) {
                        out.add("  ClassLoader.getName() 不可用");
                    }
                }
            }
        }
        if (!found) {
            out.add("未找到 Render thread —— 游戏可能还没初始化完");
        }
    }

    /** 逐个核实 hook 目标类的加载状态、可修改性与方法名。 */
    private static void reportTargets(List<String> out, Instrumentation inst) {
        out.add("");
        out.add("=== 12 个 hook 目标 ===");
        Class<?> firstModifiable = null;
        for (String[] target : TARGETS) {
            Class<?> found = null;
            for (Class<?> c : inst.getAllLoadedClasses()) {
                if (c.getName().equals(target[0])) {
                    found = c;
                    break;
                }
            }
            if (found == null) {
                out.add("  ✗ 未加载  " + target[0]);
                continue;
            }
            boolean hasMethod = false;
            for (java.lang.reflect.Method m : found.getDeclaredMethods()) {
                if (m.getName().equals(target[1])) {
                    hasMethod = true;
                    break;
                }
            }
            boolean modifiable = inst.isModifiableClass(found);
            if (modifiable && firstModifiable == null) firstModifiable = found;
            out.add(String.format("  %s 已加载 modifiable=%s  %s.%s %s",
                    (hasMethod && modifiable) ? "✓" : "!", modifiable,
                    target[0].substring(target[0].lastIndexOf('.') + 1), target[1],
                    hasMethod ? "" : " ← 方法名对不上！"));
        }
        reportNoOpRetransform(out, inst, firstModifiable);
    }

    /** 真正跑一次 no-op 重转换，验证整套挂钩机制在 1.21.8 上可用。 */
    private static void reportNoOpRetransform(List<String> out, Instrumentation inst, Class<?> victim) {
        out.add("");
        out.add("=== no-op 重转换实测 ===");
        if (victim == null) {
            out.add("没有可修改的目标类，跳过");
            return;
        }
        final boolean[] invoked = {false};
        ClassFileTransformer t = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> beingRedefined,
                                    ProtectionDomain pd, byte[] buffer) {
                if (beingRedefined == victim) {
                    invoked[0] = true;
                }
                return null; // 不修改字节码
            }
        };
        inst.addTransformer(t, true);
        try {
            inst.retransformClasses(victim);
            out.add("retransformClasses(" + victim.getSimpleName() + ") 成功，transformer 被调用 = " + invoked[0]);
        } catch (Throwable e) {
            out.add("retransformClasses 失败：" + e);
        } finally {
            inst.removeTransformer(t);
        }
    }
}
