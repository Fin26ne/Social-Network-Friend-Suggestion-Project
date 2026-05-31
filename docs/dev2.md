### 1. Implement DataStore module
*   **File chịu trách nhiệm**: `DataStore.java`
*   **Chi tiết**: Lớp `DataStore` đảm nhận vai trò quản lý lưu trữ, tự động tạo đường dẫn thư mục dữ liệu và đồng bộ trạng thái đồ thị mạng xã hội.

### 2. Read/write JSON data files
*   **File chịu trách nhiệm**: `DataStore.java`
*   **Chi tiết**:
    *   Hàm `save(Graph graph, BinarySearchTree<String, User> userBst)`: Chuyển đổi dữ liệu Đồ thị & BST Người dùng thành định dạng JSON và ghi độc lập ra 2 tệp `users.json` và `friendships.json` tại thư mục `backend-java/data/` sử dụng chuẩn mã hóa UTF-8.
    *   Hàm `load(Graph graph, BinarySearchTree<String, User> userBst)`: Đọc dữ liệu từ hai tệp JSON trên, giải mã UTF-8 và dựng lại toàn bộ cấu trúc Đồ thị (Graph) cùng Cây tìm kiếm nhị phân (BST) lưu trữ người dùng.

### 3. Create seed sample dataset
*   **File chịu trách nhiệm**: `DataStore.java`
*   **Chi tiết**: Hàm `seedIfEmpty(Graph graph, BinarySearchTree<String, User> userBst)` kiểm tra nếu tệp dữ liệu chưa tồn tại hoặc trống sẽ tự động gọi `initializeSeedData(...)` để khởi tạo dữ liệu mẫu gồm đúng 12 tài khoản người dùng Việt Nam (`u1` đến `u12`) và thiết lập tối thiểu 20 mối quan hệ bạn bè (thực tế khởi tạo sẵn 24 liên kết bạn bè).

### 4. Develop Console Menu interface
*   **File chịu trách nhiệm**: `ConsoleMenu.java`
*   **Chi tiết**: Lớp `ConsoleMenu` lập trình giao diện tương tác dòng lệnh (CLI menu), hiển thị danh sách các tùy chọn thao tác (CRUD người dùng, kết bạn/hủy kết bạn, duyệt đồ thị BFS/DFS, hiển thị ma trận kề, gợi ý bạn bè Top-K...).

### 5. Build API Handlers
Các bộ xử lý API được tách riêng biệt thành các lớp public nằm độc lập:

*   **User Handler**: Lớp `UserHandler` xử lý định tuyến:
    *   `GET /api/users`: Trả về danh sách thông tin toàn bộ người dùng.
    *   `GET /api/users/{id}`: Trả về thông tin chi tiết của một người dùng theo ID.
    *   `POST /api/users`: Tạo tài khoản người dùng mới (dữ liệu truyền qua request body JSON).
    *   `DELETE /api/users/{id}`: Xóa tài khoản người dùng và hủy toàn bộ các liên kết bạn bè liên quan.
*   **Friend Handler**: Lớp `FriendHandler` xử lý định tuyến:
    *   `GET /api/friends/{userId}`: Trả về danh sách bạn bè của người dùng theo ID chỉ định.
    *   `POST /api/friends`: Thiết lập mối quan hệ bạn bè mới giữa 2 người dùng (dữ liệu `userId1` và `userId2` gửi qua request body JSON).
    *   `DELETE /api/friends`: Hủy quan hệ bạn bè giữa 2 người dùng (dữ liệu gửi qua request body JSON).
*   **Suggestion Handler**: Lớp `SuggestionHandler` xử lý định tuyến:
    *   `GET /api/suggestions`: Trả về danh sách gợi ý kết bạn Top-K dựa trên số bạn chung hoặc hệ số tương đồng Jaccard. Yêu cầu truyền các query parameters: `userId` (ID người dùng cần gợi ý), `k` (số lượng gợi ý tối đa), và `heapType` (`min` hoặc `max` để chọn chiến lược đống tối ưu).

### 6. System Execution and Testing
*   **File chịu trách nhiệm**:
    *   `Main.java`: Điểm khởi chạy chính (hàm `main`), tiếp nhận đối số dòng lệnh (ví dụ `--console` để chỉ chạy CLI menu không bật web server), khởi động máy chủ Web API tại cổng `3001` (nếu không chạy chế độ console-only) phục vụ giao diện tĩnh từ thư mục `frontend/`, và kích hoạt giao diện `ConsoleMenu`.
    *   `ConsoleMenu.java`: Chạy vòng lặp Scanner nhận dữ liệu từ terminal và phản hồi kết quả trực quan phục vụ cho công tác kiểm thử thủ công.
