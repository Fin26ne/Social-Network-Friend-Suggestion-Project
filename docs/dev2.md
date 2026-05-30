### 1. Implement DataStore module
*   **File chịu trách nhiệm**: [`src/services/DataStore.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/services/DataStore.java)
*   **Chi tiết**: Lớp `DataStore` đảm nhận vai trò quản lý lưu trữ, tạo đường dẫn thư mục và đồng bộ dữ liệu đồ thị.

### 2. Read/write JSON data files
*   **File chịu trách nhiệm**: [`src/services/DataStore.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/services/DataStore.java)
*   **Chi tiết**:
    *   Hàm `save(Graph graph, BinarySearchTree<String, User> userBst)`: Chuyển đổi dữ liệu Đồ thị & BST Người dùng thành chuỗi JSON và ghi vào 2 tệp `users.json` và `friendships.json` trong thư mục `backend-java/data/` bằng luồng UTF-8.
    *   Hàm `load(Graph graph, BinarySearchTree<String, User> userBst)`: Đọc bytes từ các tệp JSON, giải mã UTF-8 và dựng lại cấu trúc Đồ thị & BST.

### 3. Create seed sample dataset
*   **File chịu trách nhiệm**: [`src/services/DataStore.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/services/DataStore.java)
*   **Chi tiết**: Hàm `initializeSeedData(Graph graph, BinarySearchTree<String, User> userBst)` khởi tạo dữ liệu mẫu gồm đúng 12 người dùng mẫu Việt Nam (`u1` đến `u12`) cùng ít nhất 20 mối quan hệ bạn bè (friendships - thực tế là 24 mối quan hệ) ban đầu nếu tệp JSON chưa tồn tại.

### 4. Develop Console Menu interface
*   **File chịu trách nhiệm**: [`src/console/ConsoleMenu.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/console/ConsoleMenu.java)
*   **Chi tiết**: Lớp `ConsoleMenu` chứa vòng lặp menu CLI, hiển thị danh sách các tùy chọn thao tác (CRUD người dùng, kết bạn, duyệt BFS/DFS, hiển thị ma trận kề/danh sách kề, gợi ý bạn bè Top-K, v.v.).

### 5. Build API Handlers
Các Handler này được tách riêng thành các lớp public nằm trong các file tương ứng dưới thư mục [`src/api/`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/api/):

*   **User Handler**: Lớp [`UserHandler`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/api/UserHandler.java) (xử lý định tuyến `/api/users` và `/api/users/{id}` cho các phương thức `GET`, `POST`, `DELETE`).
*   **Friend Handler**: Lớp [`FriendHandler`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/api/FriendHandler.java) (xử lý định tuyến `/api/friends` và `/api/friends/{id}` để thêm/xóa bạn bè).
*   **Suggestion Handler**: Lớp [`SuggestionHandler`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/api/SuggestionHandler.java) (xử lý định tuyến `/api/suggestions` để trả về danh sách gợi ý bạn bè sử dụng Min-Heap hoặc Max-Heap theo truy vấn).

### 6. Console interaction testing (Kiểm thử tương tác dòng lệnh)
*   **File chịu trách nhiệm**:
    *   [`src/Main.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/Main.java): Điểm khởi chạy chính (hàm `main`), xử lý tham số dòng lệnh (ví dụ `--console` để chỉ chạy Console Menu mà không bật máy chủ Web), và khởi tạo `ConsoleMenu`.
    *   [`src/console/ConsoleMenu.java`](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/console/ConsoleMenu.java): Chứa luồng xử lý quét dữ liệu nhập từ bàn phím (Scanner) và in kết quả ra terminal để phục vụ việc kiểm thử tương tác trực tiếp.
