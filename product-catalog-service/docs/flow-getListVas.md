# Flow của API `getListVas`

**Endpoint:** `GET /product-catalog-service/v1/product/getListVas?offerId={offerId}`. **Mục đích nghiệp vụ:** Trả về danh sách VAS (dịch vụ giá trị gia tăng) đang được gán cho một sản phẩm chính qua bảng quan hệ `PRODUCT_OFFER_RELATION`, kèm thuộc tính, thông tin quan hệ, và `typeIndex` đánh dấu các nhóm VAS loại trừ lẫn nhau (VAS cùng `typeIndex` chỉ được chọn tối đa 1 khi đặt hàng).

## Bước 1 — Validate tham số đầu vào

- `offerId` (query param, bắt buộc): `@Min(1)` → nếu nhỏ hơn 1 thì lỗi HTTP 400 với message "offerId phải >= 1"; `@Max(9999999999)` → nếu vượt quá thì lỗi HTTP 400 với message "offerId vượt quá độ dài cột (precision 10)". Không có bước validate nào khác ở tầng service — service không throw `BusinessException` cho `offerId` null/không hợp lệ (khác với API `hasProductAtt` cùng service).

## Bước 2 — Truy vấn danh sách VAS gốc theo sản phẩm chính

```sql
SELECT * FROM BCCS_PRODUCT.product_offering
WHERE status = '1' AND product_offering_id IN (
  SELECT a.relation_offer_id
  FROM BCCS_PRODUCT.product_offer_relation a,
       BCCS_PRODUCT.product_offer_relation_detail b,
       BCCS_PRODUCT.product_spec_char c,
       BCCS_PRODUCT.product_spec_char_value d
  WHERE 1=1 AND a.main_offer_id = :offerId AND a.relation_type_id = 4
    AND a.product_offer_relation_id = b.product_offer_relation_id
    AND b.product_spec_char_id = c.product_spec_char_id
    AND b.product_spec_char_value_id = d.product_spec_char_value_id
    AND a.status = '1' AND b.status = '1' AND c.status = '1' AND d.status = '1'
)
```
Lấy các `product_offering` đang active (`status='1'`) mà id nằm trong danh sách `relation_offer_id` của các quan hệ loại VAS (`relation_type_id = 4`) đang active thuộc sản phẩm chính `offerId`, và các bản ghi chi tiết/đặc tính/giá trị đặc tính liên quan cũng phải active.

### Nhánh A — không tìm thấy VAS nào
Nếu danh sách rỗng, hàm trả về **`null`** ngay tại đây (không phải mảng rỗng `[]`) — response `data` của API sẽ là `null` khi sản phẩm chính không có VAS nào được gán hoặc `offerId` không tồn tại.

### Nhánh B — có ít nhất 1 VAS
Tiếp tục sang các bước enrich bên dưới.

## Bước 3 — Lấy toàn bộ quan hệ của sản phẩm chính

Gọi `ProductOfferRelationService.findByMainOfferId(offerId)` — đây là query derived (`findByMainOfferId`) chỉ lọc theo `main_offer_id`, **không lọc theo status**, nên kết quả có thể bao gồm cả các bản ghi quan hệ đã bị vô hiệu (status khác '1'). Việc lọc theo loại quan hệ VAS được thực hiện thủ công ở bước 4(b).

## Bước 4 — Enrich từng VAS trong danh sách

Với mỗi VAS trong danh sách (từ Bước 2):

**(a) Lấy thuộc tính:** gọi `getProductSpecCharByOfferingIds` cho từng VAS một (gọi lặp lại theo từng phần tử, không gộp batch), chạy native SQL:
```sql
SELECT a.PRODUCT_OFFERING_ID, a.PRODUCT_OFFER_CHAR_USE_ID, a.TYPE, c.*, d.*
FROM product_offer_char_use a
JOIN product_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID AND c.STATUS = '1'
JOIN product_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID AND d.STATUS = '1'
WHERE a.STATUS = '1' AND a.PRODUCT_OFFERING_ID IN (:id0)
ORDER BY a.PRODUCT_OFFERING_ID, c.CODE
```
Lấy các đặc tính (`product_spec_char`) và giá trị (`product_spec_char_value`) đang active được gán (`product_offer_char_use` active) cho VAS đó. Nếu không có kết quả, `lstProductSpecChars` của VAS giữ nguyên giá trị mặc định (không set).

**(b) Lấy thông tin quan hệ:** lọc lại từ danh sách quan hệ đã lấy ở Bước 3, giữ các bản ghi có `relationOfferId` đúng bằng id của VAS đang xét **và** `relationTypeId = 4` (VAS). Vì bước 3 không lọc status, một quan hệ đã bị vô hiệu (status khác '1') vẫn có thể lọt vào `lstProductOfferRelations` của VAS ở đây, dù VAS đó chỉ có mặt trong danh sách vì có một quan hệ khác đang active (cần lưu ý khi set up dữ liệu test có nhiều bản ghi quan hệ trùng offer/VAS với status khác nhau).

## Bước 5 — Phân nhóm loại trừ lẫn nhau (typeIndex)

Điều kiện bao quanh toàn bộ bước này luôn đúng tại thời điểm chạy tới đây, vì danh sách rỗng đã được return sớm ở Bước 2 (Nhánh A).

Với mỗi nhóm loại trừ, danh sách mã VAS thuộc nhóm được lấy qua:
```sql
SELECT osv FROM OptionSetValueEntity osv JOIN OptionSetEntity os ON osv.optionSetId = os.optionSetId
WHERE os.code = :code AND os.status = '1'
```
với `code = VAS_EXCLUSIVE_GROUP`, sau đó lọc tiếp trong Java theo `OptionSetValue.name = <tên nhóm>` (và `value` khác null) để lấy các `value` (mã VAS) thuộc nhóm đó. Query này không lọc theo `osv.status`, nên một `OptionSetValue` đã bị inactive vẫn được tính vào nhóm — cần lưu ý khi set dữ liệu test cho các nhóm loại trừ.

Mỗi VAS trong danh sách được xét theo đúng thứ tự ưu tiên sau (VAS chỉ rơi vào nhóm đầu tiên khớp):

1. Mã VAS thuộc nhóm `PRE_GPRS`
2. Ngược lại, thuộc nhóm `POS_GPRS`
3. Ngược lại, thuộc nhóm `PRE_G1`
4. Ngược lại, thuộc nhóm `POS_G1`
5. Ngược lại, thuộc nhóm `POS_AP_BH`
6. Ngược lại, thuộc nhóm `PRE_BB` **và** `subType = '2'` (trả trước)
7. Ngược lại, thuộc nhóm `POS_BB` **và** `subType = '1'` (trả sau)
8. Ngược lại, thuộc nhóm `POS_IPP`
9. Không khớp nhóm nào ở trên → VAS đứng riêng lẻ (nhóm chỉ gồm chính nó)

Lưu ý: VAS thuộc nhóm `PRE_BB` nhưng có `subType` khác `'2'` (hoặc thuộc `POS_BB` nhưng `subType` khác `'1'`) sẽ không khớp điều kiện của nhóm đó và rơi tiếp xuống các nhóm sau/đứng riêng lẻ, dù mã của nó vẫn nằm trong OptionSetValue của nhóm — cần lưu ý khi chuẩn bị dữ liệu test cho hai nhóm này.

Sau khi phân nhóm, danh sách kết quả được xây lại: các nhóm không rỗng (kể cả nhóm "đứng riêng lẻ") được duyệt theo đúng thứ tự chèn vào danh sách kết quả (VAS không khớp nhóm nào xuất hiện trước theo thứ tự gốc, sau đó tới nhóm `PRE_GPRS`, `POS_GPRS`, `PRE_G1`, `POS_G1`, `POS_AP_BH`, `PRE_BB`, `POS_BB`, `POS_IPP` nếu có). Mỗi nhóm được gán một `typeIndex` bắt đầu từ 1 (không phải 0) theo thứ tự chèn — tất cả VAS trong cùng một nhóm nhận cùng `typeIndex`.

## Kết quả trả về

- Sản phẩm chính không có VAS nào được gán (hoặc `offerId` không tồn tại): `data` = `null`.
- Có VAS: `data` là danh sách VAS đã được enrich thuộc tính (`lstProductSpecChars`), thông tin quan hệ (`lstProductOfferRelations`), và `typeIndex` (bắt đầu từ 1) theo nhóm loại trừ.

---

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|---|---|
| HTTP 400 (validation) | `offerId` < 1 → "offerId phải >= 1" |
| HTTP 400 (validation) | `offerId` > 9999999999 → "offerId vượt quá độ dài cột (precision 10)" |
