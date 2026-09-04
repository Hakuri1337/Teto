package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.ui.next.WebClickGUI;

import java.util.List;

public class WebGUI extends Module {
    public static Value mode = new Value("风格", "现代", List.of("传统", "现代", "拟物", "春节", "端午节", "情人节", "小清新", "动物", "神奇宝贝", "温馨", "木纹"));

    public WebGUI() {
        name = "网页外置菜单";
        category = Category.render;
        addValues(mode);
    }

    @Override
    public void onEnable() {
        WebClickGUI.start();
    }

    @Override
    public void onDisable() {
        WebClickGUI.stop();
    }
}