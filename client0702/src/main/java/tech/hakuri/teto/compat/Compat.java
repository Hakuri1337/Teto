package tech.hakuri.teto.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * 版本差异收口（1.20.1 Forge 实现）。共享代码只调这里的静态方法。
 * 每个方法上都注明了 1.21.8 那边为什么不一样，配套实现见 mc1218-neoforge 下的同名类。
 */
public final class Compat {

    private Compat() {
    }

    /** 1.21.8：Forge 扩展方法没有了，改用原版属性 BLOCK_INTERACTION_RANGE。 */
    public static double blockReach(net.minecraft.world.entity.player.Player player) {
        return player.getBlockReach();
    }

    /** 1.21.8：改用原版属性 ENTITY_INTERACTION_RANGE。 */
    public static double entityReach(net.minecraft.world.entity.player.Player player) {
        return player.getEntityReach();
    }

    /** 1.21.8：isInFluidType 是 Forge 扩展，改判 isInWater()||isInLava()。 */
    public static boolean inFluid(Entity entity) {
        return entity.isInFluidType();
    }

    /** 1.21.8：附魔改成数据驱动，取等级要过注册表拿 Holder。 */
    public static int efficiencyLevel(ItemStack stack) {
        return EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
    }

    /**
     * 主手武器的基础攻击力（不含附魔）。
     * 1.21.8：ItemStack.getAttributeModifiers(EquipmentSlot) 被 ItemAttributeModifiers 组件取代。
     */
    public static double baseAttackDamage(ItemStack stack) {
        double damage = 0;
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (var modifier : modifiers.get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
            damage += modifier.getAmount();
        }
        return damage;
    }

    /** 1.21.8：Entity.getMobType() 已删除（改用实体类型 tag），附魔加成签名也变了。 */
    public static float damageBonus(ItemStack stack, LivingEntity target) {
        return EnchantmentHelper.getDamageBonus(stack, target.getMobType());
    }

    /** 1.21.8：getSlotUnderMouse() 没了，改读 protected 字段 hoveredSlot。 */
    public static Slot slotUnderMouse(AbstractContainerScreen<?> screen) {
        return screen.getSlotUnderMouse();
    }

    /** 1.21.8：Entity.sendSystemMessage 不在了；两边都走聊天组件更稳。 */
    public static void chat(Component message) {
        Minecraft.getInstance().gui.getChat().addMessage(message);
    }

    /** 1.21.8：Inventory.armor 列表随装备槽重构消失；用 getItemBySlot 两边都通。 */
    public static ItemStack helmet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD);
    }

    /** 1.21.8：StatusOnly 构造器多了一个 horizontalCollision 参数。 */
    public static Packet<?> statusOnly(boolean onGround) {
        return new ServerboundMovePlayerPacket.StatusOnly(onGround);
    }

    /** 1.21.8：Pos 构造器同样多了一个参数。 */
    public static Packet<?> movePos(double x, double y, double z, boolean onGround) {
        return new ServerboundMovePlayerPacket.Pos(x, y, z, onGround);
    }

    /**
     * 深度偏移，Chams 用来把实体贴图画到墙前面。
     * 1.21.8：RenderSystem 的 polygonOffset 家族在 1.21.5 渲染重写里被删除，
     * 深度偏移改由 RenderPipeline.withDepthBias 在管线层声明，运行时无法临时开关，
     * 所以那边只能是空实现（Chams 会退化成普通描边，不再穿墙优先）。
     */
    public static void depthOffsetOn() {
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(1f, -1000000);
    }

    public static void depthOffsetOff() {
        RenderSystem.polygonOffset(1f, 1000000);
        RenderSystem.disablePolygonOffset();
    }
    /** 1.20.1：onDisconnect 的参数本身就是 Component，直接转型。 */
    public static Component kickReason(Object raw) {
        return raw instanceof Component c ? c : Component.literal(String.valueOf(raw));
    }
}
