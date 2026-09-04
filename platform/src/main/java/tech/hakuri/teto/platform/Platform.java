package tech.hakuri.teto.platform;

/**
 * 平台服务定位器。版本专属模块在初始化时装配实现，功能模块只从这里取。
 * <p>
 * 用静态字段而不是依赖注入，是为了迁就现有代码的调用形态
 * （47 个模块里到处是 {@code RenderUtils.xxx} / {@code FontManager.s.xxx} 这类静态调用），
 * 这样迁移时改动最小。
 */
public final class Platform {

    private static volatile IRenderer renderer;

    private Platform() {
    }

    /** 由版本专属模块在客户端初始化时调用。 */
    public static void install(IRenderer impl) {
        if (impl == null) {
            throw new IllegalArgumentException("IRenderer 不能为 null");
        }
        renderer = impl;
    }

    public static IRenderer renderer() {
        IRenderer current = renderer;
        if (current == null) {
            throw new IllegalStateException(
                    "IRenderer 尚未装配。版本专属模块必须在使用绘制前调用 Platform.install(...)。");
        }
        return current;
    }

    /** 是否已装配，供可选路径判断（例如尚未初始化完成时跳过绘制）。 */
    public static boolean ready() {
        return renderer != null;
    }
}
