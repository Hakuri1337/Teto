/**
 * 版本探测冒烟测试目标：把主线程改名为 Render thread，让 loader 能走到版本探测那一步。
 * classpath 里放不放 net.minecraft.client.DeltaTracker 的桩类，就能分别验证两个分支。
 */
public final class RenderThreadTarget {
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("Render thread");
        System.out.println("[target] pid=" + ProcessHandle.current().pid()
                + " DeltaTracker 在 classpath 上 = " + hasDeltaTracker());
        Thread.sleep(60000L);
    }

    private static boolean hasDeltaTracker() {
        try {
            Class.forName("net.minecraft.client.DeltaTracker");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
