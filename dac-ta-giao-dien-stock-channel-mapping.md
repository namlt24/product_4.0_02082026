# Đặc tả giao diện: Mapping Kho số chức năng ↔ Loại kênh > Cửa hàng > User

## 1. Mục đích

Chức năng cho phép người dùng cấu hình quan hệ **nhiều – nhiều** giữa:
- **Kho số chức năng** (là 1 bản ghi `SHOP` có `CHANNEL_TYPE_ID = 8` — "Kho chức năng riêng"), và
- **Loại kênh > Cửa hàng > User** (3 mức phân cấp tùy chọn).

Mapping được thực hiện theo **file** hoặc **đơn lẻ**, với 2 **tác động**: **Thêm mới** / **Update**.

Dữ liệu lưu vào bảng `STOCK_CHANNEL_MAPPING` thuộc `organization-resource-service`.

**Yêu cầu nghiệp vụ gốc** (tham khảo `process.txt`): thông tin mapping gồm **Loại Dịch vụ, Loại
kênh, Mã kho số chức năng (chỉ lấy mã kho có `CHANNEL_TYPE_ID = 8`), Mã cửa hàng, Mã user, Ngày
hiệu lực, Ngày hết hiệu lực, Trạng thái, Tác động (Thêm mới/Update)**.

## 2. Danh mục ràng buộc dữ liệu (nguồn: bảng STOCK_CHANNEL_MAPPING)

| Cột | Kiểu | Ràng buộc | Ghi chú |
|-----|------|-----------|---------|
| `TELECOM_SERVICE_ID` | NUMBER(10) | **NOT NULL, luôn giá trị cụ thể (không dùng -1)** | Loại dịch vụ (ref `TELECOM_SERVICE`, cross-service) |
| `CHANNEL_TYPE_ID` | NUMBER(10) | NOT NULL DEFAULT -1; **được phép -1** = tất cả loại kênh | ref `CHANNEL_TYPE.CHANNEL_TYPE_ID` |
| `STOCK_SHOP_ID` | NUMBER(10) | NOT NULL DEFAULT -1; **được phép -1** = tất cả kho số | = `SHOP.SHOP_ID` có `CHANNEL_TYPE_ID=8` |
| `SHOP_ID` | NUMBER(10) | NOT NULL DEFAULT -1; **được phép -1** = mọi cửa hàng của kênh | ref `SHOP.SHOP_ID` |
| `STAFF_ID` | NUMBER(10) | NOT NULL DEFAULT -1; **được phép -1** = mọi user | ref `STAFF.STAFF_ID` |
| `EFFECT_DATE` | DATE | NOT NULL | Ngày hiệu lực |
| `EXPIRE_DATE` | DATE | NULL = không giới hạn | Ngày hết hiệu lực |
| `STATUS` | VARCHAR2(1) | NOT NULL; `'1'` hiệu lực / `'0'` không hiệu lực | |
| Audit | CREATE_USER, CREATE_DATETIME, UPDATE_USER, UPDATE_DATETIME | — | Tự gán khi lưu |

**Ràng buộc phân cấp (CHECK `CK_STOCK_CHANNEL_MAP_HIER`):**
```
(STAFF_ID <> -1 AND SHOP_ID <> -1 AND CHANNEL_TYPE_ID <> -1)   -- mức user
OR
STAFF_ID = -1                                                  -- không khai user thì tự do
```
Nghĩa là: **user cụ thể** ⇒ bắt buộc **cửa hàng cụ thể** + **loại kênh cụ thể**.

## 3. Các mức mapping được phép (3 mức phân cấp)

Giao diện chỉ cho phép khai theo **đúng 1 trong 3 hình dáng** sau:

| Mức | CHANNEL_TYPE_ID | SHOP_ID | STAFF_ID | Ý nghĩa |
|-----|-----------------|---------|----------|---------|
| **1. Kênh** | cụ thể (A) | `-1` (mọi shop) | `-1` (mọi user) | Mọi cửa hàng & user thuộc loại kênh A được dùng kho |
| **2. Cửa hàng** | cụ thể (A) | cụ thể (X) | `-1` (mọi user) | Mọi user thuộc cửa hàng X (kênh A) được dùng kho |
| **3. User** | cụ thể (A) | cụ thể (X) | cụ thể (Y) | Riêng user Y thuộc cửa hàng X (kênh A) được dùng kho |

> Trong mọi mức, `STOCK_SHOP_ID` và `TELECOM_SERVICE_ID` luôn được khai riêng (tương ứng: kho được chọn, loại dịch vụ áp dụng). `STOCK_SHOP_ID` có thể `-1` (tất cả kho) ở bất kỳ mức nào; `TELECOM_SERVICE_ID` luôn cụ thể.

**Các tổ hợp KHÔNG được phép (chặn ở UI + service, trùng CHECK DB):**
- `STAFF_ID` cụ thể nhưng `SHOP_ID = -1` (user cụ thể phải thuộc cửa hàng cụ thể).
- `STAFF_ID` cụ thể nhưng `CHANNEL_TYPE_ID = -1` (user cụ thể phải thuộc loại kênh cụ thể).
- (`CHANNEL_TYPE_ID = -1` chỉ hợp lệ khi `SHOP_ID = -1` và `STAFF_ID = -1` — mức "mọi kênh, mọi shop, mọi user".)

## 4. Cấu trúc màn hình (Layout)

```
┌─────────────────────────────────────────────────────────────────┐
│ [Vùng tìm kiếm / bộ lọc danh sách mapping hiện có]              │
│   Loại kênh [__]   Cửa hàng [__]   User [__]   Kho số [__]      │
│   Dịch vụ [__]   Ngày hiệu lực [__]   [ Tìm kiếm ] [ Làm mới ]  │
├─────────────────────────────────────────────────────────────────┤
│ Danh sách mapping (bảng)                                        │
│ ┌──┬──────────┬────────┬────────┬──────┬──────────┬──────┬────┐ │
│ │# │Loại kênh │Cửa hàng│  User  │ Kho  │Dịch vụ   │Trạng │... │ │
│ ├──┼──────────┼────────┼────────┼──────┼──────────┼──────┼────┤ │
│ │  │          │        │        │      │          │      │    │ │
│ └──┴──────────┴────────┴────────┴──────┴──────────┴──────┴────┘ │
│                                    [ Nhập file ] [ Thêm mapping ]│
└─────────────────────────────────────────────────────────────────┘

 [ Dialog: Thêm mới / Cập nhật mapping ]
   Mức mapping       (● Kênh   ○ Cửa hàng   ○ User)
   Loại dịch vụ *    [ dropdown ]
   Loại kênh *       [☑ Tất cả (-1)] [☐ Kênh A] [☐ Kênh B] ...
   Cửa hàng          [☑ Tất cả (-1)] [☐ SH X] [☐ SH Y] ...  (tuỳ mức)
   User              [☑ Tất cả (-1)] [☐ US a] [☐ US b] ...  (tuỳ mức)
   Kho số chức năng *[☐ K101] [☑ K102] ...  [☑ Tất cả kho (-1)]
   Ngày hiệu lực *   [ date ]
   Ngày hết hiệu lực [ date ]
   Tác động          (● Thêm mới   ○ Update)
   [ Lưu ]  [ Hủy ]
```

## 5. Các trường & kiểm soát nhập liệu

| Trường | Bắt buộc | Loại control | Danh mục nguồn |
|--------|----------|--------------|----------------|
| Mức mapping | Có | Radio: Kênh / Cửa hàng / User | — |
| Loại dịch vụ (`TELECOM_SERVICE_ID`) | Có | Dropdown (chỉ chọn 1, **không có "tất cả"**) | `TELECOM_SERVICE` |
| Loại kênh (`CHANNEL_TYPE_ID`) | Có | **Multi-checkbox** + item "Tất cả (-1)" | `CHANNEL_TYPE` |
| Cửa hàng (`SHOP_ID`) | Theo mức | **Multi-checkbox** + item "Tất cả (-1)" | `SHOP` (lọc theo `CHANNEL_TYPE_ID`) |
| User (`STAFF_ID`) | Theo mức | **Multi-checkbox** + item "Tất cả (-1)" | `STAFF` (lọc theo `SHOP_ID`) |
| Kho số (`STOCK_SHOP_ID`) | Có | Multi-select + checkbox "Tất cả kho" (`-1`) | `SHOP` có `CHANNEL_TYPE_ID=8` |
| Ngày hiệu lực (`EFFECT_DATE`) | Có | Date | — |
| Ngày hết hiệu lực (`EXPIRE_DATE`) | Không | Date (bỏ trống = không giới hạn) | — |
| Tác động | Có | Radio: Thêm mới / Update | — |

**Quy tắc hiển thị danh sách chọn theo mức:**
- **Mức Kênh:** khóa/ẩn block Cửa hàng & User (tự set `-1`); chỉ chọn Loại kênh + Kho + Dịch vụ.
- **Mức Cửa hàng:** hiện danh sách Cửa hàng (thuộc loại kênh đã chọn); khóa/ẩn User.
- **Mức User:** hiện danh sách Cửa hàng rồi User (user thuộc cửa hàng đã chọn).

**Luật chọn loại trừ (mỗi control kênh/cửa hàng/user):** nếu đã tích item **"Tất cả (-1)"** thì
**không được tích thêm giá trị cụ thể** của cùng control đó, và ngược lại — chọn "Tất cả" sẽ **bỏ
tự động** các giá trị cụ thể đã chọn trước đó, và chọn giá trị cụ thể sẽ **bỏ** "Tất cả". (Chặn
ngay trên giao diện lúc chọn.)

## 6. Tác động Thêm mới / Update (luồng nghiệp vụ)

> **Tiền điều kiện chung** (theo `process.txt`): trước khi lưu, mapping phải thỏa **bậc thang** —
> mức cửa hàng phải có mức kênh tương ứng đã tồn tại (kho đã map cho kênh), mức user phải có mức
> cửa hàng (kho đã map cho cửa hàng + kênh). Xem mục 8 — validate bậc thang.

Với **1 tổ hợp key mapping** `{Loại dịch vụ, Loại kênh, Cửa hàng?, User?}` và **danh sách kho được chọn**:

**a) Tác động = Thêm mới:**
- Tra cứu danh sách mapping hiện có theo key `{dịch vụ, kênh, cửa hàng?, user?}`.
- **Nếu chưa tồn tại** bản ghi mapping cho key đó → **thêm mới** từng kho trong danh sách đã chọn (mỗi dòng: 1 `STOCK_SHOP_ID`, lấy key chung).
- **Nếu đã tồn tại** → thêm mới (upsert) các kho còn thiếu so với danh sách chọn; **giữ nguyên** các kho đã có.

**b) Tác động = Update:**
- **Reset lại** danh sách kho đã gán trước đó cho key `{dịch vụ, kênh, cửa hàng?, user?}`.
  - Các kho hiện có **không còn** trong danh sách mới → **xoá mềm** (`STATUS = '0'`).
  - Upsert danh sách kho mới (thêm các kho chưa có / cập nhật lại trạng thái hiệu lực).
- Kết quả sau Update: tập kho của key đúng bằng **đúng danh sách kho đã chọn** trên giao diện.

> Lưu ý: "Thêm mới/Update" là **cờ hành vi khi gọi API**, không lưu thành cột. Tương đương:
> - Thêm mới = giữ nguyên kho cũ + upsert kho mới.
> - Update = xoá mềm kho cũ không còn + upsert kho mới.

## 7. Nhập theo file

- File mẫu: `.xlsx` / `.csv` với đúng các cột sau (cột trống = áp dụng giá trị rỗng / `-1` theo mức):

| Loại dịch vụ | Loại kênh | Cửa hàng | User | Kho số | Ngày hiệu lực | Ngày hết hạn | Tác động |
|--------------|-----------|----------|------|--------|---------------|--------------|----------|
| mã dịch vụ | mã kênh | (trống nếu mức kênh) | (trống nếu ≤ mức cửa hàng) | mã kho (hoặc "TATCA") | yyyy-MM-dd | yyyy-MM-dd | THEM_MOI / UPDATE |

- Khi đọc file, mỗi dòng phải thỏa **mức phân cấp hợp lệ** (mục 3) — nếu dòng vừa có Cửa hàng vừa có User thì phải đủ cả cửa hàng thuộc kênh & user thuộc cửa hàng.
- Dòng lỗi được **báo lại kèm số dòng + lý do**, không làm hỏng các dòng hợp lệ khác (validate toàn bộ file trước khi ghi).
- Nhập file được xử lý **y hệt nhập đơn lẻ** (không có lịch sử import riêng).

## 8. Validate (tổng hợp — thứ tự thực hiện)

1. **Validate bắt buộc:** Loại dịch vụ, Loại kênh (hoặc "tất cả"), Kho, Ngày hiệu lực, Tác động.
2. **Validate mức phân cấp (phụ thuộc radio mức chọn):**
   - User được chọn ⇒ bắt buộc chọn Cửa hàng + Loại kênh (không "tất cả kênh").
   - Cửa hàng được chọn ⇒ bắt buộc Loại kênh (không "tất cả kênh") — hoặc "tất cả kênh" chỉ khi không chọn shop/user.
3. **Validate tham chiếu + bậc thang (service, theo `process.txt`):**
   - **Mức Cửa hàng:** cửa hàng phải **thuộc loại kênh** (`SHOP.CHANNEL_TYPE_ID = mapping.CHANNEL_TYPE_ID`), **và kho số phải đã map cho kênh** (tồn tại mapping mức kênh cùng `{kênh, kho}`).
   - **Mức User:** user phải **thuộc cửa hàng** (`STAFF.SHOP_ID = mapping.SHOP_ID`), **và kho số phải đã mapping cho cửa hàng và kênh** (tồn tại mapping mức cửa hàng cùng `{kênh, cửa hàng, kho}`, kéo theo tồn tại mapping mức kênh).
   - → Quy tắc **bậc thang**: muốn map mức nhỏ (user) phải có mức lớn (cửa hàng → kênh) đã tồn tại và bao phủ kho.
4. **Validate ngày:** `EFFECT_DATE <= EXPIRE_DATE` (nếu có EXPIRE).
5. **Validate trùng khai báo:** xem **mục 8a** bên dưới.

> **Giả định cần chốt với nghiệp vụ (sentinel `-1`):** khi kiểm tra "kho đã map cho kênh/cửa hàng",
> một bản ghi có `STOCK_SHOP_ID = -1` (tất cả kho) có được coi là **đã bao phủ** kho cụ thể đang xét
> hay không? Nếu **có**, thì 1 dòng mức kênh `-1` cho phép map mọi kho ở mức cửa hàng mà không cần
> từng kho cụ thể; nếu **không**, bắt buộc phải có chính xác từng kho ở mức lớn hơn trước.

## 8a. Validate trùng khai báo (logic)

Mỗi bản ghi mapping = **key** `{Loại dịch vụ, Loại kênh, Cửa hàng, User}` (giá trị cụ thể hoặc
`-1`) + **1 kho** (`STOCK_SHOP_ID`). Việc phát hiện trùng dựa trên **mức** của các bản ghi, trong đó
mức được xếp theo thứ tự **Kênh < Cửa hàng < User** (mức Kênh rộng nhất, mức User hẹp nhất):

| Mức | key đi kèm |
|-----|-----------|
| Kênh | Cửa hàng = `-1`, User = `-1` |
| Cửa hàng | Cửa hàng cụ thể, User = `-1` |
| User | Cửa hàng cụ thể, User cụ thể |

### 8a.1 — Trùng khai báo cứng (BỊ CHẶN)

Hai bản ghi **cùng mức**, **cùng key** và **cùng kho** → trùng hoàn toàn → **báo lỗi, chặn cứng**
(áp dụng cả khi nhập đơn lẻ lẫn trong 1 file).

Tiêu chí so khớp: `TELECOM_SERVICE_ID`, `CHANNEL_TYPE_ID` giống nhau, và bộ `{SHOP_ID, STAFF_ID}`
giống nhau theo đúng mức, và cùng `STOCK_SHOP_ID`.

Ví dụ (chặn):

| # | Dịch vụ | Kênh | Cửa hàng | User | Kho | Kết quả |
|---|---------|------|----------|------|-----|---------|
| A | 3 | 2 | 12345 | `-1` | 101 | — |
| B | 3 | 2 | 12345 | `-1` | 101 | ⛔ trùng A |

### 8a.2 — Trùng phủ lấp khác mức (BỊ CHẶN)

Hai bản ghi **cùng dịch vụ + cùng kênh + cùng kho** nhưng **khác mức** (mức lớn bao mức nhỏ do `-1`)
→ **báo lỗi, chặn** — vì mức lớn đã bao phủ mức nhỏ, khai thêm là trùng/không hợp lệ. Áp dụng trên
**toàn bộ dữ liệu** (đã lưu), không chỉ trong 1 lần chọn trên giao diện.

Ví dụ (chặn — cùng `{dịch vụ 3, kênh 2, kho 101}`, khác mức):

| # | Dịch vụ | Kênh | Cửa hàng | User | Kho | Kết quả |
|---|---------|------|----------|------|-----|---------|
| C | 3 | 2 | `-1` | `-1` | 101 | (đã lưu) mức Kênh |
| D | 3 | 2 | 12345 | `-1` | 101 | ⛔ mức Cửa hàng bị C bao → chặn |

→ C (mức kênh) đã cho phép mọi shop của kênh 2 dùng kho 101; D thêm shop 12345 là thừa → chặn.

Ví dụ (cho phép — **khác dịch vụ**, không bị bao phủ):

| # | Dịch vụ | Kênh | Cửa hàng | User | Kho | Kết quả |
|---|---------|------|----------|------|-----|---------|
| C | 3 | 2 | `-1` | `-1` | 101 | (đã lưu) mức Kênh, dịch vụ 3 |
| D | 5 | 2 | 12345 | `-1` | 101 | ✅ dịch vụ 5 khác 3 → lưu bình thường |

→ D có dịch vụ 5 (khác 3) nên không bị C bao phủ → **được phép lưu**.

### 8a.3 — Cùng key, nhiều kho (CHO PHÉP, không chặn)

Hai bản ghi **cùng key** nhưng **khác kho** → không trùng; 1 key được cấp nhiều kho là hợp lệ.

Ví dụ (cho phép):

| # | Dịch vụ | Kênh | Cửa hàng | User | Kho |
|---|---------|------|----------|------|-----|
| E | 3 | 2 | 12345 | `-1` | 101 |
| F | 3 | 2 | 12345 | `-1` | 102 |

### 8a.4 — Logic tổng quát (thuật toán)

Với từng bản ghi đề xuất (key `{dịch vụ, kênh, cửa hàng, user}` + `STOCK_SHOP_ID`), trước khi ghi,
so với dữ liệu đã có (hoặc các dòng khác trong cùng 1 file), xác định **mức** của bản ghi đang xét:

1. **Tìm trùng cứng (8a.1):** bản ghi **cùng mức** (bộ `{SHOP_ID, STAFF_ID}` theo đúng mức) + cùng
   `TELECOM_SERVICE_ID`, `CHANNEL_TYPE_ID`, `STOCK_SHOP_ID` → **chặn**.
2. **Tìm trùng phủ lấp khác mức (8a.2):** tồn tại bản ghi **khác mức** mà **cùng
   `{TELECOM_SERVICE_ID, CHANNEL_TYPE_ID, STOCK_SHOP_ID}`** và mức lớn bao mức nhỏ (do `-1`) →
   **chặn** (ví dụ: đã có mức Kênh thì không thêm mức Cửa hàng/user cùng dịch vụ+kênh+kho).
   - Nếu khác **dịch vụ** (`TELECOM_SERVICE_ID` khác) → **không** coi là trùng (cho phép).
3. Trường hợp còn lại (khác kho, hoặc khác dịch vụ) → **không trùng**, cho phép (8a.3).
4. **Trên giao diện:** bổ sung chặn tức thời — một control (kênh/cửa hàng/user) đã tích "Tất cả
   (-1)" thì không chọn thêm giá trị cụ thể của cùng control đó (xem mục 5).

## 9. Luồng gọi API (backend — để UI chuẩn bị)

UI gửi 1 request tổng hợp lên service:

```
POST {service}/v1/stock-channel-mapping/save
{
  "telecomServiceId": 3,
  "channelTypeId": 2,              // hoặc -1 = tất cả kênh
  "shopId": 12345,                 // hoặc -1
  "staffId": -1,                   // hoặc cụ thể
  "stockShopIds": [101, 102],      // hoặc [-1] = tất cả kho
  "effectDate": "2026-08-19",
  "expireDate": null,
  "action": "THEM_MOI" | "UPDATE"
}
```

Service thực hiện theo đúng luồng ở mục 6 (tra key, upsert, xoá mềm nếu Update), validate tham chiếu
(mục 8.3), rồi ghi DB. UI hiển thị kết quả (số dòng thêm/update).

## 10. Thông điệp / xử lý lỗi (đề xuất)

| Tình huống | Thông báo |
|------------|-----------|
| Thiếu bắt buộc | "Vui lòng chọn {tên trường}" |
| User chọn nhưng thiếu cửa hàng/kênh | "Mức User bắt buộc chọn Loại kênh và Cửa hàng cụ thể" |
| Cửa hàng không thuộc loại kênh | "Cửa hàng {X} không thuộc loại kênh {A}" |
| User không thuộc cửa hàng | "User {Y} không thuộc cửa hàng {X}" |
| `EFFECT_DATE > EXPIRE_DATE` | "Ngày hiệu lực phải <= ngày hết hiệu lực" |
| Trùng cứng (cùng mức + cùng kho) | "Đã tồn tại mapping {mức}: dịch vụ {S}, kênh {A}, kho {K} — không thể khai trùng" |
| Trùng phủ lấp khác mức (mức lớn đã bao mức nhỏ, cùng dịch vụ+kênh+kho) | "Đã tồn tại mapping mức {mức lớn} cho dịch vụ {S}, kênh {A}, kho {K} — không thể khai mức {mức nhỏ} do bị bao phủ" |
| Đã tích "Tất cả (-1)" ở control kênh/cửa hàng/user mà chọn thêm giá trị cụ thể | "Đã chọn Tất cả (-1), không thể chọn thêm giá trị cụ thể" |
| Lỗi file (dòng N) | "File lỗi dòng {N}: {lý do}" |

---

*Đặc tả được xây theo bảng `STOCK_CHANNEL_MAPPING` và các ràng buộc đã chốt (sentinel -1, 3 mức phân cấp,
nhập file/đơn lẻ, tác động Thêm mới/Update).*
