import api.AppServer;
import console.ConsoleMenu;
import service.GraphService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Force console encoding to UTF-8 for proper Vietnamese text rendering
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Warning: UTF-8 encoding not supported for console output.");
        }

        String dataDirectoryPath = "backend-java/data";

        System.out.println("=================================================");
        System.out.println("   SOCIAL NETWORK FRIEND SUGGESTION - NATIVE JAVA");
        System.out.println("=================================================");
        System.out.println("Initializing Social Network Graph...");
        GraphService graphService = new GraphService(dataDirectoryPath);

        // Run BenchmarkRunner to generate benchmark_results.json if it doesn't exist
        java.io.File benchmarkFile = new java.io.File("backend-java/data/benchmark_results.json");
        if (!benchmarkFile.exists()) {
            System.out.println("Generating benchmark results on startup...");
            try {
                int[] ns = {1000, 5000, 10000};
                org.json.JSONArray results = services.BenchmarkRunner.runRQ2(ns, 0.001, "theory");
                java.io.FileWriter fileWriter = new java.io.FileWriter(benchmarkFile);
                fileWriter.write(results.toString(4));
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean consoleOnly = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "--console-only".equalsIgnoreCase(arg)) {
                consoleOnly = true;
                break;
            }
        }

        final AppServer apiServer;
        if (!consoleOnly) {
            final int port = 3003;
            apiServer = new AppServer(port, graphService);

            // Start ApiServer in a background thread (daemon = true) exactly once
            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        apiServer.start();
                    } catch (IOException e) {
                        System.err.println("Could not start API Server on port " + port + ": " + e.getMessage());
                    }
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
        } else {
            apiServer = null;
        }

        // Start Console Menu in main thread
        ConsoleMenu consoleMenu = new ConsoleMenu(graphService);
        consoleMenu.start();

        // Shut down API server when console menu exits
        if (apiServer != null) {
            apiServer.stop();
        }
    }
}
