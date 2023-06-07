package com.stemcraft.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.stemcraft.config.ConfigHandler;
import com.stemcraft.http.endpoints.InfoEndpoint;
import org.bukkit.plugin.java.JavaPlugin;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;

public class HttpHandler {
    private class Endpoint {
        public String method;
        public String path;
        public HttpEndpoint endpoint;

        public Pattern pattern;

        public Endpoint(String method, String path, HttpEndpoint endpoint) {
            this.method = method;
            this.path = path;
            this.endpoint = endpoint;

            String regex = path.replaceAll("\\{(.+?)\\}", "(?<group$1>.+)");
            patterns.put(template, Pattern.compile(regex));
        }
    }

    private HttpServer httpServer;
    private List<Endpoint> endpoints = new ArrayList<>();

    public final void init() {
        registerEndpoint("GET", "/info", new InfoEndpoint());
    }

    public final void start() {
        httpServer = new HttpServer(this, ConfigHandler.config.HTTP_PORT, new EndPointHandler());
        httpServer.start();
    }

    public void registerEndpoint(String method, String path, HttpEndpoint endpoint) {
        endpoints.add(new Endpoint(method.toUpperCase(), path, endpoint));
    }

    public Map<String, String> match(String uri) {
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(uri);
            if (matcher.matches()) {
                Map<String, String> result = new HashMap<>();
                Pattern pattern = Pattern.compile("\\{(.+?)\\}");
                Matcher keyMatcher = pattern.matcher(entry.getKey());
                while (keyMatcher.find()) {
                    String key = keyMatcher.group(1);
                    String value = matcher.group("group" + key);
                    result.put(key, value);
                }
                return result;
            }
        }
        return null;
    }


    private class FileHandler implements HttpServlet {

        @Override
        public void doGET(HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Handle GET requests for /files
        }

        @Override
        public void doPOST(HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Handle POST requests for file uploads
        }
    }

    private class JsonRequest {
        private String path;
        private String method;

        public JsonRequest(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    private class JsonResponse {
        private String message;

        public JsonResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
