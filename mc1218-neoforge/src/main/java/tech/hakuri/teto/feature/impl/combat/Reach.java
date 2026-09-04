package tech.hakuri.teto.feature.impl.combat;

import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 1.21.8 版本专属副本。
 * <p>
 * 1.20.1 那份靠 {@code ForgeMod.ENTITY_REACH}（Forge 自己加的属性）+ 反射读
 * {@code LivingEntity.f_22139_} 拿到 AttributeMap。到 1.21 这两处都不需要了：
 * <ul>
 *   <li>攻击距离属性进了原版：{@code Attributes.ENTITY_INTERACTION_RANGE}，
 *       不再依赖 mod loader，NeoForge 上也就没有 {@code ForgeMod} 这个类。</li>
 *   <li>{@code LivingEntity.getAttributes()} 是公开方法，不用再反射私有字段。</li>
 * </ul>
 * 所以这份实现比 1.20.1 那份更短也更稳。
 */
public class Reach extends Module {

    public static Value range = new Value("距离", 3, 3, 6);

    public Reach() {
        name = "长臂猿";
        category = Category.combat;
        addValues(range);
    }

    public static void setReach(float r) {
        try {
            Minecraft.getInstance().player.getAttributes()
                    .getInstance(Attributes.ENTITY_INTERACTION_RANGE)
                    .setBaseValue(r);
        } catch (Exception e) {
        }
    }

    @Override
    public void onEnable() {
        setReach(range.numberValue);
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.tickCount % 20 == 0) {
            setReach(range.numberValue);
        }
    }

    @Override
    public void onDisable() {
        setReach(3);
    }
}
