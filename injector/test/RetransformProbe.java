/**
 * 冒烟测试探针：value() 的返回值会被 RetransformAgent 在运行时替换。
 * 这个版本返回 OLD，probe-new 目录下的同名类返回 NEW。
 */
public class RetransformProbe {
    public static String value() {
        return "OLD";
    }
}
