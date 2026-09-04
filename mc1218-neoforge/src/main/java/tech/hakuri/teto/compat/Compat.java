package tech.hakuri.teto.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import tech.hakuri.teto.utils.ReflectBridge;

/**
 * 版本差异收口（1.21.8 NeoForge 实现）。与 client0702 下的同名类一一对应。
 */
public final class Compat {

    private Compat() {
    }

    /** Forge 的 getBlockReach() 扩展没了；1.21 把交互距离放进了原版属性。 */
    public static double blockReach(net.minecraft.world.entity.player.Player player) {
        return player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
    }

    public static double entityReach(net.minecraft.world.entity.player.Player player) {
        return player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    /** isInFluidType 是 Forge 扩展；原版等价判断是水或岩浆。 */
    public static boolean inFluid(Entity entity) {
        return entity.isInWater() || entity.isInLava();
    }

    /** 1.21 附魔数据驱动化：Enchantments.EFFICIENCY 是 ResourceKey，取等级要先过注册表换 Holder。 */
    public static int efficiencyLevel(ItemStack stack) {
        try {
            var registry = Minecraft.getInstance().level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT);
            return EnchantmentHelper.getItemEnchantmentLevel(
                    registry.getOrThrow(Enchantments.EFFICIENCY), stack);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 1.20.5 起攻击力走 ItemAttributeModifiers 组件，不再有 getAttributeModifiers(EquipmentSlot)。 */
    public static double baseAttackDamage(ItemStack stack) {
        double damage = 0;
        var modifiers = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value()
                    && entry.slot().test(EquipmentSlot.MAINHAND)) {
                damage += entry.modifier().amount();
            }
        }
        return damage;
    }

    /**
     * Entity.getMobType() 在 1.21 被删除（改成实体类型 tag），
     * 而 EnchantmentHelper 的伤害加成也不再由客户端单独算 —— 服务端会算进实际伤害。
     * 这里返回 0：AutoWeapon 只用它给武器排序，少这一项仍能按基础攻击力正确排序。
     */
    public static float damageBonus(ItemStack stack, LivingEntity target) {
        return 0F;
    }

    /** getSlotUnderMouse() 没了；hoveredSlot 是 protected 字段，反射读。 */
    public static Slot slotUnderMouse(AbstractContainerScreen<?> screen) {
        try {
            return ReflectBridge.getFieldAny(Slot.class, AbstractContainerScreen.class, screen, "hoveredSlot", "f_97734_");
        } catch (Exception e) {
            return null;
        }
    }

    public static void chat(Component message) {
        Minecraft.getInstance().gui.getChat().addMessage(message);
    }

    /** Inventory.armor 随装备槽重构消失；getItemBySlot 两版通用。 */
    public static ItemStack helmet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD);
    }

    /** 1.21.8 的 StatusOnly / Pos 都多了一个 horizontalCollision 参数。 */
    public static Packet<?> statusOnly(boolean onGround) {
        return new ServerboundMovePlayerPacket.StatusOnly(onGround, false);
    }

    public static Packet<?> movePos(double x, double y, double z, boolean onGround) {
        return new ServerboundMovePlayerPacket.Pos(x, y, z, onGround, false);
    }

    /**
     * 空实现。RenderSystem 的 polygonOffset 家族在 1.21.5 渲染重写里被删除 ——
     * 深度偏移改由 RenderPipeline.withDepthBias 在管线构建时声明，运行时没法临时开关。
     * 后果：Chams 在 1.21.8 上退化为普通渲染，不再有「贴图画在墙前面」的效果。
     * 要恢复得自建一条带 depthBias 的 RenderPipeline，属于渲染层重写的范围。
     */
    public static void depthOffsetOn() {
    }

    public static void depthOffsetOff() {
    }
    /**
     * 1.21.8：onDisconnect 收的是 DisconnectionDetails（包着 Component 的 record），
     * 取它的第一个组件当理由。
     */
    public static Component kickReason(Object raw) {
        if (raw instanceof Component c) {
            return c;
        }
        if (raw instanceof net.minecraft.network.DisconnectionDetails d) {
            return d.reason();
        }
        return Component.literal(String.valueOf(raw));
    }
}
