package client;

import a.a;
import client.feature.Module;
import client.feature.ModuleManager;
import client.feature.impl.block.FastPlace;
import client.feature.impl.block.GhostBlock;
import client.feature.impl.block.NoBreakDelay;
import client.feature.impl.block.Scaffold;
import client.feature.impl.combat.*;
import client.feature.impl.inventory.AutoTool;
import client.feature.impl.inventory.AutoWeapon;
import client.feature.impl.inventory.ChestStealer;
import client.feature.impl.inventory.FastInvClick;
import client.feature.impl.misc.*;
import client.feature.impl.move.*;
import client.feature.impl.render.*;
import client.font.FontManager;
import client.font.TrueTypeFont;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

//这个类尽量不要让攻击者很快锁定到，所以我们套了一层又一层，使得现在这个类既不继承Thread但又能多线程加载，不使用@Mod注解但在idea内调试又能正常工作
public class ClientEntry {

    private static final String CLIENT_FOLDER_NAME = "Teto";

    //写成方法比较隐蔽一点
    public static String getClientName() {
        return "幻影";
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

    /**
     * 相同目录：进程当前工作目录。游戏内组件从这里与 getClientFolder() 两处读取资源。
     */
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

    /**
     * 资源查找顺序：相同目录（当前工作目录）-> AppData\Local\Teto。
     */
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

    private static String resolveHookDllPath() {
        return resolveClientResource("hook.dll", "HookDll").toString();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public static Entity init(Entity e) {
        try {
            System.setProperty("java.awt.headless", "false");//awt有自己的线程，是异步的
            System.setProperty("file.encoding", "UTF-8");//这个用来解决乱码，唉
            System.setProperty("org.bytedeco.javacpp.logger.debug", "true");//迟早会写出来的，视频播放器

            //idea内调试时forge会在初始化mc前启动我们的客户端，同时因为awt异步的原因，该项请保留，发布时可以适当调小，或者不调
            //只要调用此函数的不是渲染线程，直接Sleep就不阻塞渲染
            Thread.sleep(3000);

            //无论如何先初始化字体
            //我直接在mc线程初始化，我就不信还不行
            RenderSystem.recordRenderCall(new RenderCall() {
                @Override
                public void execute() {
                    FontManager.xs = new TrueTypeFont(new Font("微软雅黑", Font.PLAIN, 10));
                    FontManager.s = new TrueTypeFont(new Font("微软雅黑", Font.PLAIN, 20));
                    FontManager.m = new TrueTypeFont(new Font("微软雅黑", Font.PLAIN, 25));
                    FontManager.l = new TrueTypeFont(new Font("微软雅黑", Font.PLAIN, 30));
                    FontManager.xl = new TrueTypeFont(new Font("微软雅黑", Font.BOLD, 100));
                }
            });
            //保险起见等一秒
            Thread.sleep(1000);

            //先初始化功能，再初始化Event会比较好
            ModuleManager.modules.add(new FastPlace());
            ModuleManager.modules.add(new NoBreakDelay());
            ModuleManager.modules.add(new Scaffold());

            ModuleManager.modules.add(new AutoClicker());
            ModuleManager.modules.add(new KeepRange());
            ModuleManager.modules.add(new KillAura());
            ModuleManager.modules.add(new NoClickDelay());
            ModuleManager.modules.add(new Reach());
            ModuleManager.modules.add(new SuperKB());
            ModuleManager.modules.add(new TriggerBot());

            ModuleManager.modules.add(new AutoTool());
            ModuleManager.modules.add(new AutoWeapon());
            ModuleManager.modules.add(new ChestStealer());
            ModuleManager.modules.add(new FastInvClick());

            ModuleManager.modules.add(new ClickManager());
            ModuleManager.modules.add(new KeyListener());
            ModuleManager.modules.add(new KillEffect());
            ModuleManager.modules.add(new TargetManager());

            ModuleManager.modules.add(new Eagle());
            ModuleManager.modules.add(new Fly());
            ModuleManager.modules.add(new InvMove());
            ModuleManager.modules.add(new JumpReset());
            ModuleManager.modules.add(new KeepSprint());
            ModuleManager.modules.add(new NoFall());
            ModuleManager.modules.add(new NoJumpDelay());
            ModuleManager.modules.add(new Sprint());

            ModuleManager.modules.add(new RotationManager());
            ModuleManager.modules.add(new Chams());
            ModuleManager.modules.add(new ESP());
            ModuleManager.modules.add(new ExternalClickGui());
            ModuleManager.modules.add(new InGameClickGUI());
            ModuleManager.modules.add(new NightVision());
            ModuleManager.modules.add(new WebGUI());

            ModuleManager.modules.add(new Aim());
            ModuleManager.modules.add(new PingPongKB());
            ModuleManager.modules.add(new PlayerDetector());
            ModuleManager.modules.add(new ExternalHUD());
            ModuleManager.modules.add(new AutoFishing());
            ModuleManager.modules.add(new AutoSoup());
            ModuleManager.modules.add(new BlockNoKB());
            ModuleManager.modules.add(new Criticals());
            ModuleManager.modules.add(new StopEvading());
            ModuleManager.modules.add(new AutoHead());
            ModuleManager.modules.add(new TPAura());
            ModuleManager.modules.add(new GhostBlock());
            ModuleManager.modules.add(new Regen());
            ModuleManager.modules.add(new KickReason());
            ModuleManager.modules.add(new MultiAura());
            ModuleManager.modules.add(new PulseTimer());

            HUD hud = new HUD();
            ModuleManager.modules.add(hud);

            ModuleManager.modules.sort(Comparator.comparingInt(new ToIntFunction<Module>() {
                @Override
                public int applyAsInt(Module module) {
                    return module.name.length();
                }
            }));
            Collections.reverse(ModuleManager.modules);

            hud.toggle();

            //先初始化功能，再初始化Event会比较好
            System.load(resolveHookDllPath());
            a.a(Gui.class);
            a.a(Camera.class);
            a.a(Player.class);
            a.a(Connection.class);
            a.a(KeyMapping.class);
            a.a(LocalPlayer.class);
            a.a(GameRenderer.class);
            a.a(KeyboardInput.class);
            a.a(KeyboardHandler.class);
            a.a(ClientPacketListener.class);
            a.a(EntityRenderDispatcher.class);


            //函数结束，不往下执行了，外面套的这个try刚好相当于把这一个函数拆成了两个函数
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Client initialization failed", ex);
        }
    }

}
