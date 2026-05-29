import api.AppServer;
import api.ConsoleMenu;
import service.GraphService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String dataFilePath = "data/social_network.json";
        
        System.out.println("Initializing Social Network Graph...");
        GraphService graphService = new GraphService(dataFilePath);

        boolean consoleOnly = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "--console-only".equalsIgnoreCase(arg)) {
                consoleOnly = true;
                break;
            }
        }

        AppServer apiServer = null;
        if (!consoleOnly) {
            int port = 8080;
            apiServer = new AppServer(port, graphService);
            try {
                apiServer.start();
                System.out.println("Web Dashboard is running at: http://localhost:" + port + "/");
            } catch (IOException e) {
                System.err.println("Could not start API Server on port " + port + ": " + e.getMessage());
                System.err.println("System will fall back to console-only mode.");
            }
        }

        // Start Console Menu
        ConsoleMenu consoleMenu = new ConsoleMenu(graphService);
        consoleMenu.start();

        // Shut down API server when console menu exits
        if (apiServer != null) {
            apiServer.stop();
        }
    }
}
