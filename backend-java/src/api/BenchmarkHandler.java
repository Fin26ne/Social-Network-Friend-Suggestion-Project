package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class BenchmarkHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (AppServer.handleOptions(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            AppServer.sendJsonResponse(exchange, 405, new JSONObject().put("success", false).put("error", "Method not allowed"));
            return;
        }

        try {
            Map<String, String> queryParams = AppServer.parseQueryParams(exchange.getRequestURI().getQuery());
            String type = queryParams.getOrDefault("type", "all").toLowerCase();
            
            if (!"rq1".equals(type) && !"rq2".equals(type) && !"rq3".equals(type) && !"all".equals(type)) {
                type = "all";
            }

            boolean isCustom = queryParams.containsKey("sizes") || 
                               queryParams.containsKey("avgDegree") || 
                               queryParams.containsKey("edgeDensity") || 
                               queryParams.containsKey("k");

            JSONArray mappedResults = new JSONArray();

            if (!isCustom) {
                // Run benchmarks dynamically on every request to measure real-time performance
                JSONObject results = services.BenchmarkRunner.runAll();
                
                if ("all".equals(type)) {
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", results);
                    AppServer.sendJsonResponse(exchange, 200, response);
                    return;
                }

                JSONArray rawResults = results.getJSONArray(type);

                if ("rq1".equals(type)) {
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("bfsTimeNs", r.getLong("bfsTimeNs"));
                        m.put("dfsTimeNs", r.getLong("dfsTimeNs"));
                        m.put("bfsCandidates", r.getInt("bfsNodesVisited"));
                        m.put("dfsCandidates", r.getInt("dfsNodesVisited"));
                        mappedResults.put(m);
                    }
                } else if ("rq2".equals(type)) {
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("edges", r.getInt("edgeCount"));
                        m.put("listMemKb", r.getDouble("listMemoryKB"));
                        m.put("matrixMemKb", r.getDouble("matrixMemoryKB"));
                        mappedResults.put(m);
                    }
                } else { // rq3
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("maxHeapTimeMs", r.getLong("maxHeapTimeNs") / 1_000_000.0);
                        m.put("minHeapTimeMs", r.getLong("minHeapTimeNs") / 1_000_000.0);
                        mappedResults.put(m);
                    }
                }
            } else {
                // Run custom benchmark dynamically
                if ("rq1".equals(type)) {
                    int[] ns = {100, 500, 1000, 5000, 10000};
                    int avgDegree = 20;
                    String sizesStr = queryParams.get("sizes");
                    if (sizesStr != null && !sizesStr.trim().isEmpty()) {
                        // Tăng giới hạn tối đa của RQ1 lên 100000 (100k) theo yêu cầu giáo viên
                        ns = parseSizes(sizesStr, 100000);
                    }
                    String avgDegreeStr = queryParams.get("avgDegree");
                    if (avgDegreeStr != null) {
                        try {
                            avgDegree = Math.max(2, Math.min(100, Integer.parseInt(avgDegreeStr.trim())));
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    JSONArray rawResults = services.BenchmarkRunner.runRQ1(ns, avgDegree);
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("bfsTimeNs", r.getLong("bfsTimeNs"));
                        m.put("dfsTimeNs", r.getLong("dfsTimeNs"));
                        m.put("bfsCandidates", r.getInt("bfsNodesVisited"));
                        m.put("dfsCandidates", r.getInt("dfsNodesVisited"));
                        mappedResults.put(m);
                    }
                } else if ("rq2".equals(type)) {
                    int[] ns = {100, 500, 1000, 2000, 5000};
                    double edgeDensity = 0.001;
                    String sizesStr = queryParams.get("sizes");
                    if (sizesStr != null && !sizesStr.trim().isEmpty()) {
                        // Tăng giới hạn tối đa của RQ2 lên 15000 (15k) để bảo vệ bộ nhớ ma trận
                        ns = parseSizes(sizesStr, 15000); // lower limit for matrix
                    }
                    String densityStr = queryParams.get("edgeDensity");
                    if (densityStr != null) {
                        try {
                            edgeDensity = Math.max(0.0001, Math.min(0.2, Double.parseDouble(densityStr.trim())));
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    JSONArray rawResults = services.BenchmarkRunner.runRQ2(ns, edgeDensity);
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("edges", r.getInt("edgeCount"));
                        m.put("listMemKb", r.getDouble("listMemoryKB"));
                        m.put("matrixMemKb", r.getDouble("matrixMemoryKB"));
                        mappedResults.put(m);
                    }
                } else { // rq3
                    int[] ns = {100, 1000, 5000, 10000};
                    int k = 5;
                    String sizesStr = queryParams.get("sizes");
                    if (sizesStr != null && !sizesStr.trim().isEmpty()) {
                        // Tăng giới hạn tối đa của RQ3 lên 100000 (100k) theo yêu cầu giáo viên
                        ns = parseSizes(sizesStr, 100000);
                    }
                    String kStr = queryParams.get("k");
                    if (kStr != null) {
                        try {
                            k = Math.max(1, Math.min(100, Integer.parseInt(kStr.trim())));
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    JSONArray rawResults = services.BenchmarkRunner.runRQ3(ns, k);
                    for (int i = 0; i < rawResults.length(); i++) {
                        JSONObject r = rawResults.getJSONObject(i);
                        JSONObject m = new JSONObject();
                        m.put("size", r.getInt("n"));
                        m.put("maxHeapTimeMs", r.getLong("maxHeapTimeNs") / 1_000_000.0);
                        m.put("minHeapTimeMs", r.getLong("minHeapTimeNs") / 1_000_000.0);
                        mappedResults.put(m);
                    }
                }
            }

            JSONObject responseData = new JSONObject();
            responseData.put("benchmarkResults", mappedResults);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", responseData);

            AppServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("success", false).put("error", "Benchmark failed: " + e.getMessage()));
        }
    }

    private int[] parseSizes(String sizesStr, int maxVal) {
        try {
            String[] parts = sizesStr.split(",");
            int limit = Math.min(8, parts.length); // Max 8 data points
            int[] ns = new int[limit];
            for (int i = 0; i < limit; i++) {
                ns[i] = Math.max(10, Math.min(maxVal, Integer.parseInt(parts[i].trim())));
            }
            java.util.Arrays.sort(ns);
            return ns;
        } catch (Exception e) {
            if (maxVal <= 15000) {
                return new int[]{100, 500, 1000, 2000, 5000};
            }
            return new int[]{100, 500, 1000, 5000, 10000};
        }
    }
}
