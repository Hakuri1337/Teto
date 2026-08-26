package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Regen extends Module {
    public Value count = new Value("速度", 1, 1, 100);

    public Regen() {
        name = "漏洞回血";
        category = Category.combat;
        addValues(count);
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.screen instanceof ReceivingLevelScreen) toggle();

        for (int i = 0; i < count.numberValue; i++) {
            mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true));
        }
    }

    @Override
    public void onPacket(EventPacket event) {
        if (event.packet instanceof ClientboundDisconnectPacket) toggle();
    }
}
