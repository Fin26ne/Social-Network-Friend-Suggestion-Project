package data;

import datastructures.BinarySearchTree;
import graph.Graph;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStore {
    private String filePath;

    public DataStore(String filePath) {
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    public boolean exists() {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    // Save Graph and User BST data to JSON
    public synchronized void save(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        JSONObject root = new JSONObject();
        
        // Serialize Users
        JSONArray usersJson = new JSONArray();
        for (User user : userBst.inOrderValues()) {
            usersJson.put(user.toJSONObject());
        }
        root.put("users", usersJson);

        // Serialize Friendships
        JSONArray friendshipsJson = new JSONArray();
        BinarySearchTree<String, Boolean> visitedEdges = new BinarySearchTree<>();
        
        for (String uId : graph.getVertices()) {
            for (String vId : graph.getNeighbors(uId)) {
                // To avoid storing edge (u, v) and (v, u) twice
                String edgeKey1 = uId + "-" + vId;
                String edgeKey2 = vId + "-" + uId;
                if (!visitedEdges.contains(edgeKey1) && !visitedEdges.contains(edgeKey2)) {
                    JSONObject edge = new JSONObject();
                    edge.put("userId1", uId);
                    edge.put("userId2", vId);
                    friendshipsJson.put(edge);
                    visitedEdges.put(edgeKey1, true);
                }
            }
        }
        root.put("friendships", friendshipsJson);

        // Write to file
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write(root.toString(4));
        }
    }

    // Load Graph and User BST data from JSON
    public synchronized void load(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        if (!exists()) {
            initializeSeedData(graph, userBst);
            return;
        }

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String content = new String(encoded, StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(content);

        // Load Users
        JSONArray usersJson = root.getJSONArray("users");
        for (int i = 0; i < usersJson.length(); i++) {
            User user = User.fromJSONObject(usersJson.getJSONObject(i));
            userBst.put(user.getId(), user);
            graph.addVertex(user.getId());
        }

        // Load Friendships
        JSONArray friendshipsJson = root.getJSONArray("friendships");
        for (int i = 0; i < friendshipsJson.length(); i++) {
            JSONObject friendship = friendshipsJson.getJSONObject(i);
            String uId1 = friendship.getString("userId1");
            String uId2 = friendship.getString("userId2");
            graph.addEdge(uId1, uId2);
        }
    }

    // Generate seed sample data
    public void initializeSeedData(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        User[] seedUsers = {
            new User("u1", "Alice Johnson", "alice", "Loves hiking and coding in Java", "2026-01-01"),
            new User("u2", "Bob Smith", "bob", "Coffee enthusiast & data structures fan", "2026-01-05"),
            new User("u3", "Charlie Davis", "charlie", "Algorithms student at FPT", "2026-01-10"),
            new User("u4", "David Wilson", "david", "Web designer & CSS wizard", "2026-01-12"),
            new User("u5", "Eve Martinez", "eve", "Cybersecurity analyst & gamer", "2026-01-15"),
            new User("u6", "Frank Miller", "frank", "Software engineer & tech geek", "2026-01-20"),
            new User("u7", "Grace Taylor", "grace", "AI researcher & dog lover", "2026-01-22"),
            new User("u8", "Heidi Anderson", "heidi", "Photographer & travel blogger", "2026-01-25"),
            new User("u9", "Ivan Thomas", "ivan", "Math professor & chess enthusiast", "2026-01-28"),
            new User("u10", "Judy White", "judy", "UX writer & book explorer", "2026-02-01")
        };

        for (User user : seedUsers) {
            userBst.put(user.getId(), user);
            graph.addVertex(user.getId());
        }

        // Add friendships
        String[][] friendships = {
            {"u1", "u2"}, {"u1", "u3"}, {"u1", "u4"},
            {"u2", "u3"}, {"u2", "u6"}, {"u2", "u8"},
            {"u3", "u4"}, {"u3", "u5"}, {"u3", "u9"},
            {"u4", "u7"}, {"u4", "u10"},
            {"u5", "u6"}, {"u5", "u7"},
            {"u6", "u7"}, {"u6", "u8"},
            {"u7", "u9"}, {"u7", "u10"},
            {"u8", "u10"},
            {"u9", "u10"}
        };

        for (String[] edge : friendships) {
            graph.addEdge(edge[0], edge[1]);
        }

        // Save immediately
        save(graph, userBst);
    }
}
