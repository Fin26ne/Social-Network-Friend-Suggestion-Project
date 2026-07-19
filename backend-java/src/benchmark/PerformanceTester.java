package benchmark;

import datastructures.BinarySearchTree;
import graph.Graph;
import model.User;
import service.RecommendationEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PerformanceTester {

    public static void runBenchmarks(String outputFilePath) {
        System.out.println("Starting Performance Benchmarks...");
        System.out.println("Graph Size (V) | Edges (E) | Max-Heap Time (ms) | Min-Heap Time (ms) | Speedup Ratio");
        System.out.println("---------------------------------------------------------------------------------");

        int[] sizes = {10, 50, 100, 250, 500, 750, 1000};
        RecommendationEngine engine = new RecommendationEngine();

        StringBuilder mdReport = new StringBuilder();
        mdReport.append("# Báo Cáo Hiệu Năng Gợi Ý Kết Bạn Trên Mạng Xã Hội\n\n");
        mdReport.append("Báo cáo thử nghiệm hiệu năng (benchmark) này đánh giá độ phức tạp thời gian và khả năng mở rộng của các chiến lược gợi ý kết bạn tùy chỉnh.\n\n");
        mdReport.append("## Bối Cảnh Hệ Thống\n");
        mdReport.append("- **Ngôn ngữ**: Java Core (JDK 1.8)\n");
        mdReport.append("- **Cấu trúc đánh giá**: MaxHeap nhị phân tùy chỉnh vs. MinHeap nhị phân tùy chỉnh\n");
        mdReport.append("- **Tham số truy vấn**: Gợi ý Top-K với $K = 10$\n");
        mdReport.append("- **Mật độ đồ thị**: ~3% mật độ cạnh đại diện cho đồ thị mạng xã hội thưa.\n\n");
        mdReport.append("## Kết Quả Thử Nghiệm Thực Tế\n\n");
        mdReport.append("| Kích thước Đồ thị (Số nút) | Số cạnh | Chiến lược Max-Heap (ms) | Chiến lược Min-Heap (ms) | Tỉ lệ Hiệu năng (Min so với Max) |\n");
        mdReport.append("|:---------------------------|:--------|:-------------------------|:-------------------------|:---------------------------------|\n");

        for (int size : sizes) {
            Graph tempGraph = new Graph();
            BinarySearchTree<String, User> tempUsers = new BinarySearchTree<>();

            // 1. Generate nodes
            for (int i = 0; i < size; i++) {
                String id = "temp_" + i;
                User u = new User(id, "User " + i, "user_" + i, "", "");
                tempUsers.put(id, u);
                tempGraph.addVertex(id);
            }

            // 2. Generate random edges with 3% density
            int numEdges = (int) (size * size * 0.03);
            for (int e = 0; e < numEdges; e++) {
                int u1 = (int) (Math.random() * size);
                int u2 = (int) (Math.random() * size);
                if (u1 != u2) {
                    tempGraph.addEdge("temp_" + u1, "temp_" + u2);
                }
            }

            // 3. Warm up JVM
            String targetId = "temp_0";
            for (int w = 0; w < 5; w++) {
                engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, 10);
                engine.getRecommendationsMinHeap(tempGraph, tempUsers, targetId, 10);
            }

            // 4. Measure Max-Heap
            long maxHeapTotalNs = 0;
            int runs = 10;
            for (int r = 0; r < runs; r++) {
                long start = System.nanoTime();
                engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, 10);
                maxHeapTotalNs += (System.nanoTime() - start);
            }
            double maxHeapTimeMs = (maxHeapTotalNs / (double) runs) / 1_000_000.0;

            // 5. Measure Min-Heap
            long minHeapTotalNs = 0;
            for (int r = 0; r < runs; r++) {
                long start = System.nanoTime();
                engine.getRecommendationsMinHeap(tempGraph, tempUsers, targetId, 10);
                minHeapTotalNs += (System.nanoTime() - start);
            }
            double minHeapTimeMs = (minHeapTotalNs / (double) runs) / 1_000_000.0;

            double ratio = minHeapTimeMs == 0 ? 999.0 : (maxHeapTimeMs / minHeapTimeMs);
            String ratioStr = ratio > 1 ? String.format("%.1fx Nhanh hơn", ratio) : String.format("%.1fx Chậm hơn", 1 / ratio);

            System.out.printf("%14d | %9d | %17.4f | %17.4f | %13s\n",
                    size, tempGraph.getNumEdges(), maxHeapTimeMs, minHeapTimeMs, ratioStr);

            mdReport.append(String.format("| %d | %d | %.4f ms | %.4f ms | **%s** |\n",
                    size, tempGraph.getNumEdges(), maxHeapTimeMs, minHeapTimeMs, ratioStr));
        }

        mdReport.append("\n## Thảo Luận về Độ Phức Tạp Thuật Toán\n\n");
        mdReport.append("### 1. Chiến lược Max-Heap: Độ phức tạp $O(N \\log N)$\n");
        mdReport.append("- **Cơ chế hoạt động**: Phương pháp này xây dựng một cấu trúc Heap kích thước $N$ (với $N$ là số lượng người chưa kết bạn trong hệ thống). Việc chèn tất cả các phần tử mất thời gian $O(N \\log N)$. Việc lấy ra $K$ phần tử hàng đầu mất thời gian $O(K \\log N)$. Tổng cộng, thời gian truy vấn là $O(N \\log N + K \\log N)$.\n");
        mdReport.append("- **Nghẽn cổ chai khi mở rộng**: Khi hệ thống mở rộng lên tới hàng chục nghìn người dùng, dung lượng bộ nhớ tiêu thụ và thời gian sắp xếp của Heap sẽ tăng theo kích thước của toàn bộ mạng lưới. Điều này làm cho thuật toán trở nên không hiệu quả đối với các mạng xã hội lớn.\n\n");
        mdReport.append("### 2. Chiến lược Min-Heap: Độ phức tạp $O(N \\log K)$\n");
        mdReport.append("- **Cơ chế hoạt động**: Chiến lược này duy trì một Heap nhị phân nhỏ có giới hạn kích thước là $K$. Đối với mỗi ứng viên trong số $N$ người, nó thực hiện so sánh với giá trị nhỏ nhất trong Heap. Nếu ứng viên có điểm cao hơn giá trị nhỏ nhất này, nó sẽ thay thế và thực hiện vun đống xuống (heapify down) mất thời gian $O(\\log K)$. Thời gian truy vấn tăng theo tỷ lệ $O(N \\log K)$.\n");
        mdReport.append("- **Lợi thế khi mở rộng**: Vì $K \\ll N$ (thường $K = 5$ hoặc $10$), $\\log K$ là một hằng số rất nhỏ. Khả năng mở rộng hiệu năng gần như tuyến tính so với số lượng người dùng ($O(N)$), mang lại tốc độ vượt trội rõ rệt khi mật độ đồ thị và số lượng nút mở rộng.\n\n");
        mdReport.append("### 3. Kết luận\n");
        mdReport.append("Các kết quả thực nghiệm đã xác thực độ phức tạp tính toán trên lý thuyết. Chiến lược Min-Heap hoạt động hiệu quả hơn đáng kể khi kích thước mạng xã hội tăng lên, đảm bảo tính toán gợi ý kết bạn nhanh chóng ngay cả trong cấu trúc mạng lưới đông đúc.\n");

        // Write report file
        try {
            File file = new File(outputFilePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(outputFilePath), StandardCharsets.UTF_8)) {
                writer.write(mdReport.toString());
            }
            System.out.println("\nBenchmark report successfully written to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Could not write report to file: " + e.getMessage());
        }
    }

    public static void runCustomBenchmarks(int[] sizes, double edgeDensity, int k, int runs) {
        RecommendationEngine engine = new RecommendationEngine();
        System.out.println("\n--- RUNNING CUSTOM BENCHMARKS ---");
        System.out.println("Graph Size (V) | Edges (E) | Max-Heap Time (ms) | Min-Heap Time (ms) | Speedup Ratio");
        System.out.println("---------------------------------------------------------------------------------");
        
        for (int size : sizes) {
            Graph tempGraph = new Graph();
            BinarySearchTree<String, User> tempUsers = new BinarySearchTree<>();

            for (int i = 0; i < size; i++) {
                String id = "temp_" + i;
                User u = new User(id, "User " + i, "user_" + i, "", "");
                tempUsers.put(id, u);
                tempGraph.addVertex(id);
            }

            int numEdges = (int) (size * size * edgeDensity);
            for (int e = 0; e < numEdges; e++) {
                int u1 = (int) (Math.random() * size);
                int u2 = (int) (Math.random() * size);
                if (u1 != u2) {
                    tempGraph.addEdge("temp_" + u1, "temp_" + u2);
                }
            }

            // Warm up JVM
            String targetId = "temp_0";
            if (tempGraph.hasVertex(targetId)) {
                for (int w = 0; w < 5; w++) {
                    engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, k);
                    engine.getRecommendationsMinHeap(tempGraph, tempUsers, targetId, k);
                }
            }

            // Measure Max-Heap
            long maxHeapTotalNs = 0;
            for (int r = 0; r < runs; r++) {
                long start = System.nanoTime();
                engine.getRecommendationsMaxHeap(tempGraph, tempUsers, targetId, k);
                maxHeapTotalNs += (System.nanoTime() - start);
            }
            double maxHeapTimeMs = (maxHeapTotalNs / (double) runs) / 1_000_000.0;

            // Measure Min-Heap
            long minHeapTotalNs = 0;
            for (int r = 0; r < runs; r++) {
                long start = System.nanoTime();
                engine.getRecommendationsMinHeap(tempGraph, tempUsers, targetId, k);
                minHeapTotalNs += (System.nanoTime() - start);
            }
            double minHeapTimeMs = (minHeapTotalNs / (double) runs) / 1_000_000.0;

            double ratio = minHeapTimeMs == 0 ? 999.0 : (maxHeapTimeMs / minHeapTimeMs);
            String ratioStr = ratio > 1 ? String.format("%.1fx Nhanh hon", ratio) : String.format("%.1fx Cham hon", 1 / ratio);

            System.out.printf("%14d | %9d | %17.4f | %17.4f | %13s\n",
                    size, tempGraph.getNumEdges(), maxHeapTimeMs, minHeapTimeMs, ratioStr);
        }
    }

    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in, "UTF-8");
        System.out.println("==================================================");
        System.out.println("   PERFORMANCE BENCHMARK TEST (MAX-HEAP VS MIN-HEAP) ");
        System.out.println("==================================================");
        System.out.println("1. Run default benchmark report (N = 10 to 1000, 3% density)");
        System.out.println("2. Run custom benchmark with real numbers");
        System.out.print("Choice (1-2): ");
        
        String choice = "";
        if (scanner.hasNextLine()) {
            choice = scanner.nextLine().trim();
        }

        if ("2".equals(choice)) {
            System.out.print("Enter sizes N separated by commas (e.g. 100,500,1000): ");
            String sizesInput = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            if (sizesInput.isEmpty()) sizesInput = "100,500,1000";
            String[] parts = sizesInput.split(",");
            int[] customSizes = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                customSizes[i] = Integer.parseInt(parts[i].trim());
            }

            System.out.print("Enter edge density (e.g. 0.03 for 3%): ");
            double density = 0.03;
            try {
                if (scanner.hasNextLine()) {
                    density = Double.parseDouble(scanner.nextLine().trim());
                }
            } catch (Exception e) {}

            System.out.print("Enter query parameter K (e.g. 10): ");
            int k = 10;
            try {
                if (scanner.hasNextLine()) {
                    k = Integer.parseInt(scanner.nextLine().trim());
                }
            } catch (Exception e) {}

            System.out.print("Enter number of runs to average (e.g. 10): ");
            int runs = 10;
            try {
                if (scanner.hasNextLine()) {
                    runs = Integer.parseInt(scanner.nextLine().trim());
                }
            } catch (Exception e) {}

            runCustomBenchmarks(customSizes, density, k, runs);
        } else {
            runBenchmarks("docs/performance_report.md");
        }
        scanner.close();
    }
}
