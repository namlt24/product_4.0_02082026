# Thiết kế bảng Oracle: Mapping "Kho số chức năng" ↔ Loại kênh > Cửa hàng > User

## Context

Yêu cầu gốc: `dactayeucau` (mục 3.3.4) — xây dựng mapping N-N giữa **kho số chức năng** với
**Loại kênh > Cửa hàng > User**, theo 3 mức phân cấp tùy chọn (chỉ kênh / kênh+cửa hàng /
kênh+cửa hàng+user), hỗ trợ nhập theo file hoặc đơn lẻ, với 2 tác động Thêm mới/Update áp dụng
trên danh sách kho được chọn cho 1 tổ hợp {kênh, cửa hàng?, user?}.

Đã khảo sát domain hiện có (organization-resource-service) và xác nhận:
- **"Kho số chức năng" không phải 1 bảng riêng** — nó chính là 1 bản ghi `SHOP` có
  `CHANNEL_TYPE_ID = 8` ("Kho chức năng riêng", giá trị dữ liệu cụ thể, hiện chưa seed ở local).
  Bằng chứng: đặc tả tự nói rõ "Mã kho số chức năng (chỉ lấy mã kho với ChannelTypeId là 8)".
- `CHANNEL_TYPE`, `SHOP`, `STAFF` (= "User") đều thuộc `organization-resource-service`.
  `TELECOM_SERVICE` ("Loại dịch vụ") thuộc `product-catalog-service`, tham chiếu logic qua ID
  (không FK vật lý) — đúng pattern cross-service đã dùng khắp repo (VD `STAFF.CHANNEL_TYPE_ID`).
- Toàn bộ 38 bảng trong `db-local/init/01_schema.sql` đều **không có FK constraint vật lý**,
  **không có sequence** (ID sinh ở application layer), **không có UNIQUE/INDEX** nào — nhưng
  yêu cầu lần này là **"đánh index đầy đủ"**, nên đây là bảng đầu tiên trong repo có index tường
  minh, thiết kế mới không copy y nguyên chỗ thiếu này của schema cũ.
- 2 bảng mapping N-N gần nhất để tham khảo convention: `CUST_CHANNEL_TYPE_MAPPING` (hậu tố
  `_MAPPING`, PK riêng `<TABLE>_ID`) và `PROD_PACK_SHOP` (mapping phẳng đơn giản, đúng 4 cột
  audit chuẩn `CREATE_USER/CREATE_DATETIME/UPDATE_USER/UPDATE_DATETIME` + `STATUS VARCHAR2(1)`).

**4 quyết định đã chốt:**
1. Service sở hữu: **organization-resource-service** (cùng domain với Channel/Shop/Staff).
2. Cấu trúc: **1 bảng phẳng** duy nhất (mỗi dòng = 1 tổ hợp đầy đủ {dịch vụ, kênh, kho, cửa
   hàng?, user?}) — không tách header/detail, đúng cách đọc đặc tả (9 trường liệt kê phẳng) và
   đúng convention 2 bảng mapping tham khảo ở trên.
3. Không ràng buộc UNIQUE vật lý cho tổ hợp nghiệp vụ — chỉ tạo INDEX để tăng tốc, việc chặn
   trùng do tầng service tự validate (giữ đúng convention hiện tại của repo, 0/38 bảng có UNIQUE).
4. Không cần bảng lịch sử import file — file chỉ là 1 kênh nhập liệu, xử lý y hệt nhập đơn lẻ.

## Thiết kế bảng: `STOCK_CHANNEL_MAPPING`

```sql
-- organization-resource-service — Mapping Kho so chuc nang (SHOP co CHANNEL_TYPE_ID=8)
-- voi Loai kenh > Cua hang > User (N-N, 3 muc phan cap). Xem dactayeucau muc 3.3.4.
CREATE TABLE STOCK_CHANNEL_MAPPING (
    STOCK_CHANNEL_MAPPING_ID  NUMBER(10)   NOT NULL,
    TELECOM_SERVICE_ID        NUMBER(10)   DEFAULT -1 NOT NULL,  -- Loai dich vu (ref TELECOM_SERVICE, cross-service product-catalog); -1 = tat ca
    CHANNEL_TYPE_ID           NUMBER(10)   DEFAULT -1 NOT NULL,  -- Loai kenh (ref CHANNEL_TYPE.CHANNEL_TYPE_ID); -1 = tat ca
    STOCK_SHOP_ID             NUMBER(10)   DEFAULT -1 NOT NULL,  -- Ma kho so chuc nang = SHOP.SHOP_ID voi CHANNEL_TYPE_ID=8; -1 = tat ca kho
    SHOP_ID                   NUMBER(10)   DEFAULT -1 NOT NULL,  -- Ma cua hang (ref SHOP.SHOP_ID); -1 = tat ca cua hang cua channel_type_id
    STAFF_ID                  NUMBER(10)   DEFAULT -1 NOT NULL,  -- Ma user (ref STAFF.STAFF_ID); -1 = tat ca user
    EFFECT_DATE                DATE        NOT NULL,   -- Ngay hieu luc
    EXPIRE_DATE                DATE,                   -- Ngay het hieu luc (NULL = khong gioi han)
    STATUS                    VARCHAR2(1) NOT NULL,   -- '1' Hieu luc / '0' Khong hieu luc
    CREATE_USER               VARCHAR2(50),
    CREATE_DATETIME            DATE        NOT NULL,
    UPDATE_USER                VARCHAR2(50),
    UPDATE_DATETIME             DATE,
    CONSTRAINT PK_STOCK_CHANNEL_MAPPING PRIMARY KEY (STOCK_CHANNEL_MAPPING_ID),
    CONSTRAINT CK_STOCK_CHANNEL_MAP_HIER CHECK (STAFF_ID = -1 OR SHOP_ID != -1)
);

CREATE INDEX IDX_STOCK_CH_MAP_CHANNEL ON STOCK_CHANNEL_MAPPING (CHANNEL_TYPE_ID);
CREATE INDEX IDX_STOCK_CH_MAP_STOCK   ON STOCK_CHANNEL_MAPPING (STOCK_SHOP_ID);
CREATE INDEX IDX_STOCK_CH_MAP_SHOP    ON STOCK_CHANNEL_MAPPING (SHOP_ID);
CREATE INDEX IDX_STOCK_CH_MAP_STAFF   ON STOCK_CHANNEL_MAPPING (STAFF_ID);
CREATE INDEX IDX_STOCK_CH_MAP_TELSVC  ON STOCK_CHANNEL_MAPPING (TELECOM_SERVICE_ID);
CREATE INDEX IDX_STOCK_CH_MAP_GROUP   ON STOCK_CHANNEL_MAPPING (CHANNEL_TYPE_ID, SHOP_ID, STAFF_ID, STOCK_SHOP_ID);
```

**Giải trình 3NF:**
- 1NF: mỗi dòng là 1 tổ hợp nguyên tử `{dịch vụ, kênh, kho, cửa hàng?, user?}` — không có
  danh sách/cột lặp (không nhét nhiều mã kho vào 1 cột dạng chuỗi).
- 2NF: PK là 1 cột surrogate đơn (`STOCK_CHANNEL_MAPPING_ID`) → không thể có phụ thuộc bộ phận
  (2NF chỉ vi phạm được với PK ghép).
- 3NF: mọi cột còn lại (ngày hiệu lực/hết hiệu lực, trạng thái, audit) phụ thuộc trực tiếp vào
  PK, không phụ thuộc bắc cầu qua cột nào khác trong bảng (các cột `*_ID` là tham chiếu khóa
  ngoại logic, không phải thuộc tính mô tả suy ra được từ cột khác).

**Sentinel `-1` ("tất cả") ở mọi cột `*_ID`:** mỗi cột `TELECOM_SERVICE_ID`, `CHANNEL_TYPE_ID`,
`STOCK_SHOP_ID`, `SHOP_ID`, `STAFF_ID` đều là `NUMBER(10) NOT NULL DEFAULT -1`. Giá trị `-1` nghĩa
là **"tất cả"** ở đúng chiều cột đó (không ràng buộc), giá trị cụ thể nghĩa là khóa đúng mục đó.
Đây là mô hình sentinel đã dùng cho `MAP_ACTIVE_INFO` (mỗi cột so khớp giá trị cụ thể hoặc `-1`),
không dùng `NULL` làm nghĩa đặc biệt.

**Ràng buộc phân cấp** (`CK_STOCK_CHANNEL_MAP_HIER = CHECK (STAFF_ID = -1 OR SHOP_ID != -1)`):
nếu `STAFF_ID` là user cụ thể (`!= -1`) thì bắt buộc `SHOP_ID` cũng là cửa hàng cụ thể (`!= -1`) —
đúng luật "mapping chi tiết tới user thì user phải gắn với 1 cửa hàng cụ thể" (điều kiện cấu trúc
cần thiết, không thay thế được validate chéo bảng ở service layer, chỉ chặn lỗi state không hợp
lệ ngay ở DB). Ngược lại, `STAFF_ID = -1` (mọi user) thì `SHOP_ID` có thể cụ thể hoặc `-1` đều hợp
lệ. Đây là CHECK constraint đầu tiên trong repo (khác PK) — an toàn vì chỉ kiểm tra nội bộ 1 dòng,
không phải FK vật lý, không đi ngược convention "không FK" hiện có.

**2 validate nghiệp vụ theo đúng đặc tả** (thực hiện ở tầng service khi implement API sau này,
KHÔNG phải ràng buộc DB): mapping có `SHOP_ID` cụ thể (`!= -1`) → check `SHOP.CHANNEL_TYPE_ID = mapping.CHANNEL_TYPE_ID`
+ tồn tại 1 dòng mapping mức kênh (cùng `{CHANNEL_TYPE_ID}` và `STOCK_SHOP_ID`, `SHOP_ID = -1`) làm
mức bao quát; mapping có `STAFF_ID` cụ thể (`!= -1`) → check `STAFF.SHOP_ID = mapping.SHOP_ID` +
tồn tại 1 dòng mapping mức cửa hàng (cùng `{CHANNEL_TYPE_ID, SHOP_ID, STOCK_SHOP_ID}`, `STAFF_ID = -1`).
`IDX_STOCK_CH_MAP_GROUP` phục vụ trực tiếp 2 truy vấn validate này cũng như luồng "reset danh sách
kho theo tác động Update".

**"Tác động: Thêm mới/Update"** là cờ hành vi tại thời điểm gọi API (Thêm mới = giữ nguyên kho
cũ + upsert kho mới; Update = xoá mềm (`STATUS='0'`) các kho cũ không còn trong danh sách mới +
upsert danh sách mới), không lưu thành cột trong bảng — không cần audit riêng theo quyết định 4.

## Các trường hợp cấu hình (giá trị mỗi cột: con số cụ thể, hoặc `-1` = tất cả)

Mỗi bản ghi là một bộ điều kiện tổng quát: mỗi cột `*_ID` bằng `-1` (không ràng buộc chiều đó)
hoặc giá trị cụ thể (khóa đúng chiều đó). Bảng dưới liệt kê các tổ hợp phổ biến và ý nghĩa.

| # | TELECOM_SERVICE_ID | CHANNEL_TYPE_ID | STOCK_SHOP_ID | SHOP_ID | STAFF_ID | Ý nghĩa nghiệp vụ |
|---|--------------------|-----------------|---------------|---------|----------|--------------------|
| 1 | `-1` | `-1` | `-1` | `-1` | `-1` | **Toàn quyền:** mọi dịch vụ, mọi kênh, mọi shop/user dùng mọi kho. |
| 2 | S | `-1` | `-1` | `-1` | `-1` | Dịch vụ S: mọi kênh/shop/user dùng mọi kho. |
| 3 | `-1` | A | `-1` | `-1` | `-1` | Loại kênh A (mọi shop/user, mọi dịch vụ) dùng **mọi kho**. |
| 4 | `-1` | A | kho `1` | `-1` | `-1` | Loại kênh A, mọi shop/user → dùng **kho 1**. |
| 5 | `-1` | A | kho `1` | X | `-1` | Cửa hàng X (kênh A, mọi user của X) → dùng kho 1. |
| 6 | `-1` | A | kho `1` | X | Y | Riêng **user Y** thuộc cửa hàng X (kênh A) → dùng kho 1. |
| 7 | `-1` | A | `-1` | X | `-1` | Cửa hàng X (kênh A, mọi user) → dùng **mọi kho**. |
| 8 | `-1` | A | `-1` | X | Y | Riêng user Y (cửa hàng X, kênh A) → dùng **mọi kho**. |
| 9 | `-1` | `-1` | kho `1` | `-1` | `-1` | Mọi loại kênh, mọi shop/user → dùng kho 1 (cấp kho toàn hệ thống). |
| 10 | `-1` | `-1` | kho `1` | X | `-1` | Cửa hàng X (mọi kênh) → dùng kho 1. |
| 11 | `-1` | `-1` | `-1` | X | `-1` | Cửa hàng X (mọi kênh) → dùng **mọi kho**. |
| 12 | S | A | kho `1` | `-1` | `-1` | Dịch vụ S + kênh A, mọi shop/user → dùng kho 1. |
| 13 | S | A | kho `1` | X | `-1` | Dịch vụ S + kênh A + cửa hàng X (mọi user) → dùng kho 1. |
| 14 | S | A | kho `1` | X | Y | Dịch vụ S + kênh A + cửa hàng X + **user Y** → dùng kho 1 (chi tiết nhất). |

**Quy tắc resolve:** một user `{dịch vụ S, kênh A, kho K, shop X, user Y}` được dùng kho `K` nếu
tồn tại bản ghi cấu hình mà **mỗi cột** của nó bằng `-1` *hoặc* bằng đúng giá trị của user chiều
đó. Bản ghi cụ thể hơn (ít `-1` hơn) ưu tiên hơn. Resolve không cần dừng ở "3 mức phân cấp cố
định" — vì mỗi chiều độc lập, `-1` ở `CHANNEL_TYPE_ID`/`SHOP_ID`/`STAFF_ID` đều mở rộng phạm vi
tương ứng.

**Dạng bị CHECK chặn:** chỉ duy nhất tổ hợp `STAFF_ID` cụ thể (`!= -1`) đi kèm `SHOP_ID = -1`
(vi phạm `CK_STOCK_CHANNEL_MAP_HIER`). Mọi 4 tổ hợp của cặp `{SHOP_ID, STAFF_ID}` còn lại đều hợp lệ:

| SHOP_ID \ STAFF_ID | `-1` | cụ thể |
|--------------------|------|--------|
| `-1` | ✅ (mọi shop, mọi user) | ⛔ CHECK chặn |
| cụ thể | ✅ (1 shop, mọi user) | ✅ (1 shop, 1 user) |

## Việc đã làm (kết quả thực thi plan)

1. ✅ Seed `CHANNEL_TYPE_ID = 8` ("Kho chức năng riêng") vào `db-local/init/02_sample_data.sql`.
2. ✅ DDL `STOCK_CHANNEL_MAPPING` (bảng + 6 index) vào section `organization-resource-service`
   của `db-local/init/01_schema.sql`.
3. ✅ File DDL tham khảo production:
   `organization-resource-service/src/main/resources/sql/STOCK_CHANNEL_MAPPING.sql`
   (theo đúng pattern file `CHANNEL_TYPE.sql` đã có).
4. ✅ Entity `StockChannelMappingEntity` (package
   `com.viettel.bccs.organization.stockchannelmapping.entity`), theo đúng style
   `CustChannelTypeMappingEntity` (Lombok `@Getter/@Setter`, `@Temporal(TemporalType.DATE)`).
5. ✅ `StockChannelMappingRepository extends JpaRepository<StockChannelMappingEntity, Long>`
   với các method truy vấn nền tảng cho luồng validate (`findByChannelTypeIdAndShopIdAndStaffId`,
   `existsByChannelTypeIdAndStockShopIdAndShopIdIsNullAndStaffIdIsNull`,
   `existsByChannelTypeIdAndShopIdAndStockShopIdAndStaffIdIsNull`, `findByStockShopId`) — dừng ở
   tầng repository, chưa viết service/controller/API (phạm vi 1 task lớn hơn, ngoài phạm vi
   "thiết kế bảng" lần này).
6. ✅ Sample data minh hoạ đủ 3 mức (kênh / kênh+CH / kênh+CH+user) trong `02_sample_data.sql`.

## Verification đã chạy thật (Oracle local)

1. ✅ Tạo bảng + 6 index trên Oracle local (`bccs-oracle` container) — không lỗi cú pháp.
2. ✅ Test CHECK constraint: insert dòng có `STAFF_ID` nhưng thiếu `SHOP_ID` → Oracle từ chối
   đúng `ORA-02290`; 3 dòng hợp lệ (mức kênh/cửa hàng/user) insert thành công.
3. ✅ `EXPLAIN PLAN` cho câu truy vấn nghiệp vụ chính
   (`WHERE CHANNEL_TYPE_ID=? AND SHOP_ID=? AND STAFF_ID=?`) → xác nhận optimizer chọn
   `INDEX RANGE SCAN` trên `IDX_STOCK_CH_MAP_GROUP`, không full table scan.
4. ✅ `mvn clean compile` ở `organization-resource-service` — biên dịch sạch với
   entity/repository mới.

## File đã tạo/sửa

- `db-local/init/01_schema.sql` — thêm DDL bảng (local dev).
- `db-local/init/02_sample_data.sql` — thêm seed `CHANNEL_TYPE_ID=8`, 1 SHOP đóng vai kho, 3 dòng
  mapping mẫu đủ 3 mức.
- `organization-resource-service/src/main/resources/sql/STOCK_CHANNEL_MAPPING.sql` — DDL tham
  khảo production.
- `organization-resource-service/src/main/java/com/viettel/bccs/organization/stockchannelmapping/entity/StockChannelMappingEntity.java`
- `organization-resource-service/src/main/java/com/viettel/bccs/organization/stockchannelmapping/repository/StockChannelMappingRepository.java`

**Trạng thái:** tất cả đang ở dạng chưa commit. Service/controller/API cho tính năng mapping
(thêm mới/update, validate phân cấp, import file) là bước tiếp theo, chưa thực hiện trong phạm
vi thiết kế bảng này.
