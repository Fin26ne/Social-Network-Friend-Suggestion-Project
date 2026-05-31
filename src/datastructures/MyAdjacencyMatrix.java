package datastructures;

public class MyAdjacencyMatrix {
    private boolean[][] matrix;
    private String[] vertices;
    private MyBST<String, Integer> idToIndex;
    private int size;
    private int capacity;

    private static final int DEFAULT_CAPACITY = 20;

    public MyAdjacencyMatrix() {
        this.capacity = DEFAULT_CAPACITY;
        this.matrix = new boolean[capacity][capacity];
        this.vertices = new String[capacity];
        this.idToIndex = new MyBST<>();
        this.size = 0;
    }

    public void addVertex(String userId) {
        if (userId == null) return;
        if (!idToIndex.contains(userId)) {
            if (size >= capacity) {
                resize();
            }
            vertices[size] = userId;
            idToIndex.put(userId, size);
            size++;
        }
    }

    public void removeVertex(String userId) {
        // Deleting vertex from adjacency matrix:
        // We can just clear the row and column of this vertex and mark it null in vertices array,
        // or shift elements to keep it compact. Let's keep it simple: clear its row and column.
        if (userId == null || !idToIndex.contains(userId)) return;

        int idx = idToIndex.get(userId);
        for (int i = 0; i < size; i++) {
            matrix[idx][i] = false;
            matrix[i][idx] = false;
        }
        vertices[idx] = null;
        idToIndex.delete(userId);
        // Note: we keep size unchanged to avoid shifting indices, or we can just keep the index tombstoned.
    }

    public void addEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return;
        addVertex(userId1);
        addVertex(userId2);

        int idx1 = idToIndex.get(userId1);
        int idx2 = idToIndex.get(userId2);

        matrix[idx1][idx2] = true;
        matrix[idx2][idx1] = true;
    }

    public void removeEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return;
        if (!idToIndex.contains(userId1) || !idToIndex.contains(userId2)) return;

        int idx1 = idToIndex.get(userId1);
        int idx2 = idToIndex.get(userId2);

        matrix[idx1][idx2] = false;
        matrix[idx2][idx1] = false;
    }

    public boolean hasVertex(String userId) {
        if (userId == null) return false;
        return idToIndex.contains(userId) && vertices[idToIndex.get(userId)] != null;
    }

    public boolean hasEdge(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return false;
        if (!hasVertex(userId1) || !hasVertex(userId2)) return false;

        int idx1 = idToIndex.get(userId1);
        int idx2 = idToIndex.get(userId2);
        return matrix[idx1][idx2];
    }

    public MySinglyLinkedList<String> getNeighbors(String userId) {
        MySinglyLinkedList<String> list = new MySinglyLinkedList<>();
        if (userId == null || !hasVertex(userId)) return list;

        int idx = idToIndex.get(userId);
        for (int i = 0; i < size; i++) {
            if (vertices[i] != null && matrix[idx][i]) {
                list.insertAtTail(vertices[i]);
            }
        }
        return list;
    }

    public MySinglyLinkedList<String> getVertices() {
        MySinglyLinkedList<String> list = new MySinglyLinkedList<>();
        for (int i = 0; i < size; i++) {
            if (vertices[i] != null) {
                list.insertAtTail(vertices[i]);
            }
        }
        return list;
    }

    public int getNumVertices() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (vertices[i] != null) count++;
        }
        return count;
    }

    public int getNumEdges() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (vertices[i] != null && vertices[j] != null && matrix[i][j]) {
                    count++;
                }
            }
        }
        return count;
    }

    // getMemoryBytes(): actual size of boolean[][] matrix
    // In Java, boolean[][] has 24 bytes overhead for 2D array pointer,
    // and for each row of capacity K: 24 bytes array overhead + K bytes (1 byte per boolean).
    // Total: 24 + (24 + K) * K bytes.
    public long getMemoryBytes() {
        return 24 + (24 + capacity) * (long) capacity;
    }

    // bfsLevel2: same interface as MyGraph
    public String[] bfsLevel2(String startUserId) {
        if (startUserId == null || !hasVertex(startUserId)) {
            return new String[0];
        }

        MySinglyLinkedList<String> directFriends = getNeighbors(startUserId);
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

        MyQueue<String> queue = new MyQueue<>();
        for (String friend : directFriends) {
            queue.enqueue(friend);
        }

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

        String[] result = new String[resultList.size()];
        int idx = 0;
        for (String val : resultList) {
            result[idx++] = val;
        }
        return result;
    }

    private void resize() {
        int oldCapacity = capacity;
        capacity = capacity * 2;
        boolean[][] newMatrix = new boolean[capacity][capacity];
        String[] newVertices = new String[capacity];

        for (int i = 0; i < oldCapacity; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, oldCapacity);
            newVertices[i] = vertices[i];
        }

        matrix = newMatrix;
        vertices = newVertices;
    }
}
