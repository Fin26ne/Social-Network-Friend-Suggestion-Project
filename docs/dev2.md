# Tài Liệu Nhiệm Vụ và Các File Mã Nguồn của Dev 2

Tài liệu này tổng hợp toàn bộ các file mã nguồn thuộc trách nhiệm phát triển của **Dev 2** trong dự án **Social Network Friend Suggestion System** và nêu rõ tác dụng của từng file.

---

### 1. Nhóm Cấu Trúc Dữ Liệu & Giải Thuật Cốt Lõi (Core DS & Algorithms)

*   `SinglyLinkedList.java`
    *   **Tác dụng**: Triển khai cấu trúc danh sách liên kết đơn generic. Hỗ trợ giao diện `Iterable` để phục vụ việc duyệt danh sách các đỉnh kề của đồ thị và danh sách người dùng thông qua vòng lặp.
*   `Queue.java`
    *   **Tác dụng**: Triển khai cấu trúc hàng đợi generic kiểu FIFO (First-In-First-Out) bằng liên kết nút (Node-pointer) phục vụ thuật toán duyệt đồ thị theo chiều rộng (BFS).
*   `BinarySearchTree.java`
    *   **Tác dụng**: Triển khai cấu trúc cây tìm kiếm nhị phân generic hoạt động như một bản đồ ánh xạ Khóa - Giá trị (Ordered Map). Được sử dụng để lưu trữ người dùng và lập chỉ mục phụ để tra cứu ID nhanh qua tên đăng nhập (Secondary Index) với độ phức tạp $O(\log N)$.
*   `MaxHeap.java`
    *   **Tác dụng**: Triển khai cấu trúc đống cực đại (Max-Heap) tự động co giãn kích thước mảng. Dùng để lưu trữ và sắp xếp toàn bộ danh sách gợi ý bạn bè.
*   `MinHeap.java`
    *   **Tác dụng**: Triển khai cấu trúc đống cực tiểu (Min-Heap) giới hạn kích thước phần tử ở mức $K$. Tối ưu thuật toán gợi ý Top-K với độ phức tạp bộ nhớ tối giản $O(K)$ và thời gian $O(N \log K)$.
*   `Graph.java`
    *   **Tác dụng**: Thiết lập mô hình đồ thị mạng xã hội vô hướng dựa trên danh sách kề (Adjacency List). Tích hợp các giải thuật duyệt BFS, DFS, tìm đường đi ngắn nhất (Degrees of Separation) và chuyển đổi động sang ma trận kề (Adjacency Matrix).
*   `User.java`
    *   **Tác dụng**: Lớp thực thể định nghĩa các thuộc tính của người dùng (ID, tên hiển thị, tên đăng nhập, tiểu sử bio, ngày tham gia) cùng các phương thức để tuần tự hóa và giải tuần tự hóa dữ liệu JSON.
*   `PerformanceTester.java`
    *   **Tác dụng**: Lập trình mô đun chạy stress test độc lập để đo đạc và so sánh hiệu năng thực tế của hai cấu trúc đống (Min-Heap vs Max-Heap) và thuật toán duyệt (BFS vs DFS) ở các quy mô dữ liệu lớn, sau đó ghi báo cáo ra định dạng Markdown.

---

### 2. Nhóm Lưu Trữ Dữ Liệu & Giao Diện Menu CLI (Storage & CLI Menu)

*   `DataStore.java`
    *   **Tác dụng**: Quản lý việc đọc, ghi và đồng bộ hóa cơ sở dữ liệu đồ thị mạng xã hội. Phân tách dữ liệu thành 2 tệp JSON độc lập và cung cấp phương thức `seedIfEmpty()` để tự động sinh dữ liệu mẫu ban đầu nếu tệp dữ liệu trống.
*   `users.json`
    *   **Tác dụng**: Tệp lưu trữ dữ liệu chứa thông tin hồ sơ của đúng 12 người dùng Việt Nam mẫu ban đầu.
*   `friendships.json`
    *   **Tác dụng**: Tệp lưu trữ các liên kết bạn bè (friendship edges) gồm 24 mối quan hệ bạn bè ban đầu để xây dựng cấu trúc đồ thị mạng lưới.
*   `ConsoleMenu.java`
    *   **Tác dụng**: Lập trình giao diện tương tác dòng lệnh dòng quét (CLI menu), cung cấp đầy đủ 12 lựa chọn chức năng hệ thống (CRUD người dùng, kết bạn/hủy kết bạn, duyệt BFS/DFS, xem đường đi ngắn nhất, xem ma trận kề, chạy thử nghiệm Top-K...).

---

### 3. Nhóm Bộ Xử Lý REST API (REST API Handlers)

*   `UserHandler.java`
    *   **Tác dụng**: Lớp xử lý định tuyến (API Handler) cho endpoint `/api/users` và `/api/users/{id}` tương ứng với các phương thức:
        *   `GET /api/users`: Trả về danh sách toàn bộ người dùng.
        *   `GET /api/users/{id}`: Trả về thông tin một người dùng cụ thể theo ID.
        *   `POST /api/users`: Tạo hồ sơ người dùng mới (thông qua dữ liệu JSON ở request body).
        *   `DELETE /api/users/{id}`: Xóa người dùng và tất cả liên kết bạn bè của người đó.
*   `FriendHandler.java`
    *   **Tác dụng**: Lớp xử lý định tuyến (API Handler) cho endpoint quan hệ bạn bè:
        *   `GET /api/friends/{userId}`: Trả về danh sách bạn bè của người dùng.
        *   `POST /api/friends`: Thiết lập mối quan hệ bạn bè mới giữa 2 người dùng (nhận dữ liệu ID qua request body).
        *   `DELETE /api/friends`: Hủy quan hệ bạn bè giữa 2 người dùng (nhận dữ liệu ID qua request body).
*   `SuggestionHandler.java`
    *   **Tác dụng**: Lớp xử lý định tuyến (API Handler) cho endpoint `/api/suggestions` để trả về danh sách gợi ý bạn bè phù hợp dựa trên số bạn chung và hệ số tương đồng Jaccard sử dụng cơ chế Min-Heap hoặc Max-Heap theo các tham số truy vấn (`userId`, `k`, `heapType`).

---

### 4. Nhóm Giao Diện & Kết Nối API Frontend (Frontend UI & API Connection)

*   `index.html`
    *   **Tác dụng**: Thiết lập trang chủ và giao diện chọn tài khoản đăng nhập (Select User) dạng bảng lưới thẻ thành viên (chữ đại diện, tên, tuổi, bạn bè) kèm biểu mẫu thêm mới liên kết giữa các tài khoản.
*   `home.html`
    *   **Tác dụng**: Thiết lập bảng điều khiển chính (dashboard) sau khi đăng nhập giả lập thành công, hiển thị các thông số thống kê mạng lưới, danh sách gợi ý kết bạn đồng bộ và vẽ đồ thị mạng lưới cá nhân hóa bậc 2 của thành viên hiện tại bằng thư viện D3 Canvas trực quan.
*   `api.js`
    *   **Tác dụng**: Xây dựng lớp bọc gọi API (API Helper) động kết nối trực tiếp đến backend qua cổng `3001`, tự động phân tách dữ liệu thông qua cấu trúc phản hồi dạng `.data` và hiển thị cảnh báo cảnh báo khi backend ngoại tuyến.

