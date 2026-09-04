package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;

/**
 * @author 手淫
 * 可能能绕天空之城
 */
public class AutoFishing extends Module {

    public AutoFishing() {
        name = "自动钓鱼";
        category = Category.misc;
    }

    public static boolean isOwnPlayer(FishingHook hook) {
        return Minecraft.getInstance().player.getId() == hook.getPlayerOwner().getId();
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.tickCount % 20 == 0) {
            if (mc.player.fishing == null) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    @Override
    public void onPacket(EventPacket e) {
        if (e.packet instanceof ClientboundMoveEntityPacket packet) {
            if (packet.getEntity(mc.level) instanceof FishingHook it) {
                if (isOwnPlayer(it)) {
                    if (it.isInWater()) {
                        if (packet.getYa() < -1000 && packet.getYa() > -5000) {
                            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
        }
    }
}

