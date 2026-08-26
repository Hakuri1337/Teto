/**
 * 注入器冒烟测试目标：模拟一个运行中的普通 JVM，用于验证热注入链路。
 * 运行方式：javac Target.java && java Target
 */
public final class Target {
    public static void main(String[] args) throws Exception {
        System.out.println("[target] 测试 JVM 已启动，pid=" + ProcessHandle.current().pid());
        Thread.sleep(120000);
    }
}
