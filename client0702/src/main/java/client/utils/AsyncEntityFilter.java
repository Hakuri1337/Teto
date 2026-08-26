package client.utils;

import client.feature.impl.combat.TPAura;
import client.feature.impl.misc.TargetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author 手淫
 * 有人反映原来的目标管理器太卡，应该是virbox的锅，我们给他新建一个线程，使它无论如何也没法影响帧数
 */
public class AsyncEntityFilter extends Thread {
    public static Minecraft mc = Minecraft.getInstance();
    public static List<Entity> render = List.of();
    public static List<Entity> combat = List.of();
    public static List<Entity> tpAura = List.of();
    public static Entity combatPick;
    public static Entity tpAuraPick;

    static {
        new AsyncEntityFilter().start();
    }

    public static Stream<Entity> renderRule() {
        return StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), true)
                .filter(Objects::nonNull)
                .filter(it -> !it.isRemoved())
                .filter(it -> !it.isSpectator())
                .filter(it -> it != mc.player.getVehicle())
                .filter(it -> it instanceof LocalPlayer ? false : true)
                .filter(it -> it instanceof LivingEntity e ? !e.isDeadOrDying() : true)
                .filter(it -> it instanceof LivingEntity e ? e.hasEffect(MobEffects.INVISIBILITY) ? TargetManager.invisible.enable : true : true)
                .filter(it -> it instanceof Player p ? TargetManager.isBot(p) ? TargetManager.bot.enable : true : true)
                .filter(it -> it instanceof Player p ? TargetManager.isTeam(p) ? TargetManager.teammate.enable : true : true)
                .filter(it -> it instanceof Player ? TargetManager.player.enable : true)
                .filter(it -> it instanceof Animal ? TargetManager.good.enable : true)
                .filter(it -> it instanceof Enemy ? TargetManager.bad.enable : true)
                .filter(it -> it instanceof NeutralMob ? TargetManager.middle.enable : true)
                .filter(it -> it instanceof AbstractVillager ? TargetManager.villager.enable : true)
                .filter(it -> it instanceof EndCrystal ? TargetManager.crystal.enable : true)
                .filter(it -> it instanceof Projectile p ? p instanceof Fireball ? TargetManager.fireball.enable : TargetManager.otherEntity.enable : true)
                .filter(it -> it instanceof ItemEntity ? TargetManager.otherEntity.enable : true)
                .filter(it -> it instanceof ArmorStand ? TargetManager.otherEntity.enable : true);
    }

    public static Stream<Entity> combatRule() {
        return renderRule()
                .filter(it -> RotationUtils.distanceToEntityAABBNearest(mc.player, it) < TargetManager.range.numberValue)
                .filter(it -> RotationUtils.fovCalc(mc.player, it) < TargetManager.fov.numberValue)
                .filter(it -> mc.player.hasLineOfSight(it))
                .sorted(Comparator.comparingDouble(e -> {
                    switch (TargetManager.sort.currentMode) {
                        case "视野" -> {
                            return RotationUtils.fovCalc(mc.player, e);
                        }
                        case "距离" -> {
                            return RotationUtils.distanceToEntityAABBNearest(mc.player, e);
                        }
                        case "血量" -> {
                            return e instanceof LivingEntity it ? it.getHealth() : 0;
                        }
                        case "无敌时间" -> {
                            return e instanceof LivingEntity it ? it.hurtTime : 0;
                        }
                    }
                    return 0;
                }));
    }

    public static Stream<Entity> tpAuraPickRule() {
        return renderRule()
                .filter(it -> RotationUtils.distanceToEntityAABBNearest(mc.player, it) < TPAura.tpRange.numberValue)
                .sorted(Comparator.comparingDouble(e -> {
                    switch (TargetManager.sort.currentMode) {
                        case "视野" -> {
                            return RotationUtils.fovCalc(mc.player, e);
                        }
                        case "距离" -> {
                            return RotationUtils.distanceToEntityAABBNearest(mc.player, e);
                        }
                        case "血量" -> {
                            return e instanceof LivingEntity it ? it.getHealth() : 0;
                        }
                        case "无敌时间" -> {
                            return e instanceof LivingEntity it ? it.hurtTime : 0;
                        }
                    }
                    return 0;
                }));
    }

    public static Stream<Entity> tpAuraAttackRule(Vec3 from) {
        return renderRule().filter(it -> from.distanceTo(it.position()) < TPAura.attackRange.numberValue);
    }

    @Override
    public void run() {
        try {
            while (true) {
                sleep((long) TargetManager.refreshRate.numberValue);
                if (mc.level == null || mc.player == null) continue;
                render = renderRule().toList();
                combat = combatRule().toList();
                tpAura = tpAuraPickRule().toList();
                combatPick = combat.isEmpty() ? null : combat.get(0);
                tpAuraPick = tpAura.isEmpty() ? null : tpAura.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AsyncEntityFilter().start();
            stop();
        }
    }

}