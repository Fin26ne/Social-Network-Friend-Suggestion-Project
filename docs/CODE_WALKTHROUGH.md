# Code Walkthrough — Social Network Friend Suggestion
## CSD201 · Topic 05 · FPT University SU26

---

## 1. Kiến trúc tổng thể
Hệ thống được tổ chức thành 4 Layer kiến trúc rõ rệt để tăng tính độc lập, dễ bảo trì và phân tách trách nhiệm (Separation of Concerns):
1. **LAYER 1 — Data Structures (`src/datastructures/`)**: Chứa toàn bộ các cấu trúc dữ liệu tự viết tay thuần JDK 1.8 bao gồm `MySinglyLinkedList`, `MyQueue`, `MyBST`, `MyMaxHeap`, `MyMinHeap`, `MyGraph`, `MyAdjacencyMatrix`, và `MyDFS`.
2. **LAYER 2 — Models (`src/model/`)**: Định nghĩa thực thể dữ liệu chính: `User` (thông tin người dùng), `Friendship` (quan hệ kết bạn), và `Recommendation` (đối tượng đề xuất kết bạn bọc User cùng số bạn chung và độ tương đồng Jaccard).
3. **LAYER 3 — Services (`src/service/` và `src/services/`)**: Gồm `DataStore` (nạp/ghi dữ liệu JSON từ đĩa), `GraphService` (quản lý đồ thị người dùng hệ thống), và `RecommendationEngine` / `SuggestionService` (thực hiện thuật toán tính độ tương đồng Jaccard và lọc Top-K gợi ý kết bạn).
4. **LAYER 4 — API (`src/api/`)**: Server HTTP thuần sử dụng `com.sun.net.httpserver` bọc các Handler để tiếp nhận yêu cầu từ Frontend và phản hồi định dạng JSON chuẩn.

Sơ đồ mô tả mối quan hệ giữa các Layer:
```text
+-------------------------------------------------------------+
|                     LAYER 4 — API (REST)                    |
|  [AppServer] -> [UserHandler], [FriendHandler],             |
|                 [SuggestionHandler], [BenchmarkHandler]     |
+------------------------------+------------------------------+
                               | sử dụng
                               v
+-------------------------------------------------------------+
|                   LAYER 3 — SERVICES                        |
|  [GraphService] <---+---> [RecommendationEngine]            |
|       |             |           |                           |
|       | sử dụng     | sử dụng   | sử dụng                   |
|       v             v           v                           |
|  [DataStore]   [MyGraph]   [MyMinHeap] / [MyMaxHeap]        |
+---------------------+-----------+---------------------------+
                      |           |
                      | sử dụng   | sử dụng
                      v           v
+-------------------------------------------------------------+
|                 LAYER 2 — MODELS (Dữ liệu)                  |
|  [User] <=========> [Recommendation]                        |
+---------------------+-----------+---------------------------+
                      |           |
                      | sử dụng   | sử dụng
                      v           v
+-------------------------------------------------------------+
|              LAYER 1 — CUSTOM DATA STRUCTURES               |
|  [MySinglyLinkedList] <--- [MyQueue]                        |
|  [MyBST] <--- [MyGraph]                                     |
|  [MyAdjacencyMatrix], [MyDFS]                               |
+-------------------------------------------------------------+
```

---

## 2. Custom Data Structures

### 2.1 MySinglyLinkedList
* **Mục đích & Use Case**: Quản lý tập hợp động các phần tử không xác định trước số lượng (ví dụ: danh sách bạn bè của một User, danh sách kết quả BFS/DFS, hoặc các phần tử trong Heap/Stack).
* **Code snippet quan trọng nhất (`remove`)**:
```java
public boolean remove(T data) {
    if (head == null) return false;

    if (head.data.equals(data)) {
        head = head.next;
        size--;
        return true;
    }

    Node<T> curr = head;
    while (curr.next != null) {
        if (curr.next.data.equals(data)) {
            curr.next = curr.next.next;
            size--;
            return true;
        }
        curr = curr.next;
    }
    return false;
}
```
* **Tại sao dùng ở đây thay vì java.util.LinkedList**:
  - Tuân thủ quy định nghiêm ngặt của môn học CSD201: Không được sử dụng các cấu trúc dữ liệu có sẵn của Java.
  - Tối giản hóa mã nguồn, chỉ giữ lại các thao tác cần thiết giúp giảm overhead bộ nhớ của JVM.
  - Tự cài đặt cơ chế duyệt `Iterable` thủ công để duyệt bằng cú pháp `for-each` tiện lợi mà không phụ thuộc thư viện ngoài.

### 2.2 MyBST
* **Giải thích**: Cây tìm kiếm nhị phân (Binary Search Tree) dùng để lưu trữ các cặp Key-Value. Trong hệ thống, `MyBST` đóng vai trò là một Index tốc độ cao giúp tra cứu thông tin User theo ID, hoặc tra cứu vị trí đỉnh trong danh sách kề.
* **Code snippet `delete()` xử lý đủ 3 trường hợp**:
```java
public void delete(K key) {
    if (key == null) throw new IllegalArgumentException("Key cannot be null");
    if (contains(key)) {
        root = deleteHelper(root, key);
        size--;
    }
}

private Node<K, V> deleteHelper(Node<K, V> node, K key) {
    if (node == null) return null;

    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
        node.left = deleteHelper(node.left, key);
    } else if (cmp > 0) {
        node.right = deleteHelper(node.right, key);
    } else {
        // Case 1 & 2: Node lá hoặc chỉ có 1 con
        if (node.left == null) {
            return node.right;
        }
        if (node.right == null) {
            return node.left;
        }

        // Case 3: Node có đủ 2 con. Tìm successor (nút nhỏ nhất bên nhánh phải)
        Node<K, V> successor = findMin(node.right);
        node.key = successor.key;
        node.value = successor.value;
        node.right = deleteHelper(node.right, successor.key);
    }
    return node;
}

private Node<K, V> findMin(Node<K, V> node) {
    while (node.left != null) {
        node = node.left;
    }
    return node;
}
```
* **Tại sao dùng BST để index users**:
  - Thay thế cho `java.util.HashMap` hoặc `TreeMap` bị cấm trong môn học.
  - BST cho hiệu năng tra cứu và chèn lý thuyết trung bình là $O(\log N)$.
  - Phép duyệt cây `In-order Traversal` (trái - gốc - phải) giúp trích xuất danh sách User đã được sắp xếp tăng dần theo ID tự nhiên cực kỳ nhanh chóng.

### 2.3 MyGraph + BFS Level-2
* **Giải thích kỹ**: Mạng quan hệ bạn bè của hệ thống được biểu diễn dưới dạng Đồ thị vô hướng (Undirected Graph) thông qua cấu trúc Danh sách kề. Tuy nhiên, thay vì lưu danh sách láng giềng bằng mảng hay LinkedList tuần tự tốn $O(deg)$, ta kết hợp lưu trữ neighbor bằng cấu trúc cây `MyBST` trong mỗi đỉnh để tăng tốc kiểm tra quan hệ kết bạn trực tiếp lên $O(\log deg)$.
* **BFS Level-2** là thuật toán tìm kiếm theo chiều rộng giới hạn ở mức 2 (friends-of-friends) để thu thập ứng viên kết bạn tiềm năng. Thuật toán sẽ duyệt qua các bạn bè trực tiếp (Level 1) của User, sau đó tiếp tục duyệt tới bạn bè của họ (Level 2), loại bỏ chính mình (self) và các bạn bè đã kết nối trực tiếp.
* **Code snippet `bfsLevel2()`**:
```java
public String[] bfsLevel2(String startUserId) {
    if (startUserId == null || !hasVertex(startUserId)) {
        return new String[0];
    }

    MySinglyLinkedList<String> directFriends = getNeighbors(startUserId);
    if (directFriends.isEmpty()) {
        return new String[0];
    }

    // Bộ loại trừ chứa bản thân và bạn bè trực tiếp
    MyBST<String, Boolean> excluded = new MyBST<>();
    excluded.put(startUserId, true);
    for (String friend : directFriends) {
        excluded.put(friend, true);
    }

    MySinglyLinkedList<String> resultList = new MySinglyLinkedList<>();
    MyBST<String, Boolean> visitedFOF = new MyBST<>();

    MyQueue<String> queue = new MyQueue<>();
    // Đưa tất cả bạn bè trực tiếp vào hàng đợi
    for (String friend : directFriends) {
        queue.enqueue(friend);
    }

    // Duyệt qua bạn bè trực tiếp để tìm bạn của bạn (Level 2)
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

    // Chuyển kết quả sang mảng String
    String[] result = new String[resultList.size()];
    int idx = 0;
    for (String val : resultList) {
        result[idx++] = val;
    }
    return result;
}
```
* **Trace thủ công ví dụ với 5 nodes**:
  Mạng quan hệ:
  - User A → bạn: B, C
  - User B → bạn: A, D, E
  - User C → bạn: A, D
  - User D → bạn: B, C
  - User E → bạn: B

  **Quá trình chạy BFS Level-2 xuất phát từ A**:
  1. `directFriends` của A là: `[B, C]`.
  2. Khởi tạo danh sách loại trừ `excluded` = `{A, B, C}` (gồm chính A và bạn trực tiếp).
  3. Đưa các bạn trực tiếp vào Queue: `queue` = `[B, C]`.
  4. Lấy `B` ra khỏi `queue` (dequeue):
     - Láng giềng của B là `[A, D, E]`.
     - `A` nằm trong `excluded` -> bỏ qua.
     - `D` không nằm trong `excluded` và chưa thăm -> đánh dấu `visitedFOF = true`, chèn vào `resultList`.
     - `E` không nằm trong `excluded` và chưa thăm -> đánh dấu `visitedFOF = true`, chèn vào `resultList`.
  5. Lấy `C` ra khỏi `queue` (dequeue):
     - Láng giềng của C là `[A, D]`.
     - `A` nằm trong `excluded` -> bỏ qua.
     - `D` đã nằm trong `visitedFOF` -> bỏ qua (nhưng ta biết D có kết nối qua cả B và C, tức số bạn chung `mutual = 2`).
  6. Kết quả candidates cuối cùng thu được là: `[D, E]`.

### 2.4 MyMaxHeap vs MyMinHeap
* **So sánh 2 cách**:
  - **MaxHeap**: Thêm toàn bộ $N$ người dùng trong hệ thống (không phải bạn trực tiếp) vào Heap, sau đó thực hiện lấy phần tử lớn nhất (`extractMax()`) ra $K$ lần để lấy Top-K gợi ý.
    - Độ phức tạp thời gian: $O(N \log N)$ (chèn $N$ phần tử) + $O(K \log N)$ (lấy ra $K$ lần).
    - Độ phức tạp không gian: $O(N)$ (lưu trữ tất cả ứng viên).
  - **MinHeap**: Duy trì một Heap có kích thước cố định tối đa bằng $K$. Khi duyệt qua $N$ người dùng, ta so sánh điểm số của người dùng hiện tại với phần tử nhỏ nhất ở đỉnh Heap (thông qua hàm `offerIfBetter`). Nếu tốt hơn, ta loại bỏ đỉnh và chèn người dùng mới vào.
    - Độ phức tạp thời gian: $O(N \log K)$.
    - Độ phức tạp không gian: Chỉ tốn $O(K)$ bộ nhớ đệm phụ.
* **Tại sao chọn MinHeap-K**: Khi quy mô mạng xã hội phình to ($N$ lớn), $K$ gợi ý (thường $K=5$ hoặc $K=10$) luôn cực nhỏ so với $N$ ($K \ll N$). Do đó, $\log K$ trở thành hằng số nhỏ. Hiệu năng MinHeap tối ưu hơn vượt trội so với MaxHeap, giúp tiết kiệm bộ nhớ và thời gian tính toán đống đáng kể.
* **Code snippet `offerIfBetter()`**:
```java
public void offerIfBetter(T item) {
    if (size < capacity) {
        insert(item);
    } else if (item.compareTo(peek()) > 0) {
        extractMin();
        insert(item);
    }
}
```

---

## 3. Thuật toán gợi ý bạn bè

### Bước 1 — Thu thập ứng viên (BFS Level-2)
Hệ thống lọc ra tất cả những người dùng có khoảng cách kết nối bằng đúng 2 (bạn của bạn). Điều này thu hẹp phạm vi tính toán, tránh việc tính điểm tương đồng với toàn bộ mạng lưới (hàng ngàn hoặc hàng triệu tài khoản không liên quan).
Thực thi thông qua phương thức `bfsLevel2()` đã trình bày ở mục 2.3.

### Bước 2 — Tính Jaccard Similarity
* **Công thức**:
  $$\text{Jaccard}(A, B) = \frac{|A \cap B|}{|A \cup B|} = \frac{\text{Số bạn chung}}{\text{Tổng số bạn bè không trùng lặp của cả hai}}$$
* **Code snippet tính manual loop**:
```java
public double computeJaccard(MyGraph graph, String userId1, String userId2) {
    if (!graph.hasVertex(userId1) || !graph.hasVertex(userId2)) {
        return 0.0;
    }
    MySinglyLinkedList<String> friends1 = graph.getNeighbors(userId1);
    MySinglyLinkedList<String> friends2 = graph.getNeighbors(userId2);
    
    if (friends1.isEmpty() || friends2.isEmpty()) {
        return 0.0;
    }

    int intersection = 0;
    for (String f1 : friends1) {
        if (friends2.contains(f1)) {
            intersection++;
        }
    }

    int size1 = friends1.size();
    int size2 = friends2.size();
    int union = size1 + size2 - intersection;

    if (union == 0) return 0.0;
    return (double) intersection / union;
}
```
* **Ví dụ cụ thể với số thực**:
  Giả sử ta cần tính độ tương đồng giữa User A và User E:
  - Bạn trực tiếp của A: `[B, C, D]` -> $|A| = 3$.
  - Bạn trực tiếp của E: `[B, C, F]` -> $|E| = 3$.
  - Giao nhau (Bạn chung): `[B, C]` -> $\text{intersection} = 2$.
  - Hợp nhau: `[B, C, D, F]` -> $\text{union} = 3 + 3 - 2 = 4$.
  - Điểm tương đồng Jaccard: $2 / 4 = 0.50$ (tương đương 50% trùng lặp mạng bạn bè).

### Bước 3 — Xếp hạng Top-K (MinHeap)
Thay vì lưu trữ toàn bộ ứng viên rồi gọi các hàm Sort toàn cục tốn kém $O(N \log N)$, ta duyệt qua danh sách ứng viên và đẩy vào `MyMinHeap` kích thước $K$.
Điểm so sánh giữa hai đối tượng `Recommendation` dựa trên độ tương đồng Jaccard cao nhất, nếu bằng nhau thì so sánh số lượng bạn chung, và cuối cùng sắp xếp theo thứ tự bảng chữ cái ID để đảm bảo tính ổn định (stable).
* **Code snippet**:
```java
MyMinHeap<Recommendation> minHeap = new MyMinHeap<>(k);
for (String targetId : allUserIds) {
    if (targetId.equals(userId) || graph.hasEdge(userId, targetId)) continue;
    
    double jaccard = computeJaccard(graph, userId, targetId);
    if (jaccard <= 0) continue;

    int mutuals = getMutualFriendsCount(graph, userId, targetId);
    User targetUser = userBst.get(targetId);
    minHeap.offerIfBetter(new Recommendation(targetUser, mutuals, jaccard));
}
return minHeap.toSortedList(); // Trả về danh sách được sắp xếp giảm dần (gợi ý tốt nhất lên đầu)
```

---

## 4. Research Questions

### RQ1 — BFS vs DFS
* **Phân tích lý thuyết**:
  Cả BFS và DFS đều có cùng độ phức tạp thời gian $O(V + E)$ khi duyệt đồ thị. Trong hệ thống này, cả hai thuật toán đều được triển khai ở `MyGraph.bfsLevel2()` và `MyDFS.dfsLevel2()` để tìm bạn bè cấp 2 (friends-of-friends).
* **So sánh thực tế trong code**:

  | Tiêu chí | BFS (`MyGraph.bfsLevel2`) | DFS (`MyDFS.dfsLevel2`) |
  |:---|:---|:---|
  | Cấu trúc hỗ trợ | `MyQueue` (FIFO) | `MySinglyLinkedList` làm Stack (LIFO) |
  | Đảm bảo đúng Level-2 | ✅ Duyệt từng tầng, chính xác | ✅ Có, nhưng phải theo dõi `depth` |
  | Nguy cơ StackOverflow | ❌ Không (dùng queue iterative) | ❌ Không (dùng explicit stack, không đệ quy) |
  | Overhead bộ nhớ | Thấp hơn (chỉ cần queue) | Cao hơn (tạo object `StackNode` cho mỗi phần tử) |

* **Kết luận**: BFS là lựa chọn mặc định trong hệ thống vì:
  1. Bản chất BFS duyệt đúng theo tầng (level-by-level) — phù hợp tự nhiên với bài toán tìm bạn cấp 2.
  2. Code đơn giản hơn, ít bug hơn, không cần quản lý biến `depth`.
  3. DFS được triển khai thêm trong `MyDFS.java` chủ yếu để **so sánh nghiên cứu**, không phải để dùng chính.

### RQ2 — Adjacency List vs Matrix
* **Công thức tính bộ nhớ thực tế trên JVM**:
  - **Adjacency Matrix** (dùng `boolean[][]`):
    $$\text{RAM} = 24 + (24 + N) \times N \text{ (Bytes)}$$
    Bộ nhớ tăng theo **bình phương** $O(N^2)$, không phụ thuộc số cạnh thực tế.
  - **Adjacency List** (dùng BST + LinkedList):
    $$\text{RAM} \approx V \times 56 + E \times 56 \text{ (Bytes)}$$
    Bộ nhớ tăng **tuyến tính** theo số đỉnh + số cạnh $O(V + E)$.

* **Bảng so sánh theo quy mô** (mật độ cạnh ~3%):

  | Quy mô N | Số cạnh E | Adjacency List | Adjacency Matrix | Ai tốt hơn? |
  |:---:|:---:|:---:|:---:|:---:|
  | 100 | ~292 | ~21.9 KB | ~12.2 KB | Matrix nhỏ hơn |
  | 1,000 | ~29,085 | ~1.64 MB | ~1.0 MB | Matrix nhỏ hơn |
  | 5,000 | ~50,000 | ~3.36 MB | ~23.9 MB | **List nhỏ hơn 7x** |
  | 10,000 | ~100,000 | ~6.15 MB | ~95.6 MB | **List nhỏ hơn 15x** |

* **Phân tích — Điểm giao cắt (crossover point)**:
  Ở quy mô nhỏ ($N \le 1000$), Matrix tiêu thụ ít bộ nhớ hơn List do overhead cố định của các đối tượng Java (mỗi Node chiếm ~56 bytes gồm object header + con trỏ + dữ liệu). Tuy nhiên, khi $N$ vượt qua ~2000–3000 đỉnh, bộ nhớ Matrix **bùng nổ theo $O(N^2)$** trong khi List chỉ tăng tuyến tính. Đối với mạng xã hội thực tế (thường có hàng triệu người dùng và mật độ kết nối rất thưa $E \ll N^2$), Adjacency List là lựa chọn duy nhất khả thi.

### RQ3 — MinHeap-K vs MaxHeap
* **Số liệu benchmark thực tế** (từ `PerformanceTester`, Top-K với $K=10$, trung bình 10 lần chạy):

  | Quy mô N | Cạnh E | MaxHeap (ms) | MinHeap (ms) | So sánh |
  |:---:|:---:|:---:|:---:|:---|
  | 10 | 3 | 0.0531 | 0.0408 | MinHeap nhanh hơn 1.3x |
  | 50 | 72 | 0.1241 | 0.1466 | MaxHeap nhanh hơn (N nhỏ) |
  | 100 | 292 | 0.2149 | 0.2326 | Gần tương đương |
  | 250 | 1,818 | 0.5592 | 0.6427 | Gần tương đương |
  | 500 | 7,236 | 3.4858 | 3.5854 | Gần tương đương |
  | 750 | 16,319 | 12.0709 | 12.0250 | MinHeap bắt đầu nhanh hơn |
  | **1,000** | **29,085** | **28.4094** | **25.7979** | **MinHeap nhanh hơn 1.1x** |

* **Phân tích chi tiết**:
  - Ở quy mô nhỏ ($N \le 500$), MinHeap và MaxHeap cho kết quả gần tương đương hoặc MaxHeap thậm chí nhanh hơn. Nguyên nhân: khi $N$ nhỏ, $\log N$ và $\log K$ chênh lệch không đáng kể, nhưng MinHeap phải thực hiện thêm phép so sánh `offerIfBetter()` (so sánh rồi mới quyết định chèn/loại bỏ) gây overhead.
  - Ở quy mô lớn ($N \ge 750$), MinHeap bắt đầu vượt trội. Khi $N$ tiếp tục tăng, khoảng cách sẽ ngày càng rõ rệt vì:
    - **MaxHeap**: $O(N \log N)$ — phải chèn **tất cả** $N$ ứng viên vào Heap kích thước $N$, mỗi lần chèn tốn $O(\log N)$.
    - **MinHeap**: $O(N \log K)$ — chỉ duy trì Heap kích thước cố định $K=10$, mỗi lần chèn tốn $O(\log 10) \approx O(1)$.
  - Với mạng xã hội thực tế ($N = 10,000$ hoặc $100,000$), MinHeap sẽ nhanh hơn **gấp nhiều lần** so với MaxHeap.

---

## 5. Câu hỏi vấn đáp thường gặp

| Câu hỏi | Câu trả lời ngắn |
|---------|----------------|
| Tại sao không dùng Adjacency Matrix? | Đồ thị mạng xã hội thưa (Sparse graph) — Matrix tốn bộ nhớ $O(N^2)$, bùng nổ khi $N > 3000$. |
| BFS vs DFS cái nào tốt hơn? | BFS duyệt đúng theo tầng (level-by-level) — phù hợp tự nhiên để tìm bạn cấp 2. DFS cần quản lý depth phức tạp hơn. |
| Tại sao BST không phải HashMap? | Quy định CSD201 cấm dùng `java.util.*`. BST tự viết cho phép In-order traversal trả về danh sách có thứ tự. |
| Jaccard có điểm yếu gì? | Coi mọi mối quan hệ bạn bè có trọng số bằng nhau, không phân biệt bạn thân vs bạn sơ. |
| Tại sao MinHeap thay vì MaxHeap? | MinHeap-K giữ đống kích thước cố định $K$, giảm complexity từ $O(N \log N)$ xuống $O(N \log K)$. Lợi thế rõ rệt khi $N$ lớn ($K \ll N$). |
| MinHeap có luôn nhanh hơn MaxHeap không? | Không — ở $N$ nhỏ ($\le 500$), chênh lệch không đáng kể. MinHeap vượt trội khi $N \ge 750$. |
