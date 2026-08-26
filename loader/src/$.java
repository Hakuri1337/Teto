import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Deprecated(since = "?")
public class $ extends Thread {

    public $() {
        start();
    }

    @Override
    public void run() {
        try {
            ClassLoader targetClassLoader = findMinecraftClassLoader();
            Path jarPath = resolveClientJar();
            List<byte[]> readJarClasses = readJarClasses(jarPath);
            defineJarClasses(targetClassLoader, readJarClasses);
            Class.forName("$", true, targetClassLoader).getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
            System.err.println("[loader] 客户端加载失败：" + throwable);
            throwable.printStackTrace(System.err);
        }
    }

    private static ClassLoader findMinecraftClassLoader() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if ("Render thread".equals(thread.getName())) {
                ClassLoader classLoader = thread.getContextClassLoader();
                if (classLoader != null) {
                    return classLoader;
                }
            }
        }
        throw new IllegalStateException("未找到 Minecraft 的 Render thread，必须在游戏已启动后执行 loader。");
    }

    private static List<byte[]> readJarClasses(Path jarPath) throws IOException {
        List<byte[]> classes = new LinkedList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(jarPath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()
                        && entry.getName().endsWith(".class")
                        && !entry.getName().endsWith("module-info.class")) {
                    classes.add(zipInputStream.readAllBytes());
                }
            }
        }
        if (classes.isEmpty()) {
            throw new IllegalStateException("Jar 中没有可加载的 .class：" + jarPath);
        }
        return classes;
    }

    private static void defineJarClasses(ClassLoader targetClassLoader, List<byte[]> classes) throws Exception {
        Unsafe unsafe = getUnsafe();
        Module classLoaderModule = ClassLoader.class.getModule();
        long fieldAddress = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
        unsafe.getAndSetObject($.class, fieldAddress, classLoaderModule);

        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
        defineClass.setAccessible(true);

        long timeout = System.currentTimeMillis() + 1000L;
        Random random = new Random();
        while (!classes.isEmpty() && System.currentTimeMillis() < timeout) {
            byte[] picked = classes.get(random.nextInt(classes.size()));
            try {
                defineClass.invoke(targetClassLoader, picked, 0, picked.length);
                classes.remove(picked);
            } catch (InvocationTargetException ignored) {
                // 依赖类尚未定义时保留该字节码，继续随机尝试其他类。
            } catch (ReflectiveOperationException ignored) {
                // 保持原 loader 的“失败后继续尝试”行为。
            }
        }
        if (!classes.isEmpty()) {
            System.err.println("[loader] 有 " + classes.size() + " 个 class 未在超时窗口内定义成功。");
        }
    }

    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        return (Unsafe) theUnsafeField.get(null);
    }

    private static Path resolveClientJar() {
        return resolveResource("client.jar", "Jar");
    }

    private static Path resolveResource(String fileName, String settingName) {
        List<Path> attempts = new ArrayList<>();
        addCandidate(attempts, getLocalDirectory().resolve(fileName));
        addCandidate(attempts, getClientFolder().resolve(fileName));

        for (Path attempt : attempts) {
            if (Files.isRegularFile(attempt)) {
                return attempt;
            }
        }
        throw new IllegalStateException("未找到 " + settingName + " 资源。查找顺序：" + attempts);
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
}
