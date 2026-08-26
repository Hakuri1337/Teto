package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.ui.clickgui.ClickGUI;
import tech.hakuri.teto.utils.Keyboard;

public class InGameClickGUI extends Module {

    public InGameClickGUI() {
        name = "内置菜单";
        category = Category.render;
        keyCode = Keyboard.get("INSERT");
    }

    @Override
    public void onEnable() {
        mc.execute(new Runnable() {
            @Override
            public void run() {
                mc.setScreen(new ClickGUI());
            }
        });
        toggle();
    }


}