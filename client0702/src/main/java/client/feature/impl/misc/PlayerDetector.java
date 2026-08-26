package client.feature.impl.misc;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class PlayerDetector extends Module {

    public String nameList = """
            qwe
            asd
            zxc
            """;
    public List<String> names = List.of(nameList.split("\n"));

    public PlayerDetector() {
        name = "玩家探测";
        category = Category.misc;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.tickCount % 20 == 0) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                for (String per : names) {
                    if (entity.getName().getString().equals(per)) {
                        mc.getConnection().sendCommand("hub");
                    }
                }
            }
        }
    }
}

