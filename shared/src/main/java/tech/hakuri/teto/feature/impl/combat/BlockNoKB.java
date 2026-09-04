package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.compat.Timing;
import tech.hakuri.teto.event.impl.EventPacket;
import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.impl.block.Scaffold;
import tech.hakuri.teto.feature.impl.misc.RotationManager;
import tech.hakuri.teto.utils.RotationUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockNoKB extends Module {

    public BlockNoKB() {
        name = "方块全反";
        category = Category.combat;
    }

    @Override
    public void onPacket(EventPacket e) {
        if (e.packet instanceof ClientboundSetEntityMotionPacket packet) {
            if (packet.getId() == mc.player.getId()) {
                var vec3 = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
                var vec2 = RotationUtils.aimToPoint(mc.player.getEyePosition(Timing.partialTick()), vec3);
                var yaw = RotationUtils.shortestYaw(mc.player.getYRot(), vec2.x);
                RotationManager.setRotation(yaw, mc.player.getXRot());
            }
        }
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.hurtTime > 5) {
            Scaffold.holdBlock();
            for (int i = 60; i >= 45; i -= 5) {
                RotationManager.setRotation(mc.player.getYRot(), i);
                if (mc.hitResult instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                    break;
                }
            }
        } else {
            RotationManager.resetRotation();
        }
    }

    @Override
    public void onDisable() {
        RotationManager.resetRotation();
    }
}
