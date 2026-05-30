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
                // Get friends list of user: /api/friends/{userId}
                if (path.matches("^/api/friends/[a-zA-Z0-9_-]+$")) {
                    String userId = path.substring(path.lastIndexOf("/") + 1);
                    if (!graphService.getGraph().hasVertex(userId)) {
                        AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
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
                    AppServer.sendJsonResponse(exchange, 200, new JSONObject().put("friends", friendsArr));
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("error", "User ID required in path"));
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = AppServer.readRequestBody(exchange);
                JSONObject json = new JSONObject(body);
                String userId1 = json.getString("userId1");
                String userId2 = json.getString("userId2");

                boolean success = graphService.addFriendship(userId1, userId2);
                if (success) {
                    AppServer.sendJsonResponse(exchange, 200, new JSONObject().put("message", "Friendship created successfully"));
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("error", "Could not create friendship. Invalid user IDs or already friends."));
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String body = AppServer.readRequestBody(exchange);
                JSONObject json = new JSONObject(body);
                String userId1 = json.getString("userId1");
                String userId2 = json.getString("userId2");

                boolean success = graphService.removeFriendship(userId1, userId2);
                if (success) {
                    AppServer.sendJsonResponse(exchange, 200, new JSONObject().put("message", "Friendship removed successfully"));
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("error", "Could not remove friendship. Invalid user IDs or not currently friends."));
                }
            } else {
                AppServer.sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method not allowed"));
            }
        } catch (Exception e) {
            AppServer.sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
        }
    }
}
