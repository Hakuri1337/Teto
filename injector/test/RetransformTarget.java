/**
 * 冒烟测试目标：模拟运行中的游戏进程，持续打印 RetransformProbe.value()。
 * 注入成功后，输出应从 OLD 变成 NEW，且进程不重启。
 */
public final class RetransformTarget {
    public static void main(String[] args) throws Exception {
        System.out.println("[target] 测试 JVM 已启动，pid=" + ProcessHandle.current().pid());
        for (int i = 0; i < 60; i++) {
            System.out.println("[target] RetransformProbe.value() = " + RetransformProbe.value());
            Thread.sleep(500L);
        }
    }
}
