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

        SinglyLinkedList<String> verticesList = getVertices();
        int n = verticesList.size();
        String[] keys = new String[n];
        int idx = 0;
        for (String v : verticesList) {
            keys[idx++] = v;
        }

        Queue<String> queue = new Queue<>();
        boolean[] visited = new boolean[n];

        int startIdx = java.util.Arrays.binarySearch(keys, startUserId);
        if (startIdx >= 0) {
            visited[startIdx] = true;
        }
        queue.enqueue(startUserId);

        while (!queue.isEmpty()) {
            String curr = queue.dequeue();
            order.add(curr);

            SinglyLinkedList<String> neighbors = getNeighbors(curr);
            for (String neighbor : neighbors) {
                int neighborIdx = java.util.Arrays.binarySearch(keys, neighbor);
                if (neighborIdx >= 0 && !visited[neighborIdx]) {
                    visited[neighborIdx] = true;
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

        SinglyLinkedList<String> verticesList = getVertices();
        int n = verticesList.size();
        String[] keys = new String[n];
        int idx = 0;
        for (String v : verticesList) {
            keys[idx++] = v;
        }

        String[] stack = new String[n];
        boolean[] visited = new boolean[n];
        int top = 0;
        
        int startIdx = java.util.Arrays.binarySearch(keys, startUserId);
        if (startIdx >= 0) {
            visited[startIdx] = true;
        }
        stack[top] = startUserId;

        while (top >= 0) {
            String curr = stack[top--];
            order.add(curr);

            SinglyLinkedList<String> neighbors = getNeighbors(curr);
            for (String neighbor : neighbors) {
                int neighborIdx = java.util.Arrays.binarySearch(keys, neighbor);
                if (neighborIdx >= 0 && !visited[neighborIdx]) {
                    visited[neighborIdx] = true;
                    stack[++top] = neighbor;
                }
            }
        }
        return order;
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

    // BFS traversal limited to depth 2 (Level-2 candidates / friend-of-friend)
    public SinglyLinkedList<String> bfsLevel2(String startUserId) {
        SinglyLinkedList<String> candidates = new SinglyLinkedList<>();
        if (!adjList.contains(startUserId)) return candidates;

        BinarySearchTree<String, Boolean> visited = new BinarySearchTree<>();
        Queue<String> queue = new Queue<>();
        Queue<Integer> depthQueue = new Queue<>();

        queue.enqueue(startUserId);
        depthQueue.enqueue(0);
        visited.put(startUserId, true);

        // Get direct friends to exclude them from recommendations
        SinglyLinkedList<String> directFriends = getNeighbors(startUserId);

        while (!queue.isEmpty()) {
            String curr = queue.dequeue();
            int depth = depthQueue.dequeue();

            if (depth == 2) {
                if (!curr.equals(startUserId) && !directFriends.contains(curr)) {
                    candidates.add(curr);
                }
            }

            if (depth < 2) {
                SinglyLinkedList<String> neighbors = getNeighbors(curr);
                for (String neighbor : neighbors) {
                    if (visited.get(neighbor) == null) {
                        visited.put(neighbor, true);
                        queue.enqueue(neighbor);
                        depthQueue.enqueue(depth + 1);
                    }
                }
            }
        }
        return candidates;
    }

    // DFS traversal limited to depth 2 (Level-2 candidates / friend-of-friend)
    public SinglyLinkedList<String> dfsLevel2(String startUserId) {
        SinglyLinkedList<String> candidates = new SinglyLinkedList<>();
        if (!adjList.contains(startUserId)) return candidates;

        BinarySearchTree<String, Boolean> visited = new BinarySearchTree<>();
        SinglyLinkedList<String> directFriends = getNeighbors(startUserId);

        visited.put(startUserId, true);
        dfsLevel2Helper(startUserId, 0, visited, directFriends, candidates, startUserId);
        return candidates;
    }

    private void dfsLevel2Helper(String curr, int depth, BinarySearchTree<String, Boolean> visited, 
                                 SinglyLinkedList<String> directFriends, SinglyLinkedList<String> candidates, String startUserId) {
        if (depth == 2) {
            if (!curr.equals(startUserId) && !directFriends.contains(curr)) {
                if (!candidates.contains(curr)) {
                    candidates.add(curr);
                }
            }
            return;
        }

        SinglyLinkedList<String> neighbors = getNeighbors(curr);
        for (String neighbor : neighbors) {
            if (visited.get(neighbor) == null) {
                visited.put(neighbor, true);
                dfsLevel2Helper(neighbor, depth + 1, visited, directFriends, candidates, startUserId);
                visited.remove(neighbor); // backtrack
            }
        }
    }

    public static class AdjacencyMatrixInfo {
        public String[] vertices;
        public boolean[][] matrix;

        public AdjacencyMatrixInfo(String[] vertices, boolean[][] matrix) {
            this.vertices = vertices;
            this.matrix = matrix;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== GRAPH BFS TEST ===");
        Graph socialNetwork = new Graph();
        socialNetwork.addEdge("A", "B");
        socialNetwork.addEdge("B", "C");
        socialNetwork.addEdge("C", "D");
        socialNetwork.addEdge("A", "C");
        System.out.println("Neighbors of A: " + socialNetwork.getNeighbors("A"));
        System.out.println("BFS Traversal from A: " + socialNetwork.bfs("A"));
    }
}
