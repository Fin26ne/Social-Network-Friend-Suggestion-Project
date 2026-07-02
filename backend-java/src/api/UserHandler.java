package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import service.GraphService;

import java.io.IOException;

public class UserHandler implements HttpHandler {
    private GraphService graphService;

    public UserHandler(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (AppServer.handleOptions(exchange)) return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                // Support both query parameter (?id=X) and path parameter (/api/users/X)
                String userId = null;
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("id=")) {
                    java.util.Map<String, String> queryParams = AppServer.parseQueryParams(query);
                    userId = queryParams.get("id");
                } else if (path.matches("^/api/users/[a-zA-Z0-9_-]+$")) {
                    userId = path.substring(path.lastIndexOf("/") + 1);
                }

                if (userId != null) {
                    User user = graphService.getUserById(userId);
                    if (user == null) {
                        AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("success", false).put("error", "User not found"));
                    } else {
                        JSONObject userJson = user.toJSONObject();
                        org.json.JSONArray friendsArr = new org.json.JSONArray();
                        datastructures.SinglyLinkedList<String> friends = graphService.getFriends(user.getId());
                        if (friends != null) {
                            for (String friendId : friends) {
                                friendsArr.put(friendId);
                            }
                        }
                        userJson.put("friends", friendsArr);
                        
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("data", userJson);
                        AppServer.sendJsonResponse(exchange, 200, response);
                    }
                } else {
                    // Return list of all users
                    JSONArray usersArr = new JSONArray();
                    for (User user : graphService.getAllUsers()) {
                        JSONObject userJson = user.toJSONObject();
                        org.json.JSONArray friendsArr = new org.json.JSONArray();
                        datastructures.SinglyLinkedList<String> friends = graphService.getFriends(user.getId());
                        if (friends != null) {
                            for (String friendId : friends) {
                                friendsArr.put(friendId);
                            }
                        }
                        userJson.put("friends", friendsArr);
                        usersArr.put(userJson);
                    }
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", usersArr);
                    AppServer.sendJsonResponse(exchange, 200, response);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = AppServer.readRequestBody(exchange);
                JSONObject json = new JSONObject(body);
                String name = json.has("displayName") ? json.getString("displayName") : json.optString("name", "Unknown");
                String username = json.has("username") ? json.getString("username") : name.toLowerCase().replaceAll("\\s+", "");
                String bio = json.optString("bio", "");

                try {
                    User created = graphService.addUser(name, username, bio);
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("data", new JSONObject().put("message", "User created successfully").put("user", created.toJSONObject()));
                    AppServer.sendJsonResponse(exchange, 201, response);
                } catch (IllegalArgumentException e) {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", e.getMessage()));
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("^/api/users/[a-zA-Z0-9_-]+$")) {
                    String userId = path.substring(path.lastIndexOf("/") + 1);
                    boolean deleted = graphService.deleteUser(userId);
                    if (deleted) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("data", new JSONObject().put("message", "User deleted successfully"));
                        AppServer.sendJsonResponse(exchange, 200, response);
                    } else {
                        AppServer.sendJsonResponse(exchange, 404, new JSONObject().put("success", false).put("error", "User not found"));
                    }
                } else {
                    AppServer.sendJsonResponse(exchange, 400, new JSONObject().put("success", false).put("error", "User ID required in path"));
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
