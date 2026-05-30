package services;

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
    private String directoryPath;
    private String usersFilePath;
    private String friendshipsFilePath;

    public DataStore(String directoryPath) {
        this.directoryPath = directoryPath;
        this.usersFilePath = directoryPath + (directoryPath.endsWith("/") || directoryPath.endsWith("\\") ? "" : "/") + "users.json";
        this.friendshipsFilePath = directoryPath + (directoryPath.endsWith("/") || directoryPath.endsWith("\\") ? "" : "/") + "friendships.json";
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public boolean exists() {
        File usersFile = new File(usersFilePath);
        File friendshipsFile = new File(friendshipsFilePath);
        return usersFile.exists() && usersFile.length() > 0 && friendshipsFile.exists() && friendshipsFile.length() > 0;
    }

    // Save Graph and User BST data to separate JSON files
    public synchronized void save(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        ensureDirectoryExists();

        // Serialize Users
        JSONArray usersJson = new JSONArray();
        for (User user : userBst.inOrderValues()) {
            usersJson.put(user.toJSONObject());
        }

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

        // Write users to file
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(usersFilePath), StandardCharsets.UTF_8)) {
            writer.write(usersJson.toString(4));
        }

        // Write friendships to file
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(friendshipsFilePath), StandardCharsets.UTF_8)) {
            writer.write(friendshipsJson.toString(4));
        }
    }

    // Load Graph and User BST data from separate JSON files
    public synchronized void load(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        if (!exists()) {
            initializeSeedData(graph, userBst);
            return;
        }

        // Load Users
        byte[] usersBytes = Files.readAllBytes(Paths.get(usersFilePath));
        String usersContent = new String(usersBytes, StandardCharsets.UTF_8);
        JSONArray usersJson = new JSONArray(usersContent);
        for (int i = 0; i < usersJson.length(); i++) {
            User user = User.fromJSONObject(usersJson.getJSONObject(i));
            userBst.put(user.getId(), user);
            graph.addVertex(user.getId());
        }

        // Load Friendships
        byte[] friendshipsBytes = Files.readAllBytes(Paths.get(friendshipsFilePath));
        String friendshipsContent = new String(friendshipsBytes, StandardCharsets.UTF_8);
        JSONArray friendshipsJson = new JSONArray(friendshipsContent);
        for (int i = 0; i < friendshipsJson.length(); i++) {
            JSONObject friendship = friendshipsJson.getJSONObject(i);
            String uId1 = friendship.getString("userId1");
            String uId2 = friendship.getString("userId2");
            graph.addEdge(uId1, uId2);
        }
    }

    // Generate seed sample data with 12 Vietnamese users and at least 20 relationships
    public void initializeSeedData(Graph graph, BinarySearchTree<String, User> userBst) throws IOException {
        User[] seedUsers = {
                new User("u1", "Nguyễn Văn An", "an.nguyen", "Yêu lập trình Java và cấu trúc dữ liệu", "2026-01-01"),
                new User("u2", "Trần Thị Bình", "binh.tran", "Đam mê cà phê và thuật toán đồ thị", "2026-01-05"),
                new User("u3", "Lê Hoàng Cường", "cuong.le", "Sinh viên CNTT tại Đại học FPT", "2026-01-10"),
                new User("u4", "Phạm Hồng Dung", "dung.pham", "Nhà thiết kế giao diện & CSS master", "2026-01-12"),
                new User("u5", "Hoàng Văn Em", "em.hoang", "Chuyên gia an ninh mạng & game thủ", "2026-01-15"),
                new User("u6", "Ngô Quốc Phong", "phong.ngo", "Kỹ sư phần mềm yêu thích công nghệ mới", "2026-01-20"),
                new User("u7", "Vũ Thị Giang", "giang.vu", "Nghiên cứu sĩ AI và yêu thích thú cưng", "2026-01-22"),
                new User("u8", "Đỗ Minh Hương", "huong.do", "Nhiếp ảnh gia nghiệp dư & đam mê du lịch", "2026-01-25"),
                new User("u9", "Bùi Tiến Hải", "hai.bui", "Thầy giáo toán & kỳ thủ cờ vua", "2026-01-28"),
                new User("u10", "Đặng Lan Khánh", "khanh.dang", "Người viết nội dung UX & đọc sách", "2026-02-01"),
                new User("u11", "Trịnh Văn Lâm", "lam.trinh", "Lập trình viên đam mê khám phá thiên nhiên", "2026-02-05"),
                new User("u12", "Mai Thu Minh", "minh.mai", "Nhà phân tích dữ liệu & ẩm thực", "2026-02-10")
        };

        for (User user : seedUsers) {
            userBst.put(user.getId(), user);
            graph.addVertex(user.getId());
        }

        // Add friendships (24 relationships total, which is >= 20)
        String[][] friendships = {
                { "u1", "u2" }, { "u1", "u3" }, { "u1", "u4" },
                { "u2", "u3" }, { "u2", "u6" }, { "u2", "u8" },
                { "u3", "u4" }, { "u3", "u5" }, { "u3", "u9" },
                { "u4", "u7" }, { "u4", "u10" },
                { "u5", "u6" }, { "u5", "u7" },
                { "u6", "u7" }, { "u6", "u8" },
                { "u7", "u9" }, { "u7", "u10" },
                { "u8", "u10" },
                { "u9", "u10" },
                // Additional relationships to cover u11 and u12 and ensure >= 20
                { "u11", "u12" },
                { "u1", "u11" },
                { "u2", "u12" },
                { "u11", "u3" },
                { "u12", "u4" }
        };

        for (String[] edge : friendships) {
            graph.addEdge(edge[0], edge[1]);
        }

        // Save immediately
        save(graph, userBst);
    }
}
