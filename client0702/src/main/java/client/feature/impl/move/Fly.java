package client.feature.impl.move;

import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import net.minecraft.world.entity.player.Abilities;

public class Fly extends Module {

    public Value speed = new Value("速度", 0.05f, 0.05f, 1);

    public Fly() {
        name = "飞行";
        category = Category.move;
        addValues(speed);
    }

    @Override
    public void onEnable() {
        Abilities abilities = mc.player.getAbilities();
        abilities.flying = true;
        abilities.mayfly = true;
        abilities.setFlyingSpeed(speed.numberValue);
    }

    @Override
    public void onDisable() {
        Abilities abilities = mc.player.getAbilities();
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().mayfly = false;
        abilities.setFlyingSpeed(0.05f);
    }


}
