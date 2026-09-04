/**
 * 冒烟测试探针的替换版本：只有方法体不同（OLD -> NEW），
 * 类的字段、方法签名、继承关系完全一致 —— 这正是 retransform 允许的修改范围，
 * 也和 hook/ 下 12 个 ASM transformer 的改动性质相同（只在既有方法体内插指令）。
 */
public class RetransformProbe {
    public static String value() {
        return "NEW";
    }
}
