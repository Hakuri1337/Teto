package client.ui.next.handler;

import client.feature.Module;
import client.feature.ModuleManager;
import client.feature.Value;
import client.ui.next.WebClickGUI;
import client.ui.next.bean.Config;
import client.ui.next.bean.ModuleData;
import client.ui.next.bean.ValueData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author nimo
 */
public class Load implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String configName = WebClickGUI.toJSON(exchange).get("name").getAsString();

            Path path = WebClickGUI.configPathForRead(configName);

            Config config = WebClickGUI.gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), Config.class);

            for (ModuleData moduleConfig : config.modules) {
                Module module = ModuleManager.getModule(moduleConfig.name);
                if (module == null) continue;

                if (module.enable != moduleConfig.enable) {
                    module.toggle();
                }
                module.keyCode = moduleConfig.keyCode;

                for (ValueData valueConfig : moduleConfig.values) {
                    Value value = ModuleManager.getValue(module, valueConfig.name);
                    if (value == null) continue;
                    if (value.type.equals(valueConfig.type)) {
                        if (value.isBooleanValue()) value.enable = valueConfig.enable;
                        if (value.isNumberValue()) value.numberValue = valueConfig.numberValue;
                        if (value.isModesValue()) value.currentMode = valueConfig.currentMode;
                    }
                }
            }

            WebClickGUI.sendResponse(exchange, 200, "");
        } catch (Exception e) {
        }
    }
}

