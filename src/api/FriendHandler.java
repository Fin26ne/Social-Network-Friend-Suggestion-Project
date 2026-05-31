package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import datastructures.SinglyLinkedList;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import service.GraphService;

import java.io.IOException;

public class FriendHandler implements HttpHandler {
    private GraphService graphService;

    public FriendHandler(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (AppServer.handleOptions(exchange)) return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                // Check if requesting mutual friends: /api/friends/mutual or /api/mutual
                if (path.equals("/api/friends/mutual") || path.equals("/api/mutual")) {
                    java.util.Map<String, String> queryParams = AppServer.parseQueryParams(exchange.getRequestURI().getQuery());
                    String userId1 = queryParams.get("userId1");
                    String userId2 = queryParams.get("userId2");
                    if (userId1 == null || userId2 == null || userId1.isEmpty() || userId2.isEmpty()) {
                        AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "Both userId1 and userId2 query parameters are required"));
                        return;
                    }
                    if (graphService.getUserById(userId1) == null || graphService.getUserById(userId2) == null) {
                        AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("success", false).put("error", "One or both users not found"));
                        return;
                    }

                    JSONArray mutualArr = new JSONArray();
                    SinglyLinkedList<String> friends1 = graphService.getFriends(userId1);
                    SinglyLinkedList<String> friends2 = graphService.getFriends(userId2);
                    for (String fId : friends1) {
                        if (friends2.contains(fId)) {
                            User friend = graphService.getUserById(fId);
                            if (friend != null) {
                                mutualArr.put(friend.toJSONObject());
                            }
                        }
                    }
                    
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", new JSONObject().put("mutualFriends", mutualArr));
                    AppServer.sendJsonResponse(exchange, 200, response);
                } else {
                    // Get friends list: Support both query parameter (?userId=X) and path parameter (/api/friends/{userId})
                    String userId = null;
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("userId=")) {
                        java.util.Map<String, String> queryParams = AppServer.parseQueryParams(query);
                        userId = queryParams.get("userId");
                    } else if (path.matches("^/api/friends/[a-zA-Z0-9_-]+$")) {
                        userId = path.substring(path.lastIndexOf("/") + 1);
                    }

                    if (userId == null || userId.isEmpty()) {
                        AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "User ID required in query parameter or path"));
                        return;
                    }

                    if (!graphService.getGraph().hasVertex(userId)) {
                        AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("success", false).put("error", "User not found"));
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
                    
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", new JSONObject().put("friends", friendsArr));
                    AppServer.sendJsonResponse(exchange, 200, response);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = AppServer.readRequestBody(exchange);
                JSONObject json = new JSONObject(body);
                String userId1 = json.getString("userId1");
                String userId2 = json.getString("userId2");

                boolean success = graphService.addFriendship(userId1, userId2);
                if (success) {
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", new JSONObject().put("message", "Friendship created successfully"));
                    AppServer.sendJsonResponse(exchange, 200, response);
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "Could not create friendship. Invalid user IDs or already friends."));
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String body = AppServer.readRequestBody(exchange);
                JSONObject json = new JSONObject(body);
                String userId1 = json.getString("userId1");
                String userId2 = json.getString("userId2");

                boolean success = graphService.removeFriendship(userId1, userId2);
                if (success) {
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", new JSONObject().put("message", "Friendship removed successfully"));
                    AppServer.sendJsonResponse(exchange, 200, response);
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "Could not remove friendship. Invalid user IDs or not currently friends."));
                }
            } else {
                AppServer.sendJsonResponse(exchange, 405, new JSONObject().put("success", false).put("error", "Method not allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("success", false).put("error", "Internal server error: " + e.getMessage()));
        }
    }
}
