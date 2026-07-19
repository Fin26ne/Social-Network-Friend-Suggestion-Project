package console;

import datastructures.SinglyLinkedList;
import graph.Graph;
import model.Recommendation;
import model.User;
import service.GraphService;

import java.util.Scanner;

public class ConsoleMenu {
    private GraphService graphService;
    private Scanner scanner;

    public ConsoleMenu(GraphService graphService) {
        this.graphService = graphService;
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    public void start() {
        System.out.println("==================================================");
        System.out.println("    SOCIAL NETWORK RECOMMENDATION SYSTEM (CSD201) ");
        System.out.println("==================================================");
        
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-9): ");
            String choice;
            try {
                if (!scanner.hasNextLine()) {
                    throw new java.util.NoSuchElementException("No line found");
                }
                choice = scanner.nextLine().trim();
            } catch (java.util.NoSuchElementException | java.lang.IllegalStateException e) {
                System.out.println("\nConsole input is not interactive (running in background). Console menu disabled.");
                System.out.println("The API Server will continue running. Press Ctrl+C to terminate.");
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                break;
            }
            System.out.println();

            try {
                switch (choice) {
                    case "1":
                        listAllUsers();
                        break;
                    case "2":
                        addUser();
                        break;
                    case "3":
                        deleteUser();
                        break;
                    case "4":
                        addFriendship();
                        break;
                    case "5":
                        removeFriendship();
                        break;
                    case "6":
                        viewFriendsAndMutual();
                        break;
                    case "7":
                        getRecommendations();
                        break;
                    case "8":
                        runNetworkTraversalAndAnalysis();
                        break;
                    case "9":
                        running = false;
                        System.out.println("Exiting Console Menu. Thank you!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 9.");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred during execution: " + e.getMessage());
            }
            System.out.println("\n--------------------------------------------------");
        }
    }

    private void printMenu() {
        System.out.println("\n--- MAIN MENU (9 Options) ---");
        System.out.println("1. List All Users");
        System.out.println("2. Add User");
        System.out.println("3. Delete User");
        System.out.println("4. Add Friendship (Befriend)");
        System.out.println("5. Remove Friendship (Unfriend)");
        System.out.println("6. View Friends & Mutual Friends");
        System.out.println("7. Get Friend Recommendations (Top-K)");
        System.out.println("8. Network Traversal & Analysis (BFS/DFS/Adjacency)");
        System.out.println("9. Exit");
    }

    private void listAllUsers() {
        SinglyLinkedList<User> users = graphService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users in the system.");
            return;
        }
        int totalUsers = users.size();
        System.out.println("Users list (" + totalUsers + " total):");
        
        int limit = 100;
        int count = 0;
        for (User user : users) {
            if (count < limit) {
                System.out.printf("- [%s] %s (@%s) - %s (Age: %d)\n", user.getId(), removeAccent(user.getName()), user.getUsername(), removeAccent(user.getBio()), user.getAge());
                count++;
            } else {
                break;
            }
        }
        
        if (totalUsers > limit) {
            int remaining = totalUsers - limit;
            System.out.printf("... and %d more users are loaded in memory but not displayed to prevent console scrollback limits.\n", remaining);
            System.out.print("Do you want to print all remaining " + remaining + " users? (y/n): ");
            String ans = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(ans) || "yes".equalsIgnoreCase(ans)) {
                count = 0;
                for (User user : users) {
                    if (count >= limit) {
                        System.out.printf("- [%s] %s (@%s) - %s (Age: %d)\n", user.getId(), removeAccent(user.getName()), user.getUsername(), removeAccent(user.getBio()), user.getAge());
                    }
                    count++;
                }
            }
        }
    }

    private void addUser() {
        try {
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Username (unique): ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter Bio: ");
            String bio = scanner.nextLine().trim();
            
            System.out.print("Enter Age: ");
            int age = 20;
            try {
                String ageStr = scanner.nextLine().trim();
                if (!ageStr.isEmpty()) {
                    age = Integer.parseInt(ageStr);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid age format. Defaulting to 20.");
            }

            if (name.isEmpty() || username.isEmpty()) {
                System.out.println("Error: Name and Username cannot be empty.");
                return;
            }

            User created = graphService.addUser(name, username, bio, age);
            System.out.println("User added successfully: " + removeAccent(created.getName()) + " (@" + created.getUsername() + ") - Age: " + created.getAge());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        try {
            System.out.print("Enter User ID to delete: ");
            String id = scanner.nextLine().trim();
            
            boolean deleted = graphService.deleteUser(id);
            if (deleted) {
                System.out.println("User " + id + " deleted successfully.");
            } else {
                System.out.println("Error: User not found.");
            }
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    private void addFriendship() {
        try {
            System.out.print("Enter First User ID: ");
            String id1 = scanner.nextLine().trim();
            System.out.print("Enter Second User ID: ");
            String id2 = scanner.nextLine().trim();

            boolean success = graphService.addFriendship(id1, id2);
            if (success) {
                System.out.println("Friendship established successfully between " + id1 + " and " + id2 + ".");
            } else {
                System.out.println("Error: Could not establish friendship. Check IDs or if they are already friends.");
            }
        } catch (Exception e) {
            System.out.println("Error creating friendship: " + e.getMessage());
        }
    }

    private void removeFriendship() {
        try {
            System.out.print("Enter First User ID: ");
            String id1 = scanner.nextLine().trim();
            System.out.print("Enter Second User ID: ");
            String id2 = scanner.nextLine().trim();

            boolean success = graphService.removeFriendship(id1, id2);
            if (success) {
                System.out.println("Friendship removed successfully between " + id1 + " and " + id2 + ".");
            } else {
                System.out.println("Error: Could not remove friendship. Check if they are friends.");
            }
        } catch (Exception e) {
            System.out.println("Error removing friendship: " + e.getMessage());
        }
    }

    private void viewFriendsAndMutual() {
        try {
            System.out.print("Enter User ID to view friends: ");
            String id = scanner.nextLine().trim();
            
            User user = graphService.getUserById(id);
            if (user == null) {
                System.out.println("Error: User not found.");
                return;
            }

            SinglyLinkedList<String> friends = graphService.getFriends(id);
            System.out.println("Friends of " + removeAccent(user.getName()) + " (" + friends.size() + " total):");
            if (friends.isEmpty()) {
                System.out.println("  (No friends yet)");
            } else {
                for (String fId : friends) {
                    User friend = graphService.getUserById(fId);
                    System.out.println("  - [" + fId + "] " + (friend != null ? removeAccent(friend.getName()) : "Unknown"));
                }
            }

            System.out.print("\nDo you want to see mutual friends with another user? (y/n): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(ans) || "yes".equals(ans)) {
                System.out.print("Enter second User ID: ");
                String id2 = scanner.nextLine().trim();
                User user2 = graphService.getUserById(id2);
                if (user2 == null) {
                    System.out.println("Error: Second user not found.");
                    return;
                }

                SinglyLinkedList<String> friends2 = graphService.getFriends(id2);
                System.out.println("Mutual friends between " + removeAccent(user.getName()) + " and " + removeAccent(user2.getName()) + ":");
                boolean found = false;
                for (String fId : friends) {
                    if (friends2.contains(fId)) {
                        User mf = graphService.getUserById(fId);
                        System.out.println("  - [" + fId + "] " + (mf != null ? removeAccent(mf.getName()) : "Unknown"));
                        found = true;
                    }
                }
                if (!found) {
                    System.out.println("  (No mutual friends)");
                }
            }
        } catch (Exception e) {
            System.out.println("Error viewing friends: " + e.getMessage());
        }
    }

    private void getRecommendations() {
        try {
            System.out.print("Enter User ID for recommendations: ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter number of suggestions (K): ");
            int k;
            try {
                k = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid K, using default of 3.");
                k = 3;
            }

            System.out.print("Use Min Heap (1) or Max Heap (2)? ");
            String heapChoice = scanner.nextLine().trim();
            
            User user = graphService.getUserById(id);
            if (user == null) {
                System.out.println("Error: User not found.");
                return;
            }

            long startTime = System.nanoTime();
            SinglyLinkedList<Recommendation> recs;
            if ("2".equals(heapChoice)) {
                recs = graphService.getRecommendationsMaxHeap(id, k);
                System.out.println("Using Max-Heap Strategy...");
            } else {
                recs = graphService.getRecommendationsMinHeap(id, k);
                System.out.println("Using Min-Heap Strategy...");
            }
            long duration = System.nanoTime() - startTime;

            System.out.println("\nRecommendations for " + removeAccent(user.getName()) + ":");
            if (recs.isEmpty()) {
                System.out.println("  (No recommendations found)");
            } else {
                int rank = 1;
                for (Recommendation rec : recs) {
                    System.out.printf("  Rank %d: %s (Mutual Friends: %d, Jaccard: %.2f)\n", 
                            rank++, removeAccent(rec.getUser().getName()), rec.getMutualFriends(), rec.getJaccardSimilarity());
                }
            }
            System.out.printf("Query executed in %.4f ms\n", duration / 1_000_000.0);
        } catch (Exception e) {
            System.out.println("Error loading recommendations: " + e.getMessage());
        }
    }

    private void runNetworkTraversalAndAnalysis() {
        try {
            System.out.println("Select analysis option:");
            System.out.println("  1. Run BFS Traversal");
            System.out.println("  2. Run DFS Traversal");
            System.out.println("  3. Find Shortest Path Distance");
            System.out.println("  4. Print Adjacency Matrix");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runBFS();
                    break;
                case "2":
                    runDFS();
                    break;
                case "3":
                    shortestPath();
                    break;
                case "4":
                    printAdjacencyMatrix();
                    break;
                default:
                    System.out.println("Invalid analysis choice.");
            }
        } catch (Exception e) {
            System.out.println("Error running network analysis: " + e.getMessage());
        }
    }

    private void runBFS() {
        System.out.print("Enter Start User ID: ");
        String id = scanner.nextLine().trim();
        
        if (!graphService.getGraph().hasVertex(id)) {
            System.out.println("Error: User not found.");
            return;
        }

        SinglyLinkedList<String> bfsResult = graphService.bfs(id);
        System.out.println("BFS Traversal Order starting from " + id + ":");
        StringBuilder sb = new StringBuilder();
        for (String node : bfsResult) {
            sb.append(node).append(" -> ");
        }
        if (sb.length() > 4) {
            sb.setLength(sb.length() - 4);
        }
        System.out.println(sb.toString());
    }

    private void runDFS() {
        System.out.print("Enter Start User ID: ");
        String id = scanner.nextLine().trim();
        
        if (!graphService.getGraph().hasVertex(id)) {
            System.out.println("Error: User not found.");
            return;
        }

        SinglyLinkedList<String> dfsResult = graphService.dfs(id);
        System.out.println("DFS Traversal Order starting from " + id + ":");
        StringBuilder sb = new StringBuilder();
        for (String node : dfsResult) {
            sb.append(node).append(" -> ");
        }
        if (sb.length() > 4) {
            sb.setLength(sb.length() - 4);
        }
        System.out.println(sb.toString());
    }

    private void shortestPath() {
        System.out.print("Enter Start User ID: ");
        String id1 = scanner.nextLine().trim();
        System.out.print("Enter End User ID: ");
        String id2 = scanner.nextLine().trim();

        int dist = graphService.getShortestPathDistance(id1, id2);
        if (dist == -1) {
            System.out.println("There is no path of friendships connecting " + id1 + " and " + id2 + ".");
        } else {
            System.out.println("The shortest connection distance (degrees of separation) is: " + dist);
        }
    }

    private void printAdjacencyMatrix() {
        Graph.AdjacencyMatrixInfo matrixInfo = graphService.getAdjacencyMatrix();
        System.out.println("Adjacency Matrix (" + matrixInfo.vertices.length + "x" + matrixInfo.vertices.length + "):");
        
        // Print column headers
        System.out.print("\t");
        for (String vertex : matrixInfo.vertices) {
            System.out.print(vertex + "\t");
        }
        System.out.println();

        for (int i = 0; i < matrixInfo.matrix.length; i++) {
            System.out.print(matrixInfo.vertices[i] + "\t");
            for (int j = 0; j < matrixInfo.matrix[i].length; j++) {
                System.out.print((matrixInfo.matrix[i][j] ? "1" : "0") + "\t");
            }
            System.out.println();
        }
    }

    private static String removeAccent(String s) {
        if (s == null) return null;
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(normalized).replaceAll("");
        // Replace Vietnamese specific characters that Normalizer doesn't handle fully
        result = result.replaceAll("[Đ]", "D");
        result = result.replaceAll("[đ]", "d");
        return result;
    }
}
