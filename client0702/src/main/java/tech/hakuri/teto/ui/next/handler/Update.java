package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.feature.Value;
import tech.hakuri.teto.ui.next.WebClickGUI;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author nimo
 */
public class Update implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            JsonObject request = WebClickGUI.toJSON(exchange);

            String moduleName = request.get("module").getAsString();
            String valueName = request.get("value").getAsString();

            for (Value value : ModuleManager.getValues(ModuleManager.getModule(moduleName))) {
                if (value.name.equals(valueName)) {
                    if (value.isBooleanValue()) value.enable = request.get("new").getAsBoolean();
                    if (value.isNumberValue()) value.numberValue = request.get("new").getAsFloat();
                    if (value.isModesValue()) value.currentMode = request.get("new").getAsString();
                    break;
                }
            }

            WebClickGUI.sendResponse(exchange, 200, "");
        } catch (Exception e) {
        }
    }
}