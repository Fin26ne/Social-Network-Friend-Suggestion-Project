# Hướng dẫn Kiểm tra Tiến độ Dự án (Social Network Friend Suggestion System)

Tài liệu này hướng dẫn AI agent cách tự động kiểm tra, đánh giá tiến độ của Dev 2, Dev 3, và Dev 4 theo các checklist cụ thể dưới đây. Khi cần kiểm tra tiến độ, agent chỉ cần đọc file này và đối chiếu trực tiếp với mã nguồn hiện tại trong repo.

---

## 🛠 HƯỚNG DẪN DÀNH CHO AI AGENT
1. **Kiểm tra môi trường**: Đảm bảo đã đồng bộ repo mới nhất từ nhánh `main`.
2. **Quét mã nguồn**: Duyệt qua cấu trúc thư mục của dự án và kiểm tra sự tồn tại của các file được liệt kê trong checklist.
3. **Phân tích nội dung**: Đọc chi tiết từng file để xác định xem các tính năng cốt lõi đã được viết đầy đủ hay chưa (đánh giá `DONE`, `INCOMPLETE`, hoặc `MISSING`).
4. **Kiểm tra ràng buộc (Constraints)**: Đặc biệt lưu ý các lỗi vi phạm nghiêm trọng (Critical Violations).
5. **Xuất báo cáo**: Trả về kết quả dưới dạng bảng tổng hợp điểm số kèm danh sách việc cần hoàn thành cho từng Dev.

---

## 📝 CHECKLIST CHI TIẾT TỪNG THÀNH VIÊN

### 1. DEV 2 CHECKLIST (DataStore + Handlers + ConsoleMenu + Seed Data)
* [ ] `src/services/DataStore.java` tồn tại và có phương thức `seedIfEmpty()`.
* [ ] `src/api/UserHandler.java` tồn tại và hỗ trợ các phương thức HTTP: `GET`, `POST`, `DELETE`.
* [ ] `src/api/FriendHandler.java` tồn tại và hỗ trợ các phương thức HTTP: `GET`, `POST`, `DELETE`.
* [ ] `src/api/SuggestionHandler.java` tồn tại và xử lý endpoint `/api/suggestions`.
* [ ] `src/console/ConsoleMenu.java` tồn tại và cung cấp đầy đủ ít nhất 9 tùy chọn menu tương tác (CLI).
* [ ] `backend-java/data/users.json` tồn tại và chứa chính xác 12 thông tin người dùng Việt Nam (Vietnamese users).
* [ ] `backend-java/data/friendships.json` tồn tại và chứa ít nhất 20 mối quan hệ bạn bè (friendships).
* [ ] **RÀNG BUỘC PHỦ ĐỊNH**: Không được sử dụng `java.util.HashMap` trong lớp `DataStore` (phải dùng cấu trúc dữ liệu tự định nghĩa).
* [ ] **RÀNG BUỘC PHỦ ĐỊNH**: Không sử dụng Spring Boot hoặc bất kỳ framework nào khác (chỉ dùng Java thuần JDK 1.8 và thư viện `org.json`).
* [ ] **Quy tắc 1: Tương tác qua GraphService**: Tech Lead đã đóng gói toàn bộ logic đồ thị trong [GraphService.java](file:///c:/Users/ASUS%20TUF/Downloads/CSD-Dev2/src/service/GraphService.java). Khi Dev 2 viết thêm API bạn chung hay cấu trúc mạng lưới, hãy gọi thông qua các phương thức của `GraphService`, tránh tạo mới hoặc sửa đổi cấu trúc trong gói `datastructures` hay `graph`.
* [ ] **Quy tắc 2: Tách biệt lớp bọc JSON (Response Wrapper)**: Để định dạng tất cả API về dạng `{ success: true, data: ... }`, Dev 2 hãy dùng hàm tiện ích có sẵn của Tech Lead trong `AppServer.java`:
  ```java
  // Sử dụng helper có sẵn để chuẩn hóa phản hồi:
  JSONObject response = new JSONObject();
  response.put("success", true);
  response.put("data", dataPayload); // Mảng hoặc đối tượng thô
  AppServer.sendJsonResponse(exchange, 200, response);
  ```
* [ ] **Quy tắc 3: Tránh xung đột định tuyến (Routing Conflict) trong AppServer.java**: Khi đăng ký endpoint `GET /api/network?userId=X`, hãy đăng ký một Context mới trong phương thức `start()` của `AppServer.java` trỏ về một Handler riêng biệt, ví dụ:
  ```java
  server.createContext("/api/network", new NetworkHandler());
  ```
  Tránh lồng chéo logic `/api/network` vào bên trong `SuggestionHandler` vì cổng ngữ cảnh (Context Port) `/api/suggestions` đã được định tuyến riêng biệt.
* [ ] **Quy tắc 4: Xử lý tương thích đồng thời Query Parameter & Path Parameter**: Trong `FriendHandler.java`, khi bổ sung hỗ trợ `?userId=X` cho khớp checklist, Dev 2 hãy kiểm tra xem chuỗi truy vấn (query string) có trống hay không để điều hướng xử lý:
  ```java
  String query = exchange.getRequestURI().getQuery();
  if (query != null && query.contains("userId=")) {
      // Xử lý dạng query ?userId=X
  } else {
      // Fallback về xử lý dạng path parameter /api/friends/{userId} như cũ
  }
  ```


### 2. DEV 3 CHECKLIST (Frontend CSS/JS + HTML Pages)
* [ ] `frontend/css/style.css` tồn tại và định nghĩa các CSS variables: `--bg`, `--gold`, `--surface`.
* [ ] `frontend/js/api.js` tồn tại và cấu hình đúng địa chỉ API gốc: `baseURL = http://localhost:3001/api`.
* [ ] `frontend/index.html` tồn tại, là trang chọn người dùng (Select User page) sử dụng Grid layout.
* [ ] `frontend/home.html` tồn tại, sử dụng 3-column grid layout để hiển thị thông tin và các thẻ gợi ý bạn bè (suggestion cards).
* [ ] `frontend/explore.html` tồn tại, cung cấp thanh tìm kiếm (search bar) và danh sách người dùng dạng grid.
* [ ] `frontend/network.html` tồn tại và vẽ đồ thị mạng lưới bằng D3 canvas graph.
* [ ] Các file HTML phải import các Google Fonts theo phong cách Dark Luxury: `Cormorant Garamond`, `DM Mono`, và `Outfit`.
* [ ] **RÀNG BUỘC PHỦ ĐỊNH**: Không dùng `npm`, `package.json`, hoặc thư mục `node_modules` ở thư mục frontend.
* [ ] **RÀNG BUỘC PHỦ ĐỊNH**: Không sử dụng React/Vue hoặc các framework frontend tương tự (chỉ dùng HTML/CSS/JS thuần).

### 3. DEV 4 CHECKLIST (BenchmarkRunner + Research Page + Logs + README)
* [ ] `src/services/BenchmarkRunner.java` tồn tại và có phương thức `runAll()`.
* [ ] `BenchmarkRunner` có đủ 3 phương thức đo hiệu năng tương ứng với 3 câu hỏi nghiên cứu (Research Questions):
  * `runRQ1()` — so sánh thời gian BFS vs DFS (nanoseconds) trên các tập kích thước $N = 100/500/1000/5000/10000$.
  * `runRQ2()` — so sánh dung lượng bộ nhớ sử dụng (RAM bytes) giữa Adjacency List vs Adjacency Matrix.
  * `runRQ3()` — so sánh thời gian gợi ý giữa MinHeap-K $O(N \log K)$ vs MaxHeap $O(N \log N)$.
* [ ] `frontend/research.html` tồn tại và hiển thị trực quan dữ liệu của 3 câu hỏi nghiên cứu trên.
* [ ] `research.html` kết nối trực tiếp với API `/api/benchmark` (không sử dụng dữ liệu giả lập/mock data).
* [ ] `AI_AuditLog.xlsx` tồn tại ở thư mục gốc của dự án và có ít nhất 8 bản ghi (entries).
* [ ] `README.md` tồn tại ở thư mục gốc và có hướng dẫn chạy dự án chi tiết bằng tiếng Việt.
* [ ] **RÀNG BUỘC PHỦ ĐỊNH**: Không được hardcode hoặc mock dữ liệu trong trang kết quả nghiên cứu `research.html`.

---

## 🚨 RÀNG BUỘC NGHIÊM NGẶT (CRITICAL VIOLATIONS)
Cần cảnh báo ngay lập tức nếu phát hiện các lỗi sau:
1. Sử dụng Spring Boot, Maven, Gradle, Hibernate, v.v. ở Backend.
2. Sử dụng React, Angular, Vue, Tailwind CSS (nếu không có sự đồng ý của Team Lead), npm hoặc Webpack ở Frontend.
3. Sử dụng `java.util.HashMap` hoặc các collection mặc định của Java để thay thế cho cấu trúc dữ liệu tự định nghĩa (`MySinglyLinkedList`, `MyBST`...) trong lớp `DataStore`.
4. Trang nghiên cứu hiển thị dữ liệu tĩnh (mock/hardcode) thay vì fetch từ API `/api/benchmark`.
5. API Server cấu hình sai cổng (cổng đúng: `3001`) hoặc static handler trỏ sai thư mục frontend tĩnh (phải là `frontend/` thay vì `web/`).

---

## 📊 MẪU BÁO CÁO KẾT QUẢ

Báo cáo trả về cần tuân theo định dạng sau:

```markdown
### 📊 BẢNG TỔNG HỢP TIẾN ĐỘ
- **Dev 2**: X/9 tasks done
- **Dev 3**: X/9 tasks done
- **Dev 4**: X/7 tasks done

---

### 🔍 CHI TIẾT TỪNG THÀNH VIÊN

#### 1. Dev 2
- [File/Tính năng] - [DONE / INCOMPLETE / MISSING]: Mô tả chi tiết thiếu cái gì...

#### 2. Dev 3
...

#### 3. Dev 4
...

---

### 🚨 VI PHẠM RÀNG BUỘC (Nếu có)
- [Cảnh báo cụ thể]
```
