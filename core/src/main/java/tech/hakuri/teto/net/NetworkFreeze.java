package tech.hakuri.teto.net;

import java.util.concurrent.locks.LockSupport;

/**
 * 网络读线程的冻结开关（反击退、Criticals、StopEvading 共用的底层原语）。
 * <p>
 * <b>为什么重写</b>：原实现是从外部对每个 netty/nio 线程调
 * {@code Thread.suspend()} / {@code Thread.resume()}。这两个方法在 <b>JDK 20+ 已改为
 * 无条件抛 {@code UnsupportedOperationException}</b>，而 Minecraft 1.21.8 要求 Java 21，
 * 所以旧实现在 1.21.8 上必然崩。
 * <p>
 * <b>新实现</b>：改成协作式驻留 —— 不再从外部挂起线程，而是让网络读线程自己在
 * 数据包钩子里 park。观测效果与原来等价（读线程停转、包堆在 socket 缓冲区、
 * 解冻后一次性冲出），但有两个实质改进：
 * <ul>
 *   <li>线程停在一个<b>已知的安全点</b>上。{@code suspend()} 可能在线程持锁时把它冻住，
 *       那是标准的死锁配方，也正是这个 API 被废弃的原因。</li>
 *   <li>带<b>绝对超时兜底</b>：无论上层状态机怎么错，网络最多被冻 {@link #MAX_FREEZE_MS} 毫秒，
 *       不会永久卡死连接。</li>
 * </ul>
 * 本类不引用任何 Minecraft API，因此放在 core 里，1.20.1 与 1.21.8 共用同一份字节码。
 */
public final class NetworkFreeze {

    /** 硬上限。反击退的可配超时最大 2000ms，这里留出余量后强制解冻。 */
    public static final long MAX_FREEZE_MS = 3000L;

    private static volatile boolean frozen;
    private static volatile long deadline;

    private NetworkFreeze() {
    }

    public static void set(boolean freeze) {
        if (freeze) {
            deadline = System.currentTimeMillis() + MAX_FREEZE_MS;
            frozen = true;
        } else {
            frozen = false;
        }
    }

    public static boolean isFrozen() {
        return frozen;
    }

    /**
     * 在数据包处理入口调用。只驻留网络读线程 —— 渲染线程也会走这条路径（出站包），
     * 冻住它等于卡死整个游戏。
     */
    public static void holdIfFrozen() {
        if (!frozen) {
            return;
        }
        String name = Thread.currentThread().getName().toLowerCase();
        if (!name.contains("netty") && !name.contains("nio")) {
            return;
        }
        while (frozen) {
            if (System.currentTimeMillis() > deadline) {
                frozen = false;
                break;
            }
            LockSupport.parkNanos(1_000_000L);
        }
    }
}
