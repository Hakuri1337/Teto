package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.ui.next.WebClickGUI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author nimo
 */
public class List implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            WebClickGUI.sendResponse(exchange, 200, WebClickGUI.gson.toJson(WebClickGUI.getConfigList()));
        } catch (Exception e) {
        }
    }
}
