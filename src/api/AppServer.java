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
        
        // Register handlers (extracted and inner)
        server.createContext("/api/users", new UserHandler(graphService));
        server.createContext("/api/friends", new FriendHandler(graphService));
        server.createContext("/api/suggestions", new SuggestionHandler(graphService));
        server.createContext("/api/graph", new GraphHandler());
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

    // 4. /api/graph Handler
    private class GraphHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
                return;
            }

            try {
                JSONArray nodesJson = new JSONArray();
                for (User user : graphService.getAllUsers()) {
                    nodesJson.put(user.toJSONObject());
                }

                JSONArray linksJson = new JSONArray();
                Graph graph = graphService.getGraph();
                BinarySearchTree<String, Boolean> visitedEdges = new BinarySearchTree<>();

                for (String uId : graph.getVertices()) {
                    for (String vId : graph.getNeighbors(uId)) {
                        String edgeKey1 = uId + "-" + vId;
                        String edgeKey2 = vId + "-" + uId;
                        if (!visitedEdges.contains(edgeKey1) && !visitedEdges.contains(edgeKey2)) {
                            JSONObject link = new JSONObject();
                            link.put("source", uId);
                            link.put("target", vId);
                            linksJson.put(link);
                            visitedEdges.put(edgeKey1, true);
                        }
                    }
                }

                JSONObject graphData = new JSONObject();
                graphData.put("nodes", nodesJson);
                graphData.put("links", linksJson);

                // Add adjacency matrix info as well for the research dashboard
                Graph.AdjacencyMatrixInfo matrixInfo = graph.getAdjacencyMatrix();
                JSONObject matrixJson = new JSONObject();
                JSONArray verticesArr = new JSONArray();
                for (String vertex : matrixInfo.vertices) {
                    verticesArr.put(vertex);
                }
                matrixJson.put("vertices", verticesArr);

                JSONArray matrixRows = new JSONArray();
                for (int i = 0; i < matrixInfo.matrix.length; i++) {
                    JSONArray row = new JSONArray();
                    for (int j = 0; j < matrixInfo.matrix[i].length; j++) {
                        row.put(matrixInfo.matrix[i][j]);
                    }
                    matrixRows.put(row);
                }
                matrixJson.put("matrix", matrixRows);
                graphData.put("adjacencyMatrix", matrixJson);

                sendJsonResponse(exchange, 200, graphData);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
            }
        }
    }

    // 5. /api/benchmark Handler
    private class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
                return;
            }

            try {
                int[] sizes = {10, 50, 100, 250, 500, 750, 1000};
                JSONArray results = new JSONArray();

                for (int size : sizes) {
                    Graph tempGraph = new Graph();
                    BinarySearchTree<String, User> tempUsers = new BinarySearchTree<>();

                    for (int i = 0; i < size; i++) {
                        String id = "temp_" + i;
                        User u = new User(id, "User " + i, "user_" + i, "", "");
                        tempUsers.put(id, u);
                        tempGraph.addVertex(id);
                    }

                    int numEdges = (int) (size * size * 0.03);
                    for (int e = 0; e < numEdges; e++) {
                        int u1 = (int) (Math.random() * size);
                        int u2 = (int) (Math.random() * size);
                        if (u1 != u2) {
                            tempGraph.addEdge("temp_" + u1, "temp_" + u2);
                        }
                    }

                    String targetId = "temp_0";
                    service.RecommendationEngine engine = new service.RecommendationEngine();
                    int k = 10;

                    long tStart = System.nanoTime();
                    engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, k);
                    long tMaxHeap = System.nanoTime() - tStart;

                    tStart = System.nanoTime();
                    engine.getRecommendationsMinHeap(tempGraph, tempUsers, targetId, k);
                    long tMinHeap = System.nanoTime() - tStart;

                    JSONObject result = new JSONObject();
                    result.put("size", size);
                    result.put("edges", tempGraph.getNumEdges());
                    result.put("maxHeapTimeMs", tMaxHeap / 1_000_000.0);
                    result.put("minHeapTimeMs", tMinHeap / 1_000_000.0);
                    results.put(result);
                }

                JSONObject response = new JSONObject();
                response.put("benchmarkResults", results);
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Benchmark failed: " + e.getMessage()));
            }
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
