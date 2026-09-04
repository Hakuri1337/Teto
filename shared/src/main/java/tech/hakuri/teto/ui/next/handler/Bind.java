package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.feature.Module;
import tech.hakuri.teto.feature.ModuleManager;
import tech.hakuri.teto.ui.next.WebClickGUI;
import tech.hakuri.teto.utils.Keyboard;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author nimo
 */
public class Bind implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            JsonObject request = WebClickGUI.toJSON(exchange);

            String moduleName = request.get("module").getAsString();
            String key = request.get("key").getAsString().toUpperCase();

            Module module = ModuleManager.getModule(moduleName);
            if (module != null) {
                module.keyCode = Keyboard.get(key);
            }

            WebClickGUI.sendResponse(exchange, 200, "");
        } catch (Exception e) {
        }
    }
}
