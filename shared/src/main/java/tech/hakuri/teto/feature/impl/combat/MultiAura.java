package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.utils.AsyncEntityFilter;
import tech.hakuri.teto.utils.MSTimer;
import net.minecraft.world.entity.Entity;

public class MultiAura extends Module {
    public Value delay = new Value("点击冷却", 500, 200, 5000);
    public MSTimer timer = new MSTimer();

    public MultiAura() {
        name = "发包光环";
        category = Category.combat;
        addValues(delay);
    }

    @Override
    public void onTick(EventTick event) {
        if (timer.hasTimePassed(delay.numberValue)) {
            for (Entity entity : AsyncEntityFilter.combat) {
                mc.gameMode.attack(mc.player, entity);
            }
            timer.reset();
        }
    }
}
