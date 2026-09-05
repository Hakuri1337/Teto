package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.i18n.I18n;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.IRenderer;
import tech.hakuri.teto.platform.Platform;
import tech.hakuri.teto.raster.NinePatch;
import tech.hakuri.teto.ui.legacy.BindScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Material 3 风格的 ClickGUI。完全重写，旧版保留在 {@code ui.clickgui_old}，
 * 由「内置菜单」模块的「菜单样式」值切换。
 * <p>
 * 视觉结构：左侧分类栏 + 右侧模块列表面板。所有圆角/阴影/辉光都是
 * {@link MaterialSkin} 的预烘九宫格纹理 + tint 染色，运行时零模糊。
 * 模块名 / 分类名 / 值名全走 {@link I18n}，身份（配置键）不受影响。
 * <p>
 * 交互：左键开关模块，中键进按键绑定，右键展开/收起该模块的值，
 * 滚轮滚动模块列表（超出面板高度时生效）。
 */
public class MaterialClickGUI extends Screen {

    /** 面板布局（GUI 坐标，实现内部已处理 guiScale）。 */
    private static final int PANEL_X = 40, PANEL_Y = 32;
    private static final int SIDEBAR_W = 96, HEADER_H = 30, PAD = 8;
    private static final int ROW_H = 20, VALUE_H = 16;

    private final List<String> categories = Category.getAll();
    private int selected;
    private float scroll, scrollTarget;
    private Value draggingSlider;

    public MaterialClickGUI() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        this.scissorGui = gui;
        IRenderer r = Platform.renderer();
        r.beginFrame2D(gui);

        int panelW = (int) Math.min(460, r.screenWidth() - PANEL_X * 2);
        int panelH = (int) Math.min(320, r.screenHeight() - PANEL_Y * 2);

        // 背景遮罩 + 面板（阴影 → 辉光 → 卡片底）
        r.rect(0, 0, r.screenWidth(), r.screenHeight(), UiTheme.withAlpha(0x000000, 90));
        drawPatch(MaterialSkin.cardShadow(), PANEL_X - 10, PANEL_Y - 6, panelW + 20, panelH + 16, 0);
        if (MaterialSkin.bloomOn()) {
            drawPatch(MaterialSkin.primaryGlow(), PANEL_X - 14, PANEL_Y - 10, panelW + 28, panelH + 28, 0);
        }
        drawPatch(MaterialSkin.card(), PANEL_X, PANEL_Y, panelW, panelH, UiTheme.surface());

        drawSidebar(r, PANEL_X, PANEL_Y, panelH);
        int contentX = PANEL_X + SIDEBAR_W + PAD;
        int contentW = panelW - SIDEBAR_W - PAD * 2;
        drawModules(r, contentX, PANEL_Y + PAD, contentW, panelH - PAD * 2, mouseX, mouseY);

        super.render(gui, mouseX, mouseY, delta);
    }

    /* ---------- 侧栏：分类 ---------- */

    private void drawSidebar(IRenderer r, int x, int y, int panelH) {
        r.textShadow(FontSize.M, "Teto", x + PAD, y + 10, UiTheme.primary());
        float ty = y + HEADER_H;
        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            boolean sel = i == selected;
            if (sel) {
                drawPatch(MaterialSkin.control(), x + 4, ty, SIDEBAR_W - 8, ROW_H, UiTheme.primaryContainer());
            }
            int color = sel ? UiTheme.onPrimaryContainer() : UiTheme.onSurfaceVariant();
            r.text(FontSize.S, I18n.category(cat), x + PAD + 2, ty + (ROW_H - r.textHeight(FontSize.S)) / 2f, color);
            ty += ROW_H;
        }
    }

    /* ---------- 内容区：模块 + 值 ---------- */

    private void drawModules(IRenderer r, int x, int y, int w, int h, int mouseX, int mouseY) {
        List<Module> modules = ModuleManager.getModules(categories.get(selected));
        List<float[]> layout = layout(modules, w, h);

        // 滚动插值（简单的指数趋近，手感够用了）
        scroll += (scrollTarget - scroll) * 0.35f;
        float contentH = layout.isEmpty() ? 0 : layout.get(layout.size() - 1)[1] + rowHeight(modules.get(modules.size() - 1));

        guiBeginScissor(x, y, w, h);
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            float ry = y + layout.get(i)[1] - scroll;
            if (ry + rowHeight(m) < y || ry > y + h) continue;
            drawModule(r, m, x, ry, w, mouseX, mouseY);
        }
        guiEndScissor();

        // 滚动条（内容超高才显示）
        if (contentH > h) {
            float barH = Math.max(24, h * h / contentH);
            float barY = y + (h - barH) * (scroll / Math.max(1, contentH - h));
            drawPatch(MaterialSkin.rounded(4, 3), x + w - 4, barY, 4, barH, UiTheme.withAlpha(UiTheme.outline(), 160));
        }
    }

    private void drawModule(IRenderer r, Module m, float x, float y, float w, int mouseX, int mouseY) {
        float th = r.textHeight(FontSize.S);
        boolean hover = hovering(mouseX, mouseY, x, y, w, ROW_H);

        if (m.enable) {
            drawPatch(MaterialSkin.control(), x, y, w, ROW_H, UiTheme.primaryContainer());
        } else if (hover) {
            drawPatch(MaterialSkin.control(), x, y, w, ROW_H, UiTheme.withAlpha(UiTheme.onSurface(), 18));
        }

        int nameColor = m.enable ? UiTheme.onPrimaryContainer() : UiTheme.onSurface();
        String label = m.keyCode == 0 ? m.displayName()
                : m.displayName() + " [" + tech.hakuri.teto.utils.Keyboard.get(m.keyCode) + "]";
        r.text(FontSize.S, label, x + 8, y + (ROW_H - th) / 2f, nameColor);

        drawSwitch(r, x + w - 34, y + (ROW_H - 12) / 2f, m.enable);

        // 展开的值
        if (isExpanded(m)) {
            float vy = y + ROW_H + 2;
            for (Value v : m.values) {
                drawValue(r, v, x + 10, vy, w - 20, mouseX, mouseY);
                vy += VALUE_H;
            }
        }
    }

    /** Material 3 开关：轨道 + 圆钮。 */
    private void drawSwitch(IRenderer r, float x, float y, boolean on) {
        int track = on ? UiTheme.primary() : UiTheme.withAlpha(UiTheme.outline(), 140);
        drawPatch(MaterialSkin.rounded(6, 5), x, y, 26, 12, track);
        int knob = on ? 0xFFFFFFFF : UiTheme.onSurfaceVariant();
        float kx = on ? x + 15 : x + 3;
        drawPatch(MaterialSkin.rounded(4, 3), kx, y + 2, 8, 8, knob);
    }

    private void drawValue(IRenderer r, Value v, float x, float y, float w, int mouseX, int mouseY) {
        float th = r.textHeight(FontSize.XS);
        float ty = y + (VALUE_H - th) / 2f;
        String name = I18n.value(v.name);

        if (v.isBooleanValue()) {
            boolean hover = hovering(mouseX, mouseY, x, y, w, VALUE_H);
            if (hover) {
                drawPatch(MaterialSkin.rounded(4, 3), x, y, w, VALUE_H, UiTheme.withAlpha(UiTheme.onSurface(), 12));
            }
            r.text(FontSize.XS, name, x + 2, ty, UiTheme.onSurfaceVariant());
            drawSwitch(r, x + w - 28, y + 2, v.enable);
        } else if (v.isNumberValue()) {
            String text = String.format("%s  %.2f", name, v.numberValue);
            r.text(FontSize.XS, text, x + 2, ty, UiTheme.onSurfaceVariant());
            // 滑轨 + 填充
            float trackX = x + w * 0.45f, trackW = w * 0.55f - 4;
            float pct = (v.numberValue - v.min) / Math.max(0.0001f, v.max - v.min);
            drawPatch(MaterialSkin.rounded(3, 2), trackX, y + VALUE_H / 2f - 2, trackW, 4, UiTheme.withAlpha(UiTheme.outline(), 120));
            if (pct > 0.01f) {
                drawPatch(MaterialSkin.rounded(3, 2), trackX, y + VALUE_H / 2f - 2, trackW * pct, 4, UiTheme.primary());
            }
        } else if (v.isModesValue()) {
            String text = name + ": " + v.currentMode;
            boolean hover = hovering(mouseX, mouseY, x, y, w, VALUE_H);
            if (hover) {
                drawPatch(MaterialSkin.rounded(4, 3), x, y, w, VALUE_H, UiTheme.withAlpha(UiTheme.onSurface(), 12));
            }
            r.text(FontSize.XS, text, x + 2, ty, hover ? UiTheme.onSurface() : UiTheme.onSurfaceVariant());
        }
    }

    /* ---------- 输入 ---------- */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        IRenderer r = Platform.renderer();
        int panelW = (int) Math.min(460, r.screenWidth() - PANEL_X * 2);
        int panelH = (int) Math.min(320, r.screenHeight() - PANEL_Y * 2);

        // 侧栏分类
        float ty = PANEL_Y + HEADER_H;
        for (int i = 0; i < categories.size(); i++) {
            if (hovering(mouseX, mouseY, PANEL_X + 4, ty, SIDEBAR_W - 8, ROW_H)) {
                selected = i;
                scroll = scrollTarget = 0;
                return true;
            }
            ty += ROW_H;
        }

        // 模块区
        int contentX = PANEL_X + SIDEBAR_W + PAD;
        int contentW = panelW - SIDEBAR_W - PAD * 2;
        int contentY = PANEL_Y + PAD, contentH = panelH - PAD * 2;
        List<Module> modules = ModuleManager.getModules(categories.get(selected));
        List<float[]> layout = layout(modules, contentW, contentH);
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            float ry = contentY + layout.get(i)[1] - scroll;
            if (ry + rowHeight(m) < contentY || ry > contentY + contentH) continue;

            if (hovering(mouseX, mouseY, contentX, ry, contentW, ROW_H)) {
                if (button == 0) {
                    m.toggle();
                } else if (button == 1) {
                    setExpanded(m, !isExpanded(m));
                } else if (button == 2) {
                    final Module target = m;
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new BindScreen(target)));
                }
                return true;
            }
            if (isExpanded(m)) {
                float vy = ry + ROW_H + 2;
                for (Value v : m.values) {
                    if (clickValue(v, contentX + 10, vy, contentW - 20, mouseX, mouseY, button)) return true;
                    vy += VALUE_H;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickValue(Value v, float x, float y, float w, double mx, double my, int button) {
        if (!hovering(mx, my, x, y, w, VALUE_H)) return false;
        if (v.isBooleanValue() && button == 0) {
            v.enable = !v.enable;
        } else if (v.isModesValue() && button == 0) {
            v.nextMode();
        } else if (v.isNumberValue() && button == 0) {
            draggingSlider = v;
            applySlider(v, x, w, mx);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingSlider != null) {
            IRenderer r = Platform.renderer();
            int contentW = (int) Math.min(460, r.screenWidth() - PANEL_X * 2) - SIDEBAR_W - PAD * 2;
            float x = PANEL_X + SIDEBAR_W + PAD + 10, w = contentW - 20;
            applySlider(draggingSlider, x, w, mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * 1.21.8 的新签名（水平 + 垂直两个滚动量）。1.20.1 的 Screen 没有这个方法，
     * 所以不能写 @Override —— 在 1.21.8 上它就是 override，在 1.20.1 上只是个普通方法。
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        scrollBy(dy);
        return true;
    }

    /** 1.20.1 的旧签名（只有 scrolls 一个量），转到统一处理。 */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrolls) {
        scrollBy(scrolls);
        return true;
    }

    private void scrollBy(double amount) {
        scrollTarget -= (float) amount * 24;
        scrollTarget = Math.max(0, scrollTarget);
    }

    private void applySlider(Value v, float x, float w, double mx) {
        float trackX = x + w * 0.45f, trackW = w * 0.55f - 4;
        float pct = (float) (mx - trackX) / trackW;
        pct = Math.max(0, Math.min(1, pct));
        v.numberValue = v.min + pct * (v.max - v.min);
    }

    /* ---------- 展开状态 / 布局 / 工具 ---------- */

    private final List<String> expanded = new ArrayList<>();

    private boolean isExpanded(Module m) { return expanded.contains(m.name); }

    private void setExpanded(Module m, boolean on) {
        if (on) { if (!expanded.contains(m.name)) expanded.add(m.name); }
        else expanded.remove(m.name);
    }

    private float rowHeight(Module m) {
        return isExpanded(m) && !m.values.isEmpty() ? ROW_H + 2 + m.values.size() * VALUE_H : ROW_H;
    }

    /** 每行的 y 偏移（相对内容区顶部，未减滚动）。 */
    private List<float[]> layout(List<Module> modules, int w, int h) {
        List<float[]> out = new ArrayList<>(modules.size());
        float y = 0;
        for (Module m : modules) {
            out.add(new float[]{0, y});
            y += rowHeight(m) + 3;
        }
        // 滚动上限在这里顺手夹住
        float max = Math.max(0, y - h);
        if (scrollTarget > max) scrollTarget = max;
        return out;
    }

    private static boolean hovering(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    /** 画一块九宫格：烘成目标尺寸的整图再走 IRenderer.image（每帧 1 次 blit/元素）。 */
    private void drawPatch(NinePatch patch, float x, float y, float w, float h, int tint) {
        int pw = Math.max(1, Math.round(w)), ph = Math.max(1, Math.round(h));
        Platform.renderer().image(MaterialSkin.image(patch, pw, ph), x, y, w, h, tint);
    }

    /** 裁剪：两版 Screen 都有 GuiGraphics.enableScissor（GUI 坐标）。 */
    private GuiGraphics scissorGui;
    private void guiBeginScissor(float x, float y, float w, float h) {
        GuiGraphics g = currentGui();
        if (g != null) g.enableScissor(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
    }

    private void guiEndScissor() {
        GuiGraphics g = currentGui();
        if (g != null) g.disableScissor();
    }

    private GuiGraphics currentGui() { return scissorGui; }

    /** 不画原版模糊背景，遮罩已在 render 里画。两版签名不同，用各自存在的那个。 */
    public void renderTransparentBackground(GuiGraphics gui) {
        // 什么也不画
    }
}
