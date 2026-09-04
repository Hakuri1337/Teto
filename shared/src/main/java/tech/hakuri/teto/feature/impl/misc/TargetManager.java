package tech.hakuri.teto.feature.impl.misc;

import tech.hakuri.teto.compat.Compat;

import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tech.hakuri.teto.compat.ItemColors;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class TargetManager extends Module {
    public static Minecraft mc = Minecraft.getInstance();

    public static Value player = new Value("玩家", true);
    public static Value teammate = new Value("队友", false);
    public static Value bot = new Value("假人", false);
    public static Value invisible = new Value("隐身", false);

    public static Value good = new Value("友善", false);
    public static Value bad = new Value("敌对", true);
    public static Value middle = new Value("中立", false);

    public static Value villager = new Value("村民", false);

    public static Value fireball = new Value("火球", true);
    public static Value crystal = new Value("水晶", true);

    public static Value otherEntity = new Value("其他实体", false);

    public static Value fov = new Value("视野", 180, 1, 180);
    public static Value range = new Value("距离", 4, 1, 20);
    public static Value sort = new Value("排序", "视野", List.of("视野", "距离", "血量", "无敌时间"));

    public static Value refreshRate = new Value("刷新间隔", 200, 50, 5000);


    public TargetManager() {
        name = "目标管理器";
        category = Category.misc;

        addValues(player, teammate, bot, invisible);
        addValues(good, bad, middle, villager);
        addValues(fireball, crystal, otherEntity);
        addValues(fov, range, sort, refreshRate);

        toggle();
    }

    public static boolean isBot(Entity entity) {
        boolean info = Minecraft.getInstance().getConnection().getPlayerInfo(entity.getUUID()) == null;
        boolean id = entity.getId() > 0xffffff || entity.getId() < 0;
        return info || id;
    }

    public static boolean isTeam(Entity entity) {
        if (entity instanceof Player p) {
            ItemStack myHead = Compat.helmet(Minecraft.getInstance().player);
            ItemStack hisHead = Compat.helmet(p);

            Integer myColor = getArmorColor(myHead);
            Integer hisColor = getArmorColor(hisHead);

            if (myColor == null || hisColor == null) return false;

            return myColor.equals(hisColor);
        }
        return false;
    }

    public static Integer getArmorColor(ItemStack stack) {
        return ItemColors.getArmorColor(stack);
    }
}