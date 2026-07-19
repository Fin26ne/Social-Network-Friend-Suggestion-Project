package console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class DataGenerator {
    private static final int MAX_USERS = 4039;
    private static final String DATASET_URL = "https://snap.stanford.edu/data/facebook_combined.txt.gz";
    
    // Arrays for generating fake Vietnamese names
    private static final String[] FIRST_NAMES = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"};
    private static final String[] MIDDLE_NAMES = {"Văn", "Thị", "Hồng", "Minh", "Quốc", "Gia", "Thanh", "Hoàng", "Thành", "Bảo", "Lan", "Ngọc", "Tuấn", "Phương"};
    private static final String[] LAST_NAMES = {"An", "Bình", "Cường", "Dũng", "Em", "Phong", "Giang", "Hương", "Hải", "Khánh", "Lâm", "Minh", "Nga", "Oanh", "Phát", "Quang", "Tài", "Sơn", "Tùng", "Uyên", "Vinh", "Vy", "Yến"};
    private static final String[] BIOS = {
        "Yêu lập trình Java", "Đam mê Data Structures", "Thích đọc sách", "Thích đi du lịch", "Game thủ",
        "Sinh viên FPT", "Thích nghe nhạc lofi", "Chuyên gia ăn uống", "Nhiếp ảnh gia nghiệp dư",
        "Thích công nghệ", "Yêu chó mèo", "Yêu thiên nhiên", "Fan cứng của thuật toán BFS", "Học hỏi mỗi ngày"
    };

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STANFORD SNAP DATASET GENERATOR (JAVA VERSION)");
        System.out.println("==================================================");
        
        String currentDir = System.getProperty("user.dir");
        String dataDirPath = currentDir + File.separator + "backend-java" + File.separator + "data";
        
        // Handle case where we might be running directly inside backend-java folder
        if (currentDir.endsWith("backend-java")) {
            dataDirPath = currentDir + File.separator + "data";
        }

        File dataDir = new File(dataDirPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        System.out.println("Downloading dataset from Stanford SNAP...");
        
        Set<Integer> userSet = new HashSet<>();
        List<int[]> edges = new ArrayList<>();

        try {
            URL url = new URL(DATASET_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            try (InputStream in = connection.getInputStream();
                 GZIPInputStream gzip = new GZIPInputStream(in);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzip))) {
                
                String line;
                System.out.println("Parsing edges...");
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length != 2) continue;
                    
                    int u = Integer.parseInt(parts[0]);
                    int v = Integer.parseInt(parts[1]);
                    
                    // Filter down to MAX_USERS to prevent RAM overload
                    if (u < MAX_USERS && v < MAX_USERS) {
                        edges.add(new int[]{u, v});
                        userSet.add(u);
                        userSet.add(v);
                    }
                }
            }
            
            System.out.println("-> Extracted " + userSet.size() + " users and " + edges.size() + " edges.");
            
            System.out.println("Generating JSON files...");
            Random rand = new Random();
            
            // Build users.json string manually
            StringBuilder usersJson = new StringBuilder();
            usersJson.append("[\n");
            int count = 0;
            for (Integer u : userSet) {
                String fname = FIRST_NAMES[rand.nextInt(FIRST_NAMES.length)];
                String mname = MIDDLE_NAMES[rand.nextInt(MIDDLE_NAMES.length)];
                String lname = LAST_NAMES[rand.nextInt(LAST_NAMES.length)];
                
                String name = fname + " " + mname + " " + lname;
                String username = lname.toLowerCase() + "." + fname.toLowerCase() + u;
                String bio = BIOS[rand.nextInt(BIOS.length)];
                
                int age = 18 + rand.nextInt(43); // 18 to 60
                usersJson.append("    {\n");
                usersJson.append("        \"id\": \"u").append(u).append("\",\n");
                usersJson.append("        \"name\": \"").append(name).append("\",\n");
                usersJson.append("        \"bio\": \"").append(bio).append("\",\n");
                usersJson.append("        \"username\": \"").append(username).append("\",\n");
                usersJson.append("        \"age\": ").append(age).append("\n");
                usersJson.append("    }");
                if (count < userSet.size() - 1) {
                    usersJson.append(",");
                }
                usersJson.append("\n");
                count++;
            }
            usersJson.append("]\n");
            
            // Build friendships.json string manually
            StringBuilder friendsJson = new StringBuilder();
            friendsJson.append("[\n");
            for (int i = 0; i < edges.size(); i++) {
                int[] edge = edges.get(i);
                friendsJson.append("    {\n");
                friendsJson.append("        \"userId1\": \"u").append(edge[0]).append("\",\n");
                friendsJson.append("        \"userId2\": \"u").append(edge[1]).append("\"\n");
                friendsJson.append("    }");
                if (i < edges.size() - 1) {
                    friendsJson.append(",");
                }
                friendsJson.append("\n");
            }
            friendsJson.append("]\n");

            // Write to files
            File usersFile = new File(dataDir, "users.json");
            File friendsFile = new File(dataDir, "friendships.json");
            
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(usersFile), "UTF-8")) {
                writer.write(usersJson.toString());
            }
            
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(friendsFile), "UTF-8")) {
                writer.write(friendsJson.toString());
            }
            
            System.out.println("DONE! Successfully generated data to: " + dataDir.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error generating data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
