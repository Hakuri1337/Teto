package client.ui.next;

import client.ClientEntry;
import client.ui.next.handler.*;
import client.ui.next.handler.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author nimo
 */
public class WebClickGUI {
    public static HttpServer server;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static String configDirName = ClientEntry.getConfigDirectory().toString();
    public static File configDir = new File(configDirName);

    public static Path configPathForSave(String configName) {
        return Paths.get(configDirName).resolve(configName).normalize();
    }

    public static Path configPathForRead(String configName) {
        for (Path directory : ClientEntry.getConfigDirectoriesForRead()) {
            Path candidate = directory.resolve(configName).normalize();
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return configPathForSave(configName);
    }

    public static void stop() {
        server.stop(0);
        server = null;
    }

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 12701), 0);

            server.createContext("/save", new Save());
            server.createContext("/load", new Load());
            server.createContext("/list", new List());

            server.createContext("/", new Root());
            server.createContext("/overview", new Overview());
            server.createContext("/toggle", new Toggle());
            server.createContext("/update", new Update());
            server.createContext("/bind", new Bind());

            server.start();

            Desktop.getDesktop().browse(new URI("http://127.0.0.1:12701"));
        } catch (Exception e) {
        }
    }

    public static void sendResponse(HttpExchange exchange, int code, String message) throws Exception {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);//疑似这里乱码，为什么utf8还没推开呢？
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public static java.util.List<String> getConfigList() {
        Set<String> configs = new LinkedHashSet<>();
        try {
            Files.createDirectories(ClientEntry.getConfigDirectory());
        } catch (Exception ignored) {
            // 保持 HTTP handler 的原有容错策略，读取其他候选目录仍可继续。
        }
        for (Path directory : ClientEntry.getConfigDirectoriesForRead()) {
            File[] files = directory.toFile().listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.isFile()) {
                    configs.add(file.getName());
                }
            }
        }
        return new LinkedList<>(configs);
    }

    public static JsonObject toJSON(HttpExchange exchange) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String json = reader.readLine();
            return WebClickGUI.gson.fromJson(json, JsonObject.class);
        }
    }
}

