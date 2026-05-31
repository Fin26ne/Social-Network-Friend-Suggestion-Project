# 📊 BÁO CÁO KIỂM TRA TIẾN ĐỘ TOÀN DIỆN (DEV 1, DEV 2 & DEV 4)
*CSD201 Topic 05 - Social Network Friend Suggestion*  
*Thời gian kiểm tra: 31/05/2026 10:42:00*

---

## 📈 TÓM TẮT TRẠNG THÁI TIẾN ĐỘ

| Thành Viên | Vai Trò | Tỉ Lệ Hoàn Thành | Trạng Thái | Vấn Đề Tồn Đọng |
| :--- | :--- | :---: | :---: | :--- |
| **DEV 1 (Tech Lead)** | Cấu trúc dữ liệu & Thuật toán lõi | **10/10 (100%)** | **✓ Đã Xong** | Không có (Self-test đạt 100%) |
| **DEV 2 (Backend)** | API Handlers & Frontend Lõi | **5/8 (62.5%)** | **⚠ Đang Cải Thiện** | Thiếu một số endpoints và chuẩn hóa JSON |
| **DEV 4 (Researcher)** | Benchmarking & Frontend Phân Tích | **5/7 (71.4%)** | **⚠ Đang Cải Thiện** | Thiếu file AI_AuditLog.xlsx & README.md chưa Việt hóa |

---

## 🚫 VI PHẠM NGUYÊN TẮC CHUNG (GLOBAL VIOLATIONS)
*Không phát hiện bất kỳ vi phạm nghiêm trọng nào trong toàn bộ dự án:*
- [x] **localhost:2510**: Tất cả các tệp đều sử dụng đúng cổng mặc định `3001`.
- [x] **React/Vue/npm/package.json**: Frontend hoàn toàn dùng Vanilla HTML/CSS/JS thuần, không sử dụng thư viện ngoài.
- [x] **White Background**: Các tệp CSS chỉ định tông nền tối nghệ thuật (`#080A0F`, `#0D1018`, `#111520`).
- [x] **Font cấm (Arial/Roboto/Inter)**: Đã nạp đúng 3 phông chữ Google Fonts chỉ định (`Outfit`, `DM Mono`, `Cormorant Garamond`).
- [x] **Mock data**: Mọi số liệu hiển thị trên HTML đều lấy động từ API thật, không hardcode.
- [x] **java.util.\***: Gói `src/datastructures/` chỉ nạp cụ thể các interface chức năng cần thiết (`Iterator`, `NoSuchElementException`, `Consumer`), không dùng dấu sao đại diện.

---

## 📂 THƯ MỤC CHƯA TỒN TẠI (MISSING FILES)
* `docs/AI_AuditLog.xlsx` (Chỉ tồn tại tệp tin `docs/AI Audit Log.docx` chưa đạt chuẩn định dạng bảng tính).

---

## 🛠️ CHI TIẾT TỪNG PHÂN HỆ

### 👤 CHI TIẾT DEV 1 (✓ 10/10 NHIỆM VỤ HOÀN THÀNH)
* **✓ [MySinglyLinkedList.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MySinglyLinkedList.java)**: Thiết kế Generic Class, lớp lồng `Node<T>`, có đủ phương thức đầu/cuối, tìm kiếm, duyệt `forEach` bằng Consumer.
* **✓ [MyQueue.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyQueue.java)**: Xây dựng trên nền `MySinglyLinkedList` với tốc độ loại bỏ đầu $O(1)$.
* **✓ [MyBST.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyBST.java)**: Cây nhị phân tìm kiếm chuẩn, hỗ trợ đầy đủ 3 trường hợp xóa node (lá, 1 con, 2 con), duyệt `inOrderTraversal` trả về danh sách đã sắp xếp.
* **✓ [MyMaxHeap.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyMaxHeap.java)**: Định dạng mảng tự co giãn dung lượng, chèn, trích xuất Max và trả về danh sách Top-K bằng bản sao.
* **✓ [MyMinHeap.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyMinHeap.java)**: Dung lượng cố định $K$, thực hiện phép so sánh `offerIfBetter` và xuất mảng sắp xếp giảm dần.
* **✓ [MyGraph.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyGraph.java)**: Danh sách kề dùng `MyBST`, duyệt BFS cấp 2 thông qua `MyQueue`, xử lý chống lặp và an toàn khi đồ thị bị ngắt kết nối.
* **✓ [MyAdjacencyMatrix.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyAdjacencyMatrix.java)**: Ma trận kề `boolean[][]` chia sẻ chung giao diện làm việc với MyGraph, đo dung lượng bộ nhớ động bằng bytes.
* **✓ [MyDFS.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyDFS.java)**: Duyệt chiều sâu dùng Stack thủ công bằng `MySinglyLinkedList` (không đệ quy) tránh StackOverflow.
* **✓ [SuggestionService.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/SuggestionService.java)**: Gợi ý Top-K bằng `MyMinHeap`, tính chỉ số Jaccard bằng vòng lặp thủ công, tránh chia cho 0 và hỗ trợ tự động kiểm tra `runSelfTest()`.
* **✓ [Main.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/Main.java)**: Gọi kiểm tra tự động trước tiên, khởi chạy server API dạng nền daemon thread một lần duy nhất.

---

### 👤 CHI TIẾT DEV 2 (✓ 5/8 NHIỆM VỤ HOÀN THÀNH)
* **✓ Hoàn thành:**
  * **[DataStore.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/services/DataStore.java)**: Khởi tạo dữ liệu seed khi trống, 12 thành viên tiếng Việt, 24 liên kết, không sử dụng HashMap.
  * **[ConsoleMenu.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/console/ConsoleMenu.java)**: 12 lựa chọn trực quan, xử lý định dạng số sai, cho phép ngắt kết nối giao diện web.
  * **[api.js](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/js/api.js)**: Nạp đúng cổng API 3001, lọc dữ liệu động qua `.data`, hiển thị cảnh báo offline, không mã hóa cứng thông tin.
  * **[index.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/index.html)**: Bảng lưới thẻ thành viên (chữ đại diện, tên, tuổi, bạn bè), lưu trữ tài khoản, form thêm mới liên kết tốt.
  * **[home.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/home.html)**: Bảo vệ đăng nhập, hiển thị thông số thống kê, gợi ý đồng bộ bạn chung kèm đồ thị mạng lưới cá nhân trực quan bằng D3 Canvas.
* **✗ Chưa xong / Có vấn đề:**
  * **[UserHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/UserHandler.java)**: Gửi phản hồi JSON trực tiếp thay vì bọc ngoài bằng lớp bọc `{success, data}`.
  * **[FriendHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/FriendHandler.java)**: Chưa hỗ trợ tham số truy vấn ở dạng `/api/friends?userId=X` và thiếu hoàn toàn endpoint `/api/friends/mutual` để tính toán bạn chung trên server.
  * **[SuggestionHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/SuggestionHandler.java)**: Thiếu endpoint nghiệp vụ `GET /api/network?userId=X` phục vụ dựng đồ thị cá nhân hóa.

---

### 👤 CHI TIẾT DEV 4 (✓ 5/7 NHIỆM VỤ HOÀN THÀNH)
* **✓ Hoàn thành:**
  * **[BenchmarkRunner.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/BenchmarkRunner.java)**: Tích hợp đầy đủ `runAll()`, các phép đo BFS vs DFS trên các dải [100, 500, 1000, 5000, 10000], List vs Matrix RAM, MinHeap vs MaxHeap; không dùng stream; biên dịch tương thích JDK 1.8 và xuất kết quả ra file `data/benchmark_results.json`.
  * **[style.css](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/css/style.css)**: Định nghĩa đủ biến màu, nạp font Outfit/Cormorant/Mono, dựng đủ component màu tối không vi phạm quy chuẩn.
  * **[explore.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/explore.html)**: Bảng lưới người chưa kết nối, tìm kiếm debounced 300ms, tính toán bạn chung và cho phép kết bạn trực tiếp.
  * **[network.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/network.html)**: Sơ đồ toàn hệ thống vẽ bằng D3 Canvas, phóng to/thu nhỏ, đổi màu và tích hợp thanh thuộc tính trượt khi nhấp chuột.
  * **[research.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/research.html)**: Đọc dữ liệu thô từ API, vẽ biểu đồ đường/cột hoàn toàn bằng Canvas thuần (không D3/Chart.js), bảng dữ liệu tương ứng và conclusions sinh động.
* **✗ Chưa xong / Có vấn đề:**
  * **AI_AuditLog.xlsx**: Tệp chưa tồn tại trong hệ thống.
  * **[README.md](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/README.md)**: Chưa có hướng dẫn CLI chi tiết và cách chạy `startup.bat`; Chưa giải thích 8 cấu trúc dữ liệu tự tạo; Thiếu các bảng số liệu thực tế phân tích 3 câu hỏi nghiên cứu và danh sách các API endpoints. File viết chủ yếu bằng tiếng Anh.

---

## 🔍 TRẠNG THÁI TỔNG THỂ DỰ ÁN
- [x] Project compile được JDK 1.8? **Có** (Đã kiểm thử biên dịch thành công qua javac).
- [x] startup.bat chạy không tắt terminal? **Có** (Menu tương tác Console lặp tuần hoàn giữ tiến trình).
- [x] Cả 5 trang HTML load đúng? **Có** (Các trang liên kết động và nạp dữ liệu tốt).
- [x] /api/benchmark trả data thật? **Có** (Đã liên kết chạy đo và xuất dữ liệu thông qua BenchmarkRunner).
- [ ] AI Audit Log đủ yêu cầu? **Không** (Thiếu tệp tin định dạng `.xlsx`).

---

## 📌 HÀNH ĐỘNG KHẨN CẤP CẦN THỰC HIỆN (ACTION ITEMS)

### **Công việc của Dev 2:**
1. Cấu trúc lại dữ liệu phản hồi trong `UserHandler.java`, `FriendHandler.java`, và `SuggestionHandler.java` để bao bọc bởi định dạng JSON `{ success: true, data: ... }`.
2. Bổ sung xử lý tham số query `?userId=X` trong `FriendHandler.java` bên cạnh tham số đường dẫn `/api/friends/{userId}`.
3. Viết thêm API endpoint `GET /api/friends/mutual?userId1=A&userId2=B` phục vụ tính toán trực tiếp danh sách bạn chung trên máy chủ.
4. Thêm API endpoint `GET /api/network?userId=X` để trả về các nút và liên kết thuộc sơ đồ bậc 2 cá nhân hóa của một thành viên.

### **Công việc của Dev 4:**
1. Thiết kế bảng tính `docs/AI_AuditLog.xlsx` đầy đủ:
   - Tối thiểu 8 mục nhật ký.
   - Chỉ rõ ít nhất 2 trường hợp lỗi ảo giác của AI.
   - Điền đủ 4 cấu phần Tư duy máy tính (DTC Components).
   - Viết đủ 4 câu đánh giá vai trò con người (Human Delta) cho mỗi đầu mục.
2. Viết lại tài liệu `README.md` bằng tiếng Việt:
   - Hướng dẫn rõ ràng cách biên dịch thủ công qua lệnh javac và cách khởi chạy nhanh bằng `startup.bat`.
   - Giải thích vai trò của 8 cấu trúc dữ liệu tùy chỉnh (`MySinglyLinkedList`, `MyQueue`, `MyBST`, `MyMaxHeap`, `MyMinHeap`, `MyGraph`, `MyAdjacencyMatrix`, `MyDFS`).
   - Tổng hợp bảng số liệu thực tế đo được để trả lời chi tiết cho 3 Câu hỏi Nghiên cứu (RQ1, RQ2, RQ3).
   - Liệt kê đầy đủ danh sách các API Endpoints và tham số truyền nhận của hệ thống.
