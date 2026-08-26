package tech.hakuri.teto.feature.impl.move;


import tech.hakuri.teto.event.impl.EventTick;
import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class NoFall extends Module {
    public Value mode = new Value("模式", "落地水", List.of("暴力", "落地水"));
    public Value fall = new Value("摔落距离", 3, 1, 12);

    public NoFall() {
        name = "无摔落伤害";
        category = Category.move;
        addValues(mode, fall);
    }


    public static Integer findItemHotbar(Item item) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem().equals(item)) return i;
        }
        return null;
    }

    @Override
    public void onTick(EventTick event) {
        if (mc.player.fallDistance > fall.numberValue) {//try run /tp ~ ~3.34627 ~ in signal player
            switch (mode.currentMode) {
                case "暴力" -> {
                    mc.player.resetFallDistance();
                    mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true));
                }
                case "落地水" -> {
                    Integer itemHotbar = findItemHotbar(Items.WATER_BUCKET);
                    if (itemHotbar != null) {
                        mc.player.getInventory().selected = itemHotbar;
                        if (mc.player.getMainHandItem().getItem() == Items.WATER_BUCKET) {
                            mc.player.setXRot(90);
                            if (mc.hitResult instanceof BlockHitResult hitResult) {
                                if (hitResult.getType() == HitResult.Type.BLOCK) {
                                    if (hitResult.getDirection() == Direction.UP) {
                                        KeyMapping.click(mc.options.keyUse.getKey());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}