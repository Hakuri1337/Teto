package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.compat.Compat;

import tech.hakuri.teto.event.impl.EventAfterAttack;
import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.feature.impl.move.JumpReset;
import tech.hakuri.teto.net.NetworkFreeze;
import tech.hakuri.teto.utils.MSTimer;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class PingPongKB extends Module {
    public static MSTimer cacheTimer = new MSTimer();
    public static MSTimer releaseTimer = new MSTimer();
    public static Value timeout = new Value("超时", 500, 100, 2000);
    public static Value reverseKB = new Value("反向击退", true);
    public Status status = Status.nothing;

    public PingPongKB() {
        name = "反击退";
        category = Category.combat;
        addValues(timeout, reverseKB);
    }

    /**
     * 冻结/解冻网络读线程。Criticals 与 StopEvading 也调这个方法，所以签名保持不变。
     * 实现已从 {@code Thread.suspend()/resume()} 换成 {@link NetworkFreeze} 的协作式驻留 ——
     * 那两个方法在 JDK 20+ 无条件抛 {@code UnsupportedOperationException}，1.21.8 用的是 Java 21。
     */
    public static void toggle(boolean toggle) {
        NetworkFreeze.set(toggle);
    }

    @Override
    public void onEnable() {
        status = Status.nothing;
        cacheTimer.reset();
        releaseTimer.reset();
        ModuleManager.getModule(JumpReset.class).disable();
    }

    @Override
    public void onDisable() {
        status = Status.nothing;
        cacheTimer.reset();
        releaseTimer.reset();
        toggle(false);
    }

    @Override
    public void onPacket(EventPacket it) {
        开启(it);
        攻击后关闭1(it);
    }

    @Override
    public void onTick(EventTick it) {
        超时关闭(it);
        死亡关闭(it);
    }

    @Override
    public void onAfterAttack(EventAfterAttack it) {
        攻击后关闭2(it);
    }

    public void 开启(EventPacket e) {
        if (e.packet instanceof ClientboundDamageEventPacket packet) {
            if (packet.entityId() == mc.player.getId()) {
                on();
                if (Compat.inFluid(mc.player)) return;
                if (mc.player.onGround()) {
                    if (reverseKB.enable) mc.player.jumpFromGround();
                }
            }
        }
        if (e.packet instanceof ClientboundSetEntityMotionPacket motion) {
            if (motion.getId() == mc.player.getId()) {
                on();
            }
        }
    }

    public void 死亡关闭(EventTick e) {
        if (mc.screen instanceof DeathScreen || mc.screen instanceof ReceivingLevelScreen) {
            toggle();
        }
    }

    public void 攻击后关闭1(EventPacket e) {
        if (e.packet instanceof ServerboundInteractPacket) {
            off();
        }
    }

    public void 攻击后关闭2(EventAfterAttack e) {
        off();
    }

    public void on() {
        if (status == Status.nothing) {
            status = Status.caching;
            cacheTimer.reset();
            toggle(true);
        }
        upd();
    }

    public void upd() {
        if (status == Status.releasing && releaseTimer.hasTimePassed(timeout.numberValue)) {
            status = Status.nothing;
        }
        if (status == Status.caching && cacheTimer.hasTimePassed(timeout.numberValue)) {
            status = Status.releasing;
            releaseTimer.reset();
            toggle(false);
        }
    }

    public void off() {
        upd();
    }

    public void 超时关闭(EventTick e) {
        if (status == Status.caching && cacheTimer.hasTimePassed(timeout.numberValue)) {
            off();
        }
    }

    public enum Status {
        nothing,
        caching,
        releasing
    }
}