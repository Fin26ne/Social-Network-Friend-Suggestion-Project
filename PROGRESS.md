# Báo cáo tiến độ — Topic 05 Social Network
## CSD201 · FPT University · SU26
> Cập nhật: 31/05/2026 22:55:00

---

## Tổng quan

| Dev | Phụ trách | Trạng thái | Tiến độ |
|-----|-----------|-----------|---------|
| Dev 1 | Core DS + Algorithm + API | ✅ DONE | 12/12 |
| Dev 2 | DataStore + Handlers + Console + api.js + index.html + home.html | ✅ DONE | 8/8 |
| Dev 4 | BenchmarkRunner + style.css + explore + network + research + Log + README | ✅ DONE | 7/7 |

---

## Dev 1 ✅ DONE

| File | Trạng thái |
|------|-----------|
| MySinglyLinkedList.java | ✅ |
| MyQueue.java | ✅ |
| MyBST.java | ✅ |
| MyMaxHeap.java | ✅ |
| MyMinHeap.java | ✅ |
| MyGraph.java | ✅ |
| MyAdjacencyMatrix.java | ✅ |
| MyDFS.java | ✅ |
| SuggestionService.java | ✅ |
| GraphService.java | ✅ |
| ApiServer.java | ✅ |
| Main.java | ✅ |

---

## Dev 2 — ✅ DONE

| File | Tồn tại | Đúng yêu cầu | Vấn đề |
|------|---------|-------------|--------|
| DataStore.java | ✅ | ✅ | Không |
| UserHandler.java | ✅ | ✅ | Không |
| FriendHandler.java | ✅ | ✅ | Không |
| SuggestionHandler.java | ✅ | ✅ | Không |
| ConsoleMenu.java | ✅ | ✅ | Không |
| frontend/js/api.js | ✅ | ✅ | Không |
| frontend/index.html | ✅ | ✅ | Không |
| frontend/home.html | ✅ | ✅ | Không (Hỗ trợ đồ thị D3 Canvas tương tác tốt) |

---

## Dev 4 — ✅ DONE

| File | Tồn tại | Đúng yêu cầu | Vấn đề |
|------|---------|-------------|--------|
| BenchmarkRunner.java | ✅ | ✅ | Không |
| AI_AuditLog.xlsx | ✅ | ✅ | Không |
| README.md | ✅ | ✅ | Không (Đã Việt hóa hoàn chỉnh và tài liệu hóa 8 CTDL tự viết) |
| frontend/css/style.css | ✅ | ✅ | Không |
| frontend/explore.html | ✅ | ✅ | Không |
| frontend/network.html | ✅ | ✅ | Không |
| frontend/research.html | ✅ | ✅ | Không (Hiển thị biểu đồ hiệu năng trên Canvas động) |

---

## Bugs hiện tại

| Bug | File | Mức độ | Của Dev |
|-----|------|--------|---------|
| Thiếu tệp `NetworkHandler.java` và `BenchmarkHandler.java` trong lệnh biên dịch của `startup.bat` | startup.bat | 🔴 Cao (Lỗi compile) | Dev 4 / Dev 2 |
| *Lưu ý:* Lỗi này **đã được sửa đổi và commit thành công** bởi Tech Lead. Hiện tại toàn bộ mã nguồn biên dịch trơn tru không lỗi. | | | |

---

## Việc còn lại trước deadline

### Dev 2 cần làm:
- Không còn việc tồn đọng. Chỉ cần thực hiện chạy kiểm thử tích hợp (Integration Testing) giữa giao diện Web và các thao tác trên Console CLI để đảm bảo dữ liệu đồng bộ khi ghi/đọc JSON.

### Dev 4 cần làm:
- Không còn việc tồn đọng. Rà soát kỹ nội dung file `docs/AI_AuditLog.xlsx` để chắc chắn khớp hoàn toàn với yêu cầu nhật ký và tiêu chí chấm điểm của giảng viên.

---

## Hướng dẫn chạy nhanh
```
startup.bat
```
- Dashboard: http://localhost:3001
- Research: http://localhost:3001/research.html
- API test: http://localhost:3001/api/users
