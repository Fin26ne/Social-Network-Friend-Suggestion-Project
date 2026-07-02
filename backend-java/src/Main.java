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

        // No-op startup benchmark generation (results are queried dynamically via API or runner)

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
