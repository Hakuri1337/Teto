package tech.hakuri.teto.mc1218;

import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.impl.block.FastPlace;
import tech.hakuri.teto.feature.impl.block.GhostBlock;
import tech.hakuri.teto.feature.impl.block.NoBreakDelay;
import tech.hakuri.teto.feature.impl.block.Scaffold;
import tech.hakuri.teto.feature.impl.combat.*;
import tech.hakuri.teto.feature.impl.inventory.AutoTool;
import tech.hakuri.teto.feature.impl.inventory.AutoWeapon;
import tech.hakuri.teto.feature.impl.inventory.ChestStealer;
import tech.hakuri.teto.feature.impl.inventory.FastInvClick;
import tech.hakuri.teto.feature.impl.misc.*;
import tech.hakuri.teto.feature.impl.move.*;
import tech.hakuri.teto.feature.impl.render.*;
import tech.hakuri.teto.hook.Transformer;
import tech.hakuri.teto.platform.Platform;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.ToIntFunction;

/**
 * 1.21.8 / NeoForge 的客户端初始化入口，对应 1.20.1 侧的 {@code ClientEntry}。
 * <p>
 * 与 1.20.1 版本的两处关键差别：
 * <ul>
 *   <li><b>不做字体初始化</b>。1.20.1 要在 {@code RenderSystem.recordRenderCall} 里
 *       构造 5 个 {@code TrueTypeFont}；1.21.8 的 {@link Renderer1218} 直接用原版
 *       {@code Font}（图集化、批量提交），既不需要预热也没有 {@code recordRenderCall} 这个 API。</li>
 *   <li>装配的是 {@link Renderer1218}。</li>
 * </ul>
 * 模块注册顺序与 1.20.1 保持一致，这样两版的功能列表排序和配置文件互相兼容。
 */
public final class ClientEntry1218 {

    private ClientEntry1218() {
    }

    public static void init() {
        try {
            System.setProperty("java.awt.headless", "false");
            System.setProperty("file.encoding", "UTF-8");

            //等游戏把渲染线程和世界跑起来，理由同 1.20.1
            Thread.sleep(3000);

            //绘制门面必须在 hud.toggle() 之前装好 —— onModuleToggle 会用它算行高
            Platform.install(Renderer1218.get());

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

            //用 java.lang.instrument 重转换 Minecraft 的类，与 1.20.1 走同一套机制
            Transformer.install();
        } catch (Exception ex) {
            throw new IllegalStateException("Client initialization failed", ex);
        }
    }
}
