import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 用户注入器：附加到运行中的 Minecraft JVM，通过 Attach API 热加载 loader/$.class。
 * 用法：java --add-modules jdk.attach -jar teto-injector.jar [--pid <pid>|--list|--help]
 */
public final class Injector {
    private static final String[] MC_KEYWORDS = {"minecraft", "modlauncher", "forge", "fml", "bootstraplauncher"};

    private Injector() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Throwable throwable) {
            System.err.println("[injector] " + throwable.getMessage());
            throwable.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        String pid = null;
        boolean listOnly = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--pid".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--pid 需要一个进程 ID 参数。");
                }
                pid = args[++i];
            } else if ("--list".equals(arg)) {
                listOnly = true;
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printUsage();
                return;
            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException("未知参数：" + arg);
            } else {
                pid = arg;
            }
        }

        List<VirtualMachineDescriptor> descriptors = new ArrayList<>(VirtualMachine.list());
        descriptors.sort(Comparator.comparing(VirtualMachineDescriptor::id));

        if (listOnly) {
            for (VirtualMachineDescriptor descriptor : descriptors) {
                System.out.println(descriptor.id() + "\t" + descriptor.displayName());
            }
            return;
        }

        if (pid == null || pid.isBlank()) {
            pid = chooseMinecraftPid(descriptors);
        }

        String currentPid = String.valueOf(ProcessHandle.current().pid());
        if (currentPid.equals(pid)) {
            throw new IllegalStateException(
                    "不能附加到当前 JVM（注入器自身），请指定运行中的 Minecraft 进程 ID，可用 --list 查看。");
        }

        Path jarPath = ownJarPath();
        Path loaderClassPath = resolveLoaderClass();

        System.out.println("正在附加到 JVM " + pid + " ...");
        VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            vm.loadAgent(jarPath.toString(), loaderClassPath.toString());
            System.out.println("注入完成：已向 JVM " + pid + " 热加载 loader/$.class");
        } finally {
            vm.detach();
        }
    }

    private static String chooseMinecraftPid(List<VirtualMachineDescriptor> descriptors) throws IOException {
        String currentPid = String.valueOf(ProcessHandle.current().pid());
        List<VirtualMachineDescriptor> candidates = new ArrayList<>();
        for (VirtualMachineDescriptor descriptor : descriptors) {
            if (currentPid.equals(descriptor.id())) {
                continue;
            }
            String display = descriptor.displayName();
            if (display == null) {
                continue;
            }
            String lower = display.toLowerCase();
            for (String keyword : MC_KEYWORDS) {
                if (lower.contains(keyword)) {
                    candidates.add(descriptor);
                    break;
                }
            }
        }
        if (candidates.isEmpty()) {
            for (VirtualMachineDescriptor descriptor : descriptors) {
                if (!currentPid.equals(descriptor.id())) {
                    candidates.add(descriptor);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "未检测到运行中的 Java 进程；请确认游戏已启动并进入主菜单后重试，或用 --pid 指定进程 ID。");
        }
        if (candidates.size() == 1) {
            System.out.println("自动选择 JVM " + candidates.get(0).id() + "：" + candidates.get(0).displayName());
            return candidates.get(0).id();
        }
        System.out.println("检测到多个 Java 进程，请选择目标 Minecraft JVM：");
        for (int i = 0; i < candidates.size(); i++) {
            VirtualMachineDescriptor descriptor = candidates.get(i);
            System.out.println("  [" + i + "] " + descriptor.id() + "  " + descriptor.displayName());
        }
        String input = readLine();
        int index;
        try {
            index = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("无效选择：" + input);
        }
        if (index < 0 || index >= candidates.size()) {
            throw new IllegalArgumentException("无效选择：" + input);
        }
        return candidates.get(index).id();
    }

    private static String readLine() throws IOException {
        Console console = System.console();
        if (console != null) {
            String line = console.readLine();
            if (line == null) {
                throw new IllegalStateException("无法读取输入；请在交互式窗口运行，或用 --pid 指定进程 ID。");
            }
            return line;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("无法读取输入；请在交互式窗口运行，或用 --pid 指定进程 ID。");
            }
            return line;
        }
    }

    private static Path ownJarPath() {
        try {
            Path location = Paths.get(
                    Injector.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath().normalize();
            if (Files.isDirectory(location)) {
                throw new IllegalStateException(
                        "注入器必须以 JAR 形式运行：java --add-modules jdk.attach -jar teto-injector.jar");
            }
            return location;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("无法定位注入器 JAR 路径。", ex);
        }
    }

    private static Path resolveLoaderClass() {
        List<Path> attempts = new ArrayList<>();
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

    private static void printUsage() {
        System.out.println("用法：java --add-modules jdk.attach -jar teto-injector.jar [选项]");
        System.out.println("  --pid <pid>   指定目标 Minecraft JVM 的进程 ID");
        System.out.println("  --list        列出当前所有 Java 进程");
        System.out.println("  --help        显示本帮助");
        System.out.println("不带参数时自动检测 Minecraft JVM；多个候选时交互选择。");
    }
}
