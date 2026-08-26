package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.ui.legacy.SwingClickGUI;
import tech.hakuri.teto.utils.Keyboard;

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