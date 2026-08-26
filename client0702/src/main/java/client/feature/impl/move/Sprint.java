package client.feature.impl.move;


import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;

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
