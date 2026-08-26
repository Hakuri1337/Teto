package client.feature.impl.combat;

import client.event.impl.EventTick;
import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.utils.AStar;
import client.utils.MSTimer;

public class TPAura extends Module {
    public static Value step = new Value("步长", 10, 1, 10);
    public static Value tpRange = new Value("路径范围", 100, 10, 200);
    public static Value attackRange = new Value("攻击范围", 6, 3, 10);
    public static Value back = new Value("回原位", true);
    public static Value wide = new Value("尽量绕过阻挡", true);
    public static Value delay = new Value("点击冷却", 1000, 200, 5000);
    public static Value max = new Value("最大尝试", 10000, 200, 100000);
    public static MSTimer timer = new MSTimer();


    public TPAura() {
        name = "百米大刀";
        category = Category.combat;
        addValues(step, tpRange, attackRange, back, wide, delay, max);
    }

    @Override
    public void onTick(EventTick event) {
        if (timer.hasTimePassed(delay.numberValue)) {
            new AStar().start();
            timer.reset();
        }
    }
}
