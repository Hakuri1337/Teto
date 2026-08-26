package client.utils;

import client.feature.impl.combat.TPAura;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;


/**
 * @author 豆包(算法)
 * @author 手淫(逻辑)
 * TODO:在拐弯处打上关键帧，防止香草服务端判定你穿墙
 * 多线程异步的，直接new就好，要改就改run
 */
@Deprecated(since = "豆包 & 手淫")
public class AStar extends Thread {
    /**
     * 查找从实体到目标的路径
     */
    public static List<Vec3> 路径查找_采样后(Vec3 从, Vec3 到, double 步长, int 最大尝试) {
        BlockPos 起始坐标 = BlockPos.containing(从);
        BlockPos 目标坐标 = BlockPos.containing(到);

        // 初始化开放列表和关闭列表
        PriorityQueue<节点> 待探索 = new PriorityQueue<>();
        Set<节点> 已探索 = new HashSet<>();

        // 创建起点节点并添加到开放列表
        节点 起始节点 = new 节点(起始坐标);
        起始节点.实际成本 = 0;
        起始节点.估计成本 = 估计成本(起始坐标, 目标坐标);
        起始节点.综合成本 = 起始节点.实际成本 + 起始节点.估计成本;
        待探索.add(起始节点);

        int 节点数 = 0;

        // A*主循环
        while (!待探索.isEmpty() && 节点数 < 最大尝试) {
            节点 当前节点 = 待探索.poll();
            已探索.add(当前节点);

            // 到达目标节点，构建路径
            if (当前节点.坐标.equals(目标坐标)) {
                return 最终路径(当前节点, 步长);
            }

            // 获取相邻节点
            for (节点 相邻节点 : 相邻节点(当前节点)) {
                if (已探索.contains(相邻节点)) {
                    continue;
                }

                var 暂定实际成本 = 当前节点.实际成本 + 估计成本(当前节点.坐标, 相邻节点.坐标);

                if (!待探索.contains(相邻节点) || 暂定实际成本 < 相邻节点.实际成本) {
                    相邻节点.父节点 = 当前节点;
                    相邻节点.实际成本 = 暂定实际成本;
                    相邻节点.估计成本 = 估计成本(相邻节点.坐标, 目标坐标);
                    相邻节点.综合成本 = 相邻节点.实际成本 + 相邻节点.估计成本;

                    if (!待探索.contains(相邻节点)) {
                        待探索.add(相邻节点);
                        节点数++;
                    }
                }
            }
        }

        return List.of();
    }

    public static List<Vec3> 最终路径(节点 终点, double 步长) {
        List<Vec3> 路径 = new LinkedList<>();


        for (节点 当前 = 终点; 当前 != null; 当前 = 当前.父节点) {
            // 将BlockPos转换为Vec3，并居中处理
            路径.add(new Vec3(
                    当前.坐标.getX() + 0.5,
                    当前.坐标.getY() + 0.5,
                    当前.坐标.getZ() + 0.5
            ));
        }

        // 反转列表，使路径从起点到终点
        Collections.reverse(路径);

        // 按步长采样路径点
        return 路径采样(路径, 步长);
    }

    public static List<Vec3> 路径采样(List<Vec3> 路径, double 步长) {
        if (路径.isEmpty()) return List.of();


        List<Vec3> 采样路径 = new ArrayList<>();
        采样路径.add(路径.get(0));

        Vec3 最新点 = 路径.get(0);
        double 距离 = 0;

        for (int i = 1; i < 路径.size(); i++) {
            Vec3 当前 = 路径.get(i);
            距离 += 最新点.distanceTo(当前);

            if (距离 >= 步长) {
                采样路径.add(当前);
                距离 = 0;
                最新点 = 当前;
            }
        }

        // 确保包含终点
        if (!采样路径.contains(路径.get(路径.size() - 1))) {
            采样路径.add(路径.get(路径.size() - 1));
        }

        return 采样路径;
    }

    /**
     * 获取相邻节点（包括对角线）
     */
    public static List<节点> 相邻节点(节点 节点) {
        List<节点> 相邻列表 = new LinkedList<>();
        BlockPos 坐标 = 节点.坐标;

        // 检查26个方向（3D空间中的所有相邻方块）
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // 跳过自身位置

                    BlockPos 相邻坐标 = 坐标.offset(x, y, z);
                    // 玩家2格高
                    if (有空间(相邻坐标) && 有空间(相邻坐标.above())) {
                        if (TPAura.wide.enable) {
                            // 防止卡角
                            if (有空间(相邻坐标.north()) && 有空间(相邻坐标.south()) && 有空间(相邻坐标.west()) && 有空间(相邻坐标.east())) {
                                相邻列表.add(new 节点(相邻坐标));
                            }
                        } else {
                            相邻列表.add(new 节点(相邻坐标));
                        }
                    }
                }
            }
        }
        return 相邻列表;
    }

    public static double 估计成本(BlockPos from, BlockPos to) {
        return new Vec3(from.getX(), from.getY(), from.getZ()).distanceTo(new Vec3(to.getX(), to.getY(), to.getZ()));
    }

    public static boolean 有空间(BlockPos 坐标) {
        return Minecraft.getInstance().level.getBlockState(坐标).isAir();
    }

    @Override
    public void run() {
        if (AsyncEntityFilter.tpAuraPick == null) return;
        Minecraft mc = Minecraft.getInstance();

        List<Vec3> path = AStar.路径查找_采样后(mc.player.position(), AsyncEntityFilter.tpAuraPick.position(), TPAura.step.numberValue, (int) TPAura.max.numberValue);
        for (Vec3 vec3 : path) {
            mc.level.addParticle(ParticleTypes.HEART, vec3.x, vec3.y, vec3.z, vec3.x, vec3.y, vec3.z);
            mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.Pos(vec3.x, vec3.y, vec3.z, true));
        }
        ChatUtils.msg(path.size());

        if (!path.isEmpty()) {
            Vec3 last = path.get(path.size() - 1);
            for (Entity entity : AsyncEntityFilter.tpAuraAttackRule(last).toList()) {
                mc.gameMode.attack(mc.player, entity);
            }
        }

        if (TPAura.back.enable) {
            Collections.reverse(path);
            for (Vec3 vec3 : path) {
                mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.Pos(vec3.x, vec3.y, vec3.z, true));
            }
        }
    }

    // 节点类，用于A*算法
    public static class 节点 implements Comparable<节点> {
        BlockPos 坐标;
        节点 父节点;
        double 实际成本; // 起点到当前节点的实际成本
        double 估计成本; // 当前节点到终点的预估成本
        double 综合成本; // gCost + hCost

        public 节点(BlockPos 坐标) {
            this.坐标 = 坐标;
        }

        @Override
        public int compareTo(节点 待比较) {
            return Double.compare(this.综合成本, 待比较.综合成本);
        }

        @Override
        public boolean equals(Object 待比较) {
            return 待比较 instanceof 节点 它 && 坐标.equals(它.坐标);
        }

        @Override
        public int hashCode() {
            return 坐标.hashCode();
        }
    }
}