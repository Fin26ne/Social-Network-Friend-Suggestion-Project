@echo off
echo ==================================================
echo      SOCIAL NETWORK FRIEND SUGGESTION STARTUP
echo ==================================================
echo Creating output directories...
if not exist out mkdir out

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib/json.jar" -d out src/model/Recommendation.java src/model/User.java src/datastructures/SinglyLinkedList.java src/datastructures/Queue.java src/datastructures/BinarySearchTree.java src/datastructures/MaxHeap.java src/datastructures/MinHeap.java src/datastructures/MySinglyLinkedList.java src/datastructures/MyQueue.java src/datastructures/MyBST.java src/datastructures/MyMaxHeap.java src/datastructures/MyMinHeap.java src/datastructures/MyGraph.java src/datastructures/MyAdjacencyMatrix.java src/datastructures/MyDFS.java src/datastructures/SuggestionService.java src/graph/Graph.java src/services/DataStore.java src/service/RecommendationEngine.java src/service/GraphService.java src/api/FriendHandler.java src/api/SuggestionHandler.java src/api/UserHandler.java src/api/NetworkHandler.java src/api/BenchmarkHandler.java src/api/AppServer.java src/console/ConsoleMenu.java src/benchmark/PerformanceTester.java src/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Starting Java application...
echo Opening web application and research dashboard in default browser...
start http://localhost:3001
start http://localhost:3001/research.html
java -cp "out;lib/json.jar" Main
