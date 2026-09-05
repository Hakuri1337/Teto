package tech.hakuri.teto.ui.clickgui;

import tech.hakuri.teto.anim.AnimationClock;
import tech.hakuri.teto.anim.SmoothAnimation;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.i18n.I18n;
import tech.hakuri.teto.platform.FontSize;
import tech.hakuri.teto.platform.IRenderer;
import tech.hakuri.teto.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Naven 风格 ClickGUI —— 逐帧复刻 GuardLite（src/main/java/cnm/obsoverlay/ui/ClickGUI.java）。
 * <p>
 * 布局/交互/动画全部照搬：
 * 分类页（100x140）→ 点击展开为 windowWidth x windowHeight（默认 400x250），
 * 标题栏可拖拽，右下角 10x10 区域可缩放（最小 500x300），
 * 左侧模块列（x+5, w=120）可滚动，右侧值页（x+140, w=windowWidth-155）可滚动，
 * 面包屑 "< 分类 / 模块" 点击返回，模块左键 toggle、右键进值页、中键绑键。
 * <p>
 * 与 GuardLite 的差异只有：强调色仍用它的 #3662EC；名字/值名走 {@link I18n}；
 * 分类图标（FontIcons 私有字形）用 I18n 文本代替。
 */
public class NavenClickGUI extends Screen {

    // ===== GuardLite 同款窗口状态 =====
    public static float windowX = 100.0F;
    public static float windowY = 100.0F;
    public static float windowWidth = 400.0F;
    public static float windowHeight = 250.0F;

    private String selectedCategory = null;
    private Module selectedModule = null;
    private int[] dragMousePosition = new int[]{-1, -1};
    private boolean hoveringBack = false;

    private final SmoothAnimation widthAnimation = new SmoothAnimation(100.0F);
    private final SmoothAnimation heightAnimation = new SmoothAnimation(140.0F);
    private final SmoothAnimation titleAnimation = new SmoothAnimation(100.0F);
    private final SmoothAnimation titleHoverAnimation = new SmoothAnimation(0.0F);
    private final SmoothAnimation categoryMotionY = new SmoothAnimation(0.0F);
    private final SmoothAnimation moduleValuesMotionY = new SmoothAnimation(0.0F);
    private final Map<String, SmoothAnimation> categoryXAnimation = new HashMap<>();
    private final Map<String, SmoothAnimation> categoryYAnimation = new HashMap<>();
    private final Map<Module, SmoothAnimation> modulesAnimation = new HashMap<>();
    private final Map<Module, SmoothAnimation> modulesToggleAnimation = new HashMap<>();
    private final Map<Value, SmoothAnimation> valuesAnimation = new HashMap<>();
    private String titleDisplayName = "";
    private float finalModuleHeight;
    private float finalValueHeight;
    private boolean clickOpenCategoryModules = false;
    private boolean clickResizeWindow = false;
    private boolean clickDragWindow = false;
    private String hoveringCategory = null;
    private Module hoveringModule = null;
    private Module bindingModule = null;
    private final SmoothAnimation moduleSwapAnimation = new SmoothAnimation(0.0F);
    private final SmoothAnimation bindingAnimation = new SmoothAnimation(0.0F);
    private List<Module> categoryModules;
    private List<Value> renderValues;
    private Value hoveringBooleanValue;
    private Value hoveringFloatValue;
    private Value draggingFloatValue;
    private Value hoveringModeValue;
    private int targetModeValueIndex;
    private String bindingModuleName;
    private boolean mouseDown = false;
    private float minCatY = 0.0F;
    private final SmoothAnimation moduleAlphaAnimation = new SmoothAnimation(0.0F);
    private long moduleAlphaTimer = 0;
    private float minValueY = 0.0F;
    private final SmoothAnimation valuesAlphaAnimation = new SmoothAnimation(0.0F);
    private long valuesAlphaTimer = 0;

    /** Naven 蓝（GuardLite 的 rgb(54,98,236)）。 */
    private static final int ACCENT = 0xFF3662EC;

    private static final List<String> CATEGORIES = Category.getAll();

    public NavenClickGUI() {
        super(Component.nullToEmpty("Teto"));
        for (String c : CATEGORIES) {
            categoryXAnimation.put(c, new SmoothAnimation(0.0F));
            categoryYAnimation.put(c, new SmoothAnimation(0.0F));
        }
        for (Module m : ModuleManager.modules) {
            modulesAnimation.put(m, new SmoothAnimation(0.0F, 255.0F));
            modulesToggleAnimation.put(m, new SmoothAnimation(0.0F));
            for (Value v : m.values) {
                valuesAnimation.put(v, new SmoothAnimation(0.0F, v.isModesValue() ? 255.0F : 0.0F));
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /* ==================== 渲染 ==================== */

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        AnimationClock.tick();
        IRenderer r = Platform.renderer();
        r.beginFrame2D(g);

        this.hoveringModule = null;
        this.hoveringCategory = null;
        this.clickOpenCategoryModules = false;

        if (this.selectedCategory == null) {
            this.widthAnimation.target = 100.0F;
            this.heightAnimation.target = 140.0F;
        } else {
            this.widthAnimation.target = windowWidth;
            this.heightAnimation.target = windowHeight;
        }
        this.widthAnimation.update(true);
        this.heightAnimation.update(true);
        // 面板背景模糊（1.20.1 走 Kawase 高斯，1.21.8 默认 no-op）。必须在面板内容之前画，
        // 此时主渲染目标里还是面板「后面」的场景；之后再叠一层半透明黑把模糊区压暗。
        r.blurRect(windowX, windowY, this.widthAnimation.value, this.heightAnimation.value, 5.0F);
        r.roundedRect(windowX, windowY, this.widthAnimation.value, this.heightAnimation.value, 5.0F, argb(0, 0, 0, 40));

        drawCategories(r, mouseX, mouseY);
        drawTitle(r, mouseX, mouseY);
        drawModules(r, mouseX, mouseY);
        drawValues(r, mouseX, mouseY);
        drawDragging();
        drawBinding(r);
        drawResizeHandle(r, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawCategories(IRenderer r, int mouseX, int mouseY) {
        float th = r.textHeight(FontSize.S);
        for (String category : CATEGORIES) {
            SmoothAnimation xAnimation = this.categoryXAnimation.get(category);
            SmoothAnimation yAnimation = this.categoryYAnimation.get(category);
            yAnimation.target = this.selectedCategory == null ? 255.0F : 0.0F;
            xAnimation.update(true);
            yAnimation.update(true);

            float height = CATEGORIES.indexOf(category) * 25 * (yAnimation.value / 255.0F);
            if (yAnimation.target >= 4.0F) {
                int alpha = (int) yAnimation.value;
                String name = I18n.category(category);
                r.text(FontSize.S, name,
                        windowX + 25.0F + xAnimation.value,
                        windowY + 40.0F + height,
                        withAlpha(0xFFFFFF, alpha));
            }

            boolean hovering = isHovering(mouseX, mouseY, windowX, windowY + 40.0F + height, windowX + 100.0F, windowY + 40.0F + height + 20.0F);
            xAnimation.target = hovering ? 5.0F : 0.0F;

            if (yAnimation.value >= 250.0F && hovering) {
                this.hoveringCategory = category;
                this.clickOpenCategoryModules = true;
            }
        }
    }

    private void drawTitle(IRenderer r, int mouseX, int mouseY) {
        this.titleAnimation.update(true);
        this.titleHoverAnimation.update(true);
        if (this.titleAnimation.value > 5.0F) {
            int alpha = (int) this.titleAnimation.value;
            r.text(FontSize.S, this.titleDisplayName,
                    windowX + 6.0F + this.titleHoverAnimation.value,
                    windowY + 5.0F, withAlpha(0xFFFFFF, alpha));
        }

        int titleAlpha = (int) (255.0F - this.titleAnimation.value);
        if (titleAlpha > 0) {
            String name = "Teto";
            float tw = r.textWidth(FontSize.M, name);
            r.text(FontSize.M, name,
                    windowX + 50.0F - tw / 2.0F,
                    windowY + 6.0F, withAlpha(0xFFFFFF, titleAlpha));
        }

        if (this.selectedCategory != null) {
            this.titleAnimation.target = 255.0F;
            this.titleDisplayName = "< " + I18n.category(this.selectedCategory)
                    + (this.selectedModule != null ? " / " + this.selectedModule.displayName() : "");
            this.hoveringBack = isHovering(mouseX, mouseY,
                    windowX + 8.0F, windowY + 5.0F,
                    windowX + 6.0F + r.textWidth(FontSize.S, this.titleDisplayName),
                    windowY + 5.0F + r.textHeight(FontSize.S));
            this.titleHoverAnimation.target = (this.hoveringBack && !this.clickResizeWindow && !this.clickDragWindow) ? -2.0F : 0.0F;

            if (this.categoryMotionY.target < -this.finalModuleHeight) this.categoryMotionY.target = -this.finalModuleHeight;
            if (this.categoryMotionY.target > 0.0F) this.categoryMotionY.target = 0.0F;
            this.categoryMotionY.update(true);
        } else {
            this.titleAnimation.target = 4.0F;
        }
    }

    private void drawModules(IRenderer r, int mouseX, int mouseY) {
        List<Module> inList = this.selectedCategory == null ? null : ModuleManager.getModules(this.selectedCategory);
        if (inList != null) {
            this.categoryModules = inList;
            this.moduleSwapAnimation.target = 255.0F;
        } else {
            this.moduleSwapAnimation.target = 5.0F;
        }
        this.moduleSwapAnimation.update(true);
        if (inList == null && this.moduleSwapAnimation.value < 8.0F) {
            this.categoryModules = null;
        }
        if (this.categoryModules == null) return;

        float renderModuleHeight = 0.0F;
        this.minCatY = windowHeight - 25.0F;
        float th = r.textHeight(FontSize.S);

        scissorBegin(windowX + 5.0F, windowY + 20.0F, 130.0F, windowHeight - 25.0F);
        for (Module m : this.categoryModules) {
            float mx = windowX + 5.0F;
            float my = windowY + 20.0F + renderModuleHeight + this.categoryMotionY.value;
            boolean isHovering = isHoveringBound(mouseX, mouseY, windowX + 5.0F, windowY + 20.0F, 120.0F, windowHeight - 25.0F)
                    && isHoveringBound(mouseX, mouseY, mx, my, 120.0F, 25.0F)
                    && this.moduleSwapAnimation.value > 250.0F
                    && this.bindingModule == null;

            SmoothAnimation toggleAnim = this.modulesToggleAnimation.get(m);
            toggleAnim.target = m.enable ? this.moduleSwapAnimation.value : 6.0F;
            toggleAnim.update(true);

            int alpha = (int) this.moduleSwapAnimation.value;
            r.roundedRect(mx, my, 120.0F, 25.0F, 5.0F, argb(25, 25, 25, alpha));
            r.roundedRect(mx, my, 120.0F, 25.0F, 5.0F, withAlpha(ACCENT, (int) toggleAnim.value));

            SmoothAnimation hoverAnim = this.modulesAnimation.get(m);
            if (isHovering) {
                hoverAnim.target = 150.0F;
                this.hoveringModule = m;
            } else {
                hoverAnim.target = 5.0F;
            }
            hoverAnim.update(true);
            r.roundedRect(mx, my, 120.0F, 25.0F, 5.0F, argb(255, 255, 255, (int) hoverAnim.value / 3));

            r.text(FontSize.S, m.displayName(), windowX + 13.0F, my + (25.0F - th) / 2.0F, withAlpha(0xFFFFFF, alpha));
            renderModuleHeight += 30.0F;
        }
        scissorEnd();

        this.finalModuleHeight = renderModuleHeight + 20.0F - windowHeight;
        this.minCatY -= renderModuleHeight - 5.0F;
        float totalHeight = this.finalModuleHeight + windowHeight;
        if (totalHeight > windowHeight - 25.0F) {
            this.moduleAlphaAnimation.update(true);
            this.moduleAlphaAnimation.target = delay(this.moduleAlphaTimer, 1000.0) ? 0.0F : 255.0F;

            float viewable = windowHeight - 25.0F;
            float progress = clamp(-this.categoryMotionY.value / -this.minCatY, 0.0F, 1.0F);
            float barHeight = Math.max(viewable / totalHeight * viewable, 20.0F);
            float position = progress * (viewable - barHeight);
            r.roundedRect(windowX + 127.0F, windowY + 20.0F + position, 3.0F, barHeight, 1.5F,
                    reAlpha(0x0037D5EC, this.moduleAlphaAnimation.value / 255.0F));
        }
    }

    private void drawValues(IRenderer r, int mouseX, int mouseY) {
        if (this.renderValues == null) return;

        boolean isValueInBound = isHoveringBound(mouseX, mouseY, windowX + 140.0F, windowY + 20.0F, windowWidth - 155.0F, windowHeight - 25.0F);
        float motion = this.moduleValuesMotionY.value;
        if (this.moduleValuesMotionY.target < -this.finalValueHeight) this.moduleValuesMotionY.target = -this.finalValueHeight;
        if (this.moduleValuesMotionY.target > 0.0F) this.moduleValuesMotionY.target = 0.0F;
        this.moduleValuesMotionY.update(true);

        float x = 0.0F;
        float valueHeight = 0.0F;
        this.minValueY = windowHeight - 25.0F;
        this.hoveringBooleanValue = null;
        float th = r.textHeight(FontSize.S);

        scissorBegin(windowX + 135.0F, windowY + 20.0F, windowWidth - 140.0F, windowHeight - 25.0F);

        // ---- bool 值：GuardLite 的「方块 + 内填充」 ----
        for (Value value : this.renderValues) {
            if (!value.isBooleanValue()) continue;
            SmoothAnimation animation = this.valuesAnimation.get(value);
            animation.target = value.enable ? 255.0F : 0.0F;
            animation.update(true);

            String name = I18n.value(value.name);
            float currentLength = r.textWidth(FontSize.S, name) + 23.0F;
            if (x + currentLength + 20.0F > windowWidth - 155.0F) {
                x = 0.0F;
                valueHeight += 20.0F;
            }

            if (isValueInBound && isHoveringBound(mouseX, mouseY, windowX + 130.0F + x, windowY + valueHeight + motion + 20.0F, currentLength, 13.0F)) {
                this.hoveringBooleanValue = value;
            }

            r.roundedRect(windowX + 140.0F + x, windowY + valueHeight + motion + 20.0F, 12.0F, 12.0F, 2.0F, argb(0, 0, 0, 150));
            r.roundedRect(windowX + 142.0F + x, windowY + valueHeight + motion + 22.0F, 8.0F, 8.0F, 2.0F, withAlpha(ACCENT, (int) animation.value));
            r.text(FontSize.S, name, windowX + 155.0F + x, windowY + valueHeight + motion + 19.0F + (12.0F - th) / 2.0F, 0xFFFFFFFF);
            x += currentLength;
        }

        valueHeight += 10.0F;
        this.hoveringFloatValue = null;

        // ---- number 值：标签 + 右值 + 滑轨 + 圆钮 ----
        for (Value value : this.renderValues) {
            if (!value.isNumberValue()) continue;
            SmoothAnimation animation = this.valuesAnimation.get(value);

            if (isValueInBound && isHoveringBound(mouseX, mouseY, windowX + 140.0F, windowY + valueHeight + motion + 39.5F, windowWidth - 155.0F, 10.0F)) {
                this.hoveringFloatValue = value;
            }

            String name = I18n.value(value.name);
            r.text(FontSize.S, name, windowX + 140.0F, windowY + valueHeight + motion + 25.0F, 0xFFFFFFFF);
            String currentValue = (float) Math.round(value.numberValue * 100.0F) / 100.0F + " / " + value.max;
            r.text(FontSize.S, currentValue,
                    windowX + windowWidth - r.textWidth(FontSize.S, currentValue) - 15.0F,
                    windowY + valueHeight + motion + 25.0F, 0xFFFFFFFF);

            float stage = (value.numberValue - value.min) / (value.max - value.min);
            r.roundedRect(windowX + 140.0F, windowY + valueHeight + motion + 42.0F, windowWidth - 155.0F, 5.0F, 3.0F, argb(0, 0, 0, 150));
            animation.target = (windowWidth - 155.0F) * stage;
            animation.update(true);
            r.roundedRect(windowX + 140.0F, windowY + valueHeight + motion + 42.0F, animation.value, 5.0F, 3.0F, ACCENT);
            r.roundedRect(windowX + 135.0F + animation.value, windowY + valueHeight + motion + 39.5F, 10.0F, 10.0F, 5.0F, 0xFFFFFFFF);
            valueHeight += 25.0F;
        }

        this.hoveringModeValue = null;

        // ---- modes 值：芯片流 ----
        for (Value value : this.renderValues) {
            if (!value.isModesValue()) continue;
            SmoothAnimation animation = this.valuesAnimation.get(value);
            animation.update(true);

            String name = I18n.value(value.name);
            r.text(FontSize.S, name, windowX + 140.0F, windowY + valueHeight + motion + 25.0F, 0xFFFFFFFF);
            x = 0.0F;
            valueHeight += 15.0F;

            for (int modeIndex = 0; modeIndex < value.modes.size(); modeIndex++) {
                String mode = value.modes.get(modeIndex);
                float currentLength = r.textWidth(FontSize.S, mode) + 20.0F;
                if (x + currentLength + 20.0F > windowWidth - 155.0F) {
                    x = 0.0F;
                    valueHeight += 20.0F;
                }

                if (isValueInBound && isHoveringBound(mouseX, mouseY, windowX + 140.0F + x, windowY + valueHeight + motion + 25.0F, currentLength, 13.0F)) {
                    this.hoveringModeValue = value;
                    this.targetModeValueIndex = modeIndex;
                }

                int chipAlpha = value.currentMode.equals(mode) ? (int) animation.value : 10;
                r.roundedRect(windowX + 140.0F + x, windowY + valueHeight + motion + 27.0F, 10.0F, 10.0F, 5.0F, argb(0, 0, 0, 150));
                r.roundedRect(windowX + 141.0F + x, windowY + valueHeight + motion + 28.0F, 8.0F, 8.0F, 5.0F, withAlpha(ACCENT, chipAlpha));
                r.text(FontSize.S, mode, windowX + 152.0F + x, windowY + valueHeight + motion + 25.0F, 0xFFFFFFFF);
                x += currentLength;
            }
            valueHeight += 20.0F;
        }
        scissorEnd();

        this.finalValueHeight = valueHeight - windowHeight + 25.0F;
        this.minValueY -= valueHeight;
        float valTotalHeight = this.finalValueHeight + windowHeight;
        if (valTotalHeight > windowHeight - 25.0F) {
            this.valuesAlphaAnimation.update(true);
            this.valuesAlphaAnimation.target = delay(this.valuesAlphaTimer, 1000.0) ? 0.0F : 255.0F;

            float viewable = windowHeight - 25.0F;
            float progress = clamp(-this.moduleValuesMotionY.value / -this.minValueY, 0.0F, 1.0F);
            float barHeight = Math.max(viewable / valTotalHeight * viewable, 20.0F);
            float position = progress * (viewable - barHeight);
            r.roundedRect(windowX + windowWidth - 8.0F, windowY + 20.0F + position, 3.0F, barHeight, 1.5F,
                    reAlpha(0x0037D5EC, this.valuesAlphaAnimation.value / 255.0F));
        }
    }

    private void drawDragging() {
        // 拖拽中实时更新数值（GuardLite 在 render 里做，而不是 mouseDragged）
        if (this.draggingFloatValue != null) {
            IRenderer r = Platform.renderer();
            // 需要鼠标 x：从 Minecraft 鼠标位置取（GUI 坐标）
            double mx = Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScale();
            float stage = ((float) mx - windowX - 140.0F) / (windowWidth - 160.0F);
            Value v = this.draggingFloatValue;
            float val = v.min + (v.max - v.min) * stage;
            v.numberValue = clamp(val, v.min, v.max);
        }
    }

    private void drawBinding(IRenderer r) {
        if (this.bindingModule != null) {
            this.bindingAnimation.target = 250.0F;
            this.bindingModuleName = this.bindingModule.displayName();
        } else {
            this.bindingAnimation.target = 5.0F;
        }
        this.bindingAnimation.update(true);
        if (this.bindingAnimation.value > 6.0F) {
            r.rect(windowX, windowY, windowX + this.widthAnimation.value, windowY + this.heightAnimation.value,
                    argb(0, 0, 0, (int) this.bindingAnimation.value / 2));
            int alpha = (int) this.bindingAnimation.value;
            String line1 = "Press a key to bind " + this.bindingModuleName;
            String line2 = "(Press ESC to remove/cancel key bind)";
            r.text(FontSize.S, line1,
                    windowX + this.widthAnimation.value / 2.0F - r.textWidth(FontSize.S, line1) / 2.0F,
                    windowY + (this.heightAnimation.value - r.textHeight(FontSize.S)) / 2.0F - 8.0F,
                    withAlpha(0xFFFFFF, alpha));
            r.text(FontSize.XS, line2,
                    windowX + this.widthAnimation.value / 2.0F - r.textWidth(FontSize.XS, line2) / 2.0F,
                    windowY + (this.heightAnimation.value - r.textHeight(FontSize.XS)) / 2.0F + 8.0F,
                    withAlpha(0xFFFFFF, alpha));
        }
    }

    private void drawResizeHandle(IRenderer r, int mouseX, int mouseY) {
        if (this.selectedCategory != null) {
            // 右下角把手：小三角
            float hx = windowX + this.widthAnimation.value - 10.0F;
            float hy = windowY + this.heightAnimation.value - 10.0F;
            r.rect(hx + 6, hy + 6, hx + 10, hy + 10, argb(255, 255, 255, 80));
        }
    }

    /* ==================== 输入 ==================== */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) this.mouseDown = true;

        if (this.bindingModule != null && (mouseButton == 3 || mouseButton == 4)) {
            this.bindingModule.keyCode = -mouseButton;
            this.bindingModule = null;
            return true;
        }
        if (this.bindingModule != null) return true;

        if (mouseButton == 2) {
            if (this.hoveringModule != null) this.bindingModule = this.hoveringModule;
            return true;
        }

        if (this.hoveringModule != null) {
            if (mouseButton == 0) {
                this.hoveringModule.toggle();
            } else if (mouseButton == 1) {
                this.selectedModule = this.hoveringModule;
                this.renderValues = this.hoveringModule.values;
                this.moduleValuesMotionY.target = this.moduleValuesMotionY.value = 0.0F;
            }
        }

        if (mouseButton == 0) {
            if (this.hoveringBack && !this.clickResizeWindow && !this.clickDragWindow) {
                if (this.selectedModule != null) {
                    this.selectedModule = null;
                    this.renderValues = null;
                } else {
                    this.selectedCategory = null;
                    this.selectedModule = null;
                    this.renderValues = null;
                }
            }

            if (this.clickOpenCategoryModules && this.hoveringCategory != null) {
                this.selectedCategory = this.hoveringCategory;
                this.categoryMotionY.value = this.categoryMotionY.target = 0.0F;
                this.moduleSwapAnimation.value = 5.0F;
                this.moduleSwapAnimation.target = 255.0F;
                this.clickOpenCategoryModules = false;
            }

            boolean doDragWindow = this.selectedCategory != null
                    ? isHovering((int) mouseX, (int) mouseY, windowX, windowY, windowX + windowWidth, windowY + 25.0F)
                    : isHovering((int) mouseX, (int) mouseY, windowX, windowY, windowX + 100.0F, windowY + 40.0F);
            if ((this.selectedCategory == null || !this.hoveringBack) && doDragWindow) {
                this.setDragPosition(mouseX, mouseY);
                this.clickDragWindow = true;
            }

            if (isHovering((int) mouseX, (int) mouseY,
                    windowX + windowWidth - 10.0F, windowY + windowHeight - 10.0F,
                    windowX + windowWidth, windowY + windowHeight)) {
                this.setDragPosition(mouseX, mouseY);
                this.clickResizeWindow = true;
            }

            if (this.hoveringBooleanValue != null) {
                this.hoveringBooleanValue.enable = !this.hoveringBooleanValue.enable;
            }
            if (this.hoveringFloatValue != null) {
                this.draggingFloatValue = this.hoveringFloatValue;
            }
            if (this.hoveringModeValue != null) {
                this.hoveringModeValue.currentMode = this.hoveringModeValue.modes.get(this.targetModeValueIndex);
                SmoothAnimation animation = this.valuesAnimation.get(this.hoveringModeValue);
                animation.value = 0.0F;
                animation.target = 255.0F;
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) this.mouseDown = false;
        if (this.clickResizeWindow) { this.clickResizeWindow = false; this.setDragPosition(-1, -1); }
        if (this.clickDragWindow) { this.clickDragWindow = false; this.setDragPosition(-1, -1); }
        this.draggingFloatValue = null;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (this.clickDragWindow && this.mouseDown) {
            windowX += (float) (mouseX - this.dragMousePosition[0]);
            windowY += (float) (mouseY - this.dragMousePosition[1]);
            this.setDragPosition(mouseX, mouseY);
        }
        if (this.categoryModules != null && !this.hoveringBack && this.clickResizeWindow && this.mouseDown) {
            windowWidth += (float) (mouseX - this.dragMousePosition[0]);
            windowHeight += (float) (mouseY - this.dragMousePosition[1]);
            if (windowWidth < 500.0F) windowWidth = 500.0F;
            if (windowHeight < 300.0F) windowHeight = 300.0F;
            this.setDragPosition(mouseX, mouseY);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.bindingModule != null) {
            if (keyCode == 256) { // ESC
                this.bindingModule.keyCode = 0;
            } else {
                this.bindingModule.keyCode = keyCode;
            }
            this.bindingModule = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** 1.21.8 签名。1.20.1 的 Screen 没这个方法，不加 @Override。 */
    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        scrollBy(dy);
        return true;
    }

    /** 1.20.1 签名。 */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrolls) {
        scrollBy(scrolls);
        return true;
    }

    private void scrollBy(double delta) {
        if (this.bindingModule == null && isHoveringBound(mouseXCached(), mouseYCached(), windowX + 5.0F, windowY + 20.0F, 100.0F, windowHeight - 5.0F)) {
            this.categoryMotionY.target = (float) (this.categoryMotionY.target + delta * 15.0);
            this.moduleAlphaTimer = System.currentTimeMillis();
        }
        if (this.renderValues != null && this.bindingModule == null
                && isHoveringBound(mouseXCached(), mouseYCached(), windowX + 140.0F, windowY + 20.0F, windowWidth - 155.0F, windowHeight - 25.0F)) {
            this.moduleValuesMotionY.target = (float) (this.moduleValuesMotionY.target + delta * 15.0);
            this.valuesAlphaTimer = System.currentTimeMillis();
        }
    }

    private double mouseXCached() {
        return Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScale();
    }

    private double mouseYCached() {
        return Minecraft.getInstance().mouseHandler.ypos() / Minecraft.getInstance().getWindow().getGuiScale();
    }

    /* ==================== 工具 ==================== */

    /** 裁剪统一走 IRenderer：1.20.1 调 GuiGraphics.enableScissor，1.21.8 走渲染状态。 */
    private void scissorBegin(float x, float y, float w, float h) {
        Platform.renderer().beginScissor(x, y, w, h);
    }

    private void scissorEnd() {
        Platform.renderer().endScissor();
    }

    private void setDragPosition(double x, double y) {
        this.dragMousePosition[0] = (int) x;
        this.dragMousePosition[1] = (int) y;
    }

    private static boolean isHovering(double mx, double my, float x1, float y1, float x2, float y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private static boolean isHoveringBound(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static boolean delay(long timer, double ms) {
        return System.currentTimeMillis() - timer >= ms;
    }

    private static int argb(int r, int g, int b, int a) {
        return (clampI(a) << 24) | (clampI(r) << 16) | (clampI(g) << 8) | clampI(b);
    }

    private static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | (clampI(alpha) << 24);
    }

    private static int reAlpha(int color, float alpha) {
        return (color & 0x00FFFFFF) | (clampI((int) (alpha * 255.0F)) << 24);
    }

    private static int clampI(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** 不画原版模糊背景。 */
    public void renderTransparentBackground(GuiGraphics gui) {
    }
}
