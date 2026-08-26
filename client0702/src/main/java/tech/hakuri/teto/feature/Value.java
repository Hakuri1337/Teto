package tech.hakuri.teto.feature;

import java.util.List;

//发明人Goose，发明项目，手动泛型
public class Value {

    public String name;

    public String type;

    public boolean enable;

    public float numberValue;
    public float min;
    public float max;

    public String currentMode;
    public List<String> modes;


    public Value(String name, String defaultMode, List<String> modes) {
        this.name = name;
        this.currentMode = defaultMode;
        this.modes = modes;
        this.type = "modes";
    }

    public Value(String name, boolean defaultBoolean) {
        this.name = name;
        this.enable = defaultBoolean;
        this.type = "boolean";
    }

    public Value(String name, float defaultNumber, float min, float max) {
        this.name = name;
        this.numberValue = defaultNumber;
        this.min = min;
        this.max = max;
        this.type = "number";
    }

    public boolean isModesValue() {
        return type.equals("modes");
    }

    public boolean isBooleanValue() {
        return type.equals("boolean");
    }

    public boolean isNumberValue() {
        return type.equals("number");
    }

    public void nextMode() {
        int curIndex = modes.indexOf(currentMode);
        curIndex++;
        currentMode = modes.get(curIndex % modes.size());
    }

}
