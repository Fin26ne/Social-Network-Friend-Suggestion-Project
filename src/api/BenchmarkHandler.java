package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import datastructures.BinarySearchTree;
import graph.Graph;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class BenchmarkHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (AppServer.handleOptions(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            AppServer.sendJsonResponse(exchange, 405, new JSONObject().put("success", false).put("error", "Method not allowed"));
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

            JSONObject responseData = new JSONObject();
            responseData.put("benchmarkResults", results);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", responseData);

            AppServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("success", false).put("error", "Benchmark failed: " + e.getMessage()));
        }
    }
}
