package tech.hakuri.teto.event.impl;

import tech.hakuri.teto.event.Event;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.platform.Platform;

/**
 * 2D 渲染事件。
 * <p>
 * 上下文由钩子从被挂钩的方法里直接取，而不是自己 new 一个：
 * 1.20.1 可以 {@code new GuiGraphics(mc, bufferSource)}，但 1.21.6 起 {@code GuiGraphics}
 * 要求一个 {@code GuiRenderState}（两阶段提交模型），客户端凭空造不出来。
 * 好在两版被挂钩的 {@code Gui.renderCrosshair} 第一个参数都是 {@code GuiGraphics}，
 * 钩子 {@code ALOAD 1} 拿到的就是本帧真正在用的那一个 —— 比伪造一个更正确。
 * <p>
 * 参数类型用 {@code Object} 是因为共享代码不能引用版本相关的构造方式；
 * 具体类型由各版本的 {@code IRenderer} 实现在 {@code beginFrame2D} 里 cast。
 */
public class EventRender2D extends Event {

    public EventRender2D(Object guiGraphics) {
        //把本帧的 2D 上下文交给 IRenderer 实现，模块内部就不必再传 guiGraphics
        Platform.renderer().beginFrame2D(guiGraphics);
        for (Module module : ModuleManager.modules) {
            if (module.enable) module.onRender2D(this);
        }
    }

    public static void r2d(Object guiGraphics) {
        new EventRender2D(guiGraphics);
    }
}
