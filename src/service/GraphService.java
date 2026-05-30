package service;

import datastructures.BinarySearchTree;
import datastructures.SinglyLinkedList;
import graph.Graph;
import model.Recommendation;
import model.User;
import services.DataStore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphService {
    private Graph graph;
    private BinarySearchTree<String, User> userBst;
    private BinarySearchTree<String, String> usernameToIdBst; // Secondary index for username lookup
    private DataStore dataStore;
    private RecommendationEngine recommendationEngine;
    private int nextUserIdNum = 11; // Starting number for new IDs, will be auto-adjusted based on loaded data

    public GraphService(String dataFilePath) {
        this.graph = new Graph();
        this.userBst = new BinarySearchTree<>();
        this.usernameToIdBst = new BinarySearchTree<>();
        this.dataStore = new DataStore(dataFilePath);
        this.recommendationEngine = new RecommendationEngine();

        try {
            dataStore.load(graph, userBst);
            rebuildIndexes();
        } catch (IOException e) {
            System.err.println("Failed to load data, starting with fresh dataset. Error: " + e.getMessage());
            try {
                dataStore.initializeSeedData(graph, userBst);
                rebuildIndexes();
            } catch (IOException ex) {
                System.err.println("Critical error initializing seed data: " + ex.getMessage());
            }
        }
    }

    private synchronized void rebuildIndexes() {
        usernameToIdBst = new BinarySearchTree<>();
        int maxIdNum = 0;
        for (User user : userBst.inOrderValues()) {
            usernameToIdBst.put(user.getUsername().toLowerCase(), user.getId());
            
            // Extract numeric part of id (e.g. "u12" -> 12)
            try {
                if (user.getId().startsWith("u")) {
                    int num = Integer.parseInt(user.getId().substring(1));
                    if (num > maxIdNum) {
                        maxIdNum = num;
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
        nextUserIdNum = maxIdNum + 1;
    }

    private void autoSave() {
        try {
            dataStore.save(graph, userBst);
        } catch (IOException e) {
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    public synchronized User addUser(String name, String username, String bio) {
        if (usernameToIdBst.contains(username.toLowerCase())) {
            throw new IllegalArgumentException("Username already exists: @" + username);
        }

        String newId = "u" + (nextUserIdNum++);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        User newUser = new User(newId, name, username, bio, dateStr);

        userBst.put(newId, newUser);
        usernameToIdBst.put(username.toLowerCase(), newId);
        graph.addVertex(newId);

        autoSave();
        return newUser;
    }

    public synchronized boolean deleteUser(String userId) {
        if (!userBst.contains(userId)) {
            return false;
        }

        User user = userBst.get(userId);
        usernameToIdBst.remove(user.getUsername().toLowerCase());
        userBst.remove(userId);
        graph.removeVertex(userId);

        autoSave();
        return true;
    }

    public synchronized boolean addFriendship(String userId1, String userId2) {
        if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
            return false;
        }
        if (userId1.equals(userId2)) {
            return false;
        }

        graph.addEdge(userId1, userId2);
        autoSave();
        return true;
    }

    public synchronized boolean removeFriendship(String userId1, String userId2) {
        if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
            return false;
        }

        graph.removeEdge(userId1, userId2);
        autoSave();
        return true;
    }

    public SinglyLinkedList<User> getAllUsers() {
        return userBst.inOrderValues();
    }

    public User getUserById(String userId) {
        return userBst.get(userId);
    }

    public User getUserByUsername(String username) {
        String id = usernameToIdBst.get(username.toLowerCase());
        return id == null ? null : userBst.get(id);
    }

    public Graph getGraph() {
        return graph;
    }

    public SinglyLinkedList<String> getFriends(String userId) {
        return graph.getNeighbors(userId);
    }

    public SinglyLinkedList<Recommendation> getRecommendations(String userId, int k) {
        // Default to MinHeap as it's more optimal
        return recommendationEngine.getRecommendationsMinHeap(graph, userBst, userId, k);
    }

    public SinglyLinkedList<Recommendation> getRecommendationsMinHeap(String userId, int k) {
        return recommendationEngine.getRecommendationsMinHeap(graph, userBst, userId, k);
    }

    public SinglyLinkedList<Recommendation> getRecommendationsMaxHeap(String userId, int k) {
        return recommendationEngine.getRecommendationsMaxHeap(graph, userBst, userId, k);
    }

    public SinglyLinkedList<String> bfs(String startUserId) {
        return graph.bfs(startUserId);
    }

    public SinglyLinkedList<String> dfs(String startUserId) {
        return graph.dfs(startUserId);
    }

    public int getShortestPathDistance(String start, String target) {
        return graph.getShortestPathDistance(start, target);
    }

    public Graph.AdjacencyMatrixInfo getAdjacencyMatrix() {
        return graph.getAdjacencyMatrix();
    }
}
