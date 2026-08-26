package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.feature.Category;
import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.ui.next.WebClickGUI;
import tech.hakuri.teto.ui.next.bean.CategoryData;
import tech.hakuri.teto.ui.next.bean.ModuleData;
import tech.hakuri.teto.ui.next.bean.ValueData;
import tech.hakuri.teto.utils.Keyboard;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nimo
 */
public class Overview implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<CategoryData> categoriesData = new LinkedList<>();

            for (String category : Category.getAll()) {
                CategoryData categoryData = new CategoryData();
                categoryData.name = category;
                categoryData.modules = new LinkedList<>();

                for (Module module : ModuleManager.getModules(category)) {
                    ModuleData moduleData = new ModuleData();
                    moduleData.name = module.name;
                    moduleData.enable = module.enable;
                    moduleData.keyCode = module.keyCode;
                    moduleData.keyName = Keyboard.get(module.keyCode);
                    moduleData.values = new LinkedList<>();

                    for (Value value : ModuleManager.getValues(module)) {
                        ValueData valueData = new ValueData();
                        valueData.name = value.name;
                        valueData.type = value.type;

                        if (value.isBooleanValue()) valueData.enable = value.enable;
                        if (value.isNumberValue()) {
                            valueData.numberValue = value.numberValue;
                            valueData.min = value.min;
                            valueData.max = value.max;
                        }
                        if (value.isModesValue()) {
                            valueData.currentMode = value.currentMode;
                            valueData.modes = value.modes;
                        }

                        moduleData.values.add(valueData);
                    }

                    categoryData.modules.add(moduleData);
                }

                categoriesData.add(categoryData);
            }

            String json = WebClickGUI.gson.toJson(categoriesData);

            WebClickGUI.sendResponse(exchange, 200, json);
        } catch (Exception e) {
        }
    }
}
