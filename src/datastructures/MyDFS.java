package datastructures;

public class MyDFS {
    
    private static class StackNode {
        String userId;
        int depth;

        StackNode(String userId, int depth) {
            this.userId = userId;
            this.depth = depth;
        }
    }

    // Explicit stack DFS to find Level 2 friends
    // Same interface and same output as bfsLevel2()
    // KHÔNG dùng đệ quy (tránh StackOverflow)
    public static String[] dfsLevel2(MyGraph graph, String startUserId) {
        if (startUserId == null || !graph.hasVertex(startUserId)) {
            return new String[0];
        }

        MySinglyLinkedList<String> directFriends = graph.getNeighbors(startUserId);
        if (directFriends.isEmpty()) {
            return new String[0];
        }

        // Excluded set (self and direct friends)
        MyBST<String, Boolean> excluded = new MyBST<>();
        excluded.put(startUserId, true);
        for (String friend : directFriends) {
            excluded.put(friend, true);
        }

        MySinglyLinkedList<String> resultList = new MySinglyLinkedList<>();
        MyBST<String, Boolean> visitedFOF = new MyBST<>();

        // Explicit Stack using MySinglyLinkedList
        MySinglyLinkedList<StackNode> stack = new MySinglyLinkedList<>();
        
        // Push direct friends (depth 1) to stack.
        // To preserve a specific order or just traverse all direct friends, we can push them.
        for (String friend : directFriends) {
            stack.insertAtHead(new StackNode(friend, 1));
        }

        while (!stack.isEmpty()) {
            StackNode curr = stack.removeHead(); // pop from stack
            
            if (curr.depth == 1) {
                // Direct friend, inspect their neighbors to find depth 2 friends
                MySinglyLinkedList<String> neighbors = graph.getNeighbors(curr.userId);
                for (String neighbor : neighbors) {
                    if (!excluded.contains(neighbor) && !visitedFOF.contains(neighbor)) {
                        visitedFOF.put(neighbor, true);
                        resultList.insertAtTail(neighbor);
                    }
                }
            }
        }

        // Convert resultList to String[]
        String[] result = new String[resultList.size()];
        int idx = 0;
        for (String val : resultList) {
            result[idx++] = val;
        }
        return result;
    }

    // Support Adjacency Matrix as well
    public static String[] dfsLevel2(MyAdjacencyMatrix graph, String startUserId) {
        if (startUserId == null || !graph.hasVertex(startUserId)) {
            return new String[0];
        }

        MySinglyLinkedList<String> directFriends = graph.getNeighbors(startUserId);
        if (directFriends.isEmpty()) {
            return new String[0];
        }

        MyBST<String, Boolean> excluded = new MyBST<>();
        excluded.put(startUserId, true);
        for (String friend : directFriends) {
            excluded.put(friend, true);
        }

        MySinglyLinkedList<String> resultList = new MySinglyLinkedList<>();
        MyBST<String, Boolean> visitedFOF = new MyBST<>();

        MySinglyLinkedList<StackNode> stack = new MySinglyLinkedList<>();
        
        for (String friend : directFriends) {
            stack.insertAtHead(new StackNode(friend, 1));
        }

        while (!stack.isEmpty()) {
            StackNode curr = stack.removeHead();
            
            if (curr.depth == 1) {
                MySinglyLinkedList<String> neighbors = graph.getNeighbors(curr.userId);
                for (String neighbor : neighbors) {
                    if (!excluded.contains(neighbor) && !visitedFOF.contains(neighbor)) {
                        visitedFOF.put(neighbor, true);
                        resultList.insertAtTail(neighbor);
                    }
                }
            }
        }

        String[] result = new String[resultList.size()];
        int idx = 0;
        for (String val : resultList) {
            result[idx++] = val;
        }
        return result;
    }
}
