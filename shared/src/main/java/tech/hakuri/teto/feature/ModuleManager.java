package tech.hakuri.teto.feature;


import tech.hakuri.teto.utils.ReflectBridge;

import java.util.LinkedList;
import java.util.List;

public class ModuleManager {
    public static List<Module> modules = new LinkedList<>();

    public static List<Module> getModules(String category) {
        List<Module> result = new LinkedList<>();
        for (Module module : modules) {
            if (module.category.equals(category)) result.add(module);
        }
        return result;
    }

    public static <T extends Module> T getModule(Class<T> want) {
        for (Module module : modules) {
            if (ReflectBridge.isInstance(want, module)) {
                return ReflectBridge.cast(want, module);
            }
        }
        return null;
    }

    public static Module getModule(String want) {
        for (Module module : modules) {
            if (module.name.equals(want)) {
                return module;
            }
        }
        return null;
    }

    public static List<Value> getValues(Module moduleIn) {
        List<Value> result = new LinkedList<>();
        for (Module module : modules) {
            if (module == moduleIn) {
                result.addAll(module.values);
            }
        }
        return result;
    }

    public static Value getValue(Module module, String name) {
        for (Value value : getValues(module)) {
            if (value.name.equals(name)) return value;
        }
        return null;
    }
}
