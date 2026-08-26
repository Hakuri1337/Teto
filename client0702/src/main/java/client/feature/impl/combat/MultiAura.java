package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.utils.AsyncEntityFilter;
import client.utils.MSTimer;
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
