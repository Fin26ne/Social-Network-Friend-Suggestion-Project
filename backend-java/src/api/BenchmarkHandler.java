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
            String type = queryParams.getOrDefault("type", "rq2").toLowerCase();
            
            JSONArray mappedResults = new JSONArray();

            if ("rq2".equals(type)) {
                int[] ns = {100, 500, 1000, 2000, 5000};
                double edgeDensity = 0.001;
                String sizesStr = queryParams.get("sizes");
                if (sizesStr != null && !sizesStr.trim().isEmpty()) {
                    ns = parseSizes(sizesStr, Integer.MAX_VALUE);
                }
                String densityStr = queryParams.get("edgeDensity");
                if (densityStr != null) {
                    try {
                        edgeDensity = Math.max(0.0001, Math.min(0.2, Double.parseDouble(densityStr.trim())));
                    } catch (NumberFormatException ignored) {}
                }
                String mode = queryParams.getOrDefault("mode", "theory");
                
                JSONArray rawResults = services.BenchmarkRunner.runRQ2(ns, edgeDensity, mode);
                for (int i = 0; i < rawResults.length(); i++) {
                    JSONObject r = rawResults.getJSONObject(i);
                    JSONObject m = new JSONObject();
                    m.put("size", r.getInt("n"));
                    m.put("edges", r.getInt("edgeCount"));
                    m.put("listMemKb", r.getDouble("listMemoryKB"));
                    m.put("matrixMemKb", r.getDouble("matrixMemoryKB"));
                    m.put("actualListMemKb", r.optDouble("actualListMemKb", -1));
                    m.put("actualMatrixMemKb", r.optDouble("actualMatrixMemKb", -1));
                    mappedResults.put(m);
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
            
            if (parts.length == 1) {
                int maxN = Math.max(10, Math.min(maxVal, Integer.parseInt(parts[0].trim())));
                if (maxN >= 50) {
                    int step = maxN / 5;
                    return new int[]{step, step * 2, step * 3, step * 4, maxN};
                } else {
                    return new int[]{maxN};
                }
            }

            int limit = Math.min(8, parts.length); // Max 8 data points
            int[] ns = new int[limit];
            for (int i = 0; i < limit; i++) {
                ns[i] = Math.max(10, Math.min(maxVal, Integer.parseInt(parts[i].trim())));
            }
            java.util.Arrays.sort(ns);
            return ns;
        } catch (Exception e) {
            if (maxVal <= 10000000) {
                return new int[]{100, 500, 1000, 2000, 5000};
            }
            return new int[]{100, 500, 1000, 5000, 10000};
        }
    }
}
