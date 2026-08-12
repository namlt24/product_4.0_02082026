# Flow của API `hasProductAtt`

**Endpoint:** `GET /product-catalog-service/v1/product/hasProductAtt` **Mục đích nghiệp vụ:** Kiểm tra một sản phẩm/gói cước (xác định theo `offerId` = `product_offering_id`) có đang được gán một đặc tính (`product_spec_char`) cụ thể theo `attributeCode` hay không, chỉ tính các bản ghi đang active (`status='1'`) ở tất cả các bảng liên quan.

## Bước 1 — Validate tham số đầu vào tại controller

Hai tham số bắt buộc:

- `offerId` (Long): `@Min(1)`, `@Max(9999999999L)` → nếu thiếu, `<1`, hoặc vượt quá `9999999999`, Spring trả lỗi validate (400) **trước khi vào tới service**.
- `attributeCode` (String): `@Size(min=1, max=200)`, `@Pattern(regexp = "^[A-Za-z0-9_-]{1,200}$")` → nếu thiếu, rỗng, dài hơn 200 ký tự, hoặc chứa ký tự ngoài chữ/số/`_`/`-`, Spring trả lỗi validate (400) tương tự, cũng chặn trước khi vào service.

## Bước 2 — Validate nghiệp vụ tại service

**Nếu** `offerId` **null hoặc** `attributeCode` **null/rỗng** → ném **`BusinessException("BCCS-CATALOG-PRODUCT-0004", "offerId and attributeCode are required")`**.

Message lỗi tiếng việt: Yêu cầu đầy đủ 2 tham số offerId và attributeCode

## Bước 3 — Truy vấn kiểm tra đặc tính đã gán cho sản phẩm

Câu SQL được build động; với `hasProductAtt`, chỉ điều kiện `offerId` được thêm (điều kiện `productCode`/`productType` bị bỏ qua vì cả hai đều `null`):

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
  AND a.product_offering_id = :offerId
```

Đếm số dòng nối `product_offering` (theo `offerId`) → `product_offer_char_use` (gán đặc tính cho sản phẩm) → `product_spec_char` (định nghĩa đặc tính, lọc theo `code = attributeCode`) → `product_spec_char_value` (giá trị đã chọn cho đặc tính đó), với điều kiện tất cả 4 bảng đều đang active (`status = '1'`).

Kết quả: `count.longValue() > 0` → `true`/`false`.

### Nhánh A — Sản phẩm có đặc tính này

`COUNT(1) > 0` → service trả về `true`.

### Nhánh B — Không tìm thấy dòng nào khớp

Trả về `false`.

## Bước 4 — Trả kết quả

Response thành công: `StandardResponse<Boolean>` bọc giá trị `true`/`false` từ Bước 3.

---

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| `BCCS-CATALOG-PRODUCT-0004` | `offerId` null hoặc `attributeCode` null/rỗng tại tầng service (`ProductOfferingService.hasProductAtt`). Trong thực tế qua REST endpoint **không thể trigger được** vì `@Min`/`@Size`/`@Pattern` ở controller đã chặn trước; chỉ có ý nghĩa nếu service bị gọi trực tiếp nội bộ. Không có entry fallback cho mã này trong `bccs-error-codes-fallback.json` của service — HTTP status/message thực tế cần verify qua central registry. |
| *(validate framework, không phải BusinessException)* | `offerId` thiếu, `< 1`, hoặc `> 9999999999` → lỗi 400 dạng `ErrorResponse` chuẩn của bean validation, message tương ứng theo từng constraint (`"offerId phải >= 1"`, `"offerId vượt quá độ dài cột (precision 10)"`). |
| *(validate framework, không phải BusinessException)* | `attributeCode` thiếu, rỗng, dài hơn 200 ký tự, hoặc chứa ký tự ngoài `[A-Za-z0-9_-]` → lỗi 400 dạng `ErrorResponse` chuẩn, message tương ứng (`"attributeCode tối đa 200 ký tự"`, `"attributeCode chỉ gồm chữ, số, '_' hoặc '-'"`). |
