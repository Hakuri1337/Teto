package client.feature.impl.render;


import client.feature.Category;
import client.feature.Module;
import client.feature.Value;
import client.ui.next.WebClickGUI;

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