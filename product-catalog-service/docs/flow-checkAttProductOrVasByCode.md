# Flow của API `checkAttProductOrVasByCode`

**Endpoint:** `GET /product-catalog-service/v1/product/checkAttProductOrVasByCode` **Mục đích nghiệp vụ:** Kiểm tra một sản phẩm/VAS (xác định bởi `productCode` kết hợp `productType` = `product_offer_type_id`, dùng để phân biệt product/VAS) có đang được gán một đặc tính (`product_spec_char`) cụ thể theo `attributeCode` hay không, chỉ tính các bản ghi đang active (`status='1'`) ở tất cả các bảng liên quan.

## Bước 1 — Validate tham số đầu vào tại controller

Ba tham số đều bắt buộc:

- `productCode` (String): `@Size(min=1, max=50)`, `@Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$")` → nếu thiếu, rỗng, dài hơn 50 ký tự, hoặc chứa ký tự ngoài chữ/số/`_`/`-`, Spring trả lỗi validate (400), chặn trước khi vào service.
- `productType` (String): `@Size(min=1, max=50)`, `@Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$")` → cùng điều kiện chặn 400 như trên. Lưu ý: regex này cho phép cả **chữ cái**, trong khi service sẽ ép kiểu tham số này sang `Long` ở Bước 2 — xem hệ quả bên dưới.
- `attributeCode` (String): `@Size(min=1, max=200)`, `@Pattern(regexp = "^[A-Za-z0-9_-]{1,200}$")` → nếu thiếu, rỗng, dài hơn 200 ký tự, hoặc chứa ký tự ngoài chữ/số/`_`/`-`, Spring trả lỗi validate (400).

## Bước 2 — Validate nghiệp vụ và ép kiểu `productType` tại service

**Nếu** `productCode`, `productType` hoặc `attributeCode` **null/rỗng** → ném **`BusinessException("BCCS-CATALOG-PRODUCT-0003", "productCode, productType and attributeCode are required")`**. Do cả 3 tham số đều `required` và bị chặn bởi `@Size`/`@Pattern` ở Bước 1, nhánh này trên thực tế **không thể trigger được qua REST endpoint**, chỉ có ý nghĩa nếu service bị gọi trực tiếp nội bộ.

Message lỗi tiếng việt: Yêu cầu đầy đủ 3 tham số productCode, productType và attributeCode

Sau đó service gọi `Long.valueOf(productType.trim())` để ép `productType` sang kiểu số. Vì `@Pattern` ở controller vẫn cho phép chuỗi toàn chữ cái (ví dụ `productType=ABC`) đi qua Bước 1, khi đó bước ép kiểu này ném `NumberFormatException` — một exception thô, không phải `BusinessException`/`LogicException`, không được bắt riêng ở tầng này nên sẽ lộ ra ngoài như một lỗi hệ thống (HTTP 500) thay vì một business error code rõ ràng.

## Bước 3 — Truy vấn kiểm tra đặc tính đã gán cho sản phẩm/VAS

Câu SQL được build động; với `checkAttProductOrVasByCode`, hai điều kiện `productCode` và `productType` được thêm (điều kiện `offerId` bị bỏ qua vì `null`):

```sql
SELECT COUNT(1) FROM BCCS_PRODUCT.product_offering a,
       BCCS_PRODUCT.product_offer_char_use b,
       BCCS_PRODUCT.product_spec_char c,
       BCCS_PRODUCT.product_spec_char_value d
WHERE a.product_offering_id = b.product_offering_id
  AND b.product_spec_char_id = c.product_spec_char_id
  AND d.product_spec_char_value_id = b.product_spec_char_value_id
  AND c.code = :attributeCode
  AND a.status = '1' AND b.status = '1' AND c.status = '1' AND d.status = '1'
  AND a.code = :productCode
  AND a.product_offer_type_id = :productType
```

Đếm số dòng nối `product_offering` (theo `code = productCode` và `product_offer_type_id = productType`) → `product_offer_char_use` (gán đặc tính cho sản phẩm) → `product_spec_char` (định nghĩa đặc tính, lọc theo `code = attributeCode`) → `product_spec_char_value` (giá trị đã chọn cho đặc tính đó), với điều kiện tất cả 4 bảng đều đang active (`status = '1'`).

Kết quả: `count.longValue() > 0` → `true`/`false`.

### Nhánh A — Sản phẩm/VAS có đặc tính này

`COUNT(1) > 0` → service trả về `true`.

### Nhánh B — Không tìm thấy dòng nào khớp

Trả về `false`. Điều này xảy ra không chỉ khi sản phẩm chưa từng gán đặc tính, mà còn khi `productCode` đúng nhưng `productType` sai (không khớp `product_offer_type_id` thực tế của sản phẩm), hoặc bất kỳ bản ghi liên quan nào ở 4 bảng đang bị `status != '1'`.

## Bước 4 — Trả kết quả

Response thành công: `StandardResponse<Boolean>` bọc giá trị `true`/`false` từ Bước 3.

---

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| `BCCS-CATALOG-PRODUCT-0003` | `productCode`, `productType` hoặc `attributeCode` null/rỗng tại tầng service (`ProductOfferingService.checkAttProductOrVasByCode`). Trong thực tế qua REST endpoint **không thể trigger được** vì `@Size`/`@Pattern` ở controller đã chặn trước; chỉ có ý nghĩa nếu service bị gọi trực tiếp nội bộ. Không có entry fallback cho mã này trong `bccs-error-codes-fallback.json` của service — HTTP status/message thực tế cần verify qua central registry. |
| *(không có mã lỗi cụ thể — `NumberFormatException` thô)* | `productType` đúng định dạng `@Pattern` (được phép chứa chữ cái) nhưng không phải số hợp lệ, ví dụ `productType=ABC` → `Long.valueOf()` ném lỗi không được bắt, trả về lỗi hệ thống (500) thay vì một business error code rõ ràng. |
| *(validate framework, không phải BusinessException)* | `productCode` thiếu, rỗng, dài hơn 50 ký tự, hoặc chứa ký tự ngoài `[A-Za-z0-9_-]` → lỗi 400 dạng `ErrorResponse` chuẩn, message tương ứng (`"productCode tối đa 50 ký tự"`, `"productCode chỉ gồm chữ, số, '_' hoặc '-'"`). |
| *(validate framework, không phải BusinessException)* | `productType` thiếu, rỗng, dài hơn 50 ký tự, hoặc chứa ký tự ngoài `[A-Za-z0-9_-]` → lỗi 400 tương tự, message (`"productType tối đa 50 ký tự"`, `"productType chỉ gồm chữ, số, '_' hoặc '-'"`). |
| *(validate framework, không phải BusinessException)* | `attributeCode` thiếu, rỗng, dài hơn 200 ký tự, hoặc chứa ký tự ngoài `[A-Za-z0-9_-]` → lỗi 400 tương tự, message (`"attributeCode tối đa 200 ký tự"`, `"attributeCode chỉ gồm chữ, số, '_' hoặc '-'"`). |
