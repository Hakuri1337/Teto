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
        boolean elevated = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--pid".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--pid 需要一个进程 ID 参数。");
                }
                pid = args[++i];
            } else if ("--list".equals(arg)) {
                listOnly = true;
            } else if ("--elevated".equals(arg)) {
                //内部标记：本进程已经是提权重启后的那一次。用来防止无限提权循环，
                //并在结束时把控制台停住，否则 UAC 新开的窗口会一闪而过看不到结果。
                elevated = true;
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
        VirtualMachine vm;
        try {
            vm = VirtualMachine.attach(pid);
        } catch (IOException ex) {
            if (isAccessDenied(ex) && !elevated && offerElevation(pid)) {
                relaunchElevated(pid, jarPath);
                return;
            }
            throw explainAttachFailure(pid, ex, elevated);
        }
        try {
            vm.loadAgent(agentJarForAttach(jarPath).toString(), loaderClassPath.toString());
            System.out.println("注入完成：已向 JVM " + pid + " 热加载 loader/$.class");
        } finally {
            vm.detach();
        }
        if (elevated) {
            //提权后是一个新控制台窗口，不停一下用户看不到结果
            System.out.println();
            System.out.println("按回车关闭本窗口。");
            readLineQuietly();
        }
    }

    /**
     * 把注入器 jar 复制一份到临时目录再交给 {@code loadAgent}。
     * <p>
     * 原因：{@code loadAgent} 会让目标 JVM 把 agent jar 内存映射并一直持有到进程退出。
     * 如果直接传部署目录里的那个 jar，那么只要被注入过的游戏还在跑，
     * 重新部署就会失败（Windows 报「请求的操作无法在使用用户映射区域打开的文件上执行」）。
     * 换成临时副本后，部署目录里的文件永远不会被锁。
     * <p>
     * 临时副本没法在游戏退出前删除（正被映射），所以只做启动时的顺手清理：
     * 能删的删掉，删不掉的说明还在用，跳过。
     */
    private static Path agentJarForAttach(Path ownJar) {
        try {
            Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
            cleanStaleAgentCopies(dir);
            Path copy = Files.createTempFile(dir, "teto-agent-", ".jar");
            Files.copy(ownJar, copy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return copy;
        } catch (IOException ex) {
            //复制失败就退回原路径：功能照常，只是部署时可能遇到文件占用
            System.out.println("[injector] 无法创建 agent 临时副本（" + ex.getMessage() + "），直接使用原 jar。");
            return ownJar;
        }
    }

    private static void cleanStaleAgentCopies(Path dir) {
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().startsWith("teto-agent-"))
                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                            // 还被某个运行中的游戏映射着，跳过
                        }
                    });
        } catch (IOException ignored) {
            // 列不出临时目录就算了，不影响注入
        }
    }

    private static boolean isAccessDenied(IOException ex) {
        String message = String.valueOf(ex.getMessage());
        return message.contains("拒绝访问") || message.toLowerCase().contains("access is denied");
    }

    /** 询问用户是否用管理员权限重试。默认否 —— 提权应当由用户明确同意。 */
    private static boolean offerElevation(String pid) throws IOException {
        System.out.println();
        System.out.println("附加被系统拒绝（拒绝访问）。");
        System.out.println("原因几乎总是：目标进程 " + pid + " 以管理员身份运行，而本注入器不是。");
        System.out.println("Windows 要求调用方权限不低于目标进程，否则 OpenProcess 直接失败。");
        System.out.println();
        System.out.print("是否以管理员权限重新注入同一个 PID？会弹出 UAC 确认 [y/N]: ");
        String answer = readLineQuietly();
        boolean yes = answer != null && (answer.trim().equalsIgnoreCase("y") || answer.trim().equalsIgnoreCase("yes"));
        if (!yes) {
            System.out.println("已取消。也可以改成不用管理员身份启动游戏，那样双方权限就对齐了。");
        }
        return yes;
    }

    /**
     * 用管理员权限重启自己，参数里带上同一个 PID 和 {@code --elevated}。
     * <p>
     * Java 没有原生提权手段，Windows 上唯一的正路是让 shell 以 {@code runas} 动词启动进程，
     * 由系统弹 UAC。这里借 PowerShell 的 {@code Start-Process -Verb RunAs} 完成。
     * 新进程是独立控制台窗口，所以带 {@code --elevated} 的那次会在结束时等一下回车。
     */
    private static void relaunchElevated(String pid, Path jarPath) throws IOException {
        Path javaExe = Paths.get(System.getProperty("java.home"), "bin", "java.exe");
        if (!Files.isRegularFile(javaExe)) {
            throw new IllegalStateException("找不到 java.exe：" + javaExe + "，无法提权重启。");
        }
        //单引号内的单引号在 PowerShell 里写成两个
        String argList = String.join(",",
                psQuote("-Dfile.encoding=UTF-8"),
                psQuote("-Dsun.stdout.encoding=UTF-8"),
                psQuote("-Dsun.stderr.encoding=UTF-8"),
                psQuote("--add-modules"), psQuote("jdk.attach"),
                psQuote("-jar"), psQuote(jarPath.toString()),
                psQuote("--pid"), psQuote(pid),
                psQuote("--elevated"));
        String command = "Start-Process -FilePath " + psQuote(javaExe.toString())
                + " -ArgumentList " + argList + " -Verb RunAs";

        System.out.println("正在请求管理员权限（请在 UAC 弹窗里确认）...");
        Process process = new ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", command)
                .inheritIO()
                .start();
        try {
            int code = process.waitFor();
            if (code != 0) {
                System.err.println("[injector] 提权启动失败（PowerShell 退出码 " + code + "）。"
                        + "通常是 UAC 弹窗被拒绝。");
                return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
        }
        System.out.println("已在管理员权限的新窗口里重新注入，注入结果请看那个窗口。");
    }

    private static String psQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static String readLineQuietly() throws IOException {
        Console console = System.console();
        if (console != null) {
            return console.readLine();
        }
        return new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
    }

    /**
     * Attach API 在 Windows 上失败时只会抛一个干巴巴的「拒绝访问」，
     * 而真正的原因几乎总是权限等级不匹配。这里把它翻译成可操作的提示。
     */
    private static IllegalStateException explainAttachFailure(String pid, IOException ex, boolean elevated) {
        if (!isAccessDenied(ex)) {
            return new IllegalStateException("附加到 JVM " + pid + " 失败：" + ex.getMessage(), ex);
        }
        if (elevated) {
            return new IllegalStateException(
                    "已经以管理员权限运行，附加 JVM " + pid + " 仍被拒绝。\n"
                            + "这说明不是权限等级的问题，请检查：\n"
                            + "  1. PID 是否真的指向游戏进程（任务管理器 -> 详细信息 -> PID 列）；\n"
                            + "  2. 杀毒软件/EDR 是否拦截了 OpenProcess；\n"
                            + "  3. 注入器与游戏是否为同一个 Windows 用户。", ex);
        }
        return new IllegalStateException(
                "附加到 JVM " + pid + " 被系统拒绝（拒绝访问）。\n"
                        + "最常见原因：目标游戏以管理员身份运行，而注入器不是。\n"
                        + "解法：以管理员身份运行注入器，或者不用管理员身份启动游戏（推荐后者）。", ex);
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
        System.out.println();
        System.out.println("若目标游戏以管理员身份运行，附加会被系统拒绝。此时注入器会询问是否提权，");
        System.out.println("同意后会弹 UAC 并在新窗口中以管理员权限重新注入同一个 PID。");
    }
}
