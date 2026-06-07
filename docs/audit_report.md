# 📊 BÁO CÁO KIỂM TRA TIẾN ĐỘ TOÀN DIỆN (DEV 1, DEV 2 & DEV 4)
*CSD201 Topic 05 - Social Network Friend Suggestion*  
*Thời gian kiểm tra cập nhật: 31/05/2026 22:50:00*

---

## 📈 TÓM TẮT TRẠNG THÁI TIẾN ĐỘ (MỚI NHẤT)

| Thành Viên | Vai Trò | Tỉ Lệ Hoàn Thành | Trạng Thái | Ghi Chú |
| :--- | :--- | :---: | :---: | :--- |
| **DEV 1 (Tech Lead)** | Cấu trúc dữ liệu & Thuật toán lõi | **10/10 (100%)** | **✓ ĐÃ HOÀN THÀNH** | Tự kiểm thử tự động đạt 100% thành công. |
| **DEV 2 (Backend / Core FE)** | API Handlers & Frontend Lõi | **9/9 (100%)** | **✓ ĐÃ HOÀN THÀNH** | Các API endpoints bọc JSON chuẩn, hỗ trợ đầy đủ các tính năng. |
| **DEV 4 (Researcher)** | Benchmarking & Frontend Phân Tích | **7/7 (100%)** | **✓ ĐÃ HOÀN THÀNH** | Đã commit bổ sung `AI_AuditLog.xlsx` & Việt hóa toàn bộ `README.md`. |

---

## 🚫 VI PHẠM NGUYÊN TẮC CHUNG (GLOBAL VIOLATIONS)
*Không phát hiện bất kỳ vi phạm nguyên tắc chung nào:*
- [x] **Không dùng localhost:2510**: Tất cả các file đều sử dụng đúng cổng mặc định `3001`.
- [x] **Không dùng framework FE (React/Vue/npm)**: Frontend hoàn toàn dùng Vanilla HTML/CSS/JS thuần.
- [x] **Không dùng White Background**: CSS sử dụng đúng tông màu tối cao cấp (`#080A0F`, `#0D1018`, `#111520`).
- [x] **Không dùng Font cấm (Arial/Roboto/Inter)**: Đã import đúng các Google Fonts được chỉ định (`Outfit`, `DM Mono`, `Cormorant Garamond`).
- [x] **Không Mock data ở FE**: Mọi dữ liệu phân tích và gợi ý được nạp trực tiếp từ API thực tế.
- [x] **Không import java.util.***: Gói `src/datastructures/` chỉ nạp các interface chức năng cần thiết (`Iterator`, `NoSuchElementException`, `Consumer`), không sử dụng wildcard.

---

## 📁 KIỂM TRA THƯ MỤC & FILE TỒN TẠI (FILE EXISTENCE VERIFIED)
- [x] `docs/AI_AuditLog.xlsx` -> **Tồn tại** (Được bổ sung bởi Dev 4).
- [x] `README.md` -> **Tồn tại** (Được cập nhật đầy đủ thông tin tiếng Việt bởi Dev 4).
- [x] `startup.bat` -> **Tồn tại** (Đã kiểm tra biên dịch thủ công thành công 100% không lỗi).

---

## 🛠️ CHI TIẾT TIẾN ĐỘ TỪNG THÀNH VIÊN

### 👤 1. DEV 1 (Tech Lead) — **10/10 NHIỆM VỤ HOÀN THÀNH**
*   **[MySinglyLinkedList.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MySinglyLinkedList.java)**: Đầy đủ insertAtHead, insertAtTail, remove, find, toArray, size, isEmpty, forEach, clear.
*   **[MyQueue.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyQueue.java)**: Hàng đợi xây dựng trên SinglyLinkedList, dequeue đạt $O(1)$.
*   **[MyBST.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyBST.java)**: Cây tìm kiếm nhị phân hỗ trợ xóa đủ 3 trường hợp node và duyệt `inOrderTraversal` trả về danh sách sắp xếp.
*   **[MyMaxHeap.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyMaxHeap.java)**: Mảng tự động giãn nở, chèn, trích xuất giá trị lớn nhất và trả về Top-K.
*   **[MyMinHeap.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyMinHeap.java)**: Giới hạn kích thước cố định $K$, thực hiện phép so sánh `offerIfBetter` để tối ưu hóa thời gian gợi ý xuống còn $O(N \log K)$.
*   **[MyGraph.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyGraph.java)**: Đồ thị danh sách kề được lưu vết bằng cấu trúc BST, hỗ trợ duyệt BFS bậc 2.
*   **[MyAdjacencyMatrix.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyAdjacencyMatrix.java)**: Biểu diễn ma trận kề boolean, hỗ trợ đo kích thước bộ nhớ thật theo byte.
*   **[MyDFS.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/MyDFS.java)**: Thuật toán DFS không đệ quy sử dụng Stack tự thiết kế để tránh StackOverflow.
*   **[SuggestionService.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/datastructures/SuggestionService.java)**: Tính chỉ số tương đồng Jaccard và hỗ trợ kiểm tra tự động `runSelfTest()`.
*   **[Main.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/Main.java)**: Điểm chạy chính của hệ thống, gọi kiểm tra tự động trước và khởi động API Server daemon.

---

### 👤 2. DEV 2 (Backend / Core FE) — **9/9 NHIỆM VỤ HOÀN THÀNH**
*   **[DataStore.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/services/DataStore.java)**: Seed chuẩn 12 user Việt Nam và 28 friendships mẫu. Đọc/ghi JSON chuẩn mã hóa UTF-8. Không sử dụng HashMap.
*   **[ConsoleMenu.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/console/ConsoleMenu.java)**: CLI tương tác hoàn chỉnh hỗ trợ 12 chức năng.
*   **[UserHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/UserHandler.java)**: API người dùng (`GET`, `POST`, `DELETE`) bọc phản hồi JSON trong lớp `{ success: true, data: ... }`.
*   **[FriendHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/FriendHandler.java)**: Hỗ trợ query parameter (`?userId=X`), path parameter, và API bạn chung (`/api/friends/mutual`).
*   **[SuggestionHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/SuggestionHandler.java)**: API gợi ý `/api/suggestions` trả về đề xuất Top-K kèm theo thời gian thực thi của thuật toán.
*   **[NetworkHandler.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/api/NetworkHandler.java)**: Trả về cấu trúc đồ thị mạng lưới đầy đủ để Frontend dựng đồ thị tương tác.
*   **[api.js](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/js/api.js)**: API client dùng baseURL `3001` và xử lý tốt trạng thái offline.
*   **[index.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/index.html)**: Giao diện chọn User dạng Grid, tích hợp form inline tạo người dùng mới.
*   **[home.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/home.html)**: Trang cá nhân người dùng, hiển thị danh sách bạn bè, gợi ý, lịch sử hoạt động và tích hợp đồ thị D3 Canvas mạng quan hệ sâu 2 cấp.

---

### 👤 3. DEV 4 (Researcher) — **7/7 NHIỆM VỤ HOÀN THÀNH**
*   **[PerformanceTester.java](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/src/benchmark/PerformanceTester.java)**: Chạy so sánh thực tế MaxHeap và MinHeap, sinh báo cáo định dạng Markdown.
*   **[style.css](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/css/style.css)**: Thiết lập hệ thống thiết kế Dark Luxury với Outfit, Cormorant Garamond, và DM Mono.
*   **[explore.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/explore.html)**: Tìm kiếm debounced 300ms, hiển thị bạn chung và chỉ số Jaccard thực tế.
*   **[network.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/network.html)**: Vẽ sơ đồ đồ thị toàn bộ hệ thống bằng D3 Canvas, zoom/pan và bảng thông tin trượt.
*   **[research.html](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/frontend/research.html)**: Tích hợp dữ liệu từ API `/api/benchmark`, vẽ các biểu đồ hiệu năng (line/bar charts) trực tiếp trên Canvas thuần.
*   **[AI_AuditLog.xlsx](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/docs/AI_AuditLog.xlsx)**: Đã tạo và đẩy lên thư mục `docs/` chứa đầy đủ thông tin log theo định dạng bảng tính Excel chuyên nghiệp.
*   **[README.md](file:///c:/Users/TUF/OneDrive/Documents/GitHub/Social-Network-Friend-Suggestion-Project/README.md)**: Đã cập nhật và Việt hóa hoàn chỉnh, giải thích rõ cơ chế của 8 cấu trúc dữ liệu tự tạo, hướng dẫn chạy bằng `startup.bat` và chi tiết các API Endpoints.

---

## 📌 ĐÁNH GIÁ CHUNG
*   **Biên dịch**: Hệ thống biên dịch thành công 100% không gặp lỗi cú pháp hay thiếu file.
*   **Khởi chạy**: `startup.bat` hoạt động hoàn hảo trên Windows CMD.
*   **Frontend**: Cả 5 trang đều tải đúng tài nguyên, giao diện đẹp mắt, tương tác mượt mà và kết nối trực tiếp với backend tại cổng `3001`.
*   **Tài liệu & Nhật ký**: Đạt yêu cầu về cả chất lượng và tính đầy đủ.

**DỰ ÁN ĐÃ SẴN SÀNG NGHIỆM THU!**
