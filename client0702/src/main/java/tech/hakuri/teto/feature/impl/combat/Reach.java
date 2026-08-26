package tech.hakuri.teto.feature.impl.combat;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.utils.ReflectBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.common.ForgeMod;

public class Reach extends Module {

    public static Value range = new Value("距离", 3, 3, 6);

    public Reach() {
        name = "长臂猿";
        category = Category.combat;
        addValues(range);
    }

    public static void setReach(float r) {
        try {//f_22139_ attributes
            ReflectBridge.getField(AttributeMap.class, LivingEntity.class, Minecraft.getInstance().player, "f_22139_").getInstance(ForgeMod.ENTITY_REACH.get()).setBaseValue(r);
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