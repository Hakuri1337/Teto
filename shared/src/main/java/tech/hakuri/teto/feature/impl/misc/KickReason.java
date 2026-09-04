package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.compat.Compat;

import tech.hakuri.teto.event.impl.EventKick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;

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
            Compat.chat(event.reason);
            event.cancel = true;
        }
    }
}
