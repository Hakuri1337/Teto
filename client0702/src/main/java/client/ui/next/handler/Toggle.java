package client.ui.next.handler;

import client.feature.Module;
import client.feature.ModuleManager;
import client.ui.next.WebClickGUI;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author nimo
 */
public class Toggle implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            JsonObject request = WebClickGUI.toJSON(exchange);

            String moduleName = request.get("module").getAsString();
            boolean enable = request.get("enable").getAsBoolean();

            Module module = ModuleManager.getModule(moduleName);
            if (module != null && module.enable != enable) {
                module.toggle();
            }

            WebClickGUI.sendResponse(exchange, 200, "");
        } catch (Exception e) {
        }
    }
}
