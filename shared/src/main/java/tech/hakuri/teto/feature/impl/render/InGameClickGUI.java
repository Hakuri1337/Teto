package tech.hakuri.teto.feature.impl.render;


import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.ui.clickgui.MaterialClickGUI;
import tech.hakuri.teto.ui.clickgui.NavenClickGUI;
import tech.hakuri.teto.utils.Keyboard;
import net.minecraft.client.gui.screens.Screen;

public class InGameClickGUI extends Module {

    /** 新旧两套 ClickGUI：默认 Naven 风格，可切回 Material 或旧版。 */
    public static Value mode = new Value("菜单样式", "Naven", java.util.List.of("Naven", "Material", "Old"));

    public InGameClickGUI() {
        name = "内置菜单";
        category = Category.render;
        keyCode = Keyboard.get("INSERT");
        addValues(mode);
    }

    @Override
    public void onEnable() {
        mc.execute(new Runnable() {
            @Override
            public void run() {
                mc.setScreen(screen());
            }
        });
        toggle();
    }

    /** 按当前样式返回对应界面。Old/Material 的类只在对应路径里才加载。 */
    private Screen screen() {
        if ("Old".equals(mode.currentMode)) {
            return new tech.hakuri.teto.ui.clickgui_old.ClickGUI();
        }
        if ("Naven".equals(mode.currentMode)) {
            return new NavenClickGUI();
        }
        return new MaterialClickGUI();
    }
}
