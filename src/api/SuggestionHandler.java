package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import datastructures.SinglyLinkedList;
import model.Recommendation;
import org.json.JSONArray;
import org.json.JSONObject;
import service.GraphService;

import java.io.IOException;
import java.util.Map;

public class SuggestionHandler implements HttpHandler {
    private GraphService graphService;

    public SuggestionHandler(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (AppServer.handleOptions(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            AppServer.sendJsonResponse(exchange, 405, new JSONObject().put("success", false).put("error", "Method not allowed"));
            return;
        }

        try {
            Map<String, String> queryParams = AppServer.parseQueryParams(exchange.getRequestURI().getQuery());
            String userId = queryParams.get("userId");
            String kStr = queryParams.getOrDefault("k", "5");
            String heapType = queryParams.getOrDefault("heapType", "min"); // "min" or "max"

            if (userId == null || userId.isEmpty()) {
                AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "userId parameter is required"));
                return;
            }

            if (graphService.getUserById(userId) == null) {
                AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("success", false).put("error", "User not found"));
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

            JSONObject responseData = new JSONObject();
            responseData.put("suggestions", recsArr);
            responseData.put("executionTimeMs", durationMs);
            responseData.put("heapUsed", heapType);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", responseData);

            AppServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("success", false).put("error", "Internal server error: " + e.getMessage()));
        }
    }
}
