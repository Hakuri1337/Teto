import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 注入器代理。由注入器通过 Attach API 的 loadAgent 载入目标 Minecraft JVM，
 * 负责定义并实例化 loader/$.class；loader 线程随后完成客户端热注入。
 */
public final class Agent {
    private Agent() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Path loaderClassPath = resolveLoaderClass(agentArgs);
        byte[] classBytes;
        try {
            classBytes = Files.readAllBytes(loaderClassPath);
        } catch (IOException ex) {
            throw new IllegalStateException("无法读取 loader/$.class：" + loaderClassPath, ex);
        }
        if (classBytes.length == 0) {
            throw new IllegalStateException("LoaderClass 为空：" + loaderClassPath);
        }
        try {
            LoaderClassLoader classLoader = new LoaderClassLoader();
            Class<?> loaderClass = classLoader.define("$", classBytes);
            Constructor<?> constructor = loaderClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            System.out.println("[agent] 已实例化 loader/$.class：" + loaderClassPath);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
            throw new IllegalStateException("无法加载 loader/$.class", ex);
        }
    }

    private static Path resolveLoaderClass(String agentArgs) {
        List<Path> attempts = new ArrayList<>();
        if (agentArgs != null && !agentArgs.isBlank()) {
            addCandidate(attempts, Paths.get(agentArgs));
        }
        addCandidate(attempts, getLocalDirectory().resolve("$.class"));
        addCandidate(attempts, getClientFolder().resolve("$.class"));

        for (Path attempt : attempts) {
            if (Files.isRegularFile(attempt)) {
                return attempt;
            }
        }
        throw new IllegalStateException("未找到 loader/$.class。查找顺序：" + attempts);
    }

    private static Path getClientFolder() {
        String localAppData = firstNonBlank(System.getenv("LOCALAPPDATA"), System.getProperty("LOCALAPPDATA"));
        if (localAppData == null) {
            String userHome = firstNonBlank(System.getProperty("user.home"), System.getenv("USERPROFILE"));
            if (userHome != null) {
                localAppData = Paths.get(userHome, "AppData", "Local").toString();
            }
        }
        if (localAppData == null) {
            throw new IllegalStateException("无法确定 LOCALAPPDATA；请设置 LOCALAPPDATA 或 USERPROFILE。");
        }
        return Paths.get(localAppData, "Teto").toAbsolutePath().normalize();
    }

    private static Path getLocalDirectory() {
        String userDir = System.getProperty("user.dir");
        if (userDir == null || userDir.isBlank()) {
            return Paths.get("").toAbsolutePath().normalize();
        }
        return Paths.get(userDir).toAbsolutePath().normalize();
    }

    private static void addCandidate(List<Path> attempts, Path candidate) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!attempts.contains(normalized)) {
            attempts.add(normalized);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static final class LoaderClassLoader extends ClassLoader {
        private LoaderClassLoader() {
            super(null);
        }

        private Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
