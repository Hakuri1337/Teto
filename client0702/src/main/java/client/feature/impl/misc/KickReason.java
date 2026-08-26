package client.feature.impl.misc;

import client.event.impl.EventKick;
import client.feature.Category;
import client.feature.Module;

public class KickReason extends Module {
    public KickReason() {
        name = "被封提示";
        category = Category.misc;
        toggle();
    }

    @Override
    public void onKick(EventKick event) {
        if (mc.level != null && mc.player != null) {
            mc.setScreen(null);
            mc.player.sendSystemMessage(event.reason);
            event.cancel = true;
        }
    }
}
