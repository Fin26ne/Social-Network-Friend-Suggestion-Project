# BÁO CÁO KIỂM TRA TIẾN ĐỘ CHI TIẾT
CSD201 Topic 05 - 2026-05-31T10:39:45+07:00

---
📊 KẾT QUẢ KIỂM TRA TIẾN ĐỘ
CSD201 Topic 05 - 2026-05-31T10:39:45+07:00
---

DEV 2: ✓ 5/8 nhiệm vụ hoàn thành | ✗ 3 vấn đề (Java Handlers thiếu sót)
DEV 4: ✓ 5/7 nhiệm vụ hoàn thành | ✗ 2 vấn đề (Thiếu AI_AuditLog.xlsx & README.md chưa đạt chuẩn)

VI PHẠM NGHIÊM TRỌNG:
  - Không có vi phạm nghiêm trọng về cấu trúc chung (Không dùng localhost:2510, không có React/Vue/npm, không có white background hay font cấm trong frontend, không dùng java.util.* trong cấu trúc dữ liệu tùy chỉnh).

FILE CHƯA TỒN TẠI:
  - docs/AI_AuditLog.xlsx (Chỉ tồn tại file docs/AI Audit Log.docx không đúng định dạng yêu cầu).

CHI TIẾT DEV 2:
  ✓ Hoàn thành:
    - [Java] src/services/DataStore.java: Đã có method seedIfEmpty(), seed đúng 12 thành viên tiếng Việt, thiết lập 24 liên kết bạn bè (>= 20) và hoàn toàn dùng MyBST/MyGraph tự thiết kế (không dùng HashMap).
    - [Java] src/console/ConsoleMenu.java: Có đủ 12 tùy chọn (đạt yêu cầu >= 9), đã bọc try/catch NumberFormatException khi nhập K, chạy bình thường khi không bật máy chủ web (qua đối số --console-only).
    - [Frontend] frontend/js/api.js: baseURL trỏ chính xác http://localhost:3001/api, xử lý kết quả qua biến trung gian result.data, tích hợp banner tiếng Việt khi ngoại tuyến, đầy đủ 11 hàm nghiệp vụ không cứng userId.
    - [Frontend] frontend/index.html: Giao diện thẻ thành viên hiển thị thông tin chữ cái đại diện, tên, tuổi và số bạn bè, lưu localStorage và chuyển hướng sang home.html khi click card, có form thêm người dùng mới và xử lý ngoại tuyến mượt mà.
    - [Frontend] frontend/home.html: Kiểm tra đăng nhập qua localStorage, hiển thị đầy đủ 3 cột thông tin (Bạn bè, Gợi ý, Hoạt động), biểu đồ D3 Canvas mức khuyên dùng 2, gọi API thật khi kết bạn/hủy bạn bè và tự động làm mới trang.
  ✗ Chưa xong / Có vấn đề:
    - [Java] src/api/UserHandler.java: Các phản hồi JSON trả về trực tiếp mảng hoặc đối tượng (ví dụ: {"users": [...]}, {"user": ...}) thay vì bọc ngoài bằng cấu trúc chuẩn `{success, data}`.
    - [Java] src/api/FriendHandler.java: Chỉ xử lý GET qua đường dẫn path parameter `/api/friends/{userId}`, chưa hỗ trợ truy vấn query parameter `/api/friends?userId=X`. Đồng thời, chưa có API endpoint cho việc tìm kiếm bạn chung (`GET /api/friends/mutual`).
    - [Java] src/api/SuggestionHandler.java: Chưa có endpoint `GET /api/network?userId=X` (chỉ có `/api/graph` trả về toàn bộ đồ thị không lọc theo cá nhân). Endpoint `/api/benchmark` được khai báo trong AppServer.java chứ không trực tiếp trong Handler này.

CHI TIẾT DEV 4:
  ✓ Hoàn thành:
    - [Java] src/benchmark/BenchmarkRunner.java: Chứa đầy đủ phương thức runAll() gọi runRQ1(), runRQ2(), runRQ3(); chạy BFS vs DFS trên các mức đo [100, 500, 1000, 5000, 10000]; tính toán RAM List vs Matrix; so sánh MinHeap vs MaxHeap với K=5; không dùng stream và tương thích hoàn toàn với JDK 1.8. Dữ liệu đầu ra được ghi chính xác vào data/benchmark_results.json.
    - [Frontend] frontend/css/style.css: Khai báo đủ các biến CSS chỉ định, nạp 3 font chữ Google Fonts chuẩn, tạo đủ kiểu giao diện nút/thẻ/sidebar, không có nền trắng và không chứa các phông mặc định bị cấm.
    - [Frontend] frontend/explore.html: Hiển thị danh sách người dùng tiềm năng (trừ bạn bè và bản thân), tìm kiếm debounced 300ms, bộ lọc tên thời gian thực, hiển thị bạn chung và nút kết bạn dùng style.css.
    - [Frontend] frontend/network.html: Bản đồ mạng lưới toàn hệ thống vẽ bằng D3 Canvas, có chế độ thu phóng/di chuyển, đổi màu các nút phân loại và tích hợp bảng thông tin trượt khi nhấp chuột vào nút.
    - [Frontend] frontend/research.html: Tải trực tiếp dữ liệu từ máy chủ API benchmark, dựng biểu đồ đường/cột hoàn toàn bằng Canvas thuần (không D3/Chart.js), bảng số liệu chi tiết và kết luận tự động phản ánh từ số liệu thật.
  ✗ Chưa xong / Có vấn đề:
    - [Tài liệu] docs/AI_AuditLog.xlsx: Không tồn tại trong dự án (chỉ có file Word .docx không phân tích rõ ràng 8 mục lục, 2 lỗi ảo giác và 4 thành phần tư duy máy tính/con người).
    - [Tài liệu] README.md: Chưa có hướng dẫn chi tiết lệnh biên dịch thủ công qua CLI hay cách chạy file startup.bat. Chưa giải thích 8 cấu trúc dữ liệu tùy chỉnh. Thiếu các bảng số liệu thực tế phân tích 3 câu hỏi nghiên cứu và danh sách các API endpoints. File viết chủ yếu bằng tiếng Anh.

TRẠNG THÁI TỔNG THỂ:
  [x] Project compile được JDK 1.8? -> CÓ (Biên dịch thành công qua javac).
  [x] startup.bat chạy không tắt terminal? -> CÓ (Chạy qua menu lựa chọn tuần hoàn).
  [x] Cả 5 trang HTML load đúng? -> CÓ (Tải tốt giao diện và tương tác động).
  [x] /api/benchmark trả data thật? -> CÓ (Đã cấu hình chạy các bài đo thực tế thông qua BenchmarkRunner).
  [x] AI Audit Log đủ yêu cầu? -> KHÔNG (Thiếu file .xlsx chính thức).

VIỆC CẦN LÀM NGAY:
  Dev 2:
    1. Cập nhật UserHandler.java bọc phản hồi JSON trong cấu trúc `{success, data}`.
    2. Cập nhật FriendHandler.java hỗ trợ cả tham số truy vấn `?userId=X` và thêm endpoint `GET /api/friends/mutual` để tính toán bạn chung.
    3. Thêm endpoint `GET /api/network?userId=X` trong hệ thống để phục vụ vẽ đồ thị cá nhân hóa.
  Dev 4:
    1. Thiết kế và tạo mới tệp docs/AI_AuditLog.xlsx chứa đủ 8 mục phân tích, 2 lỗi ảo giác, 4 thành phần tư duy máy tính và 4 tiêu chí Human Delta.
    2. Viết lại tài liệu README.md bằng tiếng Việt: bổ sung hướng dẫn CLI, giải thích 8 cấu trúc dữ liệu, liệt kê endpoints và điền số liệu của 3 câu hỏi nghiên cứu.
---
