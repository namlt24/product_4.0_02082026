# API VSALE Đấu nối trả sau

Tổng hợp 52 API được trích xuất từ tài liệu MTYCTĐ_VSALE_45715_48633_tối ưu Đấu nối thuê bao trả sau_v1.docx.

## Mục lục

1. [API lấy danh sách quyền của user theo actionCode – permissions-by-action-code](#api-1)
2. [API_CM_004 {API lấy thông tin khách hàng cũ} - getListCustomerByIdNo](#api-2)
3. [API_PRODUCT_019 {API lấy cấu hình chỉnh sửa các trường thông tin trên giao diện} - getComponentEditable](#api-3)
4. [API_PRODUCT_005 {API lấy danh sách nhóm loại khách hàng} - getAllGroupCustType](#api-4)
5. [API_PRODUCT_010 {API lấy danh sách loại khách hàng} – getMappingChannelCustTypeV2](#api-5)
6. [API_PRODUCT_016 {API lấy danh sách loại giấy tờ} - getListIdentityType](#api-6)
7. [API_PRODUCT_004 {API lấy danh sách địa bàn} – findAreaByParentCode](#api-7)
8. [API_PRODUCT_003 {API lấy thông tin địa bàn của user} - getStaffShopFullInfo](#api-8)
9. [API_CM_043 {API lấy số lượng TB đang hoạt động của SGT} – countSubscriberOfCust](#api-9)
10. [API_PRODUCT_013 {API lấy dữ liệu danh mục đơn giản} - getOptionSetValue](#api-10)
11. [API lấy cấu hình giá trị dài nhất của trường nơi cấp – CONFIG_MAX_LENGTH_ISSUE_PLACE](#api-11)
12. [API lấy cấu hình thay thế trường nơi cấp – IDC_CONFIG_ISSUE_PLACE](#api-12)
13. [API_CM_005 { API đối soát dữ liệu của BCA} – validateNationalData](#api-13)
14. [API_CM_034 {API lấy danh sách hợp đồng cũ} - getListAccountOld](#api-14)
15. [API_PRODUCT_021 {API lấy cấu hình bắt buộc các trường thông tin hợp đồng} - getRequiredFields](#api-15)
16. [API_PRODUCT_030 {API lấy phí thu cước tại nhà} - getProductOfferFee](#api-16)
17. [API_CM_035 {API lấy thông tin hợp đồng cũ} - viewAccountOld](#api-17)
18. [API_CM_037 {API tìm kiếm hợp đồng cũ} – searchOldAccount](#api-18)
19. [API_CM_035 {API lấy thông tin hợp đồng cũ} – viewAccountOld](#api-19)
20. [API_PRODUCT_028 {API lấy danh sách ngân hàng theo ngân hàng cha} - getLstBanksByParentBankCode](#api-20)
21. [API_CM_012 {API kiểm tra số lượng thuê bao tối đa của số giấy tờ} – validateMaxSub](#api-21)
22. [API_CM_013 { API kiểm tra thuê bao} – checkM2MSubscriber](#api-22)
23. [API_CM_017 {API lấy phí dịch vụ} – getListFee](#api-23)
24. [API_PRODUCT_048 {API lấy phí theo list số phục vụ đấu lô} - getListFeeV2](#api-24)
25. [API_PRODUCT_014 {API lấy danh sách Đối tượng} - getListObject](#api-25)
26. [API_PRODUCT_003 {API lấy thông tin địa bàn của User} – getStaffShopFullInfo](#api-26)
27. [API_CM_029 { API lấy danh sách đối tượng đặc biệt} – getListObjectSpec](#api-27)
28. [API_PRODUCT_002 {API lấy danh sách Hình thức hòa mạng} – getReasonFull](#api-28)
29. [API_PRODUCT_023 {Lấy danh sách gói cước} - getProductCodeByMapActiveInfo](#api-29)
30. [API_PRODUCT_024 {API lấy danh sách loại thuê bao} - getLsSubTypesByTelService](#api-30)
31. [API_PRODUCT_026 {API lấy danh sách khuyến mãi} - getPromotionsFull](#api-31)
32. [API_PRODUCT_027 {API lấy danh sách Cước đóng trước} - getListPrepaidFees](#api-32)
33. [API_PRODUCT_031 {API lấy danh sách hàng hoá} - getListStockTypeWS](#api-33)
34. [API_CM_036 {API Lấy danh sách hạn mức trước xác minh và hạn mức sau xác minh} – getLimitUsageBeforeConfirm](#api-34)
35. [API_CM_036 {API Lấy danh sách hạn mức trước xác minh và hạn mức sau xác minh} – getLimitUsageAfterConfirm](#api-35)
36. [API_CM_040 {API validate thông tin thuê bao} – validateSubscriberConnect](#api-36)
37. [API_IM_004 {API lấy số tự động} – searchIsdnByReasonId](#api-37)
38. [API_IM_007 {API lấy thông tin cam kết theo lý do hòa mạng và gói cước} – getDataStockWsByReasonAndProductCode](#api-38)
39. [API_CM_038 {API lấy cấu hình số lượng thuê bao tối đa và tối thiểu} – getNumSubMinMaxConnectPostPaid](#api-39)
40. [API_CM_015 {API lấy ra danh sách 3 số thuê bao nhận OTP gần nhất cho mbccs} - getListIsdnOtpConfirm](#api-40)
41. [API_CM_014 {API gửi mã OTP/gửi lại mã OTP} – sendOtpConfirm](#api-41)
42. [API_CM_016 {API kiểm tra mã OTP} - validateOTP](#api-42)
43. [API_QLHS_001 {API lấy danh sách chứng từ} – getListRecordConfig](#api-43)
44. [API_CM_007 {API view thông tin hồ sơ điện tử} – printRequest](#api-44)
45. [API_QLHS_002 {API upload file chứng từ} - upload-profile](#api-45)
46. [API_QLHS_001 {API lấy danh sách chứng từ} – getListRecordConfig – chưa có](#api-46)
47. [API_PRODUCT_006 {API lấy danh sách chính sách bảo vệ và xử lý dữ liệu cá nhân} - getListApparam](#api-47)
48. [API generate captcha - captcha](#api-48)
49. [API validate captcha – validate-captcha](#api-49)
50. [API_ORDER_001 {API tạo đơn hàng} – place-order-base-multiple-subscribers – Khách hàng Cá nhân](#api-50)
51. [API_ORDER_001 {API tạo đơn hàng} – place-order-base-business-multiple-subscribers – Khách hàng Doanh nghiệp](#api-51)
52. [API_ORDER_002 {API lấy cấu hình video call} – config-video-call](#api-52)

<a id="api-1"></a>

## 1. API lấy danh sách quyền của user theo actionCode – permissions-by-action-code

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Params** |  |  |
| actionCode | Mã hành động | Đấu nối di động mặc định truyền:<br>actionCode = VSALE_DAUNOI_DIDONG |

<a id="api-2"></a>

## 2. API_CM_004 {API lấy thông tin khách hàng cũ} - getListCustomerByIdNo

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| idNo* | Số giấy tờ | Số giấy tờ trên giao diện |
| groupType* | Mã nhóm loại khách hàng | Mã nhóm loại khách hàng |
| idType | Mã loại giấy tờ nhận diện | Mã loại giấy tờ nhận diện |
| custName | Tên khách hàng/doanh nghiệp | Chỉ truyền khi nhận diện AI<br>Tên khách hàng/doanh nghiệp<br>KHCN: recognizePeopleIdFront.name<br>KHDN: recognizeBusinessLicense.name |
| birthDate | Ngày sinh/Ngày thành lập | Chỉ truyền khi nhận diện AI<br>Ngày sinh/Ngày thành lập<br>Định dạng: yyyy-mm-dd<br>KHCN: recognizePeopleIdFront.birthday<br>KHDN: recognizeBusinessLicense.date |
| staffDTO.staffCode | Mã user đăng nhập | Mã user đăng nhập |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| description | Mô tả lỗi |  |
| compareCustInfo | Kết quả so khớp<br>True: Khớp<br>False: Không khớp |  |
| **listCustomerDTO: Thông tin khách hàng doanh nghiệp** |  |  |
| custTypeDTO.groupType | Mã nhóm loại khách hàng | Nhóm loại khách hàng (Map với API_PRODUCT_005 để trả ra Nhóm loại khách hàng tương ứng) |
| custTypeDTO.name | Loại khách hàng | Loại khách hàng |
| listCustIdentity.idType | Loại giấy tờ | Loại giấy tờ |
| listCustIdentity.idNo | Số giấy tờ | Số giấy tờ |
| name | Tên doanh nghiệp | Tên doanh nghiệp |
| listCustIdentity.idIssueDate | Ngày cấp giấy tờ | Ngày cấp giấy tờ |
| birthDate | Ngày thành lập | Ngày thành lập |
| listCustIdentity.idIssuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ |
| address | Địa chỉ chi tiết | Địa chỉ chi tiết |
| areaCode | Mã địa bàn |  |
| province | Mã tỉnh/thành phố |  |
| district | Mã quận/huyện |  |
| precinct | Mã phường/xã |  |
| streetName | Tên đường/phố |  |
| home | Số nhà |  |
| custId | Id khách hàng |  |
| **listCustomerDTO: Thông tin khách hàng cá nhân/người đại diện/người sử dụng** |  |  |
| custTypeDTO.groupType | Mã nhóm loại khách hàng | Nhóm loại khách hàng (Map với API_PRODUCT_005 để trả ra Nhóm loại khách hàng tương ứng) |
| custTypeDTO.name | Loại khách hàng | Loại khách hàng |
| listCustIdentity.idType | Loại giấy tờ | Loại giấy tờ |
| listCustIdentity.idNo | Số giấy tờ | Số giấy tờ |
| listCustIdentity.idIssueDate | Ngày cấp | Ngày cấp |
| listCustIdentity.idExpireDate | Ngày hết hạn | Ngày hết hạn |
| listCustIdentity.idIssuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ |
| name | Họ và tên | Họ và tên |
| birthDate | Ngày sinh | Ngày sinh |
| sex | Giới tính | Giới tính |
| nationality | Quốc tịch | Quốc tịch |
| address | Địa chỉ chi tiết | Địa chỉ chi tiết |
| areaCode | Mã địa bàn |  |
| province | Mã tỉnh/thành phố |  |
| district | Mã quận/huyện |  |
| precinct | Mã phường/xã |  |
| streetName | Tên đường/phố |  |
| home | Số nhà |  |
| custId | Id khách hàng |  |

---

<a id="api-3"></a>

## 3. API_PRODUCT_019 {API lấy cấu hình chỉnh sửa các trường thông tin trên giao diện} - getComponentEditable

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Params** |  |  |
| actionCode | Loại tác động | Mặc định truyền VSALE_DAUNOI_DIDONG |
| identityType | Loại giấy tờ | Loại giấy tờ<br>(PASS: Hộ chiếu<br>CMT: Chứng minh nhân dân<br>CCCD: Căn cước công dân<br>GPKD: Giấy phép kinh doanh) |

- *Response:*

| **API**           | **Mô tả**                                    | **Giao diện**        |
|-------------------|----------------------------------------------|----------------------|
| code              | Mã lỗi (200: thành công, khác 200: Thất bại) |                      |
| lstOptionSetValue |                                              |                      |
| name              | Tên trường thông tin                         | Tên trường thông tin |
| value             | Mã trường thông tin                          | Mã trường thông tin  |

---

<a id="api-4"></a>

## 4. API_PRODUCT_005 {API lấy danh sách nhóm loại khách hàng} - getAllGroupCustType

- *Request:*

<!-- -->

- *N/A*

<!-- -->

- *Response:*

| **API**           | **Mô tả**                                    | **Giao diện**        |
|-------------------|----------------------------------------------|----------------------|
| code              | Mã lỗi (200: thành công, khác 200: Thất bại) |                      |
| lstOptionSetValue |                                              |                      |
| name              | Tên nhóm loại khách hàng                     | Nhóm loại khách hàng |
| value             | Mã nhóm loại khách hàng                      |                      |

---

<a id="api-5"></a>

## 5. API_PRODUCT_010 {API lấy danh sách loại khách hàng} – getMappingChannelCustTypeV2

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Mã user đăng nhập | Mã user đăng nhập |
| groupType | Mã nhóm loại khách hàng | Mã nhóm loại khách hàng |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| **lstCustType** |  |  |
| name | Tên loại khách hàng | Loại khách hàng |
| custType | Mã loại khách hàng |  |

---

<a id="api-6"></a>

## 6. API_PRODUCT_016 {API lấy danh sách loại giấy tờ} - getListIdentityType

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| custType | Mã loại khách hàng | Mã loại khách hàng |

- *Response:*

| **API**             | **Mô tả**                                    | **Giao diện** |
|---------------------|----------------------------------------------|---------------|
| code                | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| listIdentityTypeDTO |                                              |               |
| name                | Tên loại giấy tờ                             | Loại giấy tờ  |
| idType              | Mã loại giấy tờ                              |               |

---

<a id="api-7"></a>

## 7. API_PRODUCT_004 {API lấy danh sách địa bàn} – findAreaByParentCode

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| parentCode | Mã địa bàn cha | Nếu lấy danh sách Tỉnh/TP thì không truyền input<br>Nếu lấy danh sách Quận/huyện thì truyền mã địa bàn cấp tỉnh<br>Nếu lấy danh sách Phường/xã thì truyền Mã địa bàn cấp huyện<br>Nếu lấy danh sách Tổ/thôn thì truyền Mã địa bàn cấp phường |

- *Response:*

| **API**                      | **Mô tả**                                    | **Giao diện**                                                 |
|------------------------------|----------------------------------------------|---------------------------------------------------------------|
| code                         | Mã lỗi (200: thành công, khác 200: Thất bại) |                                                               |
| lstArea                      |                                              |                                                               |
| [name](http://lstarea.name/) | Tên địa bàn                                  | Tỉnh/TP hoặc Quận/huyện hoặc Phường/Xã hoặc Tổ/thôn tương ứng |
| areaCode                     | Mã địa bàn                                   |                                                               |
| province                     | Mã tỉnh/tp                                   |                                                               |
| district                     | Mã quận/huyện                                |                                                               |
| precinct                     | Mã phường/xã                                 |                                                               |

---

<a id="api-8"></a>

## 8. API_PRODUCT_003 {API lấy thông tin địa bàn của user} - getStaffShopFullInfo

- *Request:*

| **API**   | **Mô tả**         | **Map dữ liệu truyền vào** |
|-----------|-------------------|----------------------------|
| staffCode | Mã user đăng nhập | Mã user đăng nhập          |

- *Response:*

| **API**             | **Mô tả**                                    | **Giao diện**                                                           |
|---------------------|----------------------------------------------|-------------------------------------------------------------------------|
| code                | Mã lỗi (200: thành công, khác 200: Thất bại) |                                                                         |
| staffDTO            |                                              |                                                                         |
| staffId             | Id user đăng nhập                            |                                                                         |
| shopDTO.province    | Mã Tỉnh/TP                                   | Tỉnh/TP (Map với API_PRODUCT_004 để trả ra Tên Tỉnh/TP tương ứng)       |
| shopDTO.district    | Mã Quận/huyện                                | Quận/huyện (Map với API_PRODUCT_004 để trả ra Tên Quận/huyện tương ứng) |
| shopDTO.precinct    | Mã Phường/xã                                 | Phường/xã (Map với API_PRODUCT_004 để trả ra Tên Phường/xã tương ứng)   |
| shopDTO.streetBlock | Mã Tổ/thôn                                   | Tổ/thôn (Map với API_PRODUCT_004 để trả ra Tên Tổ/thôn tương ứng)       |

---

<a id="api-9"></a>

## 9. API_CM_043 {API lấy số lượng TB đang hoạt động của SGT} – countSubscriberOfCust

- *Request:*

| **API**     | **Mô tả**     | **Map dữ liệu truyền vào**                                                                            |
|-------------|---------------|-------------------------------------------------------------------------------------------------------|
| idNo (\*)   | Số giấy tờ    | Số giấy tờ                                                                                            |
| custId (\*) | Mã khách hàng | Mã khách hàng cũ (lấy custId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin khách hàng) |

- *Response:*

| **API**      | **Mô tả**                                    | **Giao diện**              |
|--------------|----------------------------------------------|----------------------------|
| Code         | Mã lỗi (200: thành công, khác 200: Thất bại) |                            |
| Description  | Mô tả lỗi                                    |                            |
| numSubOfCust | Số lượng thuê bao hoạt động của SGT          | Số thuê bao đang hoạt động |

---

<a id="api-10"></a>

## 10. API_PRODUCT_013 {API lấy dữ liệu danh mục đơn giản} - getOptionSetValue

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Params** |  |  |
| optionSetCode | Mã danh mục cần lấy | Nếu lấy Quốc tịch thì truyền NATIONALITY<br>Nếu lấy Đơn vị thì truyền UNIVERSITY |

- *Response:*

| **API**           | **Mô tả**                                    | **Giao diện** |
|-------------------|----------------------------------------------|---------------|
| code              | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| LstOptionSetValue |                                              |               |
| name              | Tên đối tượng                                |               |
| value             | Mã đối tượng                                 |               |

---

<a id="api-11"></a>

## 11. API lấy cấu hình giá trị dài nhất của trường nơi cấp – CONFIG_MAX_LENGTH_ISSUE_PLACE

- *Request:*

| **API** | **Mô tả**   | **Map dữ liệu truyền vào**                     |
|---------|-------------|------------------------------------------------|
| parCode | Mã cấu hình | Truyền parCode = CONFIG_MAX_LENGTH_ISSUE_PLACE |

- *Response:*

| **API**  | **Mô tả**                           | **Giao diện**                       |
|----------|-------------------------------------|-------------------------------------|
| parCode  | Mã cấu hình                         | Mã cấu hình                         |
| parValue | Gía trị dài nhất của trường Nơi cấp | Gía trị dài nhất của trường Nơi cấp |

---

<a id="api-12"></a>

## 12. API lấy cấu hình thay thế trường nơi cấp – IDC_CONFIG_ISSUE_PLACE

- *Request:*

| **API** | **Mô tả**   | **Map dữ liệu truyền vào**              |
|---------|-------------|-----------------------------------------|
| parCode | Mã cấu hình | Truyền parCode = IDC_CONFIG_ISSUE_PLACE |

- *Response*: Giá trị trường thông tin Nơi cấp nếu chứa parType thì thay thế bằng giá trị parValue lưu vào DB

| **API**  | **Mô tả**                       | **Giao diện**                   |
|----------|---------------------------------|---------------------------------|
| parCode  | Mã cấu hình                     | Mã cấu hình                     |
| parType  | Gía trị kiểm tra trường Nơi cấp | Gía trị kiểm tra trường Nơi cấp |
| parValue | Gía trị thay thế                | Gía trị thay thế                |

---

<a id="api-13"></a>

## 13. API_CM_005 { API đối soát dữ liệu của BCA} – validateNationalData

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **staffDTO: Thông tin user đăng nhập** |  |  |
| staffDTO.staffCode | Mã user đăng nhập | Mã user đăng nhập |
| staffDTO.systemType |  | Mặc định truyền “MDEALER” |
| **CustomerDTO: Thông tin khách hàng cá nhân** |  |  |
| customerDTO.custId | Id khách hàng | Lấy custId trả về từ API_CM_004 |
| customerDTO.custType | Loại khách hàng | Loại khách hàng |
| customerDTO.name | Họ và tên | Họ và tên |
| customerDTO.birthDate | Ngày sinh<br>Định dạng: yyyy-mm-dd | Ngày sinh |
| customerDTO.sex | Giới tính | Giới tính |
| customerDTO.nationality | Quốc tịch | Quốc tịch |
| customerDTO.status | Trạng thái | Bỏ qua không truyền |
| customerDTO.areaCode | Mã địa bàn | Mã địa bàn |
| customerDTO.province | Mã tỉnh/tp | Mã tỉnh/tp |
| customerDTO.district | Mã quận/huyện | Mã quận/huyện |
| customerDTO.precinct | Mã phường/xã | Mã phường/xã |
| customer.streetBlock | Mã tổ/thôn | Bỏ qua không truyền |
| customerDTO.streetName | Tên đường/phố | Bỏ qua không truyền |
| customerDTO.home | Địa chỉ chi tiết | Địa chỉ chi tiết |
| customerDTO.address | Địa chỉ đầy đủ | Địa chỉ đầy đủ |
| customerDTO.createUser | Ngày tạo | Bỏ qua không truyền |
| customerDTO.createDatetime | Người tạo | Bỏ qua không truyền |
| customerDTO.updateUser | Ngày cập nhật | Bỏ qua không truyền |
| customerDTO.updateDatetime | Người cập nhật | Bỏ qua không truyền |
| **customerDTO.listCustIdentity: Thông tin giấy tờ** |  |  |
| customerDTO.listCustIdentity.idNo | Số giấy tờ | Số giấy tờ |
| customerDTO.listCustIdentity.idType | Mã loại giấy tờ | Mã loại giấy tờ |
| **subscriberDTO: Thông tin thuê bao** |  |  |
| subscriberDTO.telecomServiceId | Id dịch vụ | Lấy telecomServiceId từ API_CM_020 |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Mô tả lỗi |  |

---

<a id="api-14"></a>

## 14. API_CM_034 {API lấy danh sách hợp đồng cũ} - getListAccountOld

- *Request:*

| **API**    | **Mô tả**     | **Map dữ liệu truyền vào**      |
|------------|---------------|---------------------------------|
| custId(\*) | Mã khách hàng | Lấy custId trả về từ API_CM_004 |

- *Response:*

| **API**                                   | **Mô tả**                                    | **Giao diện** |
|-------------------------------------------|----------------------------------------------|---------------|
| Code                                      | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| Description                               | Mô tả lỗi                                    |               |
| **listAccountDTO: Danh sách hợp đồng cũ** |                                              |               |
| AccountDTO.accountId                      | Mã hợp đồng                                  |               |
| AccountDTO.accountNo                      | Số hợp đồng                                  | Số hợp đồng   |
| AccountDTO.createDatetime                 | Ngày tạo                                     |               |
| AccountDTO.signDate                       | Ngày kí                                      |               |

---

<a id="api-15"></a>

## 15. API_PRODUCT_021 {API lấy cấu hình bắt buộc các trường thông tin hợp đồng} - getRequiredFields

- *Request:*

| **API**    | **Mô tả**   | **Map dữ liệu truyền vào** |
|------------|-------------|----------------------------|
| Params     |             |                            |
| actionCode | Mã tác động | Truyền 00                  |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Chi tiết lỗi |  |
| **lstOptionSetValue** |  |  |
| name | Tên đối tượng |  |
| value | Mã đối tượng | Giá trị các trường cấu hình bắt buộc map tương ứng như sau:<br>billingCycle: Chu kỳ cước<br>payMethod: Hình thức thanh toán<br>noticeCharge: Hình thức thông báo cước<br>printMethod: In chi tiết cước<br>email: Email<br>telMobile: Điện thoại di động<br>phoneContact: Điện thoại cố định<br>signDate: Ngày ký hợp đồng<br>address: Địa chỉ TBC<br>billAddress: Địa chỉ hoá đơn cước<br>bankCode: Ngân hàng<br>accountNo: Tài khoản ngân hàng<br>accountName: Tên tài khoản<br>payDate: Ngày nhờ thu |

---

<a id="api-16"></a>

## 16. API_PRODUCT_030 {API lấy phí thu cước tại nhà} - getProductOfferFee

- *Request:*

| **API**  | **Mô tả**  | **Map dữ liệu truyền vào**                                                               |
|----------|------------|------------------------------------------------------------------------------------------|
| Body     |            |                                                                                          |
| areaCode | Mã địa bàn | Truyền mã địa bàn = mã tỉnh/tp+mã quận huyện+ Mã phường/xã+Mã tổ/thôn trường Địa chỉ TBC |

- *Response:*

| **API**            | **Mô tả**                                    | **Giao diện** |
|--------------------|----------------------------------------------|---------------|
| errorCode          | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message            | Chi tiết lỗi                                 |               |
| productOfferFeeDTO |                                              |               |
| fee                | Mức phí                                      |               |
| productOfferFeeId  | ID phí thu cước tại nhà                      |               |

---

<a id="api-17"></a>

## 17. API_CM_035 {API lấy thông tin hợp đồng cũ} - viewAccountOld

- *Request:*

| **API**        | **Mô tả**   | **Map dữ liệu truyền vào**                                                                     |
|----------------|-------------|------------------------------------------------------------------------------------------------|
| accountId (\*) | Mã hợp đồng | Mã hợp đồng (lấy accountId tương ứng với Số hợp đồng chọn trên giao diện từ API_CM_034 trả về) |

- *Response:*

| **API**                                 | **Mô tả**                                    | **Giao diện**                |
|-----------------------------------------|----------------------------------------------|------------------------------|
| Code                                    | Mã lỗi (200: thành công, khác 200: Thất bại) |                              |
| Description                             | Mô tả lỗi                                    |                              |
| **accountDTO: Thông tin hợp đồng**      |                                              |                              |
| accountId                               | Mã hợp đồng                                  | Mã hợp đồng                  |
| accountNo                               | Số hợp đồng                                  | Số hợp đồng                  |
| signDate                                | Ngày ký hợp đồng                             | Ngày ký hợp đồng             |
| billCycleId                             | Mã chu kì cước                               |                              |
| billCycleName                           | Tên chu kì cước                              | Tên chu kì cước              |
| payMethod                               | Mã hình thức thanh toán                      |                              |
| payMethodName                           | Tên hình thức thanh toán                     | Tên hình thức thanh toán     |
| noticeCharge                            | Mã hình thức thông báo cước                  |                              |
| noticeChargeName                        | Tên hình thức thông báo cước                 | Tên hình thức thông báo cước |
| printMethod                             | Mã In chi tiết cước                          |                              |
| printMethodName                         | Tên in chi tiết cước                         | Tên in chi tiết cước         |
| eMail                                   | Email                                        | Email                        |
| telMobile                               | Điện thoại di động                           | Điện thoại di động           |
| phoneContact                            | Điện thoại cố định                           | Điện thoại cố định           |
| address                                 | Địa chỉ TBC                                  | Địa chỉ TBC                  |
| billAddress                             | Địa chỉ hóa đơn cước                         | Địa chỉ hóa đơn cước         |
| **AccountBankDTO: Thông tin ngân hàng** |                                              |                              |
| bankName                                | Tên ngân hàng                                | Tên ngân hàng                |
| address                                 | Địa chỉ ngân hàng                            |                              |
| bankCode                                | Mã ngân hàng                                 |                              |
| accountName                             | Tên tài khoản                                | Tên tài khoản                |
| account                                 | Số tài khoản                                 | Tài khoản ngân hàng          |
| bankAccountDate                         | Ngày nhờ thu                                 | Ngày nhờ thu                 |

---

<a id="api-18"></a>

## 18. API_CM_037 {API tìm kiếm hợp đồng cũ} – searchOldAccount

- *Request:*

| **API** | **Mô tả**   | **Map dữ liệu truyền vào**                                   |
|---------|-------------|--------------------------------------------------------------|
| isdn    | Số thuê bao | Số thuê bao (truyền key người dùng nhập trên field tìm kiếm) |

- *Response:*

| **API**                            | **Mô tả**                                    | **Giao diện** |
|------------------------------------|----------------------------------------------|---------------|
| Code                               | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| Description                        | Mô tả lỗi                                    |               |
| **accountDTO: Thông tin hợp đồng** |                                              |               |
| accountId                          | Mã hợp đồng                                  |               |
| accountNo                          | Số hợp đồng                                  | Số hợp đồng   |
| createDatetime                     | Ngày tạo                                     |               |
| signDate                           | Ngày kí                                      |               |

---

<a id="api-19"></a>

## 19. API_CM_035 {API lấy thông tin hợp đồng cũ} – viewAccountOld

- *Request:*

| **API**   | **Mô tả**   | **Map dữ liệu truyền vào**                                                                      |
|-----------|-------------|-------------------------------------------------------------------------------------------------|
| accountId | Mã hợp đồng | Mã hợp đồng (lấy accountId tương ứng Số hợp đồng được chọn trên giao diện từ API_CM_034 trả về) |

- *Response:*

| **API**                                 | **Mô tả**                                    | **Giao diện**                |
|-----------------------------------------|----------------------------------------------|------------------------------|
| Code                                    | Mã lỗi (200: thành công, khác 200: Thất bại) |                              |
| Description                             | Mô tả lỗi                                    |                              |
| **accountDTO: Thông tin hợp đồng**      |                                              |                              |
| accountId                               | Mã hợp đồng                                  | Mã hợp đồng                  |
| accountNo                               | Số hợp đồng                                  | Số hợp đồng                  |
| signDate                                | Ngày ký hợp đồng                             | Ngày ký hợp đồng             |
| billCycleId                             | Mã chu kì cước                               |                              |
| billCycleName                           | Tên chu kì cước                              | Tên chu kì cước              |
| payMethod                               | Mã hình thức thanh toán                      |                              |
| payMethodName                           | Tên hình thức thanh toán                     | Tên hình thức thanh toán     |
| noticeCharge                            | Mã hình thức thông báo cước                  |                              |
| noticeChargeName                        | Tên hình thức thông báo cước                 | Tên hình thức thông báo cước |
| printMethod                             | Mã In chi tiết cước                          |                              |
| printMethodName                         | Tên in chi tiết cước                         | Tên in chi tiết cước         |
| eMail                                   | Email                                        | Email                        |
| telMobile                               | Điện thoại di động                           | Điện thoại di động           |
| phoneContact                            | Điện thoại cố định                           | Điện thoại cố định           |
| address                                 | Địa chỉ TBC                                  | Địa chỉ TBC                  |
| billAddress                             | Địa chỉ hóa đơn cước                         | Địa chỉ hóa đơn cước         |
| **AccountBankDTO: Thông tin ngân hàng** |                                              |                              |
| bankName                                | Tên ngân hàng                                | Tên ngân hàng                |
| address                                 | Địa chỉ ngân hàng                            |                              |
| bankCode                                | Mã ngân hàng                                 |                              |
| accountName                             | Tên tài khoản                                | Tên tài khoản                |
| account                                 | Số tài khoản                                 | Tài khoản ngân hàng          |
| bankAccountDate                         | Ngày nhờ thu                                 | Ngày nhờ thu                 |

---

<a id="api-20"></a>

## 20. API_PRODUCT_028 {API lấy danh sách ngân hàng theo ngân hàng cha} - getLstBanksByParentBankCode

- *Request:*

| **API**        | **Mô tả**        | **Map dữ liệu truyền vào**                               |
|----------------|------------------|----------------------------------------------------------|
| Params         |                  |                                                          |
| parentBankCode | Mã ngân hàng cha | Lấy danh sách ngân hàng thì truyền parentBankCode = null |

- *Response:*

| **API**   | **Mô tả**                                    | **Giao diện** |
|-----------|----------------------------------------------|---------------|
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message   | Chi tiết lỗi                                 |               |
| lstBank   |                                              |               |
| bankCode  | Mã ngân hàng                                 |               |
| name      | Tên ngân hàng                                |               |

---

<a id="api-21"></a>

## 21. API_CM_012 {API kiểm tra số lượng thuê bao tối đa của số giấy tờ} – validateMaxSub

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **staffDTO: Thông tin user đăng nhập** |  |  |
| staffDTO.staff_code | Mã user đăng nhập | Mã user đăng nhập |
| staffDTO.systemType | Mã hệ thống | Mặc định truyền “MDEALER” |
| idNo | Số giấy tờ | Số giấy tờ<br>KHDN: Truyền Số giấy tờ của Người sử dụng<br>KHCN: Truyền Số giấy tờ của KHCN |
| idType | Mã loại giấy tờ | Mã loại giấy tờ |
| custType | Mã loại khách hàng | Mã loại khách hàng |
| payType | 2: Trả trước, 1: Trả sau |  |

- *Response:*

| **API**   | **Mô tả**                                    | **Giao diện** |
|-----------|----------------------------------------------|---------------|
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message   | Mô tả                                        |               |

---

<a id="api-22"></a>

## 22. API_CM_013 { API kiểm tra thuê bao} – checkM2MSubscriber

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Mô tả |  |
| isdnM2M | Là thuê bao đầu số M2M<br>true:có<br>false: không |  |
| isProductM2M | Là thuê bao sử dụng gói cước M2M<br>true:có<br>false: không |  |

---

<a id="api-23"></a>

## 23. API_CM_017 {API lấy phí dịch vụ} – getListFee

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao |
| serial | Serial | Lấy serial màn Thông tin thuê bao<br>Nếu là eSim thì lấy serial từ API_IM_009 trả về |
| telecomServiceId | Id dịch vụ | Từ màn Chọn loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| reasonId | Lý do chọn | Lý do (lấy reasonId tương ứng với Lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| actionCode | Loại tác động | Mặc định truyền 00 |
| payMethod | Hình thức thanh toán | Hình thức thanh toán (lấy value từ API_PRODUCT_013 theo Hình thức thanh toán tương ứng trên giao diện) |
| areaCode | Địa chỉ hợp đồng | Địa chỉ hợp đồng (lấy areaCode của Địa chỉ TBC) |
| staffCode | Mã nhân viên thực hiện | Mã nhân viên thực hiện |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước (lấy prepaidCode từ API_PRODUCT_027 trả về)<br>Trường hợp không chọn CDT truyền -1 |
| prepaidId | Id cước đóng trước | Id cước đóng trước (lấy promotionDetailId từ API_PRODUCT_027 trả về)<br>Trường hợp không chọn CDT truyền -1 |
| provinceStaff | Mã tỉnh nhân viên | Mã tỉnh nhân viên (lấy staffDTO.shopDTO.province từ API_PRODUCT_003 trả về) |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Mô tả |  |
| totalFeeAll | Tổng tiền | Tổng tiền (hiển thị dạng đánh dấu chấm hàng nghìn:<br>{totalFeeAll}+’đ’)<br>Hiển thị trên màn hình Nhập thông tin thuê bao và bottom sheet Chi tiết đơn giá |
| **feeTranList: Thông tin phí dịch vụ** |  |  |
| feeCode | Mã phí | Mã phí |
| feeName | Tên phí | Tên phí |
| feePrice | Giá trị phí | Phí dịch vụ hiển thị dạng đánh dấu chấm hàng nghìn:<br>{feePrice}+’đ’ |

---

<a id="api-24"></a>

## 24. API_PRODUCT_048 {API lấy phí theo list số phục vụ đấu lô} - getListFeeV2

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| telecomServiceId | Mã dịch vụ | Mã dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023) |
| reasonId | Mã lý do | Lý do (lấy reasonId tương ứng với Lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| actionCode | Mã tác động | Mã tác động (Mặc định truyền 00) |
| payMethod | Hình thức thanh toán | Hình thức thanh toán (lấy value từ API_PRODUCT_013 theo Hình thức thanh toán tương ứng trên giao diện) |
| areaCode | Địa chỉ hợp đồng | Địa chỉ hợp đồng (lấy areaCode của Địa chỉ TBC) |
| staffCode | Mã nhân viên thực hiện | Mã nhân viên thực hiện |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước (lấy prepaidCode từ API_PRODUCT_027 trả về)<br>Trường hợp không chọn CDT truyền -1 |
| prepaidId | Id cước đóng trước | Id cước đóng trước, (lấy promotionDetailId từ API_PRODUCT_027 trả về)<br>Trường hợp không chọn CDT truyền -1 |
| provinceStaff | Mã tỉnh nhân viên | Mã tỉnh nhân viên (lấy staffDTO.shopDTO.province từ API_PRODUCT_003 trả về) |
| **listSubscriber: Danh sách thuê bao đấu lô** |  |  |
| isdn | Số thuê bao | Số thuê bao |
| serial | Mã serial | Mã serial |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| Code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Mô tả mã lỗi |  |
| totalFeeAll | Tổng số tiền của tất cả các thuê bao | Tổng tiền (hiển thị dạng đánh dấu chấm hàng nghìn:<br>{totalFeeAll}+’đ’)<br>Hiển thị trên màn hình Nhập thông tin thuê bao và bottom sheet Chi tiết đơn giá |
| feeTransAtHome | Phí thu cước tại nhà | Phí thu cước tại nhà |
| **listSubFeeInfo: Danh sách phí theo từng thuê bao** |  |  |
| isdn | Số thuê bao | Số thuê bao |
| serial | Mã serial | Mã serial |
| totalFee | Tổng tiền của 1 thuê bao | Tổng tiền (hiển thị dạng đánh dấu chấm hàng nghìn:<br>{totalFee}+’đ’)<br>Hiển thị trên bottom sheet Chi tiết đơn giá |
| **feeTranList: Danh sách phí của từng thuê bao** |  |  |
| feeCode | Mã phí | Mã phí |
| feeName | Tên phí | Tên phí |
| feePrice | Giá trị phí | Phí dịch vụ hiển thị dạng đánh dấu chấm hàng nghìn:<br>{feePrice}+’đ’<br>Hiển thị trên bottom sheet Chi tiết đơn giá > Xem chi tiết phí của từng thuê bao (Hình 6.1.3.14) |

---

<a id="api-25"></a>

## 25. API_PRODUCT_014 {API lấy danh sách Đối tượng} - getListObject

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| custType | Mã loại khách hàng | Loại khách hàng (lấy custType tương ứng Loại khách hàng chọn trên giao diện từ API_PRODUCT_010) |
| birthDate | Ngày sinh khách hàng<br>Định dạng ddMMyyyy | Ngày sinh khách hàng |

- ***Response:***

| **API**           | **Mô tả**                                    | **Giao diện** |
|-------------------|----------------------------------------------|---------------|
| Code              | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| LstOptionSetValue |                                              |               |
| name              | Tên đối tượng                                | Đối tượng     |
| value             | Mã đối tượng                                 |               |

---

<a id="api-26"></a>

## 26. API_PRODUCT_003 {API lấy thông tin địa bàn của User} – getStaffShopFullInfo

- *Request:*

| **API**   | **Mô tả**         | **Map dữ liệu truyền vào** |
|-----------|-------------------|----------------------------|
| staffCode | Mã user đăng nhập | Mã user đăng nhập          |

- *Response:*

| **API**                   | **Mô tả**                                    | **Giao diện**                                                           |
|---------------------------|----------------------------------------------|-------------------------------------------------------------------------|
| code                      | Mã lỗi (200: thành công, khác 200: Thất bại) |                                                                         |
| staffDTO                  |                                              |                                                                         |
| staffId                   | Id user đăng nhập                            |                                                                         |
| staffDTO.shopDTO.province | Mã Tỉnh/TP                                   | Tỉnh/TP (Map với API_PRODUCT_004 để trả ra Tên Tỉnh/TP tương ứng)       |
| staffDTO.shopDTO.district | Mã Quận/huyện                                | Quận/huyện (Map với API_PRODUCT_004 để trả ra Tên Quận/huyện tương ứng) |
| staffDTO.shopDTO.precinct | Mã Phường/xã                                 | Phường/xã (Map với API_PRODUCT_004 để trả ra Tên Phường/xã tương ứng)   |
| shopDTO.streetBlock       | Mã Tổ/thôn                                   | Tổ/thôn (Map với API_PRODUCT_004 để trả ra Tên Tổ/thôn tương ứng)       |

---

<a id="api-27"></a>

## 27. API_CM_029 { API lấy danh sách đối tượng đặc biệt} – getListObjectSpec

- *Request:*

| **API**     | **Mô tả**   | **Map dữ liệu truyền vào**                                                                      |
|-------------|-------------|-------------------------------------------------------------------------------------------------|
| productCode | Mã gói cước | Gói cước (lấy productCode tương ứng với Gói cước chọn trên giao diện từ API_PRODUCT_023 trả về) |

- *Response:*

| **API**                                    | **Mô tả**                                    | **Giao diện** |
|--------------------------------------------|----------------------------------------------|---------------|
| errorCode                                  | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message                                    | Mô tả                                        |               |
| **listObjectSpecDTO: Danh sách đối tượng** |                                              |               |
| code                                       | Mã đối tượng                                 |               |
| name                                       | Tên đối tượng                                |               |

---

<a id="api-28"></a>

## 28. API_PRODUCT_002 {API lấy danh sách Hình thức hòa mạng} – getReasonFull

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Mã user đăng nhập |  |
| payType | 1: Trả sau 2: Trả trước | Lấy từ màn hình Chọn loại thuê bao |
| actionCode | Mã tác động | Truyền = 00 |
| serviceType | Loại dịch vụ | Check thông tin từ màn hình Chọn loại thuê bao<br>1: là Mobile thì truyền M<br>2: là Home phone thì truyền H |
| mode |  | Truyền mode = 1 |
| getReasonCharUse |  | Truyền false |
| offerId | ID gói cước | Lấy productOfferingId từ API_PRODUCT_023 |
| subType | Loại thuê bao | Lấy subtype từ API_PRODUCT_024 |
| **listProductSpec: Danh sách thuộc tính lý do** |  |  |
| property | Thuộc tính lý do | Bỏ qua không truyền |

- *Response:*

| **API**              | **Mô tả**                                    | **Giao diện** |
|----------------------|----------------------------------------------|---------------|
| code                 | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| lstReason.reasonCode | Mã lý do                                     |               |
| lstReason.name       | Tên lý do                                    | Tên lý do     |
| lstReason.reasonId   | Id lý do                                     |               |

---

<a id="api-29"></a>

## 29. API_PRODUCT_023 {Lấy danh sách gói cước} - getProductCodeByMapActiveInfo

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Body** |  |  |
| staffCode | Mã user thực hiện | Mã user đăng nhập |
| payType | 1: Trả sau, 2: Trả trước | 1: Trả sau, 2: Trả trước |
| actionCode | Mã tác động | Truyền 00 |
| telecomServiceId | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| roleMap.values | Danh sách quyền của user | Truyền danh sách quyền của user trả về ở API permissions-by-action-code |

- *Response:*

| **API**                                                                         | **Mô tả**                                    | **Giao diện** |
|---------------------------------------------------------------------------------|----------------------------------------------|---------------|
| errorCode                                                                       | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| success                                                                         | True/false                                   |               |
| ***productOfferingDTOs: Danh sách gói cước***                                   |                                              |               |
| productOfferingId                                                               | Id gói cước                                  |               |
| name                                                                            | Tên gói cước                                 |               |
| code                                                                            | Mã gói cước                                  |               |
| Description                                                                     | Mô tả                                        |               |
| ***productOfferingDTOs.lstProductSpecCharDTOs: Danh sách thuộc tính gói cước*** |                                              |               |
| productSpecCharId                                                               | Id thuộc tính                                |               |
| code                                                                            | Mã thuộc tính                                |               |
| name                                                                            | Tên thuộc tính                               |               |
| value                                                                           | Giá trị thuộc tính                           |               |

---

<a id="api-30"></a>

## 30. API_PRODUCT_024 {API lấy danh sách loại thuê bao} - getLsSubTypesByTelService

- *Request:*

| **API**     | **Mô tả**         | **Map dữ liệu truyền vào**                                                                           |
|-------------|-------------------|------------------------------------------------------------------------------------------------------|
| Body        |                   |                                                                                                      |
| staffCode   | Mã user thực hiện | Mã user thực hiện                                                                                    |
| productCode | Mã gói cước       | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |

- *Response:*

| **API**                                   | **Mô tả**                                    | **Giao diện** |
|-------------------------------------------|----------------------------------------------|---------------|
| errorCode                                 | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message                                   | Chi tiết lỗi                                 |               |
| ***lstSubType: Danh sách loại thuê bao*** |                                              |               |
| subType                                   | Mã loại thuê bao                             |               |
| name                                      | Tên loại thuê bao                            |               |

---

<a id="api-31"></a>

## 31. API_PRODUCT_026 {API lấy danh sách khuyến mãi} - getPromotionsFull

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Body** |  |  |
| staffCode | Mã user thực hiện | Mã user thực hiện |
| payType | 1: Trả sau, 2: Trả trước | 1: Trả sau, 2: Trả trước |
| offerId | Id gói cước | Id gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| actionCode | Mã tác động | Truyền 00 |
| serviceType | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền M<br>Nếu là Homephone thì truyền H |
| regReasonId | Id lý do | Id lý do (lấy lstReason.reasonId từ API_PRODUCT_002 theo lý do tương ứng trên giao diện) |
| subType | Mã loại thuê bao | Mã loại thuê bao (lấy subType từ API_PRODUCT_024 theo Loại thuê bao tương ứng trên giao diện) |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Chi tiết lỗi |  |
| **lstDiscountPromotion: Danh sách khuyến mại** |  |  |
| discountPromotionId | Id khuyến mại | Khuyến mãi hiển thị dạng code – value<br>Hiển thị thêm option Không nhận khuyến mại, lưu mã là code = -2 |
| code | Mã khuyến mại |  |
| name | Tên khuyến mại |  |
| type | Loại khuyến mại |  |
| description | Mô tả |  |
| **lstDiscountPromotion.lstProductSpecChar: Danh sách thuộc tính khuyến mại** |  |  |
| productSpecCharId | Id thuộc tính |  |
| code | Mã thuộc tính |  |
| name | Tên thuộc tính |  |
| value | Giá trị thuộc tính |  |

---

<a id="api-32"></a>

## 32. API_PRODUCT_027 {API lấy danh sách Cước đóng trước} - getListPrepaidFees

- *Request:*

| **API**     | **Mô tả**     | **Map dữ liệu truyền vào**                                                                                |
|-------------|---------------|-----------------------------------------------------------------------------------------------------------|
| Body        |               |                                                                                                           |
| promCode    | Mã khuyến mại | Mã khuyến mại (lấy lstDiscountPromotion.code từ API_PRODUCT_026 theo khuyến mại tương ứng trên giao diện) |
| productCode | Mã gói cước   | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện)      |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Chi tiết lỗi |  |
| **lstPromotionDetail : Danh sách cước đóng trước** |  |  |
| billingPromotionId | Id khuyến mại tính cước | Cước đóng trước hiển thị dạng:<br>{numMonth}+ ‘ tháng – ’+ {totalPrepaid}+’đ’<br>Hiển thị thêm option Không đóng cước trước, billingPromotionId = -1 |
| promotionDetailId | Id chi tiết cước đóng trước |  |
| numMonth | Số tháng đóng trước |  |
| prepaidCode | Mã cước đóng trước |  |
| promValue | Tiền cước đóng trước/tháng |  |
| totalPrepaid | Tổng tiền cước đóng trước |  |
| name | Tên cước đóng trước |  |

---

<a id="api-33"></a>

## 33. API_PRODUCT_031 {API lấy danh sách hàng hoá} - getListStockTypeWS

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Body** |  |  |
| regType | Mã lý do | Mã lý do (lấy lstReason.reasonCode từ API_PRODUCT_002 theo lý do tương ứng trên giao diện) |
| serviceType | Loại dịch vụ | Nếu là Mobile thì truyền M<br>Nếu là Homephone thì truyền H |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| actionCode | Mã tác động | Truyền 00 |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| message | Chi tiết lỗi |  |
| **lstProductOfferType: Danh sách loại mặt hàng** |  |  |
| productOfferTypeId | Id loại mặt hàng | Mặt hàng không serial: hiển thị các list product_offer_type_id = 11<br>Mặt hàng: hiển thị các list product_offer_type_id != 11 |
| name | Tên loại mặt hàng |  |
| ParentId | Mã loại mặt hàng cha |  |
| **lstProductOfferType.productOfferings: Danh sách mặt hàng tương ứng thuộc loại mặt hàng** |  |  |
| productPackTypeId | Id loại mặt hàng của DVBH |  |
| productOfferTypeId | Id loại mặt hàng |  |
| productOfferTypeName | Tên loại mặt hàng |  |
| productOfferingId | Id mặt hàng |  |
| code | Mã mặt hàng |  |
| name | Tên mặt hàng | Tên mặt hàng |

---

<a id="api-34"></a>

## 34. API_CM_036 {API Lấy danh sách hạn mức trước xác minh và hạn mức sau xác minh} – getLimitUsageBeforeConfirm

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| telecomServiceId | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| channel | Mã kênh | Mã kênh (MBCCS, MDEALER, ...) |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| limitUsageType | Loại hạn mức | Loại hạn mức<br>= 1 -> Trước xác minh<br>= 2 -> Sau xác minh |
| groupType | Nhóm khách hàng | Nhóm khách hàng<br>= 1 -> Khách hàng cá nhân trong nước<br>= 2 -> Khách hàng doanh nghiệp<br>= 3 -> Khách hàng cá nhân nước ngoài |
| custType | Loại khách hàng | Loại khách hàng (lấy custType tương ứng Loại khách hàng chọn trên giao diện từ API_PRODUCT_010) |
| diffProvince | Tỉnh/TP của địa chỉ TBC khác địa bàn của user | Check province của địa chỉ TBC (màn hợp đồng) và province địa bàn của user đăng nhập - Nếu khác nhau thì là ngoại tỉnh => truyền 1 - Nếu giống nhau thì là nội tỉnh => truyền 0 |
| privilegeCode | Hạng của thuê bao | Bỏ qua không truyền |
| promotionCode | Mã khuyến mại | Mã khuyến mại (lấy lstDiscountPromotion.code tương ứng Mã khuyến mại chọn trên giao diện từ API_PRODUCT_026 trả về) |
| regTypeId | Hình thức hòa mạng | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| Code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| Description | Mô tả lỗi |  |
| limitUsage | Hạn mức | Hạn mức trước xác minh hiển thị dạng: đánh dấu chấm hàng nghìn<br>Ví dụ: 3.000.000 |

---

<a id="api-35"></a>

## 35. API_CM_036 {API Lấy danh sách hạn mức trước xác minh và hạn mức sau xác minh} – getLimitUsageAfterConfirm

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| telecomServiceId | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| limitUsageType | Loại hạn mức | Loại hạn mức<br>= 1 -> Trước xác minh<br>= 2 -> Sau xác minh |
| custType | Loại khách hàng | Loại khách hàng (lấy custType tương ứng Loại khách hàng chọn trên giao diện từ API_PRODUCT_010) |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| Code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| Description | Mô tả lỗi |  |
| **optionSetValueDTOList** |  |  |
| name | Tên hạn mức | Hạn mức sau xác minh hiển thị dạng: đánh dấu chấm hàng nghìn<br>Ví dụ: 3.000.000 |
| value | Giá trị hạn mức |  |

---

<a id="api-36"></a>

## 36. API_CM_040 {API validate thông tin thuê bao} – validateSubscriberConnect

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Nhân viên đấu nối | Nhân viên đấu nối |
| **subscriberDTO: Thông tin thuê bao** |  |  |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| serial | Số serial | Số serial (màn Nhập thông tin thuê bao)<br>Nếu là eSim thì lấy serial từ API_IM_009 trả về |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| promotionCode | Mã khuyến mại | Mã khuyến mại (lấy lstDiscountPromotion.code tương ứng Mã khuyến mại chọn trên giao diện từ API_PRODUCT_026 trả về) |
| reasonId | Mã lý do đấu nối | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |
| telecomServiceId | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| subType | Loại thuê bao | Loại thuê bao (lấy subType từ API_PRODUCT_024 theo Loại thuê bao tương ứng trên giao diện) |
| payType | 1: Trả sau 2: Trả trước | Lấy từ màn hình chọn Loại thuê bao |
| limitUsageBefore | Hạn mức trước xác minh | Hạn mức trước xác minh (lấy limitUsage từ API_CM_036 theo Hạn mức tương ứng trên giao diện) |
| limitUsageAfter | Hạn mức sau xác minh | Hạn mức sau xác minh (lấy limitUsage từ API_CM_036 theo Hạn mức tương ứng trên giao diện) |
| subObject | Đối tượng | Đối tượng (Lấy value tương ứng Đối tượng chọn trên giao diện từ API_PRODUCT_014 trả về) |
| signDate | Ngày ký | Ngày ký (lấy Ngày ký user nhập trên giao diện) |
| **prepaidMonthBO: Thông tin cước đóng trước** |  |  |
| prepaidId | id cước đóng trước | id cước đóng trước (lấy promotionDetailId từ API_PRODUCT_027 trả về)<br>Truyền -1 trong trường hợp chọn Không đóng cước trước |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước (lấy prepaidCode từ API_PRODUCT_027 trả về)<br>Truyền -1 trong trường hợp chọn Không đóng cước trước |

- *Response:*

| **API**     | **Mô tả**                                    | **Giao diện** |
|-------------|----------------------------------------------|---------------|
| Code        | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| Description | Mô tả lỗi                                    |               |

---

<a id="api-37"></a>

## 37. API_IM_004 {API lấy số tự động} – searchIsdnByReasonId

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Mã user đăng nhập | Mã user đăng nhập |
| telecomServiceId | Id dịch vụ | Từ màn Chọn loại thuê bao<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| reasonId | Lý do chọn | Lý do ( lấy reasonId tương ứng với Lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| maxRow | Số lượng số thuê bao cần lấy số tự động | Số lượng số thuê bao cần lấy số tự động |

- *Response:*

| errorCode                            | Mã lỗi (200: thành công, khác 200: Thất bại) |                                                                                               |
|--------------------------------------|----------------------------------------------|-----------------------------------------------------------------------------------------------|
| message                              | Chi tiết lỗi                                 |                                                                                               |
| ***lstIsdn: Danh sách số thuê bao*** |                                              | Lấy ra các số thuê bao đầu tiên ( = maxRow) để hiển thị vào trường Số thuê bao trên giao diện |
| isdn                                 | Số thuê bao                                  | Số thuê bao                                                                                   |

---

<a id="api-38"></a>

## 38. API_IM_007 {API lấy thông tin cam kết theo lý do hòa mạng và gói cước} – getDataStockWsByReasonAndProductCode

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| stockTypeId | Loại số | Từ màn Chọn loại thuê bao<br>1: Số mobile; 2: Số homephone; 3: Số pstn |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao) |
| telecomServiceId | Id dịch vụ | Từ màn Chọn loại thuê bao<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| reasonId | Lý do chọn | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |

- *Response:*

| **API**                                            | **Mô tả**                                        | **Giao diện**                 |
|----------------------------------------------------|--------------------------------------------------|-------------------------------|
| errorCode                                          | Mã lỗi (200: thành công, -1: lỗi không xác định) |                               |
| message                                            | Chi tiết lỗi                                     |                               |
| **stockNumberDTO: Thông tin cam kết của thuê bao** |                                                  |                               |
| pricePledgeAmount                                  | Số tiền cam kết                                  | Số tiền cam kết (gồm 10% VAT) |
| pledgeTime                                         | Số tháng cam kết                                 | Số tháng cam kết              |

<span id="_Bottom_sheet_Khuyến_1" class="anchor"></span>***API_IM_009 {API khóa eSIM cho hệ thống mDealer} – lockEsimForMBccs***

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffId | Id user đăng nhập | Id user đăng nhập (lấy staffDTO. staffId từ API_PRODUCT_003) |
| saleServiceCode | Mã dịch vụ bán hàng | Mã dịch vụ bán hàng (lấy lstReason.reasonCode từ API_PRODUCT_002 theo lý do tương ứng trên giao diện) |
| stockType | Loại kho | Loại kho<br>Nếu là Kho chung thì truyền 1<br>Nếu là Kho nhân viên thì truyền 2 |

- *Response:*

| **API**                                         | **Mô tả**                                        | **Giao diện** |
|-------------------------------------------------|--------------------------------------------------|---------------|
| errorCode                                       | Mã lỗi (200: thành công, -1: lỗi không xác định) |               |
| message                                         | Chi tiết lỗi                                     |               |
| **StockSimDTO: Thông tin khóa eSim thành công** |                                                  |               |
| serial                                          | Serial eSim                                      |               |

---

<a id="api-39"></a>

## 39. API_CM_038 {API lấy cấu hình số lượng thuê bao tối đa và tối thiểu} – getNumSubMinMaxConnectPostPaid

- *Request:*

| **API** | **Mô tả** | **Map dữ liệu truyền vào** |
|---------|-----------|----------------------------|
|         |           |                            |

- *Response:*

| **API**     | **Mô tả**                                    | **Giao diện** |
|-------------|----------------------------------------------|---------------|
| Code        | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| Description | Mô tả lỗi                                    |               |
| minNumSub   | Số lượng thuê bao tối thiểu                  |               |
| maxNumSub   | Số lưọng thuê bao tối đa                     |               |

---

<a id="api-40"></a>

## 40. API_CM_015 {API lấy ra danh sách 3 số thuê bao nhận OTP gần nhất cho mbccs} - getListIsdnOtpConfirm

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| productCode | Mã gói cước | Gói cước (lấy productCode tương ứng với Gói cước chọn trên giao diện từ API_PRODUCT_023 trả về) |
| idNo | Số giấy tờ | Số giấy tờ |
| idType | Loại giấy tờ | Loại giấy tờ |

- ***Response:***

| **API**    | **Mô tả**                                        | **Giao diện**          |
|------------|--------------------------------------------------|------------------------|
| code       | Mã lỗi (200: thành công, khác 200: Thất bại)     |                        |
| lstIsdnOtp | Danh sách 3 số thuê bao có ngày đấu nối gần nhất | Số điện thoại nhận OTP |
| lstSub     | Thông tin số thuê bao                            |                        |

---

<a id="api-41"></a>

## 41. API_CM_014 {API gửi mã OTP/gửi lại mã OTP} – sendOtpConfirm

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffDTO.staff_code | Mã user đăng nhập | Mã user đăng nhập |
| staffDTO.systemType | Mã hệ thống | Mặc định truyền “MDEALER” |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| isdnOtp | Số thuê bao nhận OTP | Số thuê bao nhận OTP |
| type | Loại tác động (ở đây là Nghiệp vụ Đấu nối trả sau) | Mặc định truyền "00" |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| idNo | Số giấy tờ | Số giấy tờ (màn Nhận diện thông tin khách hàng) |
| idType | Loại giấy tờ | Loại giấy tờ (màn Nhận diện thông tin khách hàng) |

- ***Response:***

| **API**     | **Mô tả**                                                                    | **Giao diện** |
|-------------|------------------------------------------------------------------------------|---------------|
| code        | Mã lỗi (200: thành công, khác 200: Thất bại)                                 |               |
| description | Mô tả mã lỗi                                                                 |               |
| value       | Mã otp                                                                       |               |
| nameModel   | Thiết bị nhận OTP, mdealer lấy giá trị này truyền lúc tạo đơn hàng cho order |               |

---

<a id="api-42"></a>

## 42. API_CM_016 {API kiểm tra mã OTP} - validateOTP

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| isdnOtp | Số thuê bao nhận OTP | Số thuê bao nhận OTP |
| idNo | Số giấy tờ | Số giấy tờ |
| idType | Loại giấy tờ | Loại giấy tờ |
| productCode | Mã gói cước | Gói cước (lấy productCode tương ứng với Gói cước chọn trên giao diện từ API_PRODUCT_023 trả về) |
| otp | Mã otp | Mã otp gửi đến số thuê bao nhận OTP khi gọi API_CM_014 |
| custType | Mã loại khách hàng | Loại khách hàng (lấy custType tương ứng Loại khách hàng chọn trên giao diện từ API_PRODUCT_010) |
| type | Loại tác động (ở đây là Nghiệp vụ Đấu nối trả sau) | Mặc định truyền "00" |

- ***Response:***

| **API**     | **Mô tả**                                    | **Giao diện** |
|-------------|----------------------------------------------|---------------|
| code        | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| description | Mô tả mã lỗi                                 |               |

---

<a id="api-43"></a>

## 43. API_QLHS_001 {API lấy danh sách chứng từ} – getListRecordConfig

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| idNo | Số giấy tờ | Số giấy tờ |
| idType | Mã loại giấy tờ | Mã loại giấy tờ |
| paymentTypeCode | Hình thức thanh toán | Hình thức thanh toán đã chọn ở màn Thông tin hợp đồng |
| **objectGetListRecord** |  |  |
| actionCode | Loại tác động (ở đây là Nghiệp vụ đấu nối trả sau) | Mặc định truyền “00” |
| reasonId | Id lý do | Lý do (lấy reasonId tương ứng lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| cusType | Loại khách hàng |  |
| serviceType | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền M<br>Nếu là Homephone thì truyền H |
| subType | Loại thuê bao | Loại thuê bao (lấy subType tương ứng Loại thuê bao chọn trên giao diện từ API_PRODUCT_024 trả về) |
| prepaid | Prepaid (áp dụng cho thuê bao trả trước) | Truyền 0 |
| objectCode | Mã đối tượng | Đối tượng (Lấy value tương ứng Đối tượng chọn trên giao diện từ API_PRODUCT_014 trả về) |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| electronicSign | Loại hồ sơ<br>Electronic: Hồ sơ điện tử<br>Normal: Hồ sơ thường |  |
| recordCode | Mã chứng từ |  |
| recordName | Tên chứng từ |  |
| reqScan | có bắt buộc tải file không |  |
| recordType: Mã loại chứng từ | Mã loại chứng từ ("ORIGIN": Chứng từ gốc,<br>"REPLACEMENT": Chứng từ thay thế) |  |
| recordCodeFile | Mã file chứng từ |  |
| recordId | Id chứng từ |  |

---

<a id="api-44"></a>

## 44. API_CM_007 {API view thông tin hồ sơ điện tử} – printRequest

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| fileName | Mã chứng từ | Mã chứng từ của hồ sơ điện tử tương ứng (trường recordCode từ API_QLHS_001 trả về) |
| **staffDTO: Thông tin user đăng nhập** |  |  |
| staffDTO.staff_code | Mã user đăng nhập | Mã user đăng nhập |
| staffDTO.staffId | Id user đăng nhập | Bỏ qua không truyền |
| staffDTO.shopId | Shop user đăng nhập | Bỏ qua không truyền |
| staffDTO.systemType | Mã hệ thống | Mặc định truyền “MDEALER” |
| **subscriberDTO: Thông tin thuê bao** |  |  |
| subscriberDTO.subId | Id số thuê bao | Bỏ qua không truyền |
| subscriberDTO.contractId | Số hợp đồng | Nếu là HĐ cũ thì truyền accountId từ API_CM_034 với hợp đồng cũ chọn trên giao diện<br>Nếu là HĐ mới thì bỏ qua không truyền |
| subscriberDTO.custId | Id khách hàng | Bỏ qua không truyền |
| subscriberDTO.accountId | Id tài khoản | Nếu là HĐ cũ thì truyền accountId từ API_CM_034 với hợp đồng cũ chọn trên giao diện<br>Nếu là HĐ mới thì bỏ qua không truyền |
| subscriberDTO.telecomServiceId | Id dịch vụ | Từ màn Chọn Loại thuê bao<br>Nếu chọn Homephone thì truyền 2<br>Nếu chọn Mobile thì truyền 1 |
| subscriberDTO.isdn | Số thuê bao | Số thuê bao trên màn hình Thông tin thuê bao<br>Trường hợp nhiều thuê bao thì lấy số thuê bao đầu tiên |
| subscriberDTO.imsi | imsi | Bỏ qua không truyền |
| subscriberDTO.serial | Serial | Serial trên màn hình Thông tin thuê bao<br>Trường hợp nhiều thuê bao thì lấy serial đầu tiên |
| subscriberDTO.status | Trạng thái | Bỏ qua không truyền |
| subscriberDTO.productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| subscriberDTO.offerId | Id gói cước | Id gói cước (lấy productOfferingDTOs.productOfferingId từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| subscriberDTO.staDatetime | Ngày bắt đầu | Bỏ qua không truyền |
| subscriberDTO.activeDatetime | Ngày active | Bỏ qua không truyền |
| subscriberDTO.createDatetime | Ngày tạo | Bỏ qua không truyền |
| subscriberDTO.createUser | Người tạo | Bỏ qua không truyền |
| subscriberDTO.updateDatetime | Ngày cập nhật | Bỏ qua không truyền |
| subscriberDTO.updateUser | Người cập nhật | Bỏ qua không truyền |
| subscriberDTO.deposit |  | Bỏ qua không truyền |
| subscriberDTO.limitUsage | Hạn mức | Hạn mức sau xác minh (lấy limitUsage từ API_CM_036 theo Hạn mức tương ứng trên giao diện) |
| subscriberDTO.firstConnect |  | Bỏ qua không truyền |
| subscriberDTO.payType | Trả trước/Trả sau | Từ màn Chọn loại thuê bao<br>Nếu là Trả trước thì truyền 2<br>Nếu là Trả sau thì truyền 1 |
| subscriberDTO.descriptionProduct | Mô tả gói cước | Mô tả gói cước (lấy productOfferingDTOs.description từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| subscriberDTO.actStatus |  | Bỏ qua không truyền |
| subscriberDTO.promotionCode | Mã khuyến mại | Lấy lstDiscountPromotion.code từ API_PRODUCT_026 tương ứng với khuyến mại chọn trên giao diện |
| subscriberDTO.subObject | Mã loại đối tượng | Lấy value tương ứng với giá trị trên giao diện từ API_PRODUCT_014 |
| subscriberDTO.regTypeId | Id lý do | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |
| subscriberDTO.signDate | Ngày hiện tại (dạng yyyy-mm-dd) | Ngày hiện tại (dạng yyyy-mm-dd) |
| **prepaidMonthBO** |  |  |
| prepaidId | ID cước đóng trước | ID cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| **accountDTOForInput** |  |  |
| areaCode | Mã địa bàn | Mã địa bàn |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ TBC) |
| district | Mã Quận/ Huyện | Mã Quận/ Huyện<br>(lấy trường district ở API_PRODUCT_004 trả về tương ứng Quận/ Huyện trên popup Chọn địa chỉ TBC) |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ TBC) |
| streetName | Tên đường/phố | Tên đường/phố (popup Chọn địa chỉ TBC) |
| home | Địa chỉ chi tiết | Địa chỉ chi tiết (popup Chọn địa chỉ TBC) |
| address | Địa chỉ TBC | Địa chỉ TBC (Địa chỉ đầy đủ) |
| telMobile | Điện thoại di động | Điện thoại di động |
| phoneContact | Điện thoại cố định | Điện thoại cố định |
| eMail | Email | Email |
| payMethod | Hình thức thanh toán | Hình thức thanh toán (lấy value từ API_PRODUCT_013 theo Hình thức thanh toán tương ứng trên giao diện) |
| payMethodName | Tên hình thức thanh toán | Tên hình thức thanh toán (lấy name từ API_PRODUCT_013 theo Hình thức thanh toán tương ứng trên giao diện) |
| noticeCharge | Hình thức thông báo cước | Hình thức thông báo cước (lấy value từ API_PRODUCT_013 theo Hình thức thông báo cước tương ứng trên giao diện) |
| noticeChargeName | Tên hình thức thông báo cước | Tên hình thức thông báo cước (lấy name từ API_PRODUCT_013 theo Hình thức thông báo cước tương ứng trên giao diện) |
| printMethod | In chi tiết cước | In chi tiết cước (lấy value từ API_PRODUCT_013 theo In chi tiết cước tương ứng trên giao diện) |
| printMethodName | Tên in chi tiết cước | Tên in chi tiết cước (lấy name từ API_PRODUCT_013 theo In chi tiết cước tương ứng trên giao diện) |
| **accountBank** |  |  |
| account | Số tài khoản | Tài khoản ngân hàng nhập ở màn Thông tin ủy nhiệm |
| accountName | Tên tài khoản | Tên tài khoản nhập ở màn Thông tin ủy nhiệm |
| bankAccountDate | Ngày nhờ thu | Ngày nhờ thu |
| bankAccountNo |  | Bỏ qua không truyền |
| bankCode | Mã ngân hàng | Mã ngân hàng (lấy trường bankCode mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| bankName | Tên ngân hàng | Tên ngân hàng (lấy trường name mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| **Trường hợp có chi nhánh thì truyền thêm accountBankDTO:** |  |  |
| account | Số tài khoản | Tài khoản ngân hàng nhập ở màn Thông tin ủy nhiệm |
| accountName | Tên tài khoản | Tên tài khoản nhập ở màn Thông tin ủy nhiệm |
| bankAccountDate | Ngày nhờ thu | Ngày nhờ thu |
| bankCode | Mã chi nhánh | Mã chi nhánh (lấy trường bankCode mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| bankName | Tên chi nhánh | Tên chi nhánh (lấy trường name mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| **Customer: Thông tin khách hàng doanh nghiệp** |  |  |
| customer.custId | Id khách hàng | KH cũ: lấy custId từ API_CM_004<br>KH mới: bỏ qua không truyền |
| customer.custType | Loại khách hàng | Loại khách hàng |
| customer.name | Tên doanh nghiệp | Tên doanh nghiệp |
| customer.birthDate | Ngày thành lập | Ngày thành lập |
| customer.status | Trạng thái | Bỏ qua không truyền |
| customer.areaCode | Mã địa bàn | Mã địa bàn |
| customer.province | Mã tỉnh/tp | Mã tỉnh/tp |
| customer.district | Mã quận/huyện | Mã quận/huyện |
| customer.precinct | Mã phường/xã | Mã phường/xã |
| customer.streetBlock | Mã tổ/thôn | Bỏ qua không truyền |
| customer.streetName | Tên đường/phố | Bỏ qua không truyền |
| customer.home | Địa chỉ chi tiết | Địa chỉ chi tiết |
| customer.address | Địa chỉ đầy đủ | Địa chỉ đầy đủ |
| customer.createUser | Ngày tạo | Bỏ qua không truyền |
| customer.createDatetime | Người tạo | Bỏ qua không truyền |
| customer.updateUser | Ngày cập nhật | Bỏ qua không truyền |
| customer.updateDatetime | Người cập nhật | Bỏ qua không truyền |
| actionCode | Mã tác động | Bỏ qua không truyền |
| **customer.listCustIdentity: Thông tin giấy tờ của khách hàng** |  |  |
| customer.listCustIdentity.idNo | Số giấy tờ | Số giấy tờ |
| customer.listCustIdentity.custId | Id khách hàng | Khách hàng mới: bỏ qua không truyền<br>Khách hàng cũ: Lấy listCustIdentity.custId ở API_CM_004 trả về |
| customer.listCustIdentity.custIdentityId | Id giấy tờ | Khách hàng mới: bỏ qua không truyền<br>Khách hàng cũ: Lấy listCustIdentity.custIdentityId ở API_CM_004 trả về |
| customer.listCustIdentity.idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| customer.listCustIdentity.idIssueDate | Ngày cấp | Ngày cấp |
| customer.listCustIdentity.idIssuePlace | Nơi cấp | Bỏ qua không truyền |
| customer.listCustIdentity.idType | Mã loại giấy tờ | Mã loại giấy tờ (lấy idType ở API_PRODUCT_016 tương ứng với loại giấy tờ trên màn Thông tin khách hàng) |
| **customer.lstSubPolicy: Thông tin chính sách** |  |  |
| improveQuality | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = IMPROVEQUALITY (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| provideProduct | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = PROVIDEPRODUCT (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| supportCustomer | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = SUPPORTCUSTOMER (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| marketingAdvertising | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = MARKETINGADVERTISING (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| researchMarket | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = RESEARCHMARKET (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| tradePromotion | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách<br>Check mã chính sách par_type = TRADEPROMOTION (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| sysDate | Ngày hiện tại | Truyền Ngày hiện tại |
| orderType | Loại đơn | Bỏ qua không truyền |

- ***Response:***

| **API**                           | **Mô tả**                                    | **Giao diện** |
|-----------------------------------|----------------------------------------------|---------------|
| errorCode                         | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| message                           | Mô tả mã lỗi                                 |               |
| **pdfDTO : Thông tin file hồ sơ** |                                              |               |
| invoiceUsedId                     |                                              |               |
| fileName                          | Tên file hồ sơ                               |               |
| pdfBytes                          | File pdf dạng byte                           |               |
| excelBytes                        | File excel dạng byte                         |               |
| fileExtension                     | Định dạng file                               |               |
| removeAble                        | Có thể xoá không                             |               |
| offlineFile                       | Có file offline                              |               |

---

<a id="api-45"></a>

## 45. API_QLHS_002 {API upload file chứng từ} - upload-profile

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| file | file chứng từ upload | file chứng từ upload |
| fileExtension | định dạng file | định dạng file |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| status | Success: upload file thành công<br>Khác success: upload file thất bại |  |
| description | Mô tả |  |
| File-id (ở Header) | Đường dẫn file chứng từ trên FTP |  |

---

<a id="api-46"></a>

## 46. API_QLHS_001 {API lấy danh sách chứng từ} – getListRecordConfig – chưa có

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| idNo | Số giấy tờ | Số giấy tờ |
| idType | Mã loại giấy tờ | Mã loại giấy tờ |
| paymentTypeCode | Hình thức thanh toán | Hình thức thanh toán đã chọn ở màn Thông tin hợp đồng |
| **objectGetListRecord** |  |  |
| actionCode | Loại tác động (ở đây là Nghiệp vụ đấu nối trả sau) | Mặc định truyền “00” |
| reasonId | Id lý do | Lý do (lấy reasonId tương ứng lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| cusType | Loại khách hàng |  |
| serviceType | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền M<br>Nếu là Homephone thì truyền H |
| subType | Loại thuê bao | Loại thuê bao (lấy subType tương ứng Loại thuê bao chọn trên giao diện từ API_PRODUCT_024 trả về) |
| prepaid | Prepaid (áp dụng cho thuê bao trả trước) | Truyền 0 |
| objectCode | Mã đối tượng | Đối tượng (Lấy value tương ứng Đối tượng chọn trên giao diện từ API_PRODUCT_014 trả về) |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| electronicSign | Loại hồ sơ<br>Electronic: Hồ sơ điện tử<br>Normal: Hồ sơ thường |  |
| recordCode | Mã chứng từ |  |
| recordName | Tên chứng từ |  |
| reqScan | có bắt buộc tải file không |  |
| recordType: Mã loại chứng từ | Mã loại chứng từ ("ORIGIN": Chứng từ gốc,<br>"REPLACEMENT": Chứng từ thay thế) |  |
| recordCodeFile | Mã file chứng từ |  |
| recordId | Id chứng từ |  |

---

<a id="api-47"></a>

## 47. API_PRODUCT_006 {API lấy danh sách chính sách bảo vệ và xử lý dữ liệu cá nhân} - getListApparam

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| par_name | Mã chính sách | Mặc định truyền = ACCEPT_USE_INFO_CUSTOMER_ND13 |

- ***Response:***

| **API**                                                        | **Mô tả**                                    | **Giao diện**   |
|----------------------------------------------------------------|----------------------------------------------|-----------------|
| code                                                           | Mã lỗi (200: thành công, khác 200: Thất bại) |                 |
| **Data: Danh sách chính sách bảo vệ và xử lý dữ liệu cá nhân** |                                              |                 |
| par_type                                                       | Loại chính sách                              | Loại chính sách |
| par_value                                                      | Tên chính sách                               | Tên chính sách  |
| description                                                    | Mô tả                                        |                 |

---

<a id="api-48"></a>

## 48. API generate captcha - captcha

- ***Request:***

<!-- -->

- N/A

<!-- -->

- ***Response:***

| **API**     | **Mô tả**                       | **Giao diện** |
|-------------|---------------------------------|---------------|
| imageBase64 | File ảnh Mã captcha dạng base64 |               |
| uuid        | uuid                            |               |

---

<a id="api-49"></a>

## 49. API validate captcha – validate-captcha

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| answer | Mã captcha | Mã captcha nhập vào |
| uuid | uuid | Lấy uuid ở response API captcha trả về |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| status | Success: thành công<br>Khác success: thất bại |  |
| detail | Mô tả lỗi |  |
| description | Mô tả |  |

---

<a id="api-50"></a>

## 50. API_ORDER_001 {API tạo đơn hàng} – place-order-base-multiple-subscribers – Khách hàng Cá nhân

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **Customer: Thông tin khách hàng** |  |  |
| customerType | Mã loại khách hàng | Mã loại khách hàng (lấy trường custType mà API_PRODUCT_010 trả về tương ứng Loại khách hàng ở màn Thông tin khách hàng) |
| custId | Id khách hàng cũ | Id khách hàng cũ (lấy custId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin khách hàng)<br>Nếu không có custId trả về ở API_CM_004 thì bỏ qua không truyền |
| custIdentityId | Id giấy tờ | Id giấy tờ (lấy custIdentityId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin khách hàng)<br>Nếu không có custIdentityId trả về ở API_CM_004 thì bỏ qua không truyền |
| idNo | Số giấy tờ | Số giấy tờ ở màn hình Thông tin khách hàng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idType | Mã loại giấy tờ | Mã loại giấy tờ (idType lấy từ API_PRODUCT_016 lấy danh sách Loại giấy tờ)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issueDate | Ngày cấp | Ngày cấp ở màn hình Thông tin khách hàng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| expireDate | Ngày hết hạn | Ngày hết hạn ở màn hình Thông tin khách hàng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ ở màn hình Thông tin khách hàng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| customerName | Họ và tên khách hàng | Họ và tên ở màn hình Thông tin khách hàng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| birthDate | Ngày sinh | Ngày sinh ở màn hình Thông tin khách hàng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| gender | Giới tính | Mã giới tính ở màn hình Thông tin khách hàng<br>Nam truyền M<br>Nữ truyền F<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| streetBlock | Mã Tổ/ Thôn | Mã Tổ/ Thôn<br>(lấy trường streetBlock ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetName | Số nhà/ Đường | Số nhà/ Đường<br>(lấy thông tin Số nhà/ Đường user nhập trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetBlockName | Tên Tổ/ Thôn | Tên Tổ/ Thôn<br>(lấy trường name ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| nationality | Quốc tịch | Mã quốc tịch<br>(lấy trường value ở API_PRODUCT_013 trả về tương ứng Quốc tịch ở màn Thông tin khách hàng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| fullAddress | Địa chỉ | Địa chỉ ở màn hình Thông tin khách hàng (Địa chỉ mới)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| contactPhone | Số điện thoại liên hệ của người giám hộ | Bỏ qua không truyền |
| guardianRelation | Mối quan hệ với khách hàng | Bỏ qua không truyền |
| signMethod | Người giám hộ ký qua SMS hay không | Bỏ qua không truyền |
| groupType | Mã nhóm loại khách hàng | Mã nhóm loại khách hàng (lấy trường value ở API_PRODUCT_005 trả về tương ứng Nhóm loại khách hàng ở màn Thông tin khách hàng) |
| improveQuality | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = IMPROVEQUALITY (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| marketingAdvertising | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = MARKETINGADVERTISING (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| prepaidId | ID cước đóng trước | ID cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| promotionCode | Mã khuyến mại | Mã khuyến mại (lấy trường code ở API_PRODUCT_026 trả về tương ứng Mã khuyến mại ở màn Thông tin thuê bao) |
| provideProduct | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = PROVIDEPRODUCT (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| researchMarket | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = RESEARCHMARKET (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| serviceType | Loại dịch vụ | Từ màn Chọn loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| supportCustomer | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = SUPPORTCUSTOMER (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| tradePromotion | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = TRADEPROMOTION (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| orderType | Loại đơn hàng | Truyền Loại đơn hàng theo chức năng:<br>KHCN truyền ‘CONNECT_POSPAID_DIRECT’ |
| guardianName | Họ và tên người giám hộ/người được giám hộ | Họ và tên người giám hộ/người được giám hộ (ở màn hình Thông tin thuê bao) |
| flagKHDN | Đấu nối đơn lẻ hay đấu nối theo lô | True: Đấu nối nhiều thuê bao<br>False: Đấu nối 1 thuê bao |
| payStatus | Trạng thái thanh toán<br>0: Chưa thanh toán<br>1: Đã thanh toán | Truyền: 0 |
| **PayInfo: Thông tin thanh toán** |  |  |
| immediatePay | True: Thanh toán ngay<br>False: Thanh toán sau | Truyền: False |
| payMethod | Hình thức thanh toán | Bỏ qua không truyền |
| **feeRecords: Thông tin phí** |  |  |
| feeCode | Mã phí | Mã phí (lấy feeCode từ API_CM_017 trả về) |
| feeAmount | Tiền phí | Tiền phí (lấy feePrice từ API_CM_017 trả về) |
| feeName | Tên phí | Tên phí (lấy feeName từ API_CM_017 trả về) |
| **additionalCustomerIdentities: Truyền thông tin giấy tờ bổ sung nếu có** |  |  |
| custIdentityId | Id giấy tờ | Bỏ qua không truyền |
| idNo | Số giấy tờ | Bỏ qua không truyền |
| idType | Mã loại giấy tờ | Bỏ qua không truyền |
| idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| idIssueDate | Ngày cấp | Bỏ qua không truyền |
| idIssuePlace | Nơi cấp giấy tờ | Bỏ qua không truyền |
| idTypeName | Tên loại giấy tờ | Bỏ qua không truyền |
| lstIsdnKHDN | Danh sách số thuê bao đấu nối | Danh sách số thuê bao đấu nối |
| **Subscribers: Thông tin thuê bao<br>Trường hợp nhiều thuê bao thì truyền list Subscribers** |  |  |
| isdn | Số thuê bao | Số thuê bao ở màn hình Nhập số thuê bao khách hàng |
| limitUsageBefore | Hạn mức trước xác minh | Hạn mức trước xác minh |
| limitUsageAfter | Hạn mức sau xác minh | Hạn mức sau xác minh |
| reasonId | Id lý do | Id lý do (Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002) |
| serial | Số serial | Số serial ở màn Thông tin thuê bao |
| subObject | Mã đối tượng | Mã đối tượng (lấy value tương ứng với giá trị trên giao diện từ API_PRODUCT_014) |
| subType | Loại thuê bao | Mã loại thuê bao (Lấy subType tương ứng với giá trị trên giao diện từ API_PRODUCT_024) |
| otpConfirm | Mã otp xác nhận | Mã otp xác nhận nhập ở màn Xác thực OTP |
| isdnSendOtpConfirm | Số điện thoại nhận OTP | Số điện thoại nhận OTP chọn ở màn Xác thực OTP |
| signDate | Ngày ký hợp đồng | Ngày ký hợp đồng ở màn Hoàn thiện hồ sơ<br>Định dạng: yyyy - mm - dd |
| nameModel | Model với thuê bao nhận OTP có model | Model với thuê bao nhận OTP có model<br>Truyền nameModel ở response API_CM_014 sendOtpConfirm trả về |
| offerId | Id gói cước | Id gói cước (lấy productOfferingDTOs.productOfferingId từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| telecomServiceId | Id dịch vụ | Từ màn Chọn Loại thuê bao<br>Nếu chọn Homephone thì truyền 2<br>Nếu chọn Mobile thì truyền 1 |
| **subInfrastructureDTO: Địa chỉ lắp đặt (chỉ truyền với Loại thuê bao = Homephone)** |  |  |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP ở trường Địa chỉ lắp đặt) |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Bỏ qua không truyền |
| areaCode | Mã địa bàn | Mã địa bàn |
| address | Địa chỉ lắp đặt | Địa chỉ lắp đặt (truyền địa chỉ đầy đủ của Địa chỉ lắp đặt) |
| **AccountDTOForInput: Thông tin hợp đồng** |  |  |
| accountId | Mã hợp đồng | Mã hợp đồng<br>Bắt buôc truyền với hợp đồng cũ, hợp đồng mới thì không truyền |
| address | Địa chỉ xác minh thông báo cước | Địa chỉ xác minh thông báo cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| district | Mã Quận/ Huyện | Mã Quận/ Huyện<br>(lấy trường district ở API_PRODUCT_004 trả về tương ứng Quận/ Huyện trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| addressPrinct | Địa chỉ hóa đơn cước | Địa chỉ hóa đơn cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| billCycleId | Chu kỳ cước | Chu kỳ cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| noticeCharge | Hình thức thông báo cước | Hình thức thông báo cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| payMethod | Hình thức thanh toán | Hình thức thanh toán<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| printMethod | In chi tiết cước | In chi tiết cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| signDate | Ngày ký hợp đồng | Ngày ký hợp đồng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| telMobile | Điện thoại di động | Điện thoại di động<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| phoneContact | Số điện thoại cố định | Số điện thoại cố định<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| eMail | Email | Email nhập ở màn Thông tin hợp đồng<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| **accountBank: Thông tin ngân hàng** |  |  |
| bankCode | Mã ngân hàng | Mã ngân hàng (lấy trường bankCode mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| bankName | Tên ngân hàng | Tên ngân hàng (lấy trường name mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| accountName | Tên tài khoản | Tên tài khoản nhập ở màn Thông tin ủy nhiệm |
| account | Số tài khoản | Tài khoản ngân hàng nhập ở màn Thông tin ủy nhiệm |
| **prepaidMonthBO** |  |  |
| prepaidId | ID cước đóng trước | ID cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| **lsSubGoodsDTO** |  |  |
| actionCode | Mã tác động | Truyền 00 |
| serial | Serial mặt hàng | Serial mặt hàng |
| stockModelId | Id mặt hàng | Id mặt hàng<br>(lấy trường productOfferingId ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| stockModelName | Tên mặt hàng | Tên mặt hàng<br>(lấy trường name ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| stockTypeId | Loại mặt hàng | Loại mặt hàng<br>(lấy trường productOfferTypeId ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| **profileRecords: Danh sách file chứng từ<br>(chỉ cần truyền các file chứng từ không lấy thông tin từ nhận diện, file đã có từ nhận diện AI không cần truyền)** |  |  |
| code | Mã chứng từ | Mã chứng từ tương ứng màn hình Hoàn thiện hồ sơ |
| electronicSign | Loại hồ sơ | Loại hồ sơ<br>1: Hồ sơ điện tử<br>0: Hồ sơ thường |
| name | Tên file chứng từ | Tên file chứng từ |
| url | Đường dẫn file chứng từ | Đường dẫn file chứng từ ( lấy file-id trả về khi gọi API_QLHS_002)<br>Nếu tick chọn Ký Hồ sơ điện tử thì loại chứng từ hồ sơ điện tử (giao diện chỉ gọi API view file printRequest) thì bỏ qua không truyền |
| **customerGuardian : Thông tin người giám hộ** |  |  |
| customerType | Mã loại khách hàng | Bỏ qua không truyền |
| custId | Id khách hàng cũ | Bỏ qua không truyền |
| custIdentityId | Id giấy tờ | Bỏ qua không truyền |
| idNo | Số giấy tờ | Số giấy tờ ở vùng thông tin người giám hộ |
| idType | Mã loại giấy tờ | Bỏ qua không truyền |
| issueDate | Ngày cấp | Ngày cấp ở vùng thông tin người giám hộ<br>Định dạng: yyyy - mm – dd |
| expireDate | Ngày hết hạn | Bỏ qua không truyền |
| issuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ ở vùng thông tin người giám hộ |
| customerName | Họ và tên khách hàng | Họ và tên ở vùng thông tin người giám hộ |
| birthDate | Ngày sinh | Bỏ qua không truyền |
| gender | Giới tính | Bỏ qua không truyền |
| province | Mã Tỉnh/ Thành phố | Bỏ qua không truyền |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Bỏ qua không truyền |
| streetBlock | Mã Tổ/ Thôn | Bỏ qua không truyền |
| streetName | Số nhà/ Đường | Bỏ qua không truyền |
| streetBlockName | Tên Tổ/ Thôn | Bỏ qua không truyền |
| nationality | Quốc tịch | Bỏ qua không truyền |
| fullAddress | Địa chỉ | Địa chỉ ở vùng thông tin người giám hộ |
| isPCProduct | Là gói cước PC | Bỏ qua không truyền |
| zoneProvince | Mã Tỉnh/ Thành phố | Bỏ qua không truyền |
| zoneDistrict | Mã Quận/ Huyện | Bỏ qua không truyền |
| contactPhone | Số điện thoại liên hệ của người giám hộ | Số điện thoại liên hệ của người giám hộ (lấy SĐT liên hệ ở vùng Thông tin người giám hộ - màn Thông tin thuê bao) |
| guardianRelation | Mối quan hệ với khách hàng | Mối quan hệ với khách hàng (lấy value từ API_PRODUCT_013 trả về tương ứng giá trị Mối quan hệ với KH chọn trên giao diện) |
| signMethod | Người giám hộ ký qua SMS hay không | Người giám hộ ký qua SMS hay không<br>PHONE: ký qua SMS<br>LINK: ký điện tử trực tiếp trên app |
| **additionalCustomerIdentities: Truyền thông tin giấy tờ bổ sung nếu có** |  |  |
| custIdentityId | Id giấy tờ | Bỏ qua không truyền |
| idNo | Số giấy tờ | Bỏ qua không truyền |
| idType | Mã loại giấy tờ | Bỏ qua không truyền |
| idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| idIssueDate | Ngày cấp | Bỏ qua không truyền |
| idIssuePlace | Nơi cấp giấy tờ | Bỏ qua không truyền |
| idTypeName | Tên loại giấy tờ | Bỏ qua không truyền |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| message | Mô tả |  |
| data | Mã đơn hàng |  |
| videoCall | Có cần video call không<br>True: có<br>False: không |  |
| detail | Mô tả lỗi |  |

---

<a id="api-51"></a>

## 51. API_ORDER_001 {API tạo đơn hàng} – place-order-base-business-multiple-subscribers – Khách hàng Doanh nghiệp

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| **BusinessCustomer: Thông tin khách hàng doanh nghiệp** |  |  |
| customerType | Mã loại khách hàng | Mã loại khách hàng (lấy trường custType mà API_PRODUCT_010 trả về tương ứng Loại khách hàng ở màn Thông tin khách hàng doanh nghiệp) |
| custId | Id khách hàng cũ | Id khách hàng cũ (lấy custId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin khách hàng doanh nghiệp)<br>Nếu không có custId trả về ở API_CM_004 thì bỏ qua không truyền |
| custIdentityId | Id giấy tờ | Id giấy tờ (lấy custIdentityId ở API_CM_004 trả về tương ứng với Số GPKD (idType = BUS) ở màn Thông tin khách hàng doanh nghiệp)<br>Nếu không có custIdentityId trả về ở API_CM_004 thì bỏ qua không truyền |
| idNo | Số giấy tờ | Số giấy tờ ở màn hình Thông tin khách hàng doanh nghiệp<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idType | Mã loại giấy tờ | Mã loại giấy tờ (idType lấy từ API_PRODUCT_016 lấy danh sách Loại giấy tờ)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issueDate | Ngày cấp | Ngày cấp ở màn hình Thông tin khách hàng doanh nghiệp<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| expireDate |  | Bỏ qua không truyền |
| issuePlace | Nơi cấp giấy tờ | Truyền tên tỉnh của địa chỉ |
| customerName | Tên doanh nghiệp | Tên doanh nghiệp ở màn hình Thông tin khách hàng doanh nghiệp<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| birthDate | Ngày thành lập | Ngày thành lập ở màn hình Thông tin khách hàng doanh nghiệp<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| gender |  | Bỏ qua không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ màn Thông tin khách hàng doanh nghiệp)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| district | Mã Quận/ Huyện | Mã Quận/ Huyện<br>(lấy trường district ở API_PRODUCT_004 trả về tương ứng Quận/ Huyện trên popup Chọn địa chỉ màn Thông tin khách hàng doanh nghiệp)<br>Khách hàng mới nếu sử dụng địa chỉ cũ thì truyền, khách hàng cũ thì không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ màn Thông tin khách hàng doanh nghiệp)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| streetBlock | Mã Tổ/ Thôn | Mã Tổ/ Thôn<br>(lấy trường streetBlock ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetName | Số nhà/ Đường | Số nhà/ Đường<br>(lấy thông tin Số nhà/ Đường user nhập trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetBlockName | Tên Tổ/ Thôn | Tên Tổ/ Thôn<br>(lấy trường name ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| nationality |  | Bỏ qua không truyền |
| fullAddress | Địa chỉ | Địa chỉ ở màn hình Thông tin khách hàng doanh nghiệp<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| contactPhone | Số điện thoại liên hệ của người giám hộ | Bỏ qua không truyền |
| guardianRelation | Mối quan hệ với khách hàng | Bỏ qua không truyền |
| signMethod | Người giám hộ ký qua SMS hay không | Bỏ qua không truyền |
| groupType | Mã nhóm loại khách hàng | Mã nhóm loại khách hàng (lấy trường value ở API_PRODUCT_005 trả về tương ứng Nhóm loại khách hàng ở màn Thông tin khách hàng doanh nghiệp) |
| improveQuality | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = IMPROVEQUALITY (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| marketingAdvertising | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = MARKETINGADVERTISING (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| prepaidId | ID cước đóng trước | ID cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| promotionCode | Mã khuyến mại | Mã khuyến mại (lấy trường code ở API_PRODUCT_026 trả về tương ứng Mã khuyến mại ở màn Thông tin thuê bao) |
| provideProduct | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = PROVIDEPRODUCT (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| researchMarket | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = RESEARCHMARKET (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| serviceType | Loại dịch vụ | Từ màn Chọn loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| supportCustomer | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = SUPPORTCUSTOMER (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| tradePromotion | Có chọn chính sách<br>1: Có tick chọn<br>0: Không tick chọn | Có chọn chính sách ở màn Hoàn thiện hồ sơ<br>Check mã chính sách par_type = TRADEPROMOTION (API_PRODUCT_006 trả về) thì:<br>1: Có tick chọn<br>0: Không tick chọn |
| orderType | Loại đơn hàng | Truyền Loại đơn hàng theo chức năng:<br>KHDN truyền ‘CONNECT_POSPAID_KHDN_DIRECT’ |
| guardianName | Họ và tên người giám hộ/người được giám hộ | Họ và tên người giám hộ/người được giám hộ (ở màn hình Thông tin thuê bao) |
| flagKHDN | Đấu nối đơn lẻ hay đâu nối theo lô | True: Đấu nối nhiều thuê bao<br>False: Đấu nối 1 thuê bao |
| representativeAsUsingCustomer | Sử dụng thông tin người đại diện cho Người sử dụng | Truyền true nếu chọn Sử dụng thông tin người đại diện cho Người sử dụng (ở màn Thông tin khách hàng)<br>Nếu không sử dụng thì truyền false |
| payStatus | Trạng thái thanh toán<br>0: Chưa thanh toán<br>1: Đã thanh toán | Truyền: 0 |
| **PayInfo: Thông tin thanh toán** |  |  |
| immediatePay | True: Thanh toán ngay<br>False: Thanh toán sau | Truyền: False |
| payMethod | Hình thức thanh toán | Bỏ qua không truyền |
| **feeRecords: Thông tin phí** |  |  |
| feeCode | Mã phí | Mã phí (lấy feeCode từ API_CM_017 trả về) |
| feeAmount | Tiền phí | Tiền phí (lấy feePrice từ API_CM_017 trả về) |
| feeName | Tên phí | Tên phí (lấy feeName từ API_CM_017 trả về) |
| **additionalCustomerIdentities: Truyền thông tin giấy tờ bổ sung (Mã số thuế)** |  |  |
| custIdentityId | Id giấy tờ | Id giấy tờ (lấy custIdentityId ở API_CM_004 trả về tương ứng với idType = TIN)<br>Nếu không có custIdentityId trả về ở API_CM_004 thì bỏ qua không truyền |
| idNo | Số giấy tờ | Mã số thuế ở màn hình Thông tin khách hàng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idType | Mã loại giấy tờ | Mã loại giấy tờ (idType lấy từ API_PRODUCT_016)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| idIssueDate | Ngày cấp | Ngày cấp ở màn hình Thông tin khách hàng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idIssuePlace | Nơi cấp giấy tờ | Bỏ qua không truyền |
| idTypeName | Tên loại giấy tờ | Tên loại giấy tờ (Name lấy từ API_PRODUCT_016)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| **RepresentativeCustomer: Thông tin người đại diện** |  |  |
| custId | Id khách hàng cũ | Id khách hàng cũ (lấy custId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin người đại diện)<br>Nếu không có custId trả về ở API_CM_004 thì bỏ qua không truyền |
| customerType | Mã loại khách hàng | Mã loại khách hàng (lấy trường custType mà API_PRODUCT_010 trả về tương ứng Loại khách hàng ở màn Thông tin người đại diện) |
| custIdentityId | Id giấy tờ | Id giấy tờ (lấy custIdentityId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin người đại diện)<br>Nếu không có custIdentityId trả về ở API_CM_004 thì bỏ qua không truyền |
| idNo | Số giấy tờ | Số giấy tờ ở màn hình Thông tin người đại diện<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idType | Mã loại giấy tờ | Mã loại giấy tờ (idType lấy từ API_PRODUCT_016 lấy danh sách Loại giấy tờ)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issueDate | Ngày cấp | Ngày cấp ở màn hình Thông tin người đại diện<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| expireDate | Ngày hết hạn | Ngày hết hạn ở màn hình Thông tin người đại diện<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ ở màn hình Thông tin người đại diện<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| birthDate | Ngày sinh | Ngày sinh ở màn hình Thông tin người đại diện<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| customerName | Họ và tên khách hàng | Họ và tên ở màn hình Thông tin người đại diện<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| gender | Giới tính | Mã giới tính ở màn hình Thông tin người đại diện<br>Nam truyền M<br>Nữ truyền F<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ màn Thông tin người đại diện)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ màn Thông tin người đại diện)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| streetBlock | Mã Tổ/ Thôn | Mã Tổ/ Thôn<br>(lấy trường streetBlock ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetName | Số nhà/ Đường | Số nhà/ Đường<br>(lấy thông tin Số nhà/ Đường user nhập trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| streetBlockName | Tên Tổ/ Thôn | Tên Tổ/ Thôn<br>(lấy trường name ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| nationality | Quốc tịch | Mã quốc tịch<br>(lấy trường value ở API_PRODUCT_013 trả về tương ứng Quốc tịch ở màn Thông tin người đại diện)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| fullAddress | Địa chỉ | Địa chỉ ở màn hình Thông tin người đại diện (Địa chỉ mới)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| contactPhone | Số điện thoại liên hệ của người giám hộ | Bỏ qua không truyền |
| guardianRelation | Mối quan hệ với khách hàng | Bỏ qua không truyền |
| signMethod | Người giám hộ ký qua SMS hay không | Bỏ qua không truyền |
| **additionalCustomerIdentities: Truyền thông tin giấy tờ bổ sung** |  |  |
| custIdentityId | Id giấy tờ | Bỏ qua không truyền |
| idNo | Số giấy tờ | Bỏ qua không truyền |
| idType | Mã loại giấy tờ | Bỏ qua không truyền |
| idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| idIssueDate | Ngày cấp | Bỏ qua không truyền |
| idIssuePlace | Nơi cấp giấy tờ | Bỏ qua không truyền |
| idTypeName | Tên loại giấy tờ | Bỏ qua không truyền |
| **UsingCustomer: Thông tin người sử dụng** |  |  |
| custId | Id khách hàng cũ | Id khách hàng cũ (lấy custId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin người sử dụng)<br>Nếu không có custId trả về ở API_CM_004 thì bỏ qua không truyền |
| customerType | Mã loại khách hàng | Mã loại khách hàng (lấy trường custType mà API_PRODUCT_010 trả về tương ứng Loại khách hàng ở màn Thông tin người sử dụng) |
| custIdentityId | Id giấy tờ | Id giấy tờ (lấy custIdentityId ở API_CM_004 trả về tương ứng với khách hàng ở màn Thông tin người sử dụng)<br>Nếu không có custIdentityId trả về ở API_CM_004 thì bỏ qua không truyền |
| idNo | Số giấy tờ | Số giấy tờ ở màn hình Thông tin người sử dụng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| idType | Mã loại giấy tờ | Mã loại giấy tờ (idType lấy từ API_PRODUCT_016 lấy danh sách Loại giấy tờ)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issueDate | Ngày cấp | Ngày cấp ở màn hình Thông tin người sử dụng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| expireDate | Ngày hết hạn | Ngày hết hạn ở màn hình Thông tin người sử dụng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| issuePlace | Nơi cấp giấy tờ | Nơi cấp giấy tờ ở màn hình Thông tin người sử dụng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| birthDate | Ngày sinh | Ngày sinh ở màn hình Thông tin người sử dụng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| customerName | Họ và tên khách hàng | Họ và tên ở màn hình Thông tin người sử dụng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| gender | Giới tính | Mã giới tính ở màn hình Thông tin người sử dụng<br>Nam truyền M<br>Nữ truyền F<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ màn Thông tin người sử dụng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ màn Thông tin người sử dụng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| streetBlock | Mã Tổ/ Thôn | Mã Tổ/ Thôn<br>(lấy trường streetBlock ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| nationality | Quốc tịch | Mã quốc tịch<br>(lấy trường value ở API_PRODUCT_013 trả về tương ứng Quốc tịch ở màn Thông tin người sử dụng)<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| streetName | Số nhà/ Đường | Số nhà/ Đường<br>(lấy thông tin Số nhà/ Đường user nhập trên popup Chọn địa chỉ màn Thông tin khách hàng) |
| streetBlockName | Tên Tổ/ Thôn | Tên Tổ/ Thôn<br>(lấy trường name ở API_PRODUCT_004 trả về tương ứng Tổ/ Thôn trên popup Chọn địa chỉ màn Thông tin khách hàng)<br>Khách hàng mới nếu nhập thì truyền, khách hàng cũ thì không truyền |
| fullAddress | Địa chỉ | Địa chỉ ở màn hình Thông tin người sử dụng<br>Bắt buộc truyền với khách hàng mới, khách hàng cũ thì không truyền |
| contactPhone | Số điện thoại liên hệ của người giám hộ | Bỏ qua không truyền |
| guardianRelation | Mối quan hệ với khách hàng | Bỏ qua không truyền |
| signMethod | Người giám hộ ký qua SMS hay không | Bỏ qua không truyền |
| **additionalCustomerIdentities: Truyền thông tin giấy tờ bổ sung** |  |  |
| custIdentityId | Id giấy tờ | Bỏ qua không truyền |
| idNo | Số giấy tờ | Bỏ qua không truyền |
| idType | Mã loại giấy tờ | Bỏ qua không truyền |
| idExpireDate | Ngày hết hạn | Bỏ qua không truyền |
| idIssueDate | Ngày cấp | Bỏ qua không truyền |
| idIssuePlace | Nơi cấp giấy tờ | Bỏ qua không truyền |
| idTypeName | Tên loại giấy tờ | Bỏ qua không truyền |
| lstIsdnKHDN | Danh sách số thuê bao đấu nối | Danh sách số thuê bao đấu nối |
| **Subscribers: Thông tin thuê bao<br>Trường hợp nhiều thuê bao thì truyền list Subscribers** |  |  |
| isdn | Số thuê bao | Số thuê bao ở màn hình Nhập số thuê bao khách hàng |
| limitUsageBefore | Hạn mức trước xác minh | Hạn mức trước xác minh |
| limitUsageAfter | Hạn mức sau xác minh | Hạn mức sau xác minh |
| reasonId | Id lý do | Id lý do (Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002) |
| serial | Số serial | Số serial ở màn Thông tin thuê bao |
| subObject | Mã đối tượng | Mã đối tượng (lấy value tương ứng với giá trị trên giao diện từ API_PRODUCT_014) |
| subType | Loại thuê bao | Mã loại thuê bao (Lấy subType tương ứng với giá trị trên giao diện từ API_PRODUCT_024) |
| otpConfirm | Mã otp xác nhận | Mã otp xác nhận nhập ở màn Xác thực OTP |
| isdnSendOtpConfirm | Số điện thoại nhận OTP | Số điện thoại nhận OTP chọn ở màn Xác thực OTP |
| signDate | Ngày ký hợp đồng | Ngày ký hợp đồng ở màn Hoàn thiện hồ sơ<br>Định dạng: yyyy - mm - dd |
| nameModel | Model với thuê bao nhận OTP có model | Model với thuê bao nhận OTP có model<br>Truyền nameModel ở response API_CM_014 sendOtpConfirm trả về |
| offerId | Id gói cước | Id gói cước (lấy productOfferingDTOs.productOfferingId từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| telecomServiceId | Id dịch vụ | Từ màn Chọn Loại thuê bao<br>Nếu chọn Homephone thì truyền 2<br>Nếu chọn Mobile thì truyền 1 |
| **subInfrastructureDTO: Địa chỉ lắp đặt (chỉ truyền với Loại thuê bao = Homephone)** |  |  |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP ở trường Địa chỉ lắp đặt)<br>Không bắt buộc truyền |
| district | Mã Quận/ Huyện | Bỏ qua không truyền |
| precinct | Mã Phường/ Xã | Bỏ qua không truyền |
| areaCode | Mã địa bàn | Mã địa bàn |
| address | Địa chỉ lắp đặt | Địa chỉ lắp đặt (truyền địa chỉ đầy đủ của Địa chỉ lắp đặt) |
| **AccountDTOForInput: Thông tin hợp đồng** |  |  |
| accountId | Mã hợp đồng | Mã hợp đồng<br>Bắt buôc truyền với hợp đồng cũ, hợp đồng mới thì không truyền |
| address | Địa chỉ xác minh thông báo cước | Địa chỉ xác minh thông báo cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| province | Mã Tỉnh/ Thành phố | Mã Tỉnh/ Thành phố<br>(lấy trường province ở API_PRODUCT_004 trả về tương ứng Tỉnh/ TP trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| district | Mã Quận/ Huyện | Mã Quận/ Huyện<br>(lấy trường district ở API_PRODUCT_004 trả về tương ứng Quận/ Huyện trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| precinct | Mã Phường/ Xã | Mã Phường/ Xã<br>(lấy trường precinct ở API_PRODUCT_004 trả về tương ứng Phường/ Xã trên popup Chọn địa chỉ TBC)<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| addressPrinct | Địa chỉ hóa đơn cước | Địa chỉ hóa đơn cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| billCycleId | Chu kỳ cước | Chu kỳ cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| noticeCharge | Hình thức thông báo cước | Hình thức thông báo cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| payMethod | Hình thức thanh toán | Hình thức thanh toán<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| printMethod | In chi tiết cước | In chi tiết cước<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| signDate | Ngày ký hợp đồng | Ngày ký hợp đồng<br>Định dạng: yyyy - mm – dd<br>Bắt buộc truyền với hợp đồng mới, hợp đồng cũ thì không truyền |
| telMobile | Điện thoại di động | Điện thoại di động<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| phoneContact | Số điện thoại cố định | Số điện thoại cố định<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| eMail | Email | Email nhập ở màn Thông tin hợp đồng<br>Truyền với hợp đồng mới (nếu nhập), hợp đồng cũ thì không truyền |
| **accountBank: Thông tin ngân hàng** |  |  |
| bankCode | Mã ngân hàng | Mã ngân hàng (lấy trường bankCode mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| bankName | Tên ngân hàng | Tên ngân hàng (lấy trường name mà API_PRODUCT_028 trả về tương ứng Ngân hàng ở màn Thông tin ủy nhiệm) |
| accountName | Tên tài khoản | Tên tài khoản nhập ở màn Thông tin ủy nhiệm |
| account | Số tài khoản | Tài khoản ngân hàng nhập ở màn Thông tin ủy nhiệm |
| **prepaidMonthBO** |  |  |
| prepaidId | ID cước đóng trước | ID cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| prepaidValue | Mã cước đóng trước | Mã cước đóng trước<br>Truyền -1 trong trường hợp không chọn cước đóng trước |
| **lsSubGoodsDTO** |  |  |
| actionCode | Mã tác động | Truyền 00 |
| serial | Serial mặt hàng | Serial mặt hàng |
| stockModelId | Id mặt hàng | Id mặt hàng<br>(lấy trường productOfferingId ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| stockModelName | Tên mặt hàng | Tên mặt hàng<br>(lấy trường name ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| stockTypeId | Loại mặt hàng | Loại mặt hàng<br>(lấy trường productOfferTypeId ở API_PRODUCT_031 trả về tương ứng với mặt hàng) |
| **profileRecords: Danh sách file chứng từ<br>(chỉ cần truyền các file chứng từ không lấy thông tin từ nhận diện, file đã có từ nhận diện AI không cần truyền)** |  |  |
| code | Mã chứng từ | Mã chứng từ tương ứng màn hình Hồ sơ và ký |
| electronicSign | Loại hồ sơ | Loại hồ sơ<br>1: Hồ sơ điện tử<br>0: Hồ sơ thường |
| name | Tên file chứng từ | Tên file chứng từ |
| url | Đường dẫn file chứng từ | Đường dẫn file chứng từ ( lấy file-id trả về khi gọi API_QLHS_002)<br>Nếu tick chọn Ký Hồ sơ điện tử thì loại chứng từ hồ sơ điện tử (giao diện chỉ gọi API view file printRequest) thì bỏ qua không truyền |

- ***Response:***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| message | Mô tả |  |
| data | Mã đơn hàng |  |
| videoCall | Có cần video call không<br>True: có<br>False: không |  |
| detail | Mô tả lỗi |  |

---

<a id="api-52"></a>

## 52. API_ORDER_002 {API lấy cấu hình video call} – config-video-call

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| operatingSystem | Hệ điều hành | Hệ điều hành (truyền IOS/ANDROID) |
| actionCode | Mã chức năng | Mặc định truyền VSALE_DAUNOI_DIDONG |

- ***Response (Nếu input operatingSystem = IOS và response trả về supplier = VTS):***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| Code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| Description | Mô tả lỗi |  |
| supplier | Đơn vị<br>VTS: Đơn vị là VTS |  |
| userName | userName |  |
| sipDomain | sipDomain |  |
| hashingKey | hashingKey |  |
| requestedBy | requestedBy |  |
| apiBaseUrl | apiBaseUrl |  |
| isUseTCP | isUseTCP |  |
| **clientCustomData** |  |  |
| type | type |  |
| company | company |  |
| systemType | systemType |  |
| mbccsIDCheck | mbccsIDCheck |  |
| mbccsIdTypeCheck | mbccsIdTypeCheck |  |
| channelTypeId | channelTypeId |  |

- ***Response (Nếu input operatingSystem = ANDROID và response trả về supplier = VTS):***

| API | Mô tả | Giao diện |
| --- | --- | --- |
| Code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| Description | Mô tả lỗi |  |
| supplier | Đơn vị<br>VTS: Đơn vị là VTS |  |
| baseUrl | baseUrl |  |
| socketUrl | socketUrl |  |
| socketPort | socketPort |  |
| sipProxy | sipProxy |  |
| sipDomain | sipDomain |  |
| requestedBy | requestedBy |  |
| domainHtml | domainHtml |  |
| chatUrl | chatUrl |  |
| callFlow | callFlow |  |
| videoCallFlow | videoCallFlow |  |
| hashingKey | hashingKey |  |
| userId | userId |  |
| isUseTCP | isUseTCP |  |
| **clientCustomData** |  |  |
| type | type |  |
| company | company |  |
| systemType | systemType |  |
| mbccsIDCheck | mbccsIDCheck |  |
| mbccsIdTypeCheck | mbccsIdTypeCheck |  |
| channelTypeId | channelTypeId |  |

---
