package com.stemcraft.feature;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;

public class SMWebServer extends SMFeature {
    private HttpServer httpServer;
    private File wwwRoot;

    @Override
    protected Boolean onEnable() {
        boolean enabled = SMConfig.main().getBoolean("web-server.enabled", false);
        wwwRoot = new File(STEMCraft.getPlugin().getDataFolder(), "www");

        if (enabled) {
            if (!wwwRoot.exists()) {
                if (!wwwRoot.mkdirs()) {
                    STEMCraft.severe("Failed to create 'www' directory");
                    return true;
                }
            }

            int port = SMConfig.main().getInt("web-server.port", 8950);
            String ip = SMConfig.main().getString("web-server.ip", "0.0.0.0");

            try {
                httpServer = HttpServer.create(new InetSocketAddress(ip, port), 0);
                httpServer.createContext("/", new MyHttpHandler());
                httpServer.setExecutor(null);
                httpServer.start();
                STEMCraft.info("Web server started on http://" + ip + ":" + port);
            } catch (IOException e) {
                STEMCraft.severe("Failed to start web server: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.stop(0);
            STEMCraft.info("Web server stopped");
        }
    }

    private class MyHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            File file = new File(wwwRoot, uri.substring(1));

            if (!file.getAbsolutePath().startsWith(wwwRoot.getAbsolutePath())) {
                sendErrorResponse(exchange, 403, "Forbidden");
                return;
            }

            if (file.isDirectory()) {
                sendErrorResponse(exchange, 403, "Directory listing not permitted");
                return;
            }

            if (!file.exists()) {
                sendErrorResponse(exchange, 404, "File not found");
                return;
            }

            // Serve the requested file
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
            exchange.sendResponseHeaders(statusCode, errorMessage.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMessage.getBytes());
            }
        }
    }
}
