package tech.hakuri.teto.platform;

/**
 * 绘制门面。所有版本相关的渲染 API 都被挡在这个接口后面。
 * <p>
 * 1.20.1 实现走 {@code Tesselator} + {@code getPositionColorShader}；
 * 1.21.8 实现走 {@code GuiRenderState} 两阶段提交 + {@code RenderPipeline}。
 * 调用方（47 个功能模块与 UI）只认这个接口，因此换版本不需要改它们。
 * <p>
 * 约定：
 * <ul>
 *   <li>颜色统一为 ARGB（1.21.6 起 Minecraft 的 {@code drawString} 也按 ARGB 解释，
 *       alpha 为 0 会完全不渲染，所以这里强制要求调用方给出 alpha）。</li>
 *   <li>2D 坐标是缩放后的 GUI 坐标，实现内部负责处理 guiScale，调用方不要再乘。</li>
 *   <li>3D 方法只能在世界渲染阶段调用，当前帧的变换矩阵由实现自己从渲染事件里取。</li>
 * </ul>
 */
public interface IRenderer {

    /**
     * 交出本帧的 2D 绘制上下文（1.20.1 与 1.21.8 都是 {@code GuiGraphics}，但那是两个
     * 二进制不兼容的类型，所以这里只能是 {@code Object}，由实现自己 cast）。
     * <p>
     * 由渲染事件与 ClickGUI 在绘制入口调用。这样调用方就不必 import 具体实现类，
     * 事件与 UI 代码才能在两个版本间共享。
     */
    void beginFrame2D(Object context);

    /** 交出本帧的世界变换栈（两版都是 {@code PoseStack}，同样按 {@code Object} 传递）。 */
    void beginFrame3D(Object context);

    /** 实心矩形，屏幕空间。 */
    void rect(float left, float top, float right, float bottom, int argb);

    /**
     * 把一张 CPU 栅格化的图按给定尺寸与染色贴出来。
     * 这是 Material UI 的唯一贴图原语：圆角、阴影、辉光都是预烘的
     * BufferedImage（九宫格源图），由实现负责上传成纹理并绘制。
     * <p>
     * {@code tint} 为 0 表示不染色（阴影/辉光这类本身带颜色的图用）。
     * 实现应对相同尺寸的同一 BufferedImage 缓存纹理，不要每帧重复上传。
     */
    void image(java.awt.image.BufferedImage image, float x, float y, float w, float h, int tint);

    /** 绘制文本，返回绘制结束时的 x。 */
    float text(FontSize size, String text, float x, float y, int argb);

    /** 带阴影的文本，返回绘制结束时的 x。 */
    float textShadow(FontSize size, String text, float x, float y, int argb);

    /** 在给定矩形内居中绘制带阴影的文本，返回绘制结束时的 x。 */
    float textCentered(FontSize size, String text, float minX, float minY, float maxX, float maxY, int argb);

    /** 文本宽度，单位与 2D 坐标一致。 */
    float textWidth(FontSize size, String text);

    /** 该字号的行高。 */
    float textHeight(FontSize size);

    /** 世界空间线框盒子，用于 ESP / 碰撞箱透视。 */
    void box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int argb);

    /** 屏幕宽度，GUI 坐标（已除以 guiScale），与 2D 方法同一坐标系。 */
    float screenWidth();

    /** 屏幕高度，GUI 坐标（已除以 guiScale），与 2D 方法同一坐标系。 */
    float screenHeight();

    /** GUI 缩放倍率。调用方一般不需要它 —— 坐标换算由实现负责，留这个口子是为了少数需要按物理像素对齐的场景。 */
    float guiScale();
}
