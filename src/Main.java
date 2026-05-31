import api.AppServer;
import console.ConsoleMenu;
import service.GraphService;
import datastructures.SuggestionService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String dataDirectoryPath = "backend-java/data";
        
        System.out.println("Initializing Social Network Graph...");
        GraphService graphService = new GraphService(dataDirectoryPath);

        // Run SuggestionService self-test before starting the server
        SuggestionService.runSelfTest();

        boolean consoleOnly = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "--console-only".equalsIgnoreCase(arg)) {
                consoleOnly = true;
                break;
            }
        }

        final AppServer apiServer;
        if (!consoleOnly) {
            final int port = 3001;
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
