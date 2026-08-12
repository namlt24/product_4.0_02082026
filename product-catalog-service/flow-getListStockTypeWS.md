# Flow của API `/mDealer/getListStockTypeWS`

**Endpoint:** `POST /mDealer/getListStockTypeWS` (body JSON: `RequestMDealer`) — **Mục đích nghiệp vụ:** Lấy danh sách hàng hoá (loại sản phẩm kèm giá bán) cho một gói cước, theo lý do hoà mạng, dịch vụ và mã tác động.

## Bước 1 — Tiền xử lý & validate dữ liệu đầu vào (Controller)

Body request được trim các giá trị string (`DataUtil.trimValue`), sau đó validate theo thứ tự:

- **actionCode** (Mã tác động, ví dụ `"00"`): bắt buộc nhập → nếu rỗng, ném lỗi `LogicException("103", "common.error.param.not.found")` với tham số `actionCode`; dài tối đa 10 ký tự → nếu vượt quá, ném `LogicException("", "mdealer.api.actionCode.maxLength")`.
- **regType** (Mã lý do, ví dụ `"2"`): dài tối đa 20 ký tự → nếu vượt quá, ném `LogicException("", "mdealer.api.regType.maxLength")` (`Mã lý do không được vượt quá 20 ký tự`).
- **serviceType** (Alias dịch vụ, ví dụ `"M"`): dài tối đa 3 ký tự → nếu vượt quá, ném `LogicException("", "mdealer.api.serviceType.maxLength")` (`Loại dịch vụ không được vượt quá 3 ký tự`).
- **productCode** (Mã gói cước, ví dụ `"POBAS"`): dài tối đa 50 ký tự → nếu vượt quá, ném `LogicException("", "mdealer.api.productCode.maxLength")` (`Mã gói cước không được vượt quá 50 ký tự`).

> Các lỗi validate `LogicException` được `commonResponseException` bắt và trả về **HTTP 400** với `code = "500"`, `success = false`, `description` = message.

## Bước 2 — Kiểm tra tham số bắt buộc lại ở service layer

`ExternalServiceForMbccs.getListStockTypeWS(regType, serviceType, productCode, actionCode)` kiểm tra lại (phòng trường hợp service bị gọi nội bộ trực tiếp, không qua REST):

- `regType` rỗng → **`LogicException("103", "common.error.param.not.found")`** → `Message lỗi tiếng việt: Tham số regType truyền vào không đủ`
- `productCode` rỗng → **`LogicException("103", "common.error.param.not.found")`** → `Tham số productCode truyền vào không đủ`
- `serviceType` rỗng → **`LogicException("103", "common.error.param.not.found")`** → `Tham số serviceType truyền vào không đủ`

## Bước 3 — Dịch `serviceType` (alias) sang ID dịch vụ viễn thông

Gọi `telecomServiceService.getServiceIdByAlias(serviceType)`. Query tìm bản ghi `TELECOM_SERVICE` khớp `service_alias = serviceType`, sắp theo `telecom_service_id` tăng dần (query sinh bởi Spring Data / QueryDSL, không có chuỗi SQL tĩnh):

- Khớp → lấy `telecomServiceId` của bản ghi đầu tiên.
- Không khớp hoặc `serviceType` rỗng → trả về `null`.

Nếu `telecomServiceId` là `null` hoặc `0` → **`LogicException("103", "common.error.exist")`** với tham số `telecomServiceId` → `Message lỗi tiếng việt: telecomServiceId không tồn tại hoặc không có hiệu lực`.

## Bước 4 — Tìm `reasonId` theo mã lý do + mã tác động + dịch vụ

Gọi `reasonService.getReasonIdByTypeAndCode(regType, actionCode ? actionCode : "00", telecomServiceId)`. Nếu `actionCode` rỗng thì mặc định dùng `"00"` (hằng số `Const.ACTION_CODE.SUB_CONNECTION`).

Query tìm `REASON`:

```sql
SELECT * FROM reason a
WHERE a.reason_type IN (SELECT reason_type FROM action WHERE status = '1' AND action_code = :p1)
  AND a.status = '1'
  AND (a.effect_datetime IS NULL OR a.effect_datetime < TRUNC(sysdate) + 1)
  AND (a.expire_datetime IS NULL OR a.expire_datetime >= TRUNC(sysdate))
  AND a.reason_code = :p3
  AND (','||a.tel_service||',' LIKE :p4 OR a.tel_service IS NULL)
ORDER BY a.name ASC
```
> Filter lấy lý do còn hiệu lực, thuộc `reason_type` của action tương ứng, đúng mã lý do, và dịch vụ khớp với telecomServiceId (hoặc không ràng buộc dịch vụ). Lấy bản ghi đầu tiên.

Nếu không tìm thấy → `reasonId` = `null`; nếu `null`/`0` → **`LogicException("103", "common.error.exist")`** với tham số `regType` → `Message lỗi tiếng việt: regType không tồn tại hoặc không có hiệu lực`.

## Bước 5 — Tìm `saleServiceCode` (mã gói cước bán)

Gọi `mappingService.getSaleServiceCode(telecomServiceId, reasonId, productCode, actionCode)`. Query JPQL trên bảng `MAPPING` (liên kết `REASON`, `ACTION`), dùng `a.reasonType = m.actionCode` (comment code: cột `actionCode` của bảng mapping thực chất lưu `reasonType`):

```sql
Select m from Mapping m, Reason r, Action a
where m.reasonId = r.reasonId
  and a.reasonType = m.actionCode
  and r.status = '1' and m.status = '1' and a.status = '1'
  and a.actionCode = :actionCode          -- chỉ thêm nếu actionCode không rỗng
  and m.reasonId = :reasonId
  and m.telServiceId = :telServiceId      -- nếu telServiceId != 0
  -- hoặc: and m.telServiceId is null     -- nếu telServiceId = 0 (bundle)
  and (m.productCode = :productCode or m.productCode is null)  -- chỉ thêm nếu productCode không rỗng
  -- hoặc: and m.productCode is null      -- nếu productCode rỗng
order by m.productCode
```
> Trả về giá trị `saleServiceCode` của bản ghi đầu tiên. Nếu `reasonId` = 0 thì trả về `null` ngay từ đầu.

Nếu `saleServiceCode` rỗng → **`LogicException("103", "common.error.exist")`** với tham số `saleServiceCode` → `Message lỗi tiếng việt: saleServiceCode không tồn tại hoặc không có hiệu lực`.

## Bước 6 — Truy vấn danh sách loại hàng hoá (loại sản phẩm)

Gọi `productOfferTypeService.findBySaleServiceCodeWithProductOffering(saleServiceCode, true)`.

Đầu tiên query danh sách `PRODUCT_OFFER_TYPE` theo saleServiceCode:

```sql
SELECT DISTINCT a.* FROM PRODUCT_OFFER_TYPE a, PROD_PACK_PRODUCT_OFFER_TYPE b, PRODUCT_PACKAGE c
WHERE 1 = 1
  AND a.PRODUCT_OFFER_TYPE_ID = b.PRODUCT_OFFER_TYPE_ID
  AND b.PRODUCT_PACKAGE_ID = c.PRODUCT_PACKAGE_ID
  AND a.STATUS = '1'
  AND b.STATUS = '1'
  AND c.STATUS = '1'
  AND c.CODE = :p_saleServiceCode
  AND c.TYPE = '2'          -- hằng số PRODUCT_PACKAGE_TYPE.SALE_SERVICE
```
> Lấy các loại sản phẩm còn hoạt động thuộc gói cước (PRODUCT_PACKAGE) có code = saleServiceCode và type = 2 (Gói dịch vụ bán). Nếu `saleServiceCode` rỗng → trả về danh sách rỗng, không query.

## Bước 7 — Truy vấn danh sách mặt hàng (offerings) của gói

Gọi `productOfferingService.getListStockModelBySaleServiceCode(saleServiceCode, true)`:

```sql
select b.prod_pack_type_id, c.product_offer_type_id, c.name as typeName, e.check_serial,
       e.product_offering_id, e.code, e.name, e.telecom_service_id
from product_package a, prod_pack_product_offer_type b, product_offer_type c,
     package_offer d, product_offering e, product_offer_price f
where a.product_package_id = b.product_package_id
  and b.product_offer_type_id = c.product_offer_type_id
  and b.prod_pack_type_id = d.prod_pack_type_id
  and d.product_offering_id = e.product_offering_id
  and e.product_offering_id = f.product_offering_id
  and d.product_offer_price_id = f.product_offer_price_id
  and a.status = '1' and b.status = '1' and c.status = '1' and d.status = '1'
  and e.status = '1' and f.status = '1'
  and f.effect_datetime <= sysdate
  and (f.expire_datetime >= sysdate or f.expire_datetime is null)
  and LOWER(a.code) = LOWER(:saleServiceCode)
  and a.TYPE = '2'
order by c.name, e.name
```
> Lấy các mặt hàng còn hoạt động thuộc gói bán, giá (PRODUCT_OFFER_PRICE) đang trong thời hạn hiệu lực, sắp theo tên nhóm hàng và tên mặt hàng. (Lưu ý: tham số `containNumber=true` ở lời gọi này nên **không** áp dụng điều kiện lọc `product_offer_type_id in (6,7,8,10,13,11)` trong code.)

## Bước 8 — Tính giá cho từng mặt hàng

Với mỗi mặt hàng trả về ở Bước 7, gọi tính giá theo `telecomServiceId` của mặt hàng:

### Nhánh A — `telecomServiceId` của mặt hàng bằng `241` hoặc `254`
Gọi `productOfferPriceService.getPriceInServicesForPCCC(null, saleServiceCode, productOfferTypeId, productOfferingId, 1L)` — tính giá cho nhóm PCCC.
- Nếu không tìm thấy gói hoặc mặt hàng/giá không hợp lệ → trả về `null` (danh sách giá rỗng).
- Sau khi có giá chính, nếu gói có mapping reason và có rule sensor free (`sensorFeeRuleService.checkReasonSensorFree`): ghi đè `priceEquipment` bằng `promotionalPrice` của rule đầu tiên.

### Nhánh B — `telecomServiceId` của mặt hàng khác `241`, `254` (thường)
Gọi `productOfferPriceService.getPriceInServices(null, saleServiceCode, productOfferTypeId, productOfferingId, 1L)` — tính giá cho policy giá 1 (`1L` = pricePolicy mặc định):

```sql
SELECT a.*
FROM product_offer_price a
WHERE 1 = 1
  AND status = '1'
  AND price_policy_id = :pricePolicy
  AND (effect_datetime IS NULL OR effect_datetime <= trunc(SYSDATE))
  AND (expire_datetime IS NULL OR expire_datetime >= trunc(SYSDATE))
  AND product_offer_price_id IN
      (SELECT product_offer_price_id
       FROM package_offer
       WHERE 1 = 1
         AND status = '1'
         AND product_offering_id = :productOfferId
         AND prod_pack_type_id IN
             (SELECT prod_pack_type_id
              FROM prod_pack_product_offer_type
              WHERE 1 = 1 AND status = '1'
                AND product_package_id = :productPackageId
                AND product_offer_type_id = :productOfferType))
```
> Lấy các mức giá đang hiệu lực, thuộc policy giá `1`, của mặt hàng trong gói + loại hàng tương ứng. Kết quả mỗi dòng gán `productOfferName` = tên mặt hàng.

**Nhánh con CAM (trang bị camera)** — chỉ chạy khi gói có mapping reason (`mappingService.getMappingReasonProductOfferPrice`) **và** có rule free cam (`freeCamEquipmentService.checkReasonFreeCam`):
- Nếu `optionSetValueService.findOneByCodeAndValue(Const.OPTION_SET.ON_CAM_EQUIPMENT_PRICE, "1")` tồn tại: kiểm tra đặc tính `DEVICE_TYPE_CAM` của mặt hàng; nếu `value="1"` lấy `camInsidePrice`, nếu `value="2"` lấy `camOutsidePrice` từ rule free cam; gán `priceEquipment` cho từng mức giá (hoặc thêm 1 `ProductOfferPriceDTO` rỗng nếu chưa có giá).
- Ngược lại (không có option set): gọi `getPriceEquipment(...)` lấy giá trang bị; nếu ra **nhiều hơn 1** giá trang bị → **`LogicException("", "product.error.productOfferPrice.price.equipment.more.than.one")`** → `Message lỗi tiếng việt: Mặt hàng có nhiều hơn 1 giá trang bị`; nếu đúng 1, gán `priceEquipment`, `priceEquipmentId`, `priceEquipmentTypeId`.

> Lưu ý theo code hiện tại: nhánh con CAM chỉ bổ sung trường `priceEquipment` vào giá chính, không làm thay đổi danh sách loại sản phẩm trả về.

## Bước 9 — Gộp kết quả trả về

- Mỗi `ProductOfferTypeDTO` được set `name = "Mặt hàng"` nếu `productOfferTypeId = 7`.
- Mỗi `ProductOfferingDTO` được set `productTypeName = "Mặt hàng"` nếu `productOfferTypeId = 7`.
- Offerings được nhóm vào từng loại sản phẩm theo `productOfferTypeId` (`setProductOfferings`).

Kết quả là **danh sách `ProductOfferTypeDTO`** (mỗi loại kèm danh sách mặt hàng + giá). Nếu không có loại nào → trả về danh sách rỗng (không phải exception).

Controller set `lstProductOfferType`, trả về **HTTP 200** với `code = "200"`.

## Bảng tổng hợp mã lỗi

| Mã lỗi | Khi nào |
|--------|---------|
| `103` + `common.error.param.not.found` → "Tham số {0} truyền vào không đủ" (0 = `regType`/`productCode`/`serviceType`) | Tham số bắt buộc rỗng ở service layer. Qua REST thông thường không trigger được vì controller đã trim; chỉ có ý nghĩa khi service bị gọi nội bộ trực tiếp. |
| `103` + `common.error.exist` → "{0} không tồn tại hoặc không có hiệu lực" (0 = `telecomServiceId`) | `serviceType` không map được sang telecomServiceId (Bước 3). |
| `103` + `common.error.exist` → "{0} không tồn tại hoặc không có hiệu lực" (0 = `regType`) | Không tìm thấy lý do (reason) cho cặp regType + actionCode + dịch vụ (Bước 4). |
| `103` + `common.error.exist` → "{0} không tồn tại hoặc không có hiệu lực" (0 = `saleServiceCode`) | Không tìm thấy bản ghi mapping → không có saleServiceCode (Bước 5). |
| `""` (ERROR_NOT_DEFINE) + `mdealer.api.regType.maxLength` → "Mã lý do không được vượt quá 20 ký tự" | `regType` > 20 ký tự (validate framework ở controller, HTTP 400). |
| `""` (ERROR_NOT_DEFINE) + `mdealer.api.serviceType.maxLength` → "Loại dịch vụ không được vượt quá 3 ký tự" | `serviceType` > 3 ký tự (validate framework ở controller, HTTP 400). |
| `""` (ERROR_NOT_DEFINE) + `mdealer.api.productCode.maxLength` → "Mã gói cước không được vượt quá 50 ký tự" | `productCode` > 50 ký tự (validate framework ở controller, HTTP 400). |
| `""` (ERROR_NOT_DEFINE) + `mdealer.api.actionCode.maxLength` → "Mã tác động không được vượt quá 10 ký tự" | `actionCode` > 10 ký tự (validate framework ở controller, HTTP 400). |
| `""` + `common.error.param.not.found` (0 = `actionCode`) → "Tham số actionCode truyền vào không đủ" | `actionCode` rỗng ở controller (`validateMissingParam`). |
| `""` (ERROR_NOT_DEFINE) + `product.error.productOfferPrice.price.equipment.more.than.one` → "Mặt hàng có nhiều hơn 1 giá trang bị" | Mặt hàng CAM có nhiều hơn 1 giá trang bị khi tính giá (Bước 8 nhánh con CAM). Chỉ xảy ra khi gói thoả điều kiện mapping + free cam, chưa có option set ON_CAM_EQUIPMENT_PRICE. |
