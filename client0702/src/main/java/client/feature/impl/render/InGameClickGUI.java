package client.feature.impl.render;


import client.feature.Category;
import client.feature.Module;
import client.ui.clickgui.ClickGUI;
import client.utils.Keyboard;

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