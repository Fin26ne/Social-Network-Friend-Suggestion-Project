# Social Network Friend Suggestion System
## CSD201 - Cấu Trúc Dữ Liệu | FPT University SU26

### Mô tả dự án
Hệ thống mạng xã hội đơn giản với chức năng cốt lõi là gợi ý kết bạn thông minh. Hệ thống sử dụng các cấu trúc dữ liệu tự xây dựng (Linked List, BST, Max Heap, Graph, Queue) kết hợp thuật toán duyệt đồ thị (BFS) và hệ số tương đồng Jaccard để tìm kiếm và đề xuất những người bạn chung tiềm năng nhất.

### Kiến trúc
- **Java Core (port 3003):** xử lý toàn bộ logic, lưu trữ dữ liệu cục bộ và đóng vai trò REST API (không dùng Spring/Maven).
- **Web Frontend (port 3003):** hiển thị giao diện người dùng, giao tiếp với Java API và được phục vụ trực tiếp bởi server Java.

### Cấu trúc thư mục
- `backend-java/`: Chứa toàn bộ source code Java Core.
  - `src/api/`: Các HTTP Handlers (User, Friend, Suggestion) xử lý REST API requests qua HttpServer.
  - `src/console/`: Giao diện dòng lệnh tương tác (Console Menu) dành cho testing.
  - `src/dataStructures/`: 5 cấu trúc dữ liệu tự build hoàn toàn từ đầu.
  - `src/models/`: Định nghĩa các đối tượng và logic Serialize JSON (User, Friendship).
  - `src/services/`: Logic nghiệp vụ chính (GraphService, SuggestionService, DataStore).
  - `data/`: Lưu trữ file dữ liệu cục bộ (users.json, friendships.json).
  - `lib/`: Chứa thư viện org.json phục vụ parse JSON.
- `frontend/`: Chứa toàn bộ source code React (Vite).
  - `src/components/`: Các UI component dùng chung (Card, Graph, Navbar).
  - `src/pages/`: Các trang giao diện (Home, Explore, Network, Profile).
  - `src/hooks/`: Custom hooks để fetch data và update UI lạc quan (Optimistic UI).
  - `src/utils/`: Cấu hình kết nối Axios.
- `run.sh`: Script giả lập tự động chạy (dành cho môi trường Unix).

### Cài đặt & Chạy
**Step 1:** Tải `json-20240303.jar` vào thư mục `backend-java/lib/` (hoặc để nguyên vì project đã tải sẵn).
**Step 2:** Mở Terminal tại thư mục `backend-java` và biên dịch:
```powershell
Get-ChildItem -Recurse src -Filter *.java | Select-Object -ExpandProperty FullName > sources.txt
javac -cp "lib/json-20240303.jar" -d out @sources.txt
```
**Step 3:** Chạy server Java backend:
```powershell
java -cp "out;lib/json-20240303.jar" Main
```
**Step 4 (optional):** Mở terminal mới tại thư mục `frontend`:
```powershell
npm install
npm run dev
```

### Custom Data Structures
| Data Structure | Use case | Time Complexity (Average) |
| --- | --- | --- |
| **MySinglyLinkedList** | Lưu danh sách kề (edges) của từng User trong MyGraph | Thêm: O(1), Tìm: O(N) |
| **MyBST** | Tra cứu User Lookup table, lấy thông tin user theo ID | Thêm, Tìm, Xóa: O(log N) |
| **MyQueue** | Phục vụ thuật toán duyệt đồ thị theo chiều rộng (BFS) | Enqueue, Dequeue: O(1) |
| **MyMaxHeap** | Sắp xếp Top-K gợi ý bạn bè theo điểm Jaccard | Thêm: O(log K), Lấy Max: O(log K) |
| **MyGraph** | Lưu trữ mạng lưới kết bạn bằng Adjacency List kết hợp MyBST | Lấy bạn bè: O(log N) + O(E) |

### Thuật toán gợi ý bạn bè
1. Dùng thuật toán **BFS Level 2** (qua MyQueue) bắt đầu từ current user `X` để tìm tập hợp bạn-của-bạn (candidates).
2. Lọc bỏ bạn trực tiếp (direct friends) và chính `X`.
3. Chấm điểm **Jaccard Similarity** cho mỗi ứng viên `C` so với user hiện tại `X`:
   > `Jaccard = (Số lượng bạn chung của X và C) / (Tổng số bạn bè của X và C - Bạn chung)`
4. Đưa ứng viên cùng điểm Jaccard vào **MyMaxHeap**.
5. Rút ra (`extractMax`) Top K (mặc định Top 5) từ Heap để hiển thị kết quả gợi ý tiềm năng nhất.

### API Endpoints
| HTTP Method | Endpoint | Description | Response Format |
| --- | --- | --- | --- |
| GET | `/api/users` | Lấy danh sách toàn bộ users | `{ success: true, data: [...] }` |
| GET | `/api/users?id=X` | Lấy thông tin 1 user | `{ success: true, data: {...} }` |
| POST | `/api/users` | Tạo mới user | `{ success: true, data: {...} }` |
| DELETE | `/api/users?id=X` | Xóa user | `{ success: true, data: { message: "Deleted" } }` |
| GET | `/api/friends?userId=X` | Lấy danh sách bạn bè | `{ success: true, data: [...] }` |
| POST | `/api/friends` | Thêm kết bạn | `{ success: true, data: { message: "Created" } }` |
| DELETE | `/api/friends?userId1=X&userId2=Y` | Hủy kết bạn | `{ success: true, data: { message: "Deleted" } }` |
| GET | `/api/friends/mutual?id1=X&id2=Y` | Lấy danh sách bạn chung | `{ success: true, data: [...] }` |
| GET | `/api/suggestions?userId=X` | Lấy Top-5 gợi ý bạn bè | `{ success: true, data: [...] }` |
| GET | `/api/suggestions/graph` | Lấy toàn bộ Network Data | `{ success: true, data: { nodes, edges } }` |
| GET | `/api/suggestions/graph?userId=X` | Lấy đồ thị Depth-2 | `{ success: true, data: { nodes, edges } }` |

### AI Audit Log
[Ghi chú: xem file AI_AuditLog.xlsx đính kèm trong thư mục dự án]
