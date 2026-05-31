package datastructures;

import model.Recommendation;
import model.User;

public class SuggestionService {

    public double computeJaccard(MyGraph graph, String userId1, String userId2) {
        if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
            return 0.0;
        }
        MySinglyLinkedList<String> friends1 = graph.getNeighbors(userId1);
        MySinglyLinkedList<String> friends2 = graph.getNeighbors(userId2);
        
        if (friends1.isEmpty() || friends2.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;
        for (String f1 : friends1) {
            if (friends2.contains(f1)) {
                intersection++;
            }
        }

        int size1 = friends1.size();
        int size2 = friends2.size();
        int union = size1 + size2 - intersection;

        if (union == 0) return 0.0; // Avoid division by zero
        return (double) intersection / union;
    }

    public MySinglyLinkedList<Recommendation> getSuggestions(MyGraph graph, MyBST<String, User> userBst, String userId, int k) {
        MySinglyLinkedList<Recommendation> result = new MySinglyLinkedList<>();
        if (!graph.hasVertex(userId) || k <= 0) {
            return result;
        }

        MySinglyLinkedList<String> friends = graph.getNeighbors(userId);
        if (friends.isEmpty()) {
            return result; // Trả empty list nếu user không có bạn
        }

        MyMinHeap<Recommendation> minHeap = new MyMinHeap<>(k);
        MySinglyLinkedList<String> allUserIds = userBst.inOrderKeys();

        for (String targetId : allUserIds) {
            if (targetId.equals(userId) || graph.hasEdge(userId, targetId)) {
                continue;
            }

            double jaccard = computeJaccard(graph, userId, targetId);
            if (jaccard <= 0) continue;

            int mutuals = 0;
            MySinglyLinkedList<String> targetFriends = graph.getNeighbors(targetId);
            for (String f : friends) {
                if (targetFriends.contains(f)) {
                    mutuals++;
                }
            }

            User targetUser = userBst.get(targetId);
            Recommendation rec = new Recommendation(targetUser, mutuals, jaccard);
            minHeap.offerIfBetter(rec);
        }

        return minHeap.toSortedList();
    }

    public static void runSelfTest() {
        System.out.println("Running SuggestionService Self-Test...");
        MyGraph graph = new MyGraph();
        MyBST<String, User> userBst = new MyBST<>();
        
        User u1 = new User("u1", "Alice", "alice", "Bio 1", "2026-05-31");
        User u2 = new User("u2", "Bob", "bob", "Bio 2", "2026-05-31");
        User u3 = new User("u3", "Charlie", "charlie", "Bio 3", "2026-05-31");
        User u4 = new User("u4", "David", "david", "Bio 4", "2026-05-31");
        
        userBst.put("u1", u1);
        userBst.put("u2", u2);
        userBst.put("u3", u3);
        userBst.put("u4", u4);
        
        graph.addEdge("u1", "u2"); // Alice - Bob
        graph.addEdge("u1", "u3"); // Alice - Charlie
        graph.addEdge("u2", "u4"); // Bob - David
        graph.addEdge("u3", "u4"); // Charlie - David
        
        SuggestionService service = new SuggestionService();
        MySinglyLinkedList<Recommendation> recs = service.getSuggestions(graph, userBst, "u1", 2);
        
        boolean pass = false;
        for (Recommendation r : recs) {
            if (r.getUser().getId().equals("u4") && r.getJaccardSimilarity() == 1.0) {
                pass = true;
            }
        }
        
        if (pass) {
            System.out.println("SuggestionService Self-Test PASSED!");
        } else {
            throw new RuntimeException("SuggestionService Self-Test FAILED!");
        }
    }
}
