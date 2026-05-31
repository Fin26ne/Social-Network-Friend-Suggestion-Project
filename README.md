# 🌐 HỆ THỐNG GỢI Ý BẠN BÈ MẠNG XÃ HỘI (CSD201 - RBL PROJECT)

Hệ thống giả lập mạng xã hội thu nhỏ và công cụ đề xuất kết bạn thông minh dựa trên chỉ số tương đồng Jaccard. Dự án ứng dụng 100% cấu trúc dữ liệu tự định nghĩa để tối ưu hóa hiệu năng lưu trữ và tốc độ xử lý giải thuật.

## 👥 Phân Công Nhiệm Vụ Trong Nhóm
* **DEV 1 (Tech Lead):** Thiết kế cấu trúc dữ liệu tự chế (Custom Data Structures), tối ưu hóa `BenchmarkRunner.java` xuất dữ liệu hiệu năng ra hệ thống.
* **DEV 2 (Backend / Core FE):** Quản lý tệp tin JSON, CLI tương tác và hệ thống API Endpoints.
* **DEV 4 (Researcher - Nguyễn Trí Thiện):** Khảo sát đo đạc hiệu năng (Benchmarking), lập báo cáo, thiết kế Frontend phân tích dữ liệu (`explore.html`, `network.html`, `research.html`).

---

## 🛠️ Giải Thích Cơ Chế, Ưu & Nhược Điểm Của 8 Cấu Trúc Dữ Liệu Tự Định Nghĩa
Để tuân thủ nghiêm ngặt yêu cầu không sử dụng các cấu trúc dữ liệu có sẵn trong thư viện (`java.util.*`), toàn bộ cấu trúc được xây dựng thủ công trong gói `src/datastructures/` đúng theo phân tách hệ thống:

### 1. MySinglyLinkedList
* **Cơ chế:** Danh sách liên kết đơn sử dụng các đối tượng Node bọc dữ liệu Generic và một con trỏ `next` tham chiếu đến phần tử tiếp theo.
* **Ưu điểm:** Quản lý bộ nhớ động linh hoạt, không bị giới hạn kích thước như mảng cố định. Thao tác thêm hoặc xóa phần tử ở đầu danh sách đạt tốc độ tối ưu $O(1)$.
* **Nhược điểm:** Truy cập ngẫu nhiên kém (muốn lấy phần tử thứ $k$ phải duyệt tuyến tính từ đầu mất $O(N)$). Tốn thêm bộ nhớ để lưu trữ các biến con trỏ liên kết.

### 2. MyQueue
* **Cơ chế:** Hàng đợi tĩnh hoạt động theo nguyên tắc FIFO (Vào trước Ra trước), được triển khai tối ưu trên nền tảng của cấu trúc danh sách liên kết.
* **Ưu điểm:** Thao tác nạp phần tử (`enqueue`) và lấy phần tử (`dequeue`) đều đảm bảo độ phức tạp thời gian cố định $O(1)$. Phục vụ đắc lực cho việc lưu vết các nút khi duyệt đồ thị theo chiều rộng (BFS).
* **Nhược điểm:** Không hỗ trợ duyệt hoặc tra cứu trực tiếp các phần tử nằm ở giữa hàng đợi mà bắt buộc phải giải phóng lần lượt từ đầu queue.

### 3. MyBST
* **Cơ chế:** Cây nhị phân tìm kiếm (Binary Search Tree). Mỗi Node chứa một khóa (ID người dùng) và hai con trỏ trỏ sang cây con bên trái (giá trị nhỏ hơn) và cây con bên phải (giá trị lớn hơn).
* **Ưu điểm:** Tốc độ tìm kiếm, chèn và xóa phần tử lý thuyết đạt độ phức tạp $O(\log N)$. Hỗ trợ xuất danh sách User theo thứ tự ID tăng dần thông qua phép duyệt cây theo thứ tự giữa (In-order Traversal).
* **Nhược điểm:** Trong trường hợp tệ nhất nếu dữ liệu nạp vào có xu hướng đã sắp xếp (Sorted), cây sẽ bị thoái hóa hoàn toàn thành một đường thẳng (tương đương Linked List), hiệu năng sụt giảm nghiêm trọng xuống $O(N)$.

### 4. MyMaxHeap
* **Cơ chế:** Cấu trúc vun đống cực đại được biểu diễn dưới dạng mảng tuyến tính nhưng tư duy theo cấu trúc cây nhị phân hoàn chỉnh, trong đó Node cha luôn có giá trị lớn hơn hoặc bằng các Node con.
* **Ưu điểm:** Luôn duy trì phần tử có độ tương đồng Jaccard lớn nhất ở vị trí gốc (`index 0`), cho phép lấy ra phần tử lớn nhất với tốc độ $O(1)$. Rất mạnh trong việc tái cấu trúc đống (`heapify`) mất $O(\log N)$.
* **Nhược điểm:** Thao tác tìm kiếm một phần tử ngẫu nhiên không phải là gốc diễn ra rất chậm vì phải quét qua toàn bộ các nhánh mảng.

### 5. MyMinHeap
* **Cơ chế:** Cấu trúc vun đống cực tiểu với kích thước cố định $K$. Hệ thống tích hợp hàm logic đặc biệt `offerIfBetter`. Khi duyệt qua danh sách mạng lưới, nếu phần tử mới có chỉ số gợi ý tốt hơn phần tử nhỏ nhất hiện tại ở gốc đống, hệ thống sẽ loại bỏ phần tử cũ ra và nạp phần tử mới vào rồi tự động cân bằng lại.
* **Ưu điểm:** Giới hạn dung lượng lưu trữ cố định ở mức $K$. Giúp giảm độ phức tạp thời gian khi lọc Top-K gợi ý kết bạn từ $O(N \log N)$ của thuật toán sắp xếp thông thường xuống chỉ còn $O(N \log K)$. Tiết kiệm tài nguyên bộ nhớ tối đa.
* **Nhược điểm:** Đòi hỏi chi phí tính toán liên tục để thực hiện so sánh hoán vị và duy trì cấu trúc đống mỗi khi có phần tử thỏa mãn điều kiện chèn.

### 6. MyGraph
* **Cơ chế:** Biểu diễn mạng xã hội bằng cấu trúc Đồ thị danh sách kề (Adjacency List). Để tối ưu hóa, danh sách kề của mỗi Node không dùng mảng hay Linked List thông thường mà được lưu trữ bằng một cây `MyBST`.
* **Ưu điểm:** Tiết kiệm không gian lưu trữ hơn nhiều so với ma trận kề khi biểu diễn đồ thị thưa (ít liên kết bạn bè). Tốc độ kiểm tra xem hai người dùng bất kỳ có phải là bạn của nhau không đạt hiệu năng tối đa $O(\log \text{deg})$ nhờ cấu trúc tra cứu của cây BST.
* **Nhược điểm:** Việc triển khai code phức tạp do lồng ghép cấu trúc cây nhị phân tìm kiếm vào bên trong danh sách liên kết đồ thị.

### 7. MyAdjacencyMatrix
* **Cơ chế:** Biểu diễn đồ thị bằng một ma trận kề hai chiều kiểu boolean (`boolean[][]`), trong đó giá trị tại ô `matrix[i][j]` là `true` nếu User $i$ và User $j$ có mối quan hệ bạn bè trực tiếp.
* **Ưu điểm:** Kiểm tra mối quan hệ bạn bè trực tiếp giữa hai ID cực kỳ nhanh với thời gian tuyệt đối $O(1)$. Phục vụ trực tiếp cho Frontend bóc tách ma trận kề biểu diễn cấu trúc nền tảng.
* **Nhược điểm:** Gây lãng phí bộ nhớ nghiêm trọng với độ phức tạp không gian luôn là $O(N^2)$, bất kể đồ thị có nhiều hay ít cạnh kết nối.

### 8. MyDFS
* **Cơ chế:** Thuật toán duyệt đồ thị theo chiều sâu. Điểm cải tiến cốt lõi là thay vì dùng hàm đệ quy của hệ thống, thuật toán tự triển khai một cấu trúc `Stack` bộ nhớ đệm riêng bằng vòng lặp (thuật toán duyệt không đệ quy) để quản lý các nút đã đi qua.
* **Ưu điểm:** Loại bỏ hoàn toàn nguy cơ xảy ra lỗi tràn bộ nhớ đệm hệ thống (`StackOverflowError`) khi quy mô mạng lưới phình to lên hàng ngàn tài khoản.
* **Nhược điểm:** Mã nguồn dài, khó kiểm soát logic hơn so với việc viết code đệ quy ngắn gọn thông thường.

---

## 📊 Kết Quả Thực Tế Thu Được Từ Phân Hệ Benchmark (Trả lời 3 RQs)
Hệ thống lưu trữ và đo đạc hiệu năng thông qua module `BenchmarkRunner.java`, tự động chạy với tốc độ tối ưu dưới 2 giây cho dải dữ liệu quy mô mở rộng lên tới $N=10000$ và kết xuất đồng bộ ra file dữ liệu thực tế tại `data/benchmark_results.json`. Giao diện hiển thị `frontend/research.html` bóc tách trực tiếp các khóa JSON (`rq1`, `rq2`, `rq3`) để biểu diễn đồ thị trực quan:

* **RQ1 (ArrayList vs HashSet cho hàm contains):** Khi duyệt tìm kiếm bạn bè tuyến tính trên ArrayList, hệ thống mất thời gian tăng dần theo đồ thị hàm số bậc nhất $O(N)$. Khi chuyển sang bảng băm HashSet, thời gian tra cứu giữ vững ở mức hằng số hằng số $O(1)$, chứng minh hiệu quả vượt trội khi quy mô dữ liệu phình to.
* **RQ2 (BST vs AVL Tree khi chèn dữ liệu Sorted):** Nếu dữ liệu User nạp vào hệ thống đã được sắp xếp tăng dần, cây BST thuần bị mất cân bằng và thoái hóa hoàn toàn thành một đường thẳng (chiều cao bằng đúng số User $N$), hiệu năng sụt giảm xuống $O(N)$. Cấu trúc AVL tự cân bằng khống chế chiều cao lý tưởng ở mức $\log_2(N)$, bảo toàn hiệu năng hệ thống.
* **RQ3 (Hiệu năng MaxHeap vs MinHeap Fixed-K):** Đo đạc thực nghiệm chỉ ra chiến lược ứng dụng đống `MyMinHeap` kích thước cố định kết hợp giải thuật kiểm thử `offerIfBetter` giúp tiết kiệm dung lượng tài nguyên tiêu thụ và tăng tốc độ xử lý nhanh gấp nhiều lần so với việc dùng `MyMaxHeap` sắp xếp toàn cục khi trích xuất danh sách Top-K bạn bè gợi ý.

---

## 🔌 Tài Liệu Hóa Danh Sách Hệ Thống API Endpoints (Cổng 3001)
Hệ thống Backend Java kích hoạt một Server HTTP trả về định dạng JSON mã hóa UTF-8 chuẩn hóa:
1. `GET /api/network`: Trả về cấu trúc đồ thị mạng lưới phục vụ trang Frontend `network.html` sử dụng thư viện Canvas vẽ sơ đồ quan hệ.
2. `GET /api/suggestions?userId=X&k=Y`: Tính toán chỉ số tương đồng Jaccard, lọc ra danh sách tài khoản gợi ý kết bạn cho User X, đồng thời trả về kèm tham số thời gian xử lý thực tế phục vụ trang `research.html` nạp dữ liệu vẽ biểu đồ Canvas.
3. `GET /api/friends?userId=X`: Lấy danh sách toàn bộ bạn bè chính thức của tài khoản mang ID tương ứng.
4. `POST /api/friends/mutual`: Truy vấn và trả về danh sách các tài khoản là bạn chung giữa các cặp người dùng được chỉ định.

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG QUA FILE KỊCH BẢN AUTORUN

Để thuận tiện cho quá trình vận hành và kiểm thử hệ thống, nhà phát triển không cần thực hiện các câu lệnh biên dịch thủ công phức tạp. Hãy khởi chạy ứng dụng trực tiếp bằng các bước sau:

1. Đảm bảo môi trường máy tính chạy Windows của bạn đã được cài đặt cấu hình mã nguồn JDK phù hợp.
2. Di chuyển vào thư mục ngoài cùng của dự án.
3. Nhấp đúp chuột (Double-click) vào tệp tin **`startup.bat`** để chạy kịch bản tự động trên Windows CMD.
4. File kịch bản sẽ tự động liên kết mã nguồn, khởi chạy Server HTTP Daemon ẩn tại cổng máy chủ `3001`, đồng thời kích hoạt giao diện Console điều khiển 12 chức năng tương tác trực tiếp.
5. Để xem các biểu đồ phân tích hiệu năng nghiên cứu, lập trình viên truy cập vào thư mục `frontend/` và mở trực tiếp tệp tin `research.html` trên bất kỳ trình duyệt web nào để hệ thống tự động bóc tách dữ liệu từ `data/benchmark_results.json` hiển thị trực quan.
