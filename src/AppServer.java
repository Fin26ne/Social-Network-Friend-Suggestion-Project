package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import datastructures.BinarySearchTree;
import datastructures.SinglyLinkedList;
import graph.Graph;
import model.Recommendation;
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
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/friends", new FriendsHandler());
        server.createContext("/api/suggestions", new SuggestionsHandler());
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

    // Helper to send HTTP JSON responses
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws IOException {
        sendJsonResponse(exchange, statusCode, responseJson.toString());
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseStr) throws IOException {
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

    // Helper to handle OPTIONS preflight request
    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // Helper to parse query parameters
    private static Map<String, String> parseQueryParams(String query) {
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

    // Helper to read request body
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // 1. /api/users Handler
    private class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    // Check if requesting single user: /api/users/{id}
                    if (path.matches("^/api/users/[a-zA-Z0-9_-]+$")) {
                        String userId = path.substring(path.lastIndexOf("/") + 1);
                        User user = graphService.getUserById(userId);
                        if (user == null) {
                            sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                        } else {
                            sendJsonResponse(exchange, 200, user.toJSONObject());
                        }
                    } else {
                        // Return list of all users
                        JSONArray usersArr = new JSONArray();
                        for (User user : graphService.getAllUsers()) {
                            usersArr.put(user.toJSONObject());
                        }
                        sendJsonResponse(exchange, 200, new JSONObject().put("users", usersArr));
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    JSONObject json = new JSONObject(body);
                    String name = json.getString("name");
                    String username = json.getString("username");
                    String bio = json.optString("bio", "");

                    try {
                        User created = graphService.addUser(name, username, bio);
                        sendJsonResponse(exchange, 201, new JSONObject().put("message", "User created successfully").put("user", created.toJSONObject()));
                    } catch (IllegalArgumentException e) {
                        sendJsonResponse(exchange, 400, new JSONObject().put("error", e.getMessage()));
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    if (path.matches("^/api/users/[a-zA-Z0-9_-]+$")) {
                        String userId = path.substring(path.lastIndexOf("/") + 1);
                        boolean deleted = graphService.deleteUser(userId);
                        if (deleted) {
                            sendJsonResponse(exchange, 200, new JSONObject().put("message", "User deleted successfully"));
                        } else {
                            sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                        }
                    } else {
                        sendJsonResponse(exchange, 400, new JSONObject().put("error", "User ID required in path"));
                    }
                } else {
                    sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
            }
        }
    }

    // 2. /api/friends Handler
    private class FriendsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    // Get friends list of user: /api/friends/{userId}
                    if (path.matches("^/api/friends/[a-zA-Z0-9_-]+$")) {
                        String userId = path.substring(path.lastIndexOf("/") + 1);
                        if (!graphService.getGraph().hasVertex(userId)) {
                            sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                            return;
                        }

                        SinglyLinkedList<String> friendsIds = graphService.getFriends(userId);
                        JSONArray friendsArr = new JSONArray();
                        for (String fId : friendsIds) {
                            User friend = graphService.getUserById(fId);
                            if (friend != null) {
                                friendsArr.put(friend.toJSONObject());
                            }
                        }
                        sendJsonResponse(exchange, 200, new JSONObject().put("friends", friendsArr));
                    } else {
                        sendJsonResponse(exchange, 400, new JSONObject().put("error", "User ID required in path"));
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    JSONObject json = new JSONObject(body);
                    String userId1 = json.getString("userId1");
                    String userId2 = json.getString("userId2");

                    boolean success = graphService.addFriendship(userId1, userId2);
                    if (success) {
                        sendJsonResponse(exchange, 200, new JSONObject().put("message", "Friendship created successfully"));
                    } else {
                        sendJsonResponse(exchange, 400, new JSONObject().put("error", "Could not create friendship. Invalid user IDs or already friends."));
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    JSONObject json = new JSONObject(body);
                    String userId1 = json.getString("userId1");
                    String userId2 = json.getString("userId2");

                    boolean success = graphService.removeFriendship(userId1, userId2);
                    if (success) {
                        sendJsonResponse(exchange, 200, new JSONObject().put("message", "Friendship removed successfully"));
                    } else {
                        sendJsonResponse(exchange, 400, new JSONObject().put("error", "Could not remove friendship. Invalid user IDs or not currently friends."));
                    }
                } else {
                    sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
            }
        }
    }

    // 3. /api/suggestions Handler
    private class SuggestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
                return;
            }

            try {
                Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
                String userId = queryParams.get("userId");
                String kStr = queryParams.getOrDefault("k", "5");
                String heapType = queryParams.getOrDefault("heapType", "min"); // "min" or "max"

                if (userId == null || userId.isEmpty()) {
                    sendJsonResponse(exchange, 400, new JSONObject().put("error", "userId parameter is required"));
                    return;
                }

                if (graphService.getUserById(userId) == null) {
                    sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                    return;
                }

                int k = Integer.parseInt(kStr);

                long startTime = System.nanoTime();
                SinglyLinkedList<Recommendation> recs;
                if ("max".equalsIgnoreCase(heapType)) {
                    recs = graphService.getRecommendationsMaxHeap(userId, k);
                } else {
                    recs = graphService.getRecommendationsMinHeap(userId, k);
                }
                long durationNs = System.nanoTime() - startTime;
                double durationMs = durationNs / 1_000_000.0;

                JSONArray recsArr = new JSONArray();
                for (Recommendation rec : recs) {
                    recsArr.put(rec.toJSONObject());
                }

                JSONObject response = new JSONObject();
                response.put("suggestions", recsArr);
                response.put("executionTimeMs", durationMs);
                response.put("heapUsed", heapType);

                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
            }
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
                // We will run a performance benchmark.
                // We generate temporary graphs of increasing sizes: 10, 50, 100, 200, 500, 1000 nodes.
                // For each size, we measure:
                // - Time to generate recommendations using MaxHeap
                // - Time to generate recommendations using MinHeap
                // And compile the results to return as JSON!
                
                int[] sizes = {10, 50, 100, 250, 500, 750, 1000};
                JSONArray results = new JSONArray();

                for (int size : sizes) {
                    // Build a temporary graph of size N
                    Graph tempGraph = new Graph();
                    BinarySearchTree<String, User> tempUsers = new BinarySearchTree<>();

                    // Add nodes
                    for (int i = 0; i < size; i++) {
                        String id = "temp_" + i;
                        User u = new User(id, "User " + i, "user_" + i, "", "");
                        tempUsers.put(id, u);
                        tempGraph.addVertex(id);
                    }

                    // Create random edges with density around 5% to simulate social networks
                    int numEdges = (int) (size * size * 0.03);
                    for (int e = 0; e < numEdges; e++) {
                        int u1 = (int) (Math.random() * size);
                        int u2 = (int) (Math.random() * size);
                        if (u1 != u2) {
                            tempGraph.addEdge("temp_" + u1, "temp_" + u2);
                        }
                    }

                    // Benchmark User "temp_0"
                    String targetId = "temp_0";
                    service.RecommendationEngine engine = new service.RecommendationEngine();
                    int k = 10;

                    // 1. MaxHeap run
                    long tStart = System.nanoTime();
                    engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, k);
                    long tMaxHeap = System.nanoTime() - tStart;

                    // 2. MinHeap run
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

    // 6. Static files handler to serve web frontend assets
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Normalize path to default index.html if pointing to root
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Resolve file location under the web/ directory
            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                // If it doesn't exist, we can fallback to index.html for SPA router support or return 404
                file = new File("web/index.html");
                if (!file.exists()) {
                    String error = "404 Not Found";
                    exchange.sendResponseHeaders(404, error.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(error.getBytes());
                    }
                    return;
                }
            }

            // Determine content-type based on file extension
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
