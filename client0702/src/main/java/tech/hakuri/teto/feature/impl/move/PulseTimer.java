package tech.hakuri.teto.feature.impl.move;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.feature.impl.misc.KillEffect;
import tech.hakuri.teto.utils.MSTimer;
import net.minecraft.client.Minecraft;

public class PulseTimer extends Module {
    public Value slow = new Value("慢几倍", 2, 1, 5);
    public Value slowTime = new Value("慢多久", 1000, 100, 5000);
    public Value boost = new Value("快几倍", 2, 1, 5);
    public Value boostTime = new Value("快多久", 1000, 100, 5000);


    public TimerState state = TimerState.slowing;
    public MSTimer slowTimer = new MSTimer();

    public float repay;

    public PulseTimer() {
        name = "时间管理";
        category = Category.move;
        addValues(slow, slowTime, boost, boostTime);
    }

    public static boolean isMoving() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0;
    }

    @Override
    public void onEnable() {
        state = TimerState.slowing;
        KillEffect.set(50 * slow.numberValue);
        slowTimer.reset();
    }

    @Override
    public void onDisable() {
        KillEffect.set(50);
    }

    @Override
    public void onTick(EventTick event) {
        if (KillEffect.get() == null) return;


        if (state == TimerState.slowing) {
            repay += KillEffect.get();
        }
        if (state == TimerState.boosting) {
            repay -= 1000 / KillEffect.get();
        }


        if (state == TimerState.boosting && repay <= 0) {
            toggle();
        }


        if (state == TimerState.slowing && slowTimer.hasTimePassed(slowTime.numberValue)) {
            state = TimerState.boosting;
            KillEffect.set(50 / boost.numberValue);
        }


    }

    public enum TimerState {
        slowing, boosting
    }
}
