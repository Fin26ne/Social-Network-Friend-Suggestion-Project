package graph;

import datastructures.BinarySearchTree;
import datastructures.Queue;
import datastructures.SinglyLinkedList;

public class Graph {
    // Adjacency List represented via custom BST where Key = UserID, Value = List of Neighbor UserIDs
    private BinarySearchTree<String, SinglyLinkedList<String>> adjList;
    private int numVertices;
    private int numEdges;

    public Graph() {
        this.adjList = new BinarySearchTree<>();
        this.numVertices = 0;
        this.numEdges = 0;
    }

    public void addVertex(String userId) {
        if (!adjList.contains(userId)) {
            adjList.put(userId, new SinglyLinkedList<>());
            numVertices++;
        }
    }

    public void removeVertex(String userId) {
        if (!adjList.contains(userId)) return;

        // Remove the vertex itself
        SinglyLinkedList<String> neighbors = adjList.get(userId);
        adjList.remove(userId);
        numVertices--;

        // Remove references to this vertex from all of its neighbors
        for (String neighbor : neighbors) {
            SinglyLinkedList<String> neighborList = adjList.get(neighbor);
            if (neighborList != null) {
                if (neighborList.remove(userId)) {
                    numEdges--;
                }
            }
        }
    }

    public void addEdge(String userId1, String userId2) {
        addVertex(userId1);
        addVertex(userId2);

        SinglyLinkedList<String> neighbors1 = adjList.get(userId1);
        SinglyLinkedList<String> neighbors2 = adjList.get(userId2);

        if (!neighbors1.contains(userId2)) {
            neighbors1.add(userId2);
            neighbors2.add(userId1);
            numEdges++;
        }
    }

    public void removeEdge(String userId1, String userId2) {
        SinglyLinkedList<String> neighbors1 = adjList.get(userId1);
        SinglyLinkedList<String> neighbors2 = adjList.get(userId2);

        if (neighbors1 != null && neighbors2 != null) {
            boolean r1 = neighbors1.remove(userId2);
            boolean r2 = neighbors2.remove(userId1);
            if (r1 || r2) {
                numEdges--;
            }
        }
    }

    public boolean hasVertex(String userId) {
        return adjList.contains(userId);
    }

    public boolean hasEdge(String userId1, String userId2) {
        SinglyLinkedList<String> neighbors = adjList.get(userId1);
        return neighbors != null && neighbors.contains(userId2);
    }

    public SinglyLinkedList<String> getNeighbors(String userId) {
        SinglyLinkedList<String> list = adjList.get(userId);
        return list != null ? list : new SinglyLinkedList<>();
    }

    public SinglyLinkedList<String> getVertices() {
        return adjList.inOrderKeys();
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        return numEdges;
    }

    // BFS Traversal
    public SinglyLinkedList<String> bfs(String startUserId) {
        SinglyLinkedList<String> order = new SinglyLinkedList<>();
        if (!adjList.contains(startUserId)) return order;

        Queue<String> queue = new Queue<>();
        BinarySearchTree<String, Boolean> visited = new BinarySearchTree<>();

        queue.enqueue(startUserId);
        visited.put(startUserId, true);

        while (!queue.isEmpty()) {
            String curr = queue.dequeue();
            order.add(curr);

            SinglyLinkedList<String> neighbors = getNeighbors(curr);
            for (String neighbor : neighbors) {
                if (visited.get(neighbor) == null) {
                    visited.put(neighbor, true);
                    queue.enqueue(neighbor);
                }
            }
        }
        return order;
    }

    // DFS Traversal
    public SinglyLinkedList<String> dfs(String startUserId) {
        SinglyLinkedList<String> order = new SinglyLinkedList<>();
        if (!adjList.contains(startUserId)) return order;

        BinarySearchTree<String, Boolean> visited = new BinarySearchTree<>();
        dfsHelper(startUserId, visited, order);
        return order;
    }

    private void dfsHelper(String curr, BinarySearchTree<String, Boolean> visited, SinglyLinkedList<String> order) {
        visited.put(curr, true);
        order.add(curr);

        SinglyLinkedList<String> neighbors = getNeighbors(curr);
        for (String neighbor : neighbors) {
            if (visited.get(neighbor) == null) {
                dfsHelper(neighbor, visited, order);
            }
        }
    }

    // Shortest path distance between start and target
    public int getShortestPathDistance(String start, String target) {
        if (!adjList.contains(start) || !adjList.contains(target)) return -1;
        if (start.equals(target)) return 0;

        Queue<String> queue = new Queue<>();
        BinarySearchTree<String, Integer> distance = new BinarySearchTree<>();

        queue.enqueue(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            String curr = queue.dequeue();
            int currDist = distance.get(curr);

            if (curr.equals(target)) {
                return currDist;
            }

            SinglyLinkedList<String> neighbors = getNeighbors(curr);
            for (String neighbor : neighbors) {
                if (distance.get(neighbor) == null) {
                    distance.put(neighbor, currDist + 1);
                    queue.enqueue(neighbor);
                }
            }
        }
        return -1;
    }

    // Adjacency Matrix representation
    public AdjacencyMatrixInfo getAdjacencyMatrix() {
        SinglyLinkedList<String> verticesList = getVertices();
        
        int n = verticesList.size();
        String[] verticesArray = new String[n];
        BinarySearchTree<String, Integer> idToIndex = new BinarySearchTree<>();
        
        int idx = 0;
        for (String vertex : verticesList) {
            verticesArray[idx] = vertex;
            idToIndex.put(vertex, idx);
            idx++;
        }

        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            String u = verticesArray[i];
            SinglyLinkedList<String> neighbors = getNeighbors(u);
            for (String v : neighbors) {
                Integer j = idToIndex.get(v);
                if (j != null) {
                    matrix[i][j] = true;
                }
            }
        }

        return new AdjacencyMatrixInfo(verticesArray, matrix);
    }

    public static class AdjacencyMatrixInfo {
        public String[] vertices;
        public boolean[][] matrix;

        public AdjacencyMatrixInfo(String[] vertices, boolean[][] matrix) {
            this.vertices = vertices;
            this.matrix = matrix;
        }
    }
}
