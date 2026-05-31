package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import datastructures.BinarySearchTree;
import graph.Graph;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import service.GraphService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class AppServer {
    private HttpServer server;
    private GraphService graphService;
    private int port;

    public AppServer(int port, GraphService graphService) {
        this.port = port;
        this.graphService = graphService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Register handlers
        server.createContext("/api/users", new UserHandler(graphService));
        
        FriendHandler friendHandler = new FriendHandler(graphService);
        server.createContext("/api/friends", friendHandler);
        server.createContext("/api/mutual", friendHandler);
        
        server.createContext("/api/suggestions", new SuggestionHandler(graphService));
        
        NetworkHandler networkHandler = new NetworkHandler(graphService);
        server.createContext("/api/network", networkHandler);
        server.createContext("/api/graph", networkHandler);
        
        server.createContext("/api/benchmark", new BenchmarkHandler());
        
        // Static files handler (serves frontend)
        server.createContext("/", new StaticFileHandler());

        // Multi-threaded server executor
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Java API Server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Java API Server stopped.");
        }
    }

    // Helper to send HTTP JSON responses (package-private for access by handlers)
    static void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws IOException {
        sendJsonResponse(exchange, statusCode, responseJson.toString());
    }

    static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseStr) throws IOException {
        byte[] bytes = responseStr.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Helper to handle OPTIONS preflight request (package-private for access by handlers)
    static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // Helper to parse query parameters (package-private for access by handlers)
    static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            try {
                if (entry.length > 1) {
                    result.put(URLDecoder.decode(entry[0], "UTF-8"), URLDecoder.decode(entry[1], "UTF-8"));
                } else {
                    result.put(URLDecoder.decode(entry[0], "UTF-8"), "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return result;
    }

    // Helper to read request body (package-private for access by handlers)
    static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }


    // 6. Static files handler to serve web frontend assets from frontend/ directory
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("frontend" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("frontend/index.html");
                if (!file.exists()) {
                    String error = "404 Not Found";
                    exchange.sendResponseHeaders(404, error.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(error.getBytes());
                    }
                    return;
                }
            }

            String contentType = "text/plain";
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".html")) {
                contentType = "text/html; charset=utf-8";
            } else if (filename.endsWith(".css")) {
                contentType = "text/css; charset=utf-8";
            } else if (filename.endsWith(".js")) {
                contentType = "application/javascript; charset=utf-8";
            } else if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (filename.endsWith(".json")) {
                contentType = "application/json; charset=utf-8";
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
