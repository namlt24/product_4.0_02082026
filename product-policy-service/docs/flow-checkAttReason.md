# Flow của API `checkAttReason`

**Endpoint:** `GET /product-policy-service/v1/reason/checkAttReason` (params `reasonId`, `attributeCode`). **Mục đích nghiệp vụ:** Kiểm tra một hình thức hòa mạng (reason) có đang được gán đặc tính (product spec char) trùng với mã `attributeCode` truyền vào hay không, chỉ tính các bản ghi gán đặc tính đang active.

## Bước 1 — Validate tham số đầu vào (tầng controller)

- `reasonId`: bắt buộc (không có `required = false`), kiểu `Long`, `@Min(0)` và `@Max(9999999999L)` — thiếu tham số, âm, hoặc vượt quá 9999999999 sẽ bị chặn ở tầng framework trước khi vào service, trả HTTP 400.
- `attributeCode`: bắt buộc, `@Size(min = 1, max = 50)` và `@Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$")` — rỗng, dài quá 50 ký tự, hoặc chứa ký tự ngoài chữ/số/`_`/`-` (ví dụ khoảng trắng, dấu chấm) đều bị chặn ở tầng framework, trả HTTP 400.

## Bước 2 — Validate lại ở tầng service

Service kiểm tra `reasonId` và `attributeCode` không null/rỗng bằng `DataUtil.isAnyNull`. Do cả hai tham số đã bắt buộc và được validate chặt ở Bước 1 (controller không cho phép giá trị null lọt qua), nhánh lỗi này trên thực tế không thể bị kích hoạt khi gọi qua HTTP — chỉ còn ý nghĩa nếu method được gọi nội bộ (không qua controller).

Nếu vi phạm: **`BusinessException("BCCS-POLICY-REASON-0003", "reasonId and attributeCode are required")`**
Message lỗi tiếng việt: Thiếu thông tin reasonId hoặc attributeCode, vui lòng nhập đầy đủ.

## Bước 3 — Truy vấn danh sách gán đặc tính đang active của reason

Service gọi `reasonCharUseRepository.findByReasonIdInAndStatus(List.of(reasonId), "1")` — đây là Spring Data derived query, tương đương lọc bảng `REASON_CHAR_USE` theo `REASON_ID = :reasonId AND STATUS = '1'` (giá trị `"1"` là mã trạng thái active, cấu hình tại `Const.STATUS.ACTIVE`).

### Nhánh A — Không có bản ghi `REASON_CHAR_USE` nào active cho reason này
Trả về `false` ngay lập tức (reason không có gán đặc tính active nào, nên chắc chắn không khớp `attributeCode`).

### Nhánh B — Có ít nhất một bản ghi active
Đi tiếp sang Bước 4.

## Bước 4 — Lọc danh sách `productSpecCharId`

Từ các bản ghi `REASON_CHAR_USE` active, lấy `PRODUCT_SPEC_CHAR_ID`, loại bỏ giá trị null, loại trùng.

### Nhánh A — Sau khi lọc, danh sách `productSpecCharId` rỗng
Xảy ra khi tất cả bản ghi active của reason đều có `PRODUCT_SPEC_CHAR_ID` là null (ví dụ bản ghi chỉ gán `PRODUCT_SPEC_CHAR_VALUE_ID` mà không gán `PRODUCT_SPEC_CHAR_ID`). Trả về `false`.

### Nhánh B — Danh sách có ít nhất 1 id
Đi tiếp sang Bước 5.

## Bước 5 — Gọi sang product-catalog-service để lấy mã đặc tính

Gọi `product-catalog-service` qua `ProductSpecCharClient.findByIds` (`POST /v1/productspecchar/findByIds`, body là danh sách `productSpecCharId`) để lấy danh sách `{productSpecCharId, code, status}` tương ứng.

Nếu lời gọi này ném `RuntimeException` (lỗi kết nối, timeout, response lỗi...): **`IntegrationException("BCCS-SYS-INT-0001", "Error calling product-catalog-service productspecchar/findByIds for ids=" + ids)`**
Message lỗi tiếng việt: Lỗi tích hợp — không gọi được sang product-catalog-service để lấy dữ liệu đặc tính sản phẩm, vui lòng thử lại sau.

Nếu response trả về `null` hoặc `data` là `null`, coi như danh sách rỗng (không throw lỗi) — dẫn tới kết quả cuối cùng là `false` ở Bước 6.

## Bước 6 — So khớp mã đặc tính

Service duyệt danh sách kết quả từ Bước 5, kiểm tra có bất kỳ phần tử nào có `code` trùng khớp chính xác (so sánh chuỗi, phân biệt hoa/thường) với `attributeCode` truyền vào không (`anyMatch`).

### Nhánh A — Có ít nhất một phần tử khớp
Trả về `true`.

### Nhánh B — Không có phần tử nào khớp (kể cả khi danh sách từ Bước 5 rỗng)
Trả về `false`.

Đây là kết quả trả về cuối cùng của API — response luôn là `StandardResponse<Boolean>` bọc giá trị `true`/`false`, không có khái niệm "không tìm thấy" trả về null hay danh sách rỗng ở tầng response.

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| HTTP 400 (validation) | `reasonId` thiếu, âm, hoặc > 9999999999 |
| HTTP 400 (validation) | `attributeCode` thiếu, rỗng, dài > 50 ký tự, hoặc chứa ký tự ngoài `[A-Za-z0-9_-]` |
| `BCCS-POLICY-REASON-0003` | `reasonId` hoặc `attributeCode` null/rỗng khi vào tới service — trên thực tế không thể xảy ra khi gọi qua HTTP endpoint vì đã bị chặn ở Bước 1 |
| `BCCS-SYS-INT-0001` | Gọi sang product-catalog-service (`POST /v1/productspecchar/findByIds`) thất bại (lỗi kết nối, timeout, exception khác) |
