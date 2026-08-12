# Flow của API `getListPricePlanByOfferId`

**Endpoint:** `GET /product-catalog-service/v1/product/getListPricePlanByOfferId?productOfferingId={id}`. **Mục đích nghiệp vụ:** Lấy danh sách các đặc tính sản phẩm (product_spec_char) thuộc nhóm giá cước (CHAR_TYPE = price plan) đang được gán cho một product offering, kèm giá trị (product_spec_char_value) tương ứng của từng đặc tính.

## Bước 1 — Validate tham số đầu vào

- `productOfferingId`: truyền qua query string, là tham số bắt buộc (`@RequestParam` không có giá trị mặc định) — nếu không truyền, request bị chặn ngay ở tầng framework với lỗi 400 "Required request parameter 'productOfferingId' ... is not present".
- `@Min(value = 1, message = "productOfferingId phải >= 1")`: nếu truyền giá trị nhỏ hơn 1 (ví dụ 0 hoặc số âm) → 400 với message trên.
- `@Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")`: nếu truyền giá trị lớn hơn 9999999999 → 400 với message trên.

## Bước 2 — Guard null ở tầng service

Kết quả của bước này được cache theo key `PRICE_PLAN:<productOfferingId>` (annotation `@Cacheable`) — với cùng một `productOfferingId`, các lần gọi sau trong thời gian cache còn hiệu lực sẽ trả thẳng dữ liệu cache mà không truy vấn lại DB, tester cần lưu ý khi test kịch bản dữ liệu vừa được cập nhật.

### Nhánh A: `productOfferingId` null
Trả về danh sách rỗng ngay, không truy vấn DB. Trên thực tế nhánh này gần như không thể kích hoạt được qua HTTP vì Bước 1 đã bắt buộc và validate tham số này trước khi vào service.

### Nhánh B: `productOfferingId` có giá trị
Đi tiếp sang Bước 3 để truy vấn dữ liệu.

## Bước 3 — Truy vấn các đặc tính giá cước theo offering

```sql
SELECT
  a.PRODUCT_OFFERING_ID, a.PRODUCT_OFFER_CHAR_USE_ID, a.TYPE,
  c.PRODUCT_SPEC_CHAR_ID, c.NAME, c.DESCRIPTION, c.VALUE_TYPE, c.CHAR_TYPE,
  c.MIN_CARDINALITY, c.MAX_CARDINALITY, c.STATUS AS char_status, c.CODE,
  c.PRODUCT_SPEC_CHAR_TYPE_ID, c.VALUE_SET_TYPE, c.RESPONSE_CLASS, c.SQL_QUERY,
  c.DISPLAY_OBJECT, c.VALUE_OBJECT, c.SOLR_QUERY, c.SOLR_CORE, c.SOLR_SCHEMA,
  c.DATA_TYPE, c.WS_WSDL, c.TEMPLATE_REQUEST, c.VALIDATE_PATTERN, c.EXT_DATA, c.NOTE AS char_note,
  d.PRODUCT_SPEC_CHAR_VALUE_ID, d.PRODUCT_SPEC_CHAR_ID AS value_spec_char_id, d.VALUE_TYPE AS value_value_type,
  d.IS_DEFAULT, d.VALUE, d.UNIT_OF_MEASURE, d.VALUE_FROM, d.VALUE_TO, d.RANGE_INTERVAL,
  d.STATUS AS value_status, d.NAME AS value_name, d.SPECIFIC_VALUE, d.NOTE AS value_note, d.NAME AS value_name_from
FROM BCCS_PRODUCT.product_offer_char_use a
JOIN BCCS_PRODUCT.product_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID AND c.STATUS = '1'
JOIN BCCS_PRODUCT.product_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID AND d.STATUS = '1'
WHERE a.STATUS = '1'
  AND a.PRODUCT_OFFERING_ID = :offeringId
  AND c.CHAR_TYPE = :charType
  AND a.TYPE IN ('1', '2')
ORDER BY c.CODE
```

Lấy các bản ghi `product_offer_char_use` đang active (`STATUS = '1'`) của đúng `productOfferingId` truyền vào, join sang `product_spec_char` đang active và có `CHAR_TYPE = '2'` (hằng số nội bộ `PRICE_PLAN`), join tiếp sang `product_spec_char_value` đang active để lấy giá trị đã gán; đồng thời chỉ lấy các bản ghi `a.TYPE` là `'1'` hoặc `'2'` (2 giá trị loại gán đặc tính hợp lệ, code không có comment giải thích ý nghĩa cụ thể của từng giá trị). Kết quả sắp theo `c.CODE`.

### Nhánh A: không có bản ghi nào khớp điều kiện trên
Trả về danh sách rỗng.

### Nhánh B: có ít nhất một bản ghi
Với mỗi dòng kết quả, service build một `ProductSpecCharEntity` (thông tin đặc tính) và một `ProductSpecCharValueEntity` (thông tin giá trị), lấy `valueName` là tên giá trị, rồi map qua `ProductSpecCharUseMapper.toDtoWithValue(...)` thành một `ProductSpecCharDTO` gồm: toàn bộ thông tin đặc tính, object giá trị (`productSpecCharValueDTO`), `valueName`, cùng `productOfferingId`, `offerCharUseId` (= `PRODUCT_OFFER_CHAR_USE_ID`) và `offerCharUseType` (= `a.TYPE`). Trả về danh sách các DTO này theo đúng thứ tự đã sắp ở Bước 3.

Flow này không gọi sang service khác và không có `BusinessException`/`LogicException` nào được ném ra.

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| HTTP 400 (framework, "Required request parameter") | Không truyền `productOfferingId` trong query string |
| HTTP 400 — "productOfferingId phải >= 1" | `productOfferingId` < 1 |
| HTTP 400 — "productOfferingId vượt quá độ dài cột (precision 10)" | `productOfferingId` > 9999999999 |
