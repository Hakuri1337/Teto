package tech.hakuri.teto.feature.impl.move;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;

public class Sprint extends Module {

    public Sprint() {
        name = "疾跑";
        category = Category.move;
        toggle();
    }

    @Override
    public void onTick(EventTick event) {
        mc.options.keySprint.setDown(true);
    }
}
