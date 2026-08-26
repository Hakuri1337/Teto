package client.ui.legacy;

import client.feature.Category;
import client.feature.Module;
import client.feature.ModuleManager;
import client.feature.Value;
import client.utils.Keyboard;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 手淫
 */
public class SwingClickGUI extends JFrame {

    public SwingClickGUI() {//注意顺序
        this.setTitle("ImGui-Java");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.add(category());
        this.setSize(300, 300);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.toFront();
        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                e.getWindow().dispose();
            }
        });
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public JTabbedPane category() {
        JTabbedPane result = new JTabbedPane();

        for (String category : Category.getAll()) {
            result.addTab(category, module(category));
        }

        return result;
    }

    public JTabbedPane module(String category) {
        JTabbedPane result = new JTabbedPane();

        for (Module module : ModuleManager.getModules(category)) {
            result.addTab(module.name, wrapY(enable(module), bind(module), values(module)));
        }

        return result;
    }

    public JPanel enable(Module module) {
        JCheckBox checkBox = new JCheckBox("启用", module.enable);
        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                module.toggle();
            }
        });
        return wrapY(checkBox);
    }

    public JPanel bind(Module module) {
        JButton button = new JButton(String.format("快捷键：%s", Keyboard.get(module.keyCode)));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popBindScreen(module);
            }
        });
        return wrapY(button);
    }

    public void popBindScreen(Module module) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new BindScreen(module));
            }
        });
    }

    public JPanel values(Module module) {
        List<JComponent> result = new LinkedList<>();

        for (Value value : ModuleManager.getValues(module)) {
            if (value.isBooleanValue()) result.add(booleanValue(value));
            if (value.isNumberValue()) result.add(numberValue(value));
            if (value.isModesValue()) result.add(modeValue(value));
        }

        return wrapY(result.toArray(new JComponent[]{}));
    }

    public JPanel booleanValue(Value value) {
        JCheckBox checkBox = new JCheckBox(value.name, value.enable);
        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                value.enable = checkBox.isSelected();
            }
        });
        return wrapY(checkBox);
    }

    public JPanel numberValue(Value value) {
        JLabel text = new JLabel(String.format("%s：%.2f", value.name, value.numberValue));

        JSlider slider = new JSlider((int) Math.min(value.min * 100, value.numberValue * 100), (int) Math.max(value.max * 100, value.numberValue * 100), (int) (value.numberValue * 100));

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                value.numberValue = slider.getValue() / 100f;
                text.setText(String.format("%s：%.2f", value.name, value.numberValue));
            }
        });

        return wrapX(text, slider);
    }

    public JPanel modeValue(Value value) {
        JLabel text = new JLabel(String.format("%s：", value.name));

        JComboBox<String> comboBox = new JComboBox<>(value.modes.toArray(new String[]{}));
        comboBox.setSelectedItem(value.currentMode);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem() instanceof String it) {
                    value.currentMode = it;
                }
            }
        });

        return wrapX(text, comboBox);
    }

    public JPanel wrapY(JComponent... things) {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setAlignmentY(Component.TOP_ALIGNMENT);

        for (JComponent thing : things) {
            result.add(thing);
        }

        return result;
    }

    public JPanel wrapX(JComponent... things) {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setAlignmentY(Component.TOP_ALIGNMENT);

        for (JComponent thing : things) {
            result.add(thing);
        }

        return result;
    }
}