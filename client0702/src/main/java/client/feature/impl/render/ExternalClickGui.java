package client.feature.impl.render;


import client.feature.Category;
import client.feature.Module;
import client.ui.legacy.SwingClickGUI;
import client.utils.Keyboard;

import javax.swing.*;

public class ExternalClickGui extends Module {

    public ExternalClickGui() {
        name = "外置菜单";
        category = Category.render;
        keyCode = Keyboard.get("DELETE");
    }

    @Override
    public void onEnable() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingClickGUI();
            }
        });
        toggle();
    }
}