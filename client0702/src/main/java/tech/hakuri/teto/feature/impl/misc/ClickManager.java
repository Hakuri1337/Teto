package tech.hakuri.teto.feature.impl.misc;


import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.utils.MSTimer;

import java.util.Random;


public class ClickManager extends Module {
    public static Value min = new Value("最小每秒点击", 15, 1, 20);
    public static Value max = new Value("最大每秒点击", 20, 1, 20);

    public static MSTimer timer = new MSTimer();
    public static Random random = new Random();


    public ClickManager() {
        name = "点击管理器";
        category = Category.misc;
        addValues(min, max);
        toggle();
    }

    public static float genRandomDelayMS() {
        return 1000f / random.nextFloat(min.numberValue, max.numberValue);
    }


}
