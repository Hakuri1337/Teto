package tech.hakuri.teto.feature.impl.block;

import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import net.minecraft.network.protocol.game.*;

public class GhostBlock extends Module {
    public GhostBlock() {
        name = "假方块";
        category = Category.block;
    }

    @Override
    public void onPacket(EventPacket event) {
        if (event.packet instanceof ClientboundBlockEventPacket it) event.cancel = true;
        if (event.packet instanceof ClientboundBlockUpdatePacket it) event.cancel = true;
        if (event.packet instanceof ClientboundBlockChangedAckPacket it) event.cancel = true;
        if (event.packet instanceof ClientboundBlockEntityDataPacket it) event.cancel = true;
        if (event.packet instanceof ClientboundBlockDestructionPacket it) event.cancel = true;
        if (event.packet instanceof ClientboundSectionBlocksUpdatePacket it) event.cancel = true;
        if (event.packet instanceof ClientboundPlayerPositionPacket) toggle();
    }

}
