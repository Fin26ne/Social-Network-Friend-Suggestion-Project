package services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import datastructures.BinarySearchTree;
import datastructures.MaxHeap;
import datastructures.MinHeap;
import datastructures.SinglyLinkedList;
import graph.Graph;
import model.User;
import model.Recommendation;

public class BenchmarkRunner {
    
    static {
        System.out.println("==================================================");
        System.out.println(" Warming up JVM JIT compiler for Benchmarks... ");
        System.out.println("==================================================");
        try {
            Graph g = new Graph();
            for (int i = 0; i < 100; i++) g.addVertex("u" + i);
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 10; j++) {
                    g.addEdge("u" + i, "u" + (int)(Math.random() * 100));
                }
            }
            for (int i = 0; i < 2000; i++) {
                g.bfs("u0");
                g.dfs("u0");
            }
            
            Recommendation[] recs = new Recommendation[100];
            for (int i = 0; i < 100; i++) {
                recs[i] = new Recommendation(new User("u" + i, "U", "u", "", ""), 1, Math.random());
            }
            for (int i = 0; i < 1000; i++) {
                MaxHeap<Recommendation> maxHeap = new MaxHeap<>();
                MinHeap<Recommendation> minHeap = new MinHeap<>();
                for (int j = 0; j < 100; j++) {
                    maxHeap.insert(recs[j]);
                    if (minHeap.size() < 5) {
                        minHeap.insert(recs[j]);
                    } else {
                        if (recs[j].compareTo(minHeap.peekMin()) > 0) {
                            minHeap.extractMin();
                            minHeap.insert(recs[j]);
                        }
                    }
                }
                for (int j = 0; j < 5; j++) {
                    maxHeap.extractMax();
                }
                while (!minHeap.isEmpty()) {
                    minHeap.extractMin();
                }
            }
            System.out.println("JVM JIT compiler Warmup complete!");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("Warmup failed: " + e.getMessage());
        }
    }

    public static JSONArray runRQ2(int[] ns, double edgeDensity, String mode) {
        JSONArray arr = new JSONArray();
        boolean doActual = "actual".equalsIgnoreCase(mode);

        for (int n : ns) {
            long numVertices = n;
            long numEdges = (long) (n * (long)n * edgeDensity);

            // Adjacency List: numVertices * 80 + numEdges * 48
            long listMemoryBytes = numVertices * 80 + numEdges * 48;
            
            // Adjacency Matrix: N^2 + 32N + 24
            long matrixMemoryBytes = numVertices * numVertices + numVertices * 32 + 24;
            
            double actualListKB = -1;
            double actualMatrixKB = -1;
            
            if (doActual && n <= 500000) {
                try {
                    // Measure Matrix
                    System.out.println("[DO THUC TE] Dang don rac (GC) truoc khi do Matrix " + n + "x" + n + "...");
                    System.gc();
                    Thread.sleep(10);
                    long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    System.out.println("[DO THUC TE] Dang ep Java cap phat mang 2 chieu " + n + "x" + n + " (Co the gay lag may)...");
                    boolean[][] matrix = new boolean[n][n];
                    long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    
                    actualMatrixKB = Math.max(0, (mem2 - mem1) / 1024.0);
                    System.out.println("[DO THUC TE] Matrix " + n + "x" + n + " ton: " + actualMatrixKB + " KB. Dang giai phong RAM...");
                    if (n > 0) matrix[0][0] = true;
                    matrix = null;
                    
                    // Measure List
                    System.out.println("[DO THUC TE] Dang don rac (GC) truoc khi do List...");
                    System.gc();
                    Thread.sleep(10);
                    long mem3 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    System.out.println("[DO THUC TE] Dang ep Java cap phat Do thi: " + n + " Nodes va " + numEdges + " Edges (Se rat lag neu so luong lon)...");
                    Graph g = new Graph();
                    for (int i = 0; i < n; i++) g.addVertex("u" + i);
                    int edgeCount = (int) numEdges;
                    for (int i = 0; i < edgeCount; i++) {
                        g.addEdge("u" + (int)(Math.random() * n), "u" + (int)(Math.random() * n));
                    }
                    long mem4 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    
                    actualListKB = Math.max(0, (mem4 - mem3) / 1024.0);
                    System.out.println("[DO THUC TE] List ton: " + actualListKB + " KB. Dang giai phong RAM...");
                    g.hasVertex("u0");
                    g = null;
                    System.gc();
                } catch (OutOfMemoryError e) {
                    System.out.println("[CANH BAO TRAN RAM] Java da vang OutOfMemoryError o N = " + n + ". RAM cua ban da can kiet!");
                    System.gc();
                    actualListKB = -1;
                    actualMatrixKB = -1;
                } catch (Exception e) {}
            }
            
            JSONObject obj = new JSONObject();
            obj.put("n", numVertices);
            obj.put("edgeDensity", edgeDensity);
            obj.put("edgeCount", numEdges);
            
            obj.put("listMemoryBytes", listMemoryBytes);
            obj.put("matrixMemoryBytes", matrixMemoryBytes);
            obj.put("listMemoryKB", listMemoryBytes / 1024.0);
            obj.put("matrixMemoryKB", matrixMemoryBytes / 1024.0);
            
            obj.put("actualListMemKb", actualListKB);
            obj.put("actualMatrixMemKb", actualMatrixKB);
            
            double savings = (double) (matrixMemoryBytes - listMemoryBytes) / matrixMemoryBytes * 100.0;
            obj.put("memorySavedPercent", savings);
            obj.put("winner", listMemoryBytes < matrixMemoryBytes ? "List" : "Matrix");
            
            arr.put(obj);
        }
        return arr;
    }


    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("==================================================");
        System.out.println("   BAT DAU CHAY BENCHMARK DOC LAP TREN CONSOLE ");
        System.out.println("==================================================");
        
        System.out.print("Nhap cac kich thuoc N (VD: 1000,5000,10000): ");
        String sizesInput = scanner.nextLine().trim();
        if (sizesInput.isEmpty()) sizesInput = "1000,5000,10000";
        String[] parts = sizesInput.split(",");
        int[] ns = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ns[i] = Integer.parseInt(parts[i].trim());
        }
        
        System.out.print("Nhap mat do canh (Density - VD: 0.001): ");
        double edgeDensity = 0.001;
        try {
            String den = scanner.nextLine().trim();
            if (!den.isEmpty()) edgeDensity = Double.parseDouble(den);
        } catch (Exception e) {}
        
        System.out.print("Chon che do do luong bo nho RQ2 (1 = Ly thuyet, 2 = Thuc te): ");
        String modeInput = scanner.nextLine().trim();
        String mode = "2".equals(modeInput) ? "actual" : "theory";

        System.out.println("\n--- DANG CHAY RQ2: SO SANH MEMORY (CHE DO: " + mode.toUpperCase() + ") ---");
        JSONArray rq2Results = runRQ2(ns, edgeDensity, mode);
        
        System.out.println("\n--- KET QUA RQ2 ---");
        System.out.printf("%-10s | %-20s | %-20s | %-20s | %-20s\n", "Node (N)", "List (Ly thuyet)", "Matrix (Ly thuyet)", "List (Thuc te)", "Matrix (Thuc te)");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < rq2Results.length(); i++) {
            JSONObject res = rq2Results.getJSONObject(i);
            
            String actualList = res.optDouble("actualListMemKb", -1) >= 0 ? 
                                String.format("%.1f KB", res.getDouble("actualListMemKb")) : "[Qua tai RAM]";
            String actualMatrix = res.optDouble("actualMatrixMemKb", -1) >= 0 ? 
                                String.format("%.1f KB", res.getDouble("actualMatrixMemKb")) : "[Qua tai RAM]";
                                
            System.out.printf("%-10d | %-20.1f | %-20.1f | %-20s | %-20s\n", 
                res.getInt("n"), 
                res.getDouble("listMemoryKB"), 
                res.getDouble("matrixMemoryKB"),
                actualList,
                actualMatrix);
        }
        
        System.out.println("\n[HOAN TAT BENCHMARK]");
        scanner.close();
    }
}
