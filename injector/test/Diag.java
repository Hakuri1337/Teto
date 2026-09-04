import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 诊断代理：附加到已注入的游戏进程，把客户端状态写入文件。
 * 之所以写文件而不是打印，是因为游戏进程的 stdout 被启动器接管，未必落到 latest.log。
 * 用法：agentArgs = 输出文件的绝对路径。
 */
public final class Diag {

    private Diag() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        List<String> out = new ArrayList<>();
        out.add("isRetransformClassesSupported = " + inst.isRetransformClassesSupported());

        Object viaProp = System.getProperties().get("teto.instrumentation");
        out.add("通道二 System.getProperties(teto.instrumentation) = " + (viaProp != null));
        try {
            Class<?> agent = Class.forName("Agent", false, ClassLoader.getSystemClassLoader());
            out.add("通道一 Agent.INSTRUMENTATION = " + (agent.getField("INSTRUMENTATION").get(null) != null));
        } catch (Throwable t) {
            out.add("通道一 Agent 不可见：" + t);
        }

        int tetoCount = 0;
        boolean eventTick = false;
        boolean eventRender2D = false;
        Class<?> moduleManager = null;
        Class<?> platform = null;
        Class<?> renderer1201 = null;
        for (Class<?> c : inst.getAllLoadedClasses()) {
            String n = c.getName();
            if (!n.startsWith("tech.hakuri.teto")) continue;
            tetoCount++;
            switch (n) {
                case "tech.hakuri.teto.feature.ModuleManager" -> moduleManager = c;
                case "tech.hakuri.teto.platform.Platform" -> platform = c;
                case "tech.hakuri.teto.mc1201.Renderer1201" -> renderer1201 = c;
                // 这两个类只有在 ASM 注入的 INVOKESTATIC 首次执行时才会被加载
                case "tech.hakuri.teto.event.impl.EventTick" -> eventTick = true;
                case "tech.hakuri.teto.event.impl.EventRender2D" -> eventRender2D = true;
                default -> {
                }
            }
        }
        out.add("已加载的 tech.hakuri.teto.* 类数 = " + tetoCount);
        out.add("EventTick 已加载（证明 TickHook 的注入调用真的在执行）= " + eventTick);
        out.add("EventRender2D 已加载（证明 Render2DHook 生效）= " + eventRender2D);

        // 绘制门面是否装配。getAllLoadedClasses 给的是真实 Class 对象，
        // 所以不必操心它属于 Minecraft 的哪个 ClassLoader，直接反射调用即可。
        if (platform == null) {
            out.add("Platform 未加载 —— 绘制抽象层没有参与运行");
        } else {
            Object ready = platform.getMethod("ready").invoke(null);
            out.add("Platform.ready() = " + ready + (Boolean.TRUE.equals(ready) ? "" : "  ← 绘制门面没装上"));
        }
        out.add("Renderer1201 已加载 = " + (renderer1201 != null));

        if (moduleManager == null) {
            out.add("ModuleManager 未加载 —— 客户端没有成功初始化");
        } else {
            List<?> list = (List<?>) moduleManager.getField("modules").get(null);
            out.add("ModuleManager.modules 数量 = " + list.size());
            List<String> enabled = new ArrayList<>();
            for (Object m : list) {
                Field enableField = findField(m.getClass(), "enable");
                if (enableField != null && enableField.getBoolean(m)) {
                    Field nameField = findField(m.getClass(), "name");
                    enabled.add(nameField == null ? m.getClass().getSimpleName() : String.valueOf(nameField.get(m)));
                }
            }
            out.add("已启用模块数 = " + enabled.size() + "  " + enabled);
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(
                Paths.get(agentArgs), StandardCharsets.UTF_8))) {
            out.forEach(w::println);
        }
    }

    /** enable/name 声明在 Module 基类上，模块实例是子类，所以要往父类找。 */
    private static Field findField(Class<?> c, String name) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try {
                Field f = k.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                // 继续往父类找
            }
        }
        return null;
    }
}
