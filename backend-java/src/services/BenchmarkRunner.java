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
    
    public static JSONObject runAll() {
        JSONObject results = new JSONObject();
        
        // Add metadata to prove ownership and reproducibility
        JSONObject metadata = new JSONObject();
        metadata.put("generatedAt", java.time.LocalDateTime.now().toString());
        metadata.put("runByHostUser", System.getProperty("user.name"));
        metadata.put("osName", System.getProperty("os.name"));
        metadata.put("osArch", System.getProperty("os.arch"));
        metadata.put("javaVersion", System.getProperty("java.version"));
        metadata.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        results.put("metadata", metadata);
        
        System.out.println("Running RQ1 (BFS vs DFS)...");
        results.put("rq1", runRQ1(new int[]{100, 500, 1000, 5000, 10000}, 20));
        
        System.out.println("Running RQ2 (List vs Matrix Memory)...");
        results.put("rq2", runRQ2(new int[]{100, 500, 1000, 2000, 5000}, 0.001));
        
        System.out.println("Running RQ3 (MaxHeap vs MinHeap-K)...");
        results.put("rq3", runRQ3(new int[]{100, 1000, 5000, 10000}, 5));
        
        try {
            File dir = new File("backend-java/data");
            if (!dir.exists()) dir.mkdirs();
            FileWriter writer = new FileWriter("backend-java/data/benchmark_results.json");
            writer.write(results.toString(2));
            writer.close();
            System.out.println("Benchmark results saved to backend-java/data/benchmark_results.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return results;
    }

    public static JSONArray runRQ1(int[] ns, int avgDegree) {
        JSONArray arr = new JSONArray();

        for (int n : ns) {
            Graph g = new Graph();
            for (int i = 0; i < n; i++) {
                g.addVertex("u" + i);
            }
            
            // Add random edges to get roughly avgDegree * N edges total
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < avgDegree / 2; j++) {
                    int target = (int) (Math.random() * n);
                    g.addEdge("u" + i, "u" + target);
                }
            }
            
            String source = "u" + (int)(Math.random() * n);
            
            long totalBfsTime = 0, totalDfsTime = 0;
            int bfsCount = 0, dfsCount = 0;
            int runs = 5;
            
            for (int i = 0; i < runs; i++) {
                long startBfs = System.nanoTime();
                SinglyLinkedList<String> bfsRes = g.bfsLevel2(source);
                totalBfsTime += (System.nanoTime() - startBfs);
                bfsCount = bfsRes.size();
                
                long startDfs = System.nanoTime();
                SinglyLinkedList<String> dfsRes = g.dfsLevel2(source);
                totalDfsTime += (System.nanoTime() - startDfs);
                dfsCount = dfsRes.size();
            }
            
            long avgBfsTime = totalBfsTime / runs;
            long avgDfsTime = totalDfsTime / runs;
            
            JSONObject obj = new JSONObject();
            obj.put("n", n);
            obj.put("avgDegree", avgDegree);
            obj.put("bfsTimeNs", avgBfsTime);
            obj.put("dfsTimeNs", avgDfsTime);
            obj.put("bfsNodesVisited", bfsCount);
            obj.put("dfsNodesVisited", dfsCount);
            
            double bfsFasterBy = avgBfsTime == 0 ? 1.0 : (double) avgDfsTime / avgBfsTime;
            obj.put("bfsFasterBy", bfsFasterBy);
            obj.put("winner", avgBfsTime < avgDfsTime ? "BFS" : "DFS");
            
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray runRQ2(int[] ns, double edgeDensity) {
        JSONArray arr = new JSONArray();

        for (int n : ns) {
            Graph g = new Graph();
            for (int i = 0; i < n; i++) {
                g.addVertex("u" + i);
            }
            
            int targetEdgeCount = (int) (n * n * edgeDensity);
            for (int i = 0; i < targetEdgeCount; i++) {
                int u1 = (int) (Math.random() * n);
                int u2 = (int) (Math.random() * n);
                g.addEdge("u" + u1, "u" + u2);
            }
            
            int numVertices = g.getNumVertices();
            int numEdges = g.getNumEdges();

            // Adjacency List: numVertices * 80 + numEdges * 48
            long listMemoryBytes = (long) numVertices * 80 + (long) numEdges * 48;
            
            // Adjacency Matrix: n * n + n * 8 + 32
            long matrixMemoryBytes = (long) numVertices * numVertices + (long) numVertices * 8 + 32;
            
            JSONObject obj = new JSONObject();
            obj.put("n", numVertices);
            obj.put("edgeDensity", edgeDensity);
            obj.put("edgeCount", numEdges);
            
            obj.put("listMemoryBytes", listMemoryBytes);
            obj.put("matrixMemoryBytes", matrixMemoryBytes);
            obj.put("listMemoryKB", listMemoryBytes / 1024.0);
            obj.put("matrixMemoryKB", matrixMemoryBytes / 1024.0);
            
            double savings = (double) (matrixMemoryBytes - listMemoryBytes) / matrixMemoryBytes * 100.0;
            obj.put("memorySavedPercent", savings);
            obj.put("winner", listMemoryBytes < matrixMemoryBytes ? "List" : "Matrix");
            
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray runRQ3(int[] ns, int k) {
        JSONArray arr = new JSONArray();

        for (int n : ns) {
            String[] ids = new String[n];
            double[] scores = new double[n];
            Recommendation[] recs = new Recommendation[n];
            for (int i = 0; i < n; i++) {
                ids[i] = "u" + i;
                scores[i] = Math.random();
                User u = new User(ids[i], "User " + i, "user_" + i, "", "");
                recs[i] = new Recommendation(u, (int)(Math.random() * 10), scores[i]);
            }
            
            long totalMaxHeapTime = 0;
            long totalMinHeapTime = 0;
            int runs = 5;
            
            for (int r = 0; r < runs; r++) {
                // MaxHeap approach
                long startMax = System.nanoTime();
                MaxHeap<Recommendation> maxHeap = new MaxHeap<>();
                for (int i = 0; i < n; i++) {
                    maxHeap.insert(recs[i]);
                }
                Recommendation[] maxRes = new Recommendation[k];
                for (int i = 0; i < k; i++) {
                    maxRes[i] = maxHeap.extractMax();
                }
                totalMaxHeapTime += (System.nanoTime() - startMax);
                
                // MinHeap-K approach
                long startMin = System.nanoTime();
                MinHeap<Recommendation> minHeap = new MinHeap<>();
                for (int i = 0; i < n; i++) {
                    if (minHeap.size() < k) {
                        minHeap.insert(recs[i]);
                    } else {
                        if (recs[i].compareTo(minHeap.peekMin()) > 0) {
                            minHeap.extractMin();
                            minHeap.insert(recs[i]);
                        }
                    }
                }
                Recommendation[] minRes = new Recommendation[minHeap.size()];
                for (int i = minRes.length - 1; i >= 0; i--) {
                    minRes[i] = minHeap.extractMin();
                }
                totalMinHeapTime += (System.nanoTime() - startMin);
            }
            
            long avgMaxTime = totalMaxHeapTime / runs;
            long avgMinTime = totalMinHeapTime / runs;
            
            JSONObject obj = new JSONObject();
            obj.put("n", n);
            obj.put("k", k);
            obj.put("maxHeapTimeNs", avgMaxTime);
            obj.put("minHeapTimeNs", avgMinTime);
            obj.put("maxHeapOps", n + k); // Rough ops proxy
            obj.put("minHeapOps", n);     
            
            double speedup = avgMinTime == 0 ? 1.0 : (double) avgMaxTime / avgMinTime;
            obj.put("speedupFactor", speedup);
            obj.put("winner", avgMinTime < avgMaxTime ? "MinHeap-K" : "MaxHeap");
            
            arr.put(obj);
        }
        return arr;
    }
}
