package tech.hakuri.teto.platform;

/**
 * 字号档位。取代原先 {@code FontManager} 上的 xs/s/m/l/xl 五个静态字段 ——
 * 那些字段直接暴露了 {@code TrueTypeFont}（1.21.8 上不存在的实现类型）。
 */
public enum FontSize {
    XS,
    S,
    M,
    L,
    XL
}
