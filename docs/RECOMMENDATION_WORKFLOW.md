# Sơ đồ Luồng Hoạt Động Của Hệ Thống Gợi Ý Kết Bạn

Dưới đây là sơ đồ trực quan hóa toàn bộ thuật toán gợi ý bạn bè (Recommendation Engine) được mô tả trong `CODE_WALKTHROUGH.md`, giúp bạn dễ dàng hình dung hơn.

## 1. Sơ đồ Thuật Toán Tổng Quát

```mermaid
flowchart TD
    classDef step fill:#f0f9ff,stroke:#0284c7,stroke-width:2px,color:#0f172a,rx:5px,ry:5px
    classDef condition fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#0f172a
    classDef startend fill:#10b981,stroke:#047857,stroke-width:2px,color:#fff,rx:20px,ry:20px
    classDef data fill:#f3e8ff,stroke:#7e22ce,stroke-width:2px,color:#0f172a,rx:5px,ry:5px

    Start([Bắt đầu: User ID]) ::: startend --> S1

    subgraph Phase1 [Bước 1: Thu thập ứng viên (BFS Level-2)]
        S1[Lấy danh sách bạn trực tiếp <br> Level 1] ::: step --> S2
        S2[Lấy bạn của bạn <br> Level 2] ::: step --> S3
        S3[Loại bỏ: Chính mình & <br> Bạn trực tiếp đã kết bạn] ::: step
    end
    
    S3 --> Candidates[(Danh sách <br> Ứng viên tiềm năng)] ::: data
    Candidates --> S4

    subgraph Phase2 [Bước 2: Chấm điểm Jaccard Similarity]
        S4[Tìm số bạn chung <br> Intersection] ::: step --> S5
        S5[Tìm tổng số bạn của cả 2 <br> Union] ::: step --> S6
        S6[Tính điểm: <br> Jaccard = Intersection / Union] ::: step
    end

    S6 --> S7

    subgraph Phase3 [Bước 3: Lọc Top-K bằng MinHeap]
        S7{MinHeap <br> đã đủ K người chưa?} ::: condition
        
        S7 -- Chưa --> S8[Đẩy vào MinHeap] ::: step
        S7 -- Rồi --> S9{Điểm Jaccard > <br> người thấp nhất trong Heap?} ::: condition
        
        S9 -- Có --> S10[Xóa người thấp nhất <br> Đẩy người mới vào] ::: step
        S9 -- Không --> S11[Bỏ qua] ::: step
    end

    S8 --> S12
    S10 --> S12
    S11 --> S12

    S12([Kết thúc: Trả về Top K gợi ý]) ::: startend
```

## 2. Ví Dụ Cụ Thể (Mô phỏng mạng 5 người)

Để dễ hiểu hơn, hãy xem một ví dụ thực tế khi hệ thống gợi ý bạn bè cho **User A**.

```mermaid
graph TD
    classDef userA fill:#3b82f6,stroke:#1d4ed8,color:#fff,rx:50px,ry:50px
    classDef friend fill:#10b981,stroke:#047857,color:#fff,rx:50px,ry:50px
    classDef fof fill:#f59e0b,stroke:#b45309,color:#fff,rx:50px,ry:50px
    classDef ignore fill:#94a3b8,stroke:#475569,color:#fff,rx:50px,ry:50px

    A((User A)) ::: userA
    B((User B)) ::: friend
    C((User C)) ::: friend
    D((User D <br> ƯCV số 1)) ::: fof
    E((User E <br> ƯCV số 2)) ::: fof

    A ---|Bạn trực tiếp| B
    A ---|Bạn trực tiếp| C
    
    B ---|Bạn chung của A & D, E| D
    B ---|Bạn chung của A & E| E
    
    C ---|Bạn chung của A & D| D
```

### Quá trình phân tích cho User A:

**Bước 1: BFS Level-2 (Tìm ứng viên)**
- Bạn trực tiếp của A (Level 1): `B, C`
- Bạn của bạn (Level 2): Qua B tìm được `D, E`. Qua C tìm được `D`.
- Kết luận: Các ứng viên tiềm năng là **D và E**.

**Bước 2: Tính điểm Jaccard**
- **Đánh giá User D:**
  - Bạn của A: `{B, C}`
  - Bạn của D: `{B, C}`
  - Bạn chung (Giao): `{B, C}` = 2
  - Tổng số bạn (Hợp): `{B, C}` = 2
  - **Điểm Jaccard của D = 2 / 2 = 1.0 (100% phù hợp)**
  
- **Đánh giá User E:**
  - Bạn của A: `{B, C}`
  - Bạn của E: `{B}`
  - Bạn chung (Giao): `{B}` = 1
  - Tổng số bạn (Hợp): `{B, C}` = 2
  - **Điểm Jaccard của E = 1 / 2 = 0.5 (50% phù hợp)**

**Bước 3: Lọc MinHeap Top-1**
- Nếu hệ thống chỉ cần gợi ý 1 người (Top 1).
- Đưa **User D (1.0)** vào Heap.
- Đến lượt **User E (0.5)**: Vì 0.5 < 1.0 nên bị bỏ qua.
- **Kết quả cuối cùng:** Hệ thống đề xuất **User D** cho **User A**.
