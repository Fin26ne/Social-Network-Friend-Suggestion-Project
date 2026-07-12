package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import datastructures.BinarySearchTree;
import graph.Graph;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import service.GraphService;

import java.io.IOException;

public class NetworkHandler implements HttpHandler {
    private GraphService graphService;

    public NetworkHandler(GraphService graphService) {
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
            String query = exchange.getRequestURI().getQuery();
            String userId = null;
            if (query != null && query.contains("userId=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("userId=")) {
                        userId = param.split("=")[1];
                        break;
                    }
                }
            }

            Graph graph = graphService.getGraph();
            datastructures.SinglyLinkedList<String> targetVertices = null;
            
            if (userId != null && graph.hasVertex(userId)) {
                targetVertices = graph.getLocalSubgraphVertices(userId);
            } else {
                // Limit to 500 nodes if no userId is provided to prevent browser crashes
                targetVertices = new datastructures.SinglyLinkedList<>();
                int count = 0;
                for (String v : graph.getVertices()) {
                    targetVertices.add(v);
                    count++;
                    if (count >= 500) break;
                }
            }

            JSONArray nodesJson = new JSONArray();
            for (String vId : targetVertices) {
                User user = graphService.getUserById(vId);
                if (user != null) {
                    nodesJson.put(user.toJSONObject());
                }
            }

            JSONArray linksJson = new JSONArray();
            BinarySearchTree<String, Boolean> visitedEdges = new BinarySearchTree<>();
            BinarySearchTree<String, Boolean> targetSet = new BinarySearchTree<>();
            for (String vId : targetVertices) {
                targetSet.put(vId, true);
            }

            for (String uId : targetVertices) {
                for (String vId : graph.getNeighbors(uId)) {
                    if (targetSet.contains(vId)) {
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
            }

            JSONObject graphData = new JSONObject();
            graphData.put("nodes", nodesJson);
            graphData.put("links", linksJson);
            graphData.put("edges", linksJson);

            // Add adjacency matrix info as well for the research dashboard
            // Only generate matrix for the subgraph to avoid massive 4000x4000 payload
            int n = targetVertices.size();
            String[] verticesArray = new String[n];
            BinarySearchTree<String, Integer> idToIndex = new BinarySearchTree<>();
            int idx = 0;
            for (String vertex : targetVertices) {
                verticesArray[idx] = vertex;
                idToIndex.put(vertex, idx);
                idx++;
            }

            boolean[][] matrix = new boolean[n][n];
            for (int i = 0; i < n; i++) {
                String u = verticesArray[i];
                datastructures.SinglyLinkedList<String> neighbors = graph.getNeighbors(u);
                for (String v : neighbors) {
                    Integer j = idToIndex.get(v);
                    if (j != null) {
                        matrix[i][j] = true;
                    }
                }
            }

            JSONObject matrixJson = new JSONObject();
            JSONArray verticesArr = new JSONArray();
            for (String vertex : verticesArray) {
                verticesArr.put(vertex);
            }
            matrixJson.put("vertices", verticesArr);

            JSONArray matrixRows = new JSONArray();
            for (int i = 0; i < matrix.length; i++) {
                JSONArray row = new JSONArray();
                for (int j = 0; j < matrix[i].length; j++) {
                    row.put(matrix[i][j]);
                }
                matrixRows.put(row);
            }
            matrixJson.put("matrix", matrixRows);
            graphData.put("adjacencyMatrix", matrixJson);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", graphData);

            AppServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("success", false).put("error", "Internal server error: " + e.getMessage()));
        }
    }
}
