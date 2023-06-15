package com.stemcraft.api;

import com.stemcraft.STEMCraft;
import com.stemcraft.config.SMConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMApi {
    public enum HttpMethod {
        GET,
        POST,
        PATCH,
        DELETE,
    }

    private static class Endpoint {
        public HttpMethod method;
        public String path;
        public SMApiEndpoint endpoint;
        public Pattern pattern;

        public Endpoint(HttpMethod method, String path, SMApiEndpoint endpoint) {
            this.method = method;
            this.path = path;
            this.endpoint = endpoint;

            String regex = path.replaceAll("\\{(.+?)\\}", "(?<group$1>.+)");
            pattern = Pattern.compile(regex);
        }

        public boolean matches(HttpMethod method, String path) {
            return this.method == method && pattern.matcher(path).matches();
        }

        public Map<String, String> extractPathParams(String path) {
            Map<String, String> pathParams = new HashMap<>();
            Matcher matcher = pattern.matcher(path);
            
            if (matcher.matches()) {
                Pattern keyPattern = Pattern.compile("\\{(.+?)\\}");
                Matcher keyMatcher = keyPattern.matcher(this.path);
                while (keyMatcher.find()) {
                    String key = keyMatcher.group(1);
                    String value = matcher.group("group" + key);
                    pathParams.put(key, value);
                }
            }
            
            return pathParams;
        }

        public SMApiResponse doMethod(String path, Map<String, String> pathParams) {
            String methodName = "do" + method.name().substring(0, 1).toUpperCase() + method.name().substring(1).toLowerCase();

            try {
                Object result = endpoint.getClass().getMethod(methodName, String.class, Map.class)
                        .invoke(endpoint, path, pathParams);

                if (result instanceof SMApiResponse) {
                    return (SMApiResponse) result;
                } else {
                    // Handle the case where the method does not return a valid SMApiResponse
                    return new SMApiResponse(500, "Internal server error");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new SMApiResponse(500, "Internal server error");
            }
        }
    }

    private static HttpServer httpServer = null;
    private static List<Endpoint> endpoints = new ArrayList<>();

    public static void loadServer() {
        loadEndpoints();
        if(SMConfig.API_ENABLED) {
            startServer();
        } else {
            System.out.println("API server disabled in config");
        }
    }

    public static void loadEndpoints() {
        List<Class<?>> classEndpointList = STEMCraft.getClassList("com/stemcraft/api/endpoint", false);

        for (Class<?> classEndpointItem : classEndpointList) {
            try {
                Constructor<?> constructor = classEndpointItem.getDeclaredConstructor();
                SMApiEndpoint endpointInstance = (SMApiEndpoint) constructor.newInstance();
                
                endpointInstance.doRegister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void startServer() {
        if (httpServer != null) {
            stopServer();
        }
        try {
            httpServer = HttpServer.create(new InetSocketAddress(SMConfig.API_PORT), 0);
            httpServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    String requestPath = exchange.getRequestURI().getPath();
                    SMApiResponse response = new SMApiResponse(403, "Not found");

                    for (Endpoint endpoint : endpoints) {
                        if (endpoint.matches(HttpMethod.valueOf(requestMethod), requestPath)) {
                            Map<String, String> pathParams = endpoint.extractPathParams(requestPath);
                            
                            response = endpoint.doMethod(requestPath, pathParams);
                            break;
                        }
                    }

                    response.send(exchange);
                }
            });

            httpServer.start();
            System.out.println("API server started on port " + String.valueOf(SMConfig.API_PORT));
        } catch (IOException e) {
            // Handle the exception accordingly
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0); // Stops the HTTP server gracefully
            System.out.println("API server stopped");
            httpServer = null;
        }
    }

    public static void registerEndpoint(HttpMethod method, String path, SMApiEndpoint endpoint) {
        for (Endpoint existingEndpoint : endpoints) {
            if (existingEndpoint.method == method && existingEndpoint.path.equals(path) && existingEndpoint.endpoint == endpoint) {
                // Endpoint already exists, no need to register again
                return;
            }
        }

        endpoints.add(new Endpoint(method, path, endpoint));
    }
}
