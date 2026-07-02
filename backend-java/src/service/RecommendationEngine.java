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
}
