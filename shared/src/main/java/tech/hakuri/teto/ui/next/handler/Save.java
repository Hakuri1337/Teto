package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.ui.next.WebClickGUI;
import tech.hakuri.teto.ui.next.bean.Config;
import tech.hakuri.teto.ui.next.bean.ModuleData;
import tech.hakuri.teto.ui.next.bean.ValueData;
import tech.hakuri.teto.utils.Keyboard;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

/**
 * @author nimo
 */
public class Save implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            Config config = new Config();
            config.modules = new LinkedList<>();

            for (Module module : ModuleManager.modules) {
                ModuleData moduleConfig = new ModuleData();
                moduleConfig.name = module.name;
                moduleConfig.enable = module.enable;
                moduleConfig.keyCode = module.keyCode;
                moduleConfig.keyName = Keyboard.get(module.keyCode);
                moduleConfig.values = new LinkedList<>();

                for (Value value : ModuleManager.getValues(module)) {
                    ValueData valueConfig = new ValueData();
                    valueConfig.name = value.name;
                    valueConfig.type = value.type;

                    if (value.isBooleanValue()) valueConfig.enable = value.enable;
                    if (value.isNumberValue()) valueConfig.numberValue = value.numberValue;
                    if (value.isModesValue()) valueConfig.currentMode = value.currentMode;

                    moduleConfig.values.add(valueConfig);
                }

                config.modules.add(moduleConfig);
            }

            String json = WebClickGUI.gson.toJson(config);
            String configName = WebClickGUI.toJSON(exchange).get("name").getAsString();
            Files.createDirectories(WebClickGUI.configDir.toPath());
            Path path = WebClickGUI.configPathForSave(configName);

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(json);
            }

            WebClickGUI.sendResponse(exchange, 200, "");
        } catch (Exception e) {
        }
    }
}

