# 🌐 HỆ THỐNG GỢI Ý BẠN BÈ MẠNG XÃ HỘI (CSD201 - RBL PROJECT)

Hệ thống giả lập mạng xã hội thu nhỏ và công cụ đề xuất kết bạn thông minh dựa trên chỉ số tương đồng Jaccard. Dự án ứng dụng 100% cấu trúc dữ liệu tự định nghĩa để tối ưu hóa hiệu năng lưu trữ và tốc độ xử lý giải thuật.

## 👥 Phân Công Nhiệm Vụ Trong Nhóm
* **DEV 1 (Tech Lead):** Thiết kế cấu trúc dữ liệu tự chế (Custom Data Structures) & Thuật toán lõi.
* **DEV 2 (Backend / Core FE):** Quản lý tệp tin JSON, CLI tương tác và hệ thống API Endpoints.
* **DEV 4 (Researcher - Nguyễn Trí Thiện):** Chạy đo đạc kiểm thử hiệu năng (Benchmarking), lập báo cáo, thiết kế Frontend phân tích dữ liệu (`explore.html`, `network.html`, `research.html`).

---

## 🛠️ Giải Thích Cơ Chế, Ưu & Nhược Điểm Của 8 Cấu Trúc Dữ Liệu Tự Định Nghĩa
Để tuân thủ nghiêm ngặt yêu cầu không sử dụng các cấu trúc dữ liệu có sẵn trong thư viện (`java.util.*`), toàn bộ cấu trúc được xây dựng thủ công trong gói `src/datastructures/`:

### 1. MySinglyLinkedList.java
* **Cơ chế:** Danh sách liên kết đơn sử dụng các đối tượng Node bọc dữ liệu Generic và một con trỏ `next` tham chiếu đến phần tử tiếp theo.
* **Ưu điểm:** Quản lý bộ nhớ động linh hoạt, không bị giới hạn kích thước như mảng cố định. Hành vi thêm hoặc xóa một phần tử ở đầu danh sách đạt tốc độ tối đa $O(1)$.
* **Nhược điểm:** Truy cập ngẫu nhiên kém (muốn lấy phần tử thứ $k$ phải duyệt tuyến tính từ đầu mất $O(N)$). Tốn thêm không gian bộ nhớ để lưu trữ các con trỏ liên kết.

### 2. MyQueue.java
* **Cơ chế:** Hàng đợi hoạt động theo nguyên tắc FIFO (Vào trước Ra trước), được triển khai dựa trên nền tảng của Danh sách liên kết đơn nhằm tối ưu luồng dữ liệu.
* **Ưu điểm:** Các thao tác nạp phần tử (`enqueue`) và lấy phần tử (`dequeue`) đều đảm bảo độ phức tạp thời gian cố định $O(1)$. Phục vụ đắc lực cho việc lưu vết các nút khi duyệt đồ thị theo chiều rộng (BFS).
* **Nhược điểm:** Không hỗ trợ duyệt hoặc tìm kiếm trực tiếp các phần tử nằm ở giữa hàng đợi mà bắt buộc phải giải phóng lần lượt từ đầu queue.

### 3. MyBST.java
* **Cơ chế:** Cây nhị phân tìm kiếm (Binary Search Tree). Mỗi Node chứa một khóa (ID người dùng) và hai con trỏ trỏ sang cây con bên trái (giá trị nhỏ hơn) và cây con bên phải (giá trị lớn hơn).
* **Ưu điểm:** Tốc độ tìm kiếm, chèn và xóa phần tử lý thuyết đạt độ phức tạp $O(\log N)$. Hỗ trợ xuất danh sách User theo thứ tự ID tăng dần một cách dễ dàng thông qua phép duyệt cây theo thứ tự giữa (In-order Traversal).
* **Nhược điểm:** Trong trường hợp tệ nhất nếu dữ liệu nạp vào có xu hướng tăng dần hoặc giảm dần, cây sẽ bị thoái hóa thành một đường thẳng (tương đương Linked List), hiệu năng sụt giảm nghiêm trọng xuống $O(N)$.

### 4. MyMaxHeap.java
* **Cơ chế:** Đống cực đại được biểu diễn dưới dạng mảng tuyến tính nhưng tư duy theo cấu trúc cây nhị phân hoàn chỉnh, trong đó Node cha luôn có giá trị lớn hơn hoặc bằng các Node con.
* **Ưu điểm:** Luôn duy trì phần tử có độ tương đồng Jaccard lớn nhất ở vị trí gốc (`index 0`), cho phép lấy ra phần tử lớn nhất với tốc độ $O(1)$. Rất mạnh trong việc tái cấu trúc đống (`heapify`) mất $O(\log N)$.
* **Nhược điểm:** Thao tác tìm kiếm một phần tử ngẫu nhiên không phải là gốc diễn ra rất chậm vì phải quét qua toàn bộ các nhánh mảng.

### 5. MyMinHeap.java
* **Cơ chế:** Đống cực tiểu với kích thước cố định $K$. Hệ thống tích hợp hàm logic đặc biệt `offerIfBetter`. Khi duyệt qua danh sách mạng lưới, nếu phần tử mới có chỉ số gợi ý tốt hơn phần tử nhỏ nhất hiện tại ở gốc đống, hệ thống sẽ đá phần tử cũ ra và nạp phần tử mới vào rồi tự động cân bằng lại.
* **Ưu điểm:** Giới hạn dung lượng lưu trữ cố định ở mức $K$. Giúp giảm độ phức tạp thời gian khi lọc Top-K gợi ý kết bạn từ $O(N \log N)$ của thuật toán sắp xếp thông thường xuống chỉ còn $O(N \log K)$. Tiết kiệm tài nguyên bộ nhớ tối đa.
* **Nhược điểm:** Đòi hỏi chi phí tính toán liên tục để thực hiện so sánh hoán vị và duy trì cấu trúc đống mỗi khi có phần tử thỏa mãn điều kiện chèn.

### 6. MyGraph.java
* **Cơ chế:** Biểu diễn mạng xã hội bằng cấu trúc Đồ thị danh sách kề (Adjacency List). Để tối ưu hóa, danh sách kề của mỗi Node không dùng mảng hay Linked List thông thường mà được lưu trữ bằng một cây `MyBST`.
* **Ưu điểm:** Tiết kiệm không gian lưu trữ hơn nhiều so với ma trận kề khi biểu diễn đồ thị thưa (ít liên kết bạn bè). Tốc độ kiểm tra xem hai người dùng bất kỳ có phải là bạn của nhau không đạt hiệu năng tối đa $O(\log \text{deg})$ nhờ cấu trúc tra cứu của cây BST.
* **Nhược điểm:** Việc triển khai code cực kỳ phức tạp do lồng ghép cấu trúc cây nhị phân tìm kiếm vào bên trong danh sách liên kết đồ thị.

### 7. MyAdjacencyMatrix.java
* **Cơ chế:** Ma trận kề biểu diễn đồ thị bằng một mảng hai chiều kiểu boolean (`boolean[][]`), trong đó giá trị tại ô `matrix[i][j]` là `true` nếu User $i$ và User $j$ có quan hệ bạn bè.
* **Ưu điểm:** Kiểm tra mối quan hệ bạn bè trực tiếp giữa hai ID cực kỳ nhanh với thời gian tuyệt đối $O(1)$. Phục vụ trực tiếp cho Frontend bóc tách ma trận để vẽ sơ đồ trực quan.
* **Nhược điểm:** Gây lãng phí bộ nhớ nghiêm trọng với độ phức tạp không gian luôn là $O(N^2)$, bất kể đồ thị có nhiều hay ít cạnh kết nối.

### 8. MyDFS.java
* **Cơ chế:** Thuật toán duyệt đồ thị theo chiều sâu. Điểm cải tiến cốt lõi là thay vì dùng hàm đệ quy của hệ thống, thuật toán tự triển khai một cấu trúc `Stack` bộ nhớ đệm riêng bằng vòng lặp để quản lý các nút đã đi qua.
* **Ưu điểm:** Loại bỏ hoàn toàn nguy cơ xảy ra lỗi tràn bộ nhớ đệm hệ thống (`StackOverflowError`) khi quy mô mạng lưới phình to lên hàng ngàn tài khoản.
* **Nhược điểm:** Mã nguồn dài, khó kiểm soát logic hơn so với việc viết code đệ quy ngắn gọn thông thường.

---

## 📊 Bảng Kết Quả Thực Tế Thu Được Từ Phân Hệ Benchmark (Trả lời 3 RQs)
Dựa trên các số liệu thực nghiệm đo đạc thực tế thu được từ file `PerformanceTester.java` và báo cáo hiệu năng tại `docs/performance_report.md` khi tăng dần số lượng nút mạng từ 10 đến 1000 nút:

| Quy mô đồ thị (Số lượng nút) | Thời gian xử lý MaxHeap chiến lược ($O(N \log N)$) | Thời gian xử lý MinHeap Fixed-K ($O(N \log K)$) | Chiều cao cây BST (Dữ liệu ngẫu nhiên) | Chiều cao cây BST (Dữ liệu đã sắp xếp - Sorted) | Tốc độ tìm kiếm ArrayList | Tốc độ tìm kiếm HashSet |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **10 nút** | 1.2 ms | 0.4 ms | 4 | 10 (Thoái hóa) | 0.5 ms | 0.2 ms |
| **100 nút** | 8.5 ms | 2.1 ms | 9 | 100 (Thoái hóa) | 2.4 ms | 0.5 ms |
| **500 nút** | 35.2 ms | 7.4 ms | 13 | 500 (Thoái hóa) | 12.1 ms | 0.6 ms |
| **1000 nút** | 84.1 ms | 14.2 ms | 16 | 1000 (Thoái hóa) | 28.7 ms | 0.6 ms |

### KẾT LUẬN NGHIÊN CỨU (ANSWERS TO RQs):
* **RQ1 (ArrayList vs HashSet):** Khi quy mô tăng lên 1000 nút, tốc độ tìm kiếm bạn bè trên ArrayList tăng tuyến tính theo hàm bậc nhất $O(N)$ mất tới 28.7ms. Trong khi đó, nhờ cơ chế bảng băm, HashSet giữ vững tốc độ ở mức hằng số $O(1)$ ổn định (~0.6ms).
* **RQ2 (BST thoái hóa):** Khi chèn dữ liệu đã qua sắp xếp (Sorted), cây BST thuần bị mất cân bằng hoàn toàn, chiều cao cây chạm ngưỡng tối đa bằng đúng số nút (1000). Điều này khẳng định sự cần thiết phải nâng cấp lên cây tự cân bằng (như AVL Tree) để khống chế chiều cao ở mức tối ưu $\log_2(N) \approx 10$.
* **RQ3 (Chiến lược Đống):** Thực nghiệm chứng minh giải thuật sử dụng cấu trúc `MyMinHeap` kích thước cố định kết hợp hàm `offerIfBetter` giúp cắt giảm lượng tài nguyên tiêu thụ và tăng tốc độ xử lý nhanh gấp **5.9 lần** so với việc dùng `MyMaxHeap` sắp xếp toàn cục khi hệ thống đạt ngưỡng 1000 tài khoản.

---

## 🔌 Tài Liệu Hóa Danh Sách Hệ Thống API Endpoints (Cổng 3001)
Hệ thống Backend Java kích hoạt một Server HTTP trả về định dạng JSON mã hóa UTF-8 chuẩn hóa:
1. `GET /api/network`: Trả về cấu trúc toàn diện của đồ thị (gồm mảng danh sách các tài khoản Nodes, các liên kết quan hệ Links và ma trận kề boolean) để trang Frontend `network.html` sử dụng thư viện D3 Canvas vẽ sơ đồ mạng lưới.
2. `GET /api/suggestions?userId=X&k=Y`: Gọi đến `SuggestionService`, tính toán chỉ số tương đồng Jaccard, lọc ra Top-K tài khoản gợi ý kết bạn cho User X, đồng thời trả về kèm tham số thời gian xử lý thực tế (`runtime`) của thuật toán để trang `research.html` nạp dữ liệu vẽ biểu đồ Canvas.
3. `GET /api/friends?userId=X`: Lấy danh sách toàn bộ bạn bè chính thức của tài khoản mang ID tương ứng.
4. `POST /api/friends/mutual`: Truy vấn và trả về danh sách các tài khoản là bạn chung giữa các cặp người dùng được chỉ định.

---

## 🚀 Hướng Dẫn Vận Hành & Khởi Chạy Hệ Thống

### 1. Hướng dẫn biên dịch thủ công qua Command Line (CLI)
Nếu không vận hành thông qua các công cụ IDE trực quan (như Apache NetBeans), người dùng có thể thực hiện biên dịch mã nguồn thủ công bằng các câu lệnh sau tại thư mục gốc dự án:
```bash
# Tạo thư mục chứa các file thực thi sau khi biên dịch
mkdir bin

# Biên dịch toàn bộ các file mã nguồn Java sang mã bytecode .class
javac -d bin src/main/java/com/mycompany/csd2026/*.java
