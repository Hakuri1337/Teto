package client.feature.impl.move;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.feature.impl.misc.KeyListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Eagle extends Module {

    public Value place = new Value("自动放置", true);

    public Eagle() {
        name = "边缘蹲下";
        category = Category.move;
        addValues(place);
    }

    //感谢氯雷他定，修了老goose的类型强转的屎
    public static BlockPos getBlockPosUnderPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return BlockPos.containing(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ());
    }

    public static BlockState getBlockStateUnderPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.getBlockState(getBlockPosUnderPlayer());
    }

    public static boolean canPlace() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hitResult instanceof BlockHitResult hitResult) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos willPlacePos = hitResult.getBlockPos().relative(hitResult.getDirection());
                return mc.level.getBlockState(willPlacePos).canBeReplaced();
            }
        }
        return false;
    }

    public static boolean holdingBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem it) {
            if (it.getBlock() instanceof FallingBlock) {
            } else {
                if (it.getBlock().defaultBlockState().isSolid()) {
                    return true;
                }
            }
        }
        if (mc.player.getOffhandItem().getItem() instanceof BlockItem it) {
            if (it.getBlock() instanceof FallingBlock) {
            } else {
                if (it.getBlock().defaultBlockState().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.getXRot() > 60) {
            if (mc.player.input.forwardImpulse <= 0) {
                BlockState blockState = getBlockStateUnderPlayer();
                if (blockState.isAir() || blockState.canBeReplaced()) {
                    mc.options.keyShift.setDown(true);
                    if (place.enable && holdingBlock() && canPlace()) {
                        KeyMapping.click(mc.options.keyUse.getKey());
                    }
                    return;
                }
            }
        }
        KeyListener.resetShift();
    }

    @Override
    public void onDisable() {
        KeyListener.resetShift();
    }
}
