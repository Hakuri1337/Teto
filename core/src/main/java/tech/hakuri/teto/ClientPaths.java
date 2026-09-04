package tech.hakuri.teto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 客户端名称与资源路径。纯环境/路径逻辑，不碰 Minecraft，所以放在 core 里两个版本共用。
 * <p>
 * 原先这些方法长在 {@code ClientEntry} 上，而 {@code ClientEntry} 是版本专属的
 * （里面有模块注册、字体初始化、hook 安装），导致 HUD 和 WebClickGUI 只为了取一个
 * 名字或配置目录就被钉死在某个版本上。拆出来之后那两个类就能共享了。
 */
public final class ClientPaths {

    private static final String CLIENT_FOLDER_NAME = "Teto";

    private ClientPaths() {
    }

    /** 写成方法比较隐蔽一点。 */
    public static String getClientName() {
        return "Teto";
    }

    /**
     * 客户端资源根目录：%LOCALAPPDATA%\Teto（即 AppData\Local\Teto）。
     * LOCALAPPDATA 不可用时，使用 user.home\AppData\Local 作为等价回退。
     */
    public static Path getClientFolder() {
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
        return Paths.get(localAppData, CLIENT_FOLDER_NAME).toAbsolutePath().normalize();
    }

    /** 相同目录：进程当前工作目录。游戏内组件从这里与 getClientFolder() 两处读取资源。 */
    public static Path getLocalDirectory() {
        String userDir = System.getProperty("user.dir");
        if (userDir == null || userDir.isBlank()) {
            return Paths.get("").toAbsolutePath().normalize();
        }
        return Paths.get(userDir).toAbsolutePath().normalize();
    }

    public static Path getConfigDirectory() {
        return getClientFolder().resolve("config").normalize();
    }

    public static List<Path> getConfigDirectoriesForRead() {
        Set<Path> directories = new LinkedHashSet<>();
        directories.add(getLocalDirectory().resolve("config").normalize());
        directories.add(getConfigDirectory());
        return new ArrayList<>(directories);
    }

    /** 资源查找顺序：相同目录（当前工作目录）-> AppData\Local\Teto。 */
    public static Path resolveClientResource(String fileName, String settingName) {
        List<Path> attempts = new ArrayList<>();
        addResourceCandidate(attempts, getLocalDirectory().resolve(fileName));
        addResourceCandidate(attempts, getClientFolder().resolve(fileName));

        for (Path attempt : attempts) {
            if (Files.isRegularFile(attempt)) {
                return attempt;
            }
        }
        throw new IllegalStateException("未找到资源 " + fileName + "。查找顺序：" + attempts);
    }

    private static void addResourceCandidate(List<Path> attempts, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
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
