# Tổng hợp thay đổi từ V5 đến nay

Trạng thái: chưa commit. Migration V5–V8 cần DBA ban giao lên DB thật theo đúng thứ tự.

## 1. Migration DB

| File | Nội dung |
|---|---|
| `V5__backend_step_condition_groups.sql` | Cột `CONDITION_GROUPS` (CLOB) lưu cây điều kiện nhiều nguồn. |
| `V6__backend_step_condition_operator_source.sql` | Nới CHECK cho toán tử mới (CONTAINS, IN, STARTS_WITH, MATCHES_REGEX, …) và nguồn mới (QUERY_PARAM, HEADER, SYSTEM_FLAG). Constraint của V1 không có tên nên migration tìm theo nội dung để drop. |
| `V7__backend_step_switch_size.sql` | Cột `SWITCH_BRANCHES` (CLOB) cho rẽ nhiều nhánh; nới CHECK thêm toán tử `SIZE_*`. |
| `V8__backend_step_false_error.sql` | Cột `FALSE_ERROR_CODE`, `FALSE_ERROR_MESSAGE` cho nhánh Sai báo lỗi. |

- V6, V7, V8 đã áp dụng trên DB local.
- V4 (tính năng alert) vẫn chưa có trong repo từ trước; không thuộc phạm vi các thay đổi này.

## 2. Engine (backend)

**Toán tử mới**
- Mảng: `CONTAINS`, `NOT_CONTAINS`, `IN`, `NOT_IN`.
- Chuỗi: `STARTS_WITH`, `ENDS_WITH`, `STRING_CONTAINS`, `MATCHES_REGEX` (giới hạn 50 ms, chống catastrophic backtracking).
- Rỗng: `IS_EMPTY`, `IS_NOT_EMPTY`.
- Kích thước: `SIZE_EQUALS`, `SIZE_GREATER_THAN`, `SIZE_GREATER_THAN_OR_EQUAL`, `SIZE_LESS_THAN`, `SIZE_LESS_THAN_OR_EQUAL`.

**Nguồn mới**
- `QUERY_PARAM`, `HEADER` (dispatcher truyền headers của client vào engine).

**Điều kiện nâng cao**
- So sánh hai field (vế phải lấy từ field khác).
- Cây điều kiện đệ quy: nhóm AND/OR lồng nhau, tối đa 5 cấp; lá có thể là cờ hệ thống.
- Lặp mảng: `ANY`, `ALL` (mảng rỗng không khớp), `COUNT`. Điều kiện con đọc field từ từng phần tử.

**Rẽ nhánh**
- Rẽ nhiều nhánh (switch): nhánh đầu tiên khớp được chọn; không khớp thì dùng step mặc định.
- Báo lỗi cho nhánh Sai: rẽ 2 nhánh hoặc switch không khớp có thể dừng và trả lỗi với mã và thông điệp.

**Trace**
- Mỗi điều kiện ghi `conditionInfo` (field đọc, giá trị thấy, kết quả); hiển thị trong "Thử nhanh".

**Validation**
- Nhóm rỗng, logic sai, regex sai, COUNT thiếu ngưỡng, switch thiếu đích, vòng lặp nhánh, cờ chưa khai báo, lồng quá sâu, báo lỗi thiếu mã/thông điệp.

## 3. Giao diện (frontend)

- Trình soạn cây điều kiện dùng chung cho form và canvas: nhóm lồng nhau, lặp mảng, so sánh hai field, cờ hệ thống.
- Switch: có trong form và panel canvas. Canvas vẽ mũi tên `Nhánh n` và `Mặc định`.
- Báo lỗi: chọn trong ô step của nhánh Sai (điều kiện 2 nhánh và switch).
- Trang sơ đồ luồng `/endpoints/:id/flow` và panel "Sơ đồ" trong canvas, có xuất Mermaid.
- Nhãn toán tử và bố cục thống nhất giữa form và canvas.

## 4. Lỗi đã sửa trong quá trình làm

- JSON mất field `quantifier`: Jackson coi `isQuantifier()` là getter và bỏ field. Đổi thành `hasQuantifier()`.
- Jackson chọn sai constructor: thêm `@JsonCreator` cho canonical constructor.
- DB thiếu toán tử CONTAINS trong CHECK (đã sửa ở V6).
- Form: `formArrayName` bao trùm ô chọn chế độ ghép response, gây lỗi "Cannot find control".
- Ô so sánh hai field tự tắt vì chuỗi rỗng là falsy.
- Canvas: thay đổi trong panel phải bấm "Áp dụng" trước khi "Lưu" (hành vi có sẵn).
- Dọn code không dùng và import thừa.

## 5. Kiểm thử

- Backend: 472 test, 0 failure.
- Frontend: `ng build` pass.
- API live: bộ V7 16/16, bộ hồi quy 20/20.
- Trình duyệt: form, canvas, panel switch, trang sơ đồ, báo lỗi.

**Chưa kiểm thử đầy đủ bằng UI:** điều kiện theo cờ hệ thống trên giao diện; lặp mảng trực tiếp trên canvas.
