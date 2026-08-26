package tech.hakuri.teto.feature.impl.block;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.feature.impl.combat.KillAura;
import tech.hakuri.teto.feature.impl.misc.KeyListener;
import tech.hakuri.teto.feature.impl.misc.RotationManager;
import tech.hakuri.teto.feature.impl.move.Eagle;
import tech.hakuri.teto.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * @author 豆包
 * @author 氯雷他定
 * @author 手淫
 */
public class Scaffold extends Module {

    public static Value mode = new Value("模式", "快", List.of("慢", "中", "快"));
    public static Direction[] d6 = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static Direction[] d8 = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public BlockPos lastSolid;
    public double startY;

    public Scaffold() {
        name = "自动搭路";
        category = Category.block;
        addValues(mode);
    }

    public static Integer findBlockHotBar() {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 9; i++) {
            if (isBlockItem(mc.player.getInventory().getItem(i).getItem())) {
                return i;
            }
        }
        return null;
    }

    public static boolean isBlockItem(Item item) {
        if (item instanceof BlockItem it) {
            if (it.getBlock() instanceof FallingBlock) {
            } else {
                if (it.getBlock().defaultBlockState().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    //豆包
    public static boolean isFaceVisible(BlockPos pos, Direction face) {
        Minecraft mc = Minecraft.getInstance();

        BlockState state = mc.level.getBlockState(pos);
        Vec3 myEye = mc.player.getEyePosition(mc.getFrameTime());

        // 获取方块的边界盒
        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return false;//有异常
        AABB aabb = shape.bounds();

        // 计算面的中心位置
        Vec3 faceCenter = getFaceCenter(pos, aabb, face);

        // 创建从玩家眼睛到面中心的射线
        Vec3 direction = faceCenter.subtract(myEye).normalize();
        double maxDistance = mc.player.getBlockReach();
        Vec3 endPos = myEye.add(direction.scale(maxDistance));

        // 进行射线追踪
        BlockHitResult hitResult = mc.level.clip(new ClipContext(new Vec3(myEye.x, myEye.y, myEye.z), new Vec3(endPos.x, endPos.y, endPos.z), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));

        // 如果击中的是当前方块，并且是同一个面，返回true
        BlockPos hitPos = hitResult.getBlockPos();
        Direction hitFace = hitResult.getDirection();

        return hitPos.equals(pos) && hitFace == face;
    }

    //豆包
    public static Vec3 getFaceCenter(BlockPos pos, AABB aabb, Direction face) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        // 根据面的方向调整坐标到面的中心
        double centerX = x + aabb.minX + (aabb.maxX - aabb.minX) / 2;
        double centerY = y + aabb.minY + (aabb.maxY - aabb.minY) / 2;
        double centerZ = z + aabb.minZ + (aabb.maxZ - aabb.minZ) / 2;
        return switch (face) {
            case DOWN -> new Vec3(centerX, y + aabb.minY, centerZ);
            case UP -> new Vec3(centerX, y + aabb.maxY, centerZ);
            case NORTH -> new Vec3(centerX, centerY, z + aabb.minZ);
            case SOUTH -> new Vec3(centerX, centerY, z + aabb.maxZ);
            case WEST -> new Vec3(x + aabb.minX, centerY, centerZ);
            case EAST -> new Vec3(x + aabb.maxX, centerY, centerZ);
        };
    }

    public static void holdBlock() {
        Minecraft mc = Minecraft.getInstance();
        Integer block = findBlockHotBar();
        if (block != null) {
            mc.player.getInventory().selected = block;
        }
    }

    public static BlockPos keepFindUntilBlock() {
        Minecraft mc = Minecraft.getInstance();
        //其实我感觉往下找2格够了
        //太多疑似有问题
        for (int i = 1; i <= 2; i++) {
            var pos = BlockPos.containing(mc.player.getX(), mc.player.getY() - i, mc.player.getZ());
            if (!mc.level.getBlockState(pos).isAir()) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void onEnable() {
        ModuleManager.getModule(KillAura.class).disable();
        lastSolid = mc.player.getOnPos();
        startY = mc.player.getY();
    }

    @Override
    public void onTick(EventTick event) {
        //debug
        if (lastSolid != null)
            mc.level.addParticle(ParticleTypes.HEART, lastSolid.getX() + 0.5, lastSolid.getY() + 0.5, lastSolid.getZ() + 0.5, lastSolid.getX() + 0.5, lastSolid.getY() + 0.5, lastSolid.getZ() + 0.5);


        //更新目标
        BlockPos foot = keepFindUntilBlock();
        if (foot != null) {
            BlockState footState = mc.level.getBlockState(foot);
            if (!footState.isAir()) {
                lastSolid = foot;
            }
        }


        var lastState = mc.level.getBlockState(lastSolid);


        for (Direction direction : d6) {
            //面可见
            if (isFaceVisible(lastSolid, direction)) {
                //算转头
                var tmp = RotationUtils.aimToPoint(mc.player.getEyePosition(mc.getFrameTime()), getFaceCenter(lastSolid, lastState.getShape(mc.level, lastSolid).bounds(), direction));
                //转过去
                RotationManager.setRotation(tmp.x, tmp.y);
                //拿方块
                if (!Eagle.holdingBlock()) holdBlock();
                //右键
                InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(getFaceCenter(lastSolid, lastState.getShape(mc.level, lastSolid).bounds(), direction), direction, lastSolid, true));
                if (result == InteractionResult.SUCCESS) {
                    //别忘了挥手，但是grim不检测挥手
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    if (mc.hitResult instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
                        //斜搭
                        lastSolid = hitResult.getBlockPos();
                    }
                }
                //这个return必须有，为啥我也忘了
                return;
            }
        }

        switch (mode.currentMode) {
            case "慢" -> {

            }
            case "中" -> {
                if (mc.player.onGround()) RotationManager.resetRotation();
            }
            case "快" -> {
                RotationManager.resetRotation();
            }
        }
    }

    @Override
    public void onDisable() {
        RotationManager.resetRotation();
        KeyListener.resetShift();
    }
}
