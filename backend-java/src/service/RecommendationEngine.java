package service;

import datastructures.BinarySearchTree;
import datastructures.MaxHeap;
import datastructures.MinHeap;
import datastructures.SinglyLinkedList;
import graph.Graph;
import model.Recommendation;
import model.User;

public class RecommendationEngine {

    public int getMutualFriendsCount(Graph graph, String userId1, String userId2) {
        if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
            return 0;
        }

        SinglyLinkedList<String> friends1 = graph.getNeighbors(userId1);
        SinglyLinkedList<String> friends2 = graph.getNeighbors(userId2);

        int count = 0;
        for (String friend : friends1) {
            if (friends2.contains(friend)) {
                count++;
            }
        }
        return count;
    }

    public double getJaccardSimilarity(Graph graph, String userId1, String userId2) {
        if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
            return 0.0;
        }

        int intersection = getMutualFriendsCount(graph, userId1, userId2);
        if (intersection == 0) {
            return 0.0;
        }

        int size1 = graph.getNeighbors(userId1).size();
        int size2 = graph.getNeighbors(userId2).size();
        int union = size1 + size2 - intersection;

        return union == 0 ? 0.0 : (double) intersection / union;
    }

    // Recommendation using MaxHeap (inserts all non-friends, extracts K times) - O(N log N)
    public SinglyLinkedList<Recommendation> getRecommendationsMaxHeap(Graph graph, BinarySearchTree<String, User> userBst, String userId, int k) {
        SinglyLinkedList<Recommendation> result = new SinglyLinkedList<>();
        if (!graph.hasVertex(userId)) {
            return result;
        }

        SinglyLinkedList<String> friends = graph.getNeighbors(userId);
        int size1 = friends.size();
        BinarySearchTree<String, Boolean> friendsSet = new BinarySearchTree<>();
        for (String friend : friends) {
            friendsSet.put(friend, true);
        }

        MaxHeap<Recommendation> maxHeap = new MaxHeap<>();
        SinglyLinkedList<String> allUserIds = userBst.inOrderKeys();

        for (String targetId : allUserIds) {
            if (targetId.equals(userId) || graph.hasEdge(userId, targetId)) {
                continue;
            }

            int mutuals = 0;
            SinglyLinkedList<String> targetFriends = graph.getNeighbors(targetId);
            for (String f : targetFriends) {
                if (friendsSet.contains(f)) {
                    mutuals++;
                }
            }

            int size2 = targetFriends.size();
            int union = size1 + size2 - mutuals;
            double jaccard = (union == 0) ? 0.0 : (double) mutuals / union;

            User targetUser = userBst.get(targetId);
            maxHeap.insert(new Recommendation(targetUser, mutuals, jaccard));
        }

        int count = 0;
        while (!maxHeap.isEmpty() && count < k) {
            result.add(maxHeap.extractMax());
            count++;
        }

        return result;
    }

    // Recommendation using MinHeap (maintains top-K elements in heap) - O(N log K)
    public SinglyLinkedList<Recommendation> getRecommendationsMinHeap(Graph graph, BinarySearchTree<String, User> userBst, String userId, int k) {
        SinglyLinkedList<Recommendation> result = new SinglyLinkedList<>();
        if (!graph.hasVertex(userId) || k <= 0) {
            return result;
        }

        SinglyLinkedList<String> friends = graph.getNeighbors(userId);
        int size1 = friends.size();
        BinarySearchTree<String, Boolean> friendsSet = new BinarySearchTree<>();
        for (String friend : friends) {
            friendsSet.put(friend, true);
        }

        MinHeap<Recommendation> minHeap = new MinHeap<>();
        SinglyLinkedList<String> allUserIds = userBst.inOrderKeys();

        for (String targetId : allUserIds) {
            if (targetId.equals(userId) || graph.hasEdge(userId, targetId)) {
                continue;
            }

            int mutuals = 0;
            SinglyLinkedList<String> targetFriends = graph.getNeighbors(targetId);
            for (String f : targetFriends) {
                if (friendsSet.contains(f)) {
                    mutuals++;
                }
            }

            int size2 = targetFriends.size();
            int union = size1 + size2 - mutuals;
            double jaccard = (union == 0) ? 0.0 : (double) mutuals / union;

            User targetUser = userBst.get(targetId);
            Recommendation rec = new Recommendation(targetUser, mutuals, jaccard);

            if (minHeap.size() < k) {
                minHeap.insert(rec);
            } else {
                if (rec.compareTo(minHeap.peekMin()) > 0) {
                    minHeap.extractMin();
                    minHeap.insert(rec);
                }
            }
        }

        // Extracting from min-heap gives elements in ascending order.
        // We put them in a temporary list.
        SinglyLinkedList<Recommendation> temp = new SinglyLinkedList<>();
        while (!minHeap.isEmpty()) {
            temp.add(minHeap.extractMin());
        }

        // Reverse to get descending order (highest score first) using an array in O(K)
        int tempSize = temp.size();
        if (tempSize > 0) {
            Recommendation[] tempArr = new Recommendation[tempSize];
            int idx = 0;
            for (Recommendation r : temp) {
                tempArr[idx++] = r;
            }
            for (int i = tempSize - 1; i >= 0; i--) {
                result.add(tempArr[i]);
            }
        }

        return result;
    }

    // MAIN METHOD FOR INDEPENDENT TESTING
    public static void main(String[] args) {
        System.out.println("=== RECOMMENDATION ENGINE INTERACTIVE TEST ===");
        Graph graph = new Graph();
        BinarySearchTree<String, User> userBst = new BinarySearchTree<>();

        // Add users
        User u1 = new User("u1", "Alice", "alice", "Developer", "2023-01-01");
        User u2 = new User("u2", "Bob", "bob", "Designer", "2023-01-01");
        User u3 = new User("u3", "Charlie", "charlie", "Manager", "2023-01-01");
        User u4 = new User("u4", "Dave", "dave", "QA", "2023-01-01");
        User u5 = new User("u5", "Eve", "eve", "Data Scientist", "2023-01-01");

        userBst.put("u1", u1);
        userBst.put("u2", u2);
        userBst.put("u3", u3);
        userBst.put("u4", u4);
        userBst.put("u5", u5);

        // Add friendships
        graph.addEdge("u1", "u2"); // Alice - Bob
        graph.addEdge("u1", "u3"); // Alice - Charlie
        graph.addEdge("u4", "u2"); // Dave - Bob
        graph.addEdge("u4", "u3"); // Dave - Charlie
        graph.addEdge("u5", "u3"); // Eve - Charlie

        RecommendationEngine engine = new RecommendationEngine();
        
        System.out.println("\nSample Graph loaded:");
        for (String uId : userBst.inOrderKeys()) {
            User u = userBst.get(uId);
            System.out.println("- [" + uId + "] " + u.getName() + " | Friends: " + graph.getNeighbors(uId));
        }

        java.util.Scanner scanner = new java.util.Scanner(System.in, "UTF-8");
        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Get Friend Recommendations for a User");
            System.out.println("2. Calculate Jaccard Similarity between two Users");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            String choice;
            if (scanner.hasNextLine()) {
                choice = scanner.nextLine().trim();
            } else {
                break;
            }

            if (choice.equals("1")) {
                System.out.print("Enter User ID (e.g. u1): ");
                String userId = scanner.nextLine().trim();
                if (!graph.hasVertex(userId)) {
                    System.out.println("User not found!");
                    continue;
                }

                System.out.print("Enter number of suggestions (K): ");
                int k = 2;
                try {
                    k = Integer.parseInt(scanner.nextLine().trim());
                } catch (Exception e) {}

                System.out.println("Choose Heap Strategy:");
                System.out.println("  1. Min-Heap (K-bounded - O(N log K))");
                System.out.println("  2. Max-Heap (Standard - O(N log N))");
                System.out.print("Choice (1-2): ");
                String strategy = scanner.nextLine().trim();

                SinglyLinkedList<Recommendation> recs;
                long start = System.nanoTime();
                if ("2".equals(strategy)) {
                    recs = engine.getRecommendationsMaxHeap(graph, userBst, userId, k);
                    System.out.println("Running Max-Heap Strategy...");
                } else {
                    recs = engine.getRecommendationsMinHeap(graph, userBst, userId, k);
                    System.out.println("Running Min-Heap Strategy...");
                }
                long duration = System.nanoTime() - start;

                System.out.println("Result recommendations for " + userBst.get(userId).getName() + ":");
                if (recs.isEmpty()) {
                    System.out.println("  (No recommendations found)");
                } else {
                    int rank = 1;
                    for (Recommendation r : recs) {
                        System.out.printf("  Rank %d: %s | Mutual Friends: %d | Jaccard Score: %.2f\n", 
                                rank++, r.getUser().getName(), r.getMutualFriends(), r.getJaccardSimilarity());
                    }
                }
                System.out.printf("Query executed in %.4f ms\n", duration / 1_000_000.0);

            } else if (choice.equals("2")) {
                System.out.print("Enter first User ID: ");
                String id1 = scanner.nextLine().trim();
                System.out.print("Enter second User ID: ");
                String id2 = scanner.nextLine().trim();

                if (!graph.hasVertex(id1) || !graph.hasVertex(id2)) {
                    System.out.println("One or both users not found!");
                    continue;
                }

                double jaccard = engine.getJaccardSimilarity(graph, id1, id2);
                int mutuals = engine.getMutualFriendsCount(graph, id1, id2);
                System.out.printf("Mutual friends count: %d\n", mutuals);
                System.out.printf("Jaccard Similarity Score: %.4f\n", jaccard);

            } else if (choice.equals("3")) {
                System.out.println("Exiting test.");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}
