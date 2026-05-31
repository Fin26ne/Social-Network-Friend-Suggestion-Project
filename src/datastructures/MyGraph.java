package datastructures;

public class MyGraph {
    private MyBST<String, MySinglyLinkedList<String>> adjList;
    private int numVertices;
    private int numEdges;

    public MyGraph() {
        this.adjList = new MyBST<>();
        this.numVertices = 0;
        this.numEdges = 0;
    }

    public void addVertex(String userId) {
        if (userId == null) return;
        if (!adjList.contains(userId)) {
            adjList.put(userId, new MySinglyLinkedList<>());
            numVertices++;
        }
    }

    public void removeVertex(String userId) {
        if (userId == null || !adjList.contains(userId)) return;

        MySinglyLinkedList<String> neighbors = adjList.get(userId);
        adjList.delete(userId);
        numVertices--;

        // Remove this user from all neighbors' lists
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                MySinglyLinkedList<String> neighborList = adjList.get(neighbor);
                if (neighborList != null) {
                    if (neighborList.remove(userId)) {
                        numEdges--;
                    }
                }
            }
        }
    }

    public void addEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return;
        addVertex(userId1);
        addVertex(userId2);

        MySinglyLinkedList<String> neighbors1 = adjList.get(userId1);
        MySinglyLinkedList<String> neighbors2 = adjList.get(userId2);

        if (neighbors1 != null && neighbors2 != null && !neighbors1.contains(userId2)) {
            neighbors1.insertAtTail(userId2);
            neighbors2.insertAtTail(userId1);
            numEdges++;
        }
    }

    public void removeEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return;
        MySinglyLinkedList<String> neighbors1 = adjList.get(userId1);
        MySinglyLinkedList<String> neighbors2 = adjList.get(userId2);

        if (neighbors1 != null && neighbors2 != null) {
            boolean r1 = neighbors1.remove(userId2);
            boolean r2 = neighbors2.remove(userId1);
            if (r1 || r2) {
                numEdges--;
            }
        }
    }

    public boolean hasVertex(String userId) {
        if (userId == null) return false;
        return adjList.contains(userId);
    }

    public boolean hasEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return false;
        MySinglyLinkedList<String> neighbors = adjList.get(userId1);
        return neighbors != null && neighbors.contains(userId2);
    }

    public MySinglyLinkedList<String> getNeighbors(String userId) {
        if (userId == null) return new MySinglyLinkedList<>();
        MySinglyLinkedList<String> neighbors = adjList.get(userId);
        return neighbors != null ? neighbors : new MySinglyLinkedList<>();
    }

    public MySinglyLinkedList<String> getVertices() {
        return adjList.inOrderKeys();
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        return numEdges;
    }

    // bfsLevel2: returns friends of friends (depth = 2)
    // Loại: self, direct friends, và đã visited
    // Sử dụng MyQueue
    public String[] bfsLevel2(String startUserId) {
        if (startUserId == null || !hasVertex(startUserId)) {
            return new String[0];
        }

        MySinglyLinkedList<String> directFriends = getNeighbors(startUserId);
        if (directFriends.isEmpty()) {
            return new String[0]; // Handle: user không có bạn -> trả empty array
        }

        // excluded contains self and direct friends
        MyBST<String, Boolean> excluded = new MyBST<>();
        excluded.put(startUserId, true);
        for (String friend : directFriends) {
            excluded.put(friend, true);
        }

        MySinglyLinkedList<String> resultList = new MySinglyLinkedList<>();
        MyBST<String, Boolean> visitedFOF = new MyBST<>();

        // We can do standard queue bfs but we only process level 1 neighbors to discover level 2.
        // Let's use MyQueue for BFS Level 2.
        MyQueue<String> queue = new MyQueue<>();
        
        // Enqueue all direct friends
        for (String friend : directFriends) {
            queue.enqueue(friend);
        }

        // We process all level 1 direct friends
        while (!queue.isEmpty()) {
            String friend = queue.dequeue();
            MySinglyLinkedList<String> neighborsOfFriend = getNeighbors(friend);
            for (String fof : neighborsOfFriend) {
                if (!excluded.contains(fof) && !visitedFOF.contains(fof)) {
                    visitedFOF.put(fof, true);
                    resultList.insertAtTail(fof);
                }
            }
        }

        // Convert to array
        String[] result = new String[resultList.size()];
        int idx = 0;
        for (String val : resultList) {
            result[idx++] = val;
        }
        return result;
    }
}
