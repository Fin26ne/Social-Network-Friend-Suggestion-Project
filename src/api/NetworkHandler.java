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
