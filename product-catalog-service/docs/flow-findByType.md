# Flow của API `findByType`

**Endpoint:** `GET /product-catalog-service/v1/product-package/getByType` **Mục đích nghiệp vụ:** Truy vấn danh sách bản ghi `PRODUCT_PACKAGE` theo cột `TYPE` (loại gói sản phẩm). Lưu ý: API này thuộc `ProductPackageService` trong **product-catalog-service**, không phải `identitytype`/`identityservice` trong `organization-resource-service` — service đó không có method `findByType`.

## Bước 1 — Validate tham số đầu vào tại controller

- `type` (String, `@RequestParam`, bắt buộc): `@Size(min=1, max=1, message = "type đúng 1 ký tự")` và `@Pattern(regexp = "^[12]$", message = "type chỉ nhận giá trị 1 hoặc 2")` → nếu thiếu, rỗng, dài hơn 1 ký tự, hoặc không phải `"1"`/`"2"`, Spring trả lỗi validate (400) trước khi vào tới service.

## Bước 2 — Truy vấn danh sách theo TYPE

Service gọi thẳng repository, không có thêm bước validate hay business-logic nào khác. `repository.findByType(type)` là derived query của Spring Data JPA, tương đương:

```sql
SELECT * FROM PRODUCT_PACKAGE WHERE TYPE = :type
```

Lọc các bản ghi `PRODUCT_PACKAGE` có cột `TYPE` đúng bằng giá trị `type` truyền vào — không lọc thêm theo `status`, không lọc theo hiệu lực ngày (`effect_datetime`/`expire_datetime`).

### Nhánh A — Có bản ghi khớp `TYPE`

Mỗi entity tìm được được map qua `ProductPackageMapper::toResponse` thành `ProductPackageResponse` (đầy đủ các trường: `productPackageId`, `code`, `name`, `status`, `type`, `saleType`, `effectDatetime`, `expireDatetime`, `telecomServiceId`, ...) — trả về danh sách các response này, bao gồm cả bản ghi `status = 0` (đã ngừng hoạt động) nếu `type` khớp, vì query không lọc status.

### Nhánh B — Không có bản ghi nào khớp `TYPE`

Trả về danh sách rỗng (`[]`), không throw exception.

## Bước 3 — Trả kết quả

Response thành công: `StandardResponse<List<ProductPackageResponse>>` bọc danh sách kết quả từ Bước 2 (rỗng nếu không có bản ghi nào khớp).

---

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| *(validate framework, không phải BusinessException)* | `type` thiếu, rỗng, dài hơn 1 ký tự, hoặc khác `"1"`/`"2"` → lỗi 400 dạng `ErrorResponse` chuẩn của bean validation, message tương ứng theo constraint (`"type đúng 1 ký tự"` hoặc `"type chỉ nhận giá trị 1 hoặc 2"`). |
