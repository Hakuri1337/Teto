import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 逐 hook 验证探针。
 * <p>
 * 原理：每个 EventXxx 类只有在 ASM 注入的 INVOKESTATIC <b>首次真正执行</b>时才会被加载。
 * 所以「该类是否已加载」就是对应 hook 是否生效的硬证据，比读 stdout 可靠
 * （游戏 stdout 被启动器接管，不落 latest.log）。
 */
public final class HookProbe {

    /** 事件类 -> 负责触发它的 hook（用于把结果读成人话）。 */
    private static final String[][] EVENTS = {
            {"EventTick", "TickHook"},
            {"EventRender2D", "Render2DHook"},
            {"EventRender3D", "Render3DHook"},
            {"EventCamera", "CameraHook"},
            {"EventInvMove", "InvMoveHook"},
            {"EventAfterAttack", "AfterAttackHook（需打一次怪）"},
            {"EventKey", "KeyHook（需按一次键）"},
            {"EventMoveInput", "MoveInputHook"},
            {"EventPacket", "PacketReceiveHook / PacketSendHook"},
            {"EventChamsPre", "ChamsHook（需开 Chams）"},
            {"EventChamsAfter", "ChamsHook（需开 Chams）"},
            {"EventKick", "KickHook（需被踢一次，平时不会加载）"},
            {"EventModuleToggle", "模块开关（非 hook）"},
    };

    private HookProbe() {
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        List<String> out = new ArrayList<>();
        Map<String, Class<?>> loaded = new LinkedHashMap<>();
        for (Class<?> c : inst.getAllLoadedClasses()) {
            String n = c.getName();
            if (n.startsWith("tech.hakuri.teto.")) {
                loaded.put(n, c);
            }
        }

        out.add("=== 渲染实现 ===");
        out.add("Renderer1201 已加载 = " + loaded.containsKey("tech.hakuri.teto.mc1201.Renderer1201"));
        out.add("Renderer1218 已加载 = " + loaded.containsKey("tech.hakuri.teto.mc1218.Renderer1218"));
        reportAtlas(out, loaded);

        out.add("");
        out.add("=== 逐 hook 生效情况（事件类被加载 = 注入的调用真的执行过）===");
        for (String[] e : EVENTS) {
            boolean hit = loaded.containsKey("tech.hakuri.teto.event.impl." + e[0]);
            out.add(String.format("  %s %-18s <- %s", hit ? "✓" : "·", e[0], e[1]));
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(
                Paths.get(args), StandardCharsets.UTF_8))) {
            out.forEach(w::println);
        }
    }

    private static void reportAtlas(List<String> out, Map<String, Class<?>> loaded) {
        Class<?> rc = loaded.get("tech.hakuri.teto.mc1218.Renderer1218");
        out.add("AtlasFont 已加载 = " + loaded.containsKey("tech.hakuri.teto.mc1218.AtlasFont"));
        if (rc == null) {
            return;
        }
        try {
            Object singleton = rc.getMethod("get").invoke(null);
            for (String name : new String[]{"fontsUnavailable", "fonts"}) {
                try {
                    Field f = rc.getDeclaredField(name);
                    f.setAccessible(true);
                    Object v = f.get(singleton);
                    if (v instanceof Map<?, ?> m) {
                        out.add("已建成的字号 = " + m.size() + " " + m.keySet()
                                + (m.isEmpty() ? "   ← 空说明还在用原版字体" : "   ← 图集生效中"));
                    } else {
                        out.add("fontsUnavailable = " + v
                                + (Boolean.TRUE.equals(v) ? "   ← 真则永久退回原版字体" : ""));
                    }
                } catch (NoSuchFieldException ex) {
                    out.add(name + " 字段不存在 —— 进程里跑的是旧版 client-1218.jar");
                }
            }
        } catch (Throwable t) {
            out.add("读取 Renderer1218 状态失败：" + t);
        }
    }
}
