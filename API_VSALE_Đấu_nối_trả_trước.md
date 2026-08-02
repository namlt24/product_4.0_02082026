# Tổng hợp API - PYC 42897 VSALE Nâng cấp nghiệp vụ Đấu nối trả trước

_Tài liệu tổng hợp 32 API được trích xuất từ tài liệu MTYCTĐ_VSALE_PYC_42897_VSALE_Nâng cấp nghiệp vụ Đấu nối trả trước.docx.

## Mục lục

1. [API lấy danh sách quyền của user theo actionCode – permissions-by-action-code](#api-1)
2. [API_CM_004 {API lấy thông tin khách hàng cũ} - getListCustomerByIdNo](#api-2)
3. [API_PRODUCT_019 {API lấy cấu hình chỉnh sửa các trường thông tin trên giao diện} - getComponentEditable](#api-3)
4. [API_PRODUCT_005 {API lấy danh sách nhóm loại khách hàng} - getAllGroupCustType](#api-4)
5. [API_PRODUCT_010 {API lấy danh sách loại khách hàng} – getMappingChannelCustTypeV2](#api-5)
6. [API_PRODUCT_016 {API lấy danh sách loại giấy tờ} - getListIdentityType](#api-6)
7. [API_PRODUCT_004 {API lấy danh sách địa bàn} – findAreaByParentCode](#api-7)
8. [API_PRODUCT_003 {API lấy thông tin địa bàn của user} - getStaffShopFullInfo](#api-8)
9. [API_PRODUCT_042 {API lấy thông tin mapping theo địa bàn} - getListAreaMappingByAreaCode](#api-9)
10. [API_PRODUCT_013 {API lấy dữ liệu danh mục đơn giản} - getOptionSetValue](#api-10)
11. [API lấy cấu hình giá trị dài nhất của trường nơi cấp – CONFIG_MAX_LENGTH_ISSUE_PLACE](#api-11)
12. [API lấy cấu hình thay thế trường nơi cấp – IDC_CONFIG_ISSUE_PLACE](#api-12)
13. [API_CM_005 { API đối soát dữ liệu của BCA} – validateNationalData](#api-13)
14. [API_CM_012 {API kiểm tra số lượng thuê bao tối đa của số giấy tờ} – validateMaxSub](#api-14)
15. [API_CM_013 {API kiểm tra thuê bao} – checkM2MSubscriber](#api-15)
16. [API_CM_017 {API lấy phí dịch vụ} – getListFee](#api-16)
17. [API_PRODUCT_048 {API lấy phí theo list số phục vụ đấu lô} - getListFeeV2](#api-17)
18. [API_PRODUCT_023 {API lấy ra danh sách gói cước} – getProductCodeByMapActiveInfo](#api-18)
19. [API_PRODUCT_002 {API lấy danh sách Hình thức hòa mạng} – getReasonFull](#api-19)
20. [API_PRODUCT_014 {API lấy danh sách Đối tượng} - getListObject](#api-20)
21. [API_PRODUCT_003 {API lấy thông tin địa bàn của User} – getStaffShopFullInfo](#api-21)
22. [API_CM_029 { API lấy danh sách đối tượng đặc biệt} – getListObjectSpec](#api-22)
23. [API_CM_040 {API validate thông tin thuê bao} – validateSubscriberConnect](#api-23)
24. [API_IM_004 {API lấy số tự động} – searchIsdnByReasonId](#api-24)
25. [API_CM_038 {API lấy cấu hình số lượng thuê bao tối đa và tối thiểu} – getNumSubMinMaxConnectPostPaid](#api-25)
26. [API_PRODUCT_031 {API lấy danh sách hàng hoá} - getListStockTypeWS](#api-26)
27. [API_QLHS_001 {API lấy danh sách chứng từ} - getListRecordConfig](#api-27)
28. [API_CM_007 {API view thông tin hồ sơ điện tử} - printRequest](#api-28)
29. [API_QLHS_002 {API upload file chứng từ} - upload-profile](#api-29)
30. [API_PRODUCT_006 {API lấy danh sách chính sách bảo vệ và xử lý dữ liệu cá nhân} - getListApparam](#api-30)
31. [API generate captcha - captcha](#api-31)
32. [API validate captcha – validate-captcha](#api-32)

<a id="api-1"></a>

## 1. API lấy danh sách quyền của user theo actionCode – permissions-by-action-code

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| actionCode | Mã hành động | Màn Đấu nối di động mặc định truyền:<br>actionCode = VSALE_DAUNOI_DIDONG |


<a id="api-2"></a>

## 2. API_CM_004 {API lấy thông tin khách hàng cũ} - getListCustomerByIdNo

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| idNo* | Số giấy tờ | Số giấy tờ trên giao diện |
| groupType* | Mã nhóm loại khách hàng | Mã nhóm loại khách hàng |
| idType | Mã loại giấy tờ nhận diện | Chỉ truyền khi nhận diện AI<br>Mã loại giấy tờ nhận diện |
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
| listCustIdentity.idTypeName | Loại giấy tờ | Loại giấy tờ |
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
| listCustIdentity.idTypeName | Loại giấy tờ | Loại giấy tờ |
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
| isNew | Mã địa bàn cũ/mới<br>Null: địa bàn cũ<br>1: Địa bàn mới<br>2: Tất cả địa bàn (cũ + mới) |  |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| code | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| **1stArea** |  |  |
| name | Tên địa bàn | Tỉnh/TP hoặc Quận/huyện hoặc Phường/Xã hoặc Tổ/thôn tương ứng |
| areaCode | Mã địa bàn |  |
| province | Mã tỉnh/tp |  |
| district | Mã quận/huyện |  |
| precinct | Mã phường/xã |  |
| streetBlock | Mã tổ/thôn |  |
| isNew | Địa bàn mới/cũ<br>Null: địa bàn cũ<br>1: Địa bàn mới |  |

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
| shopDTO.province    | Mã Tỉnh/TP                                   | Tỉnh/TP (Map với API_PRODUCT_004 để trả ra Tên Tỉnh/TP tương ứng)       |
| shopDTO.district    | Mã Quận/huyện                                | Quận/huyện (Map với API_PRODUCT_004 để trả ra Tên Quận/huyện tương ứng) |
| shopDTO.precinct    | Mã Phường/xã                                 | Phường/xã (Map với API_PRODUCT_004 để trả ra Tên Phường/xã tương ứng)   |
| shopDTO.streetBlock | Mã Tổ/thôn                                   | Tổ/thôn (Map với API_PRODUCT_004 để trả ra Tên Tổ/thôn tương ứng)       |

---

<a id="api-9"></a>

## 9. API_PRODUCT_042 {API lấy thông tin mapping theo địa bàn} - getListAreaMappingByAreaCode

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| lstAreaCode | Danh sách mã địa bàn | Mã địa bàn<br>KH mới: lấy areaCode từ API_MBCCS_004 trả về sau khi nhận diện giấy tờ<br>KH cũ: lấy areaCode từ API_CM_004 |

- *Response:*

| API | Mô tả | Giao diện |
| --- | --- | --- |
| errorCode | Mã lỗi (200: thành công, khác 200: Thất bại) |  |
| success | Kết quả (true/false) |  |
| message | Mô tả |  |
| **lstArea: Danh sách thông tin địa bàn: Thông tin địa bàn truyền vào** |  |  |
| areaCode | Mã địa bàn |  |
| fullName | Tên địa bàn đầy đủ |  |
| isNew | 1: Địa bàn mới<br>Null: Địa bàn cũ |  |
| **lstArea.LstMappingAreaDTO: Danh sách thông tin địa bàn mapping** |  |  |
| areaCode | Mã địa bàn |  |
| fullName | Tên địa bàn đầy đủ |  |
| province | Mã tỉnh/thành phố |  |
| district | Mã quận/huyện |  |
| precinct | Mã phường/xã |  |
| streetBlock | Mã tổ/thôn |  |
| isNew | 1: Địa bàn mới<br>Null: Địa bàn cũ |  |
| provinceName | Tên tỉnh/tp |  |
| districtName | Tên quận/huyện |  |
| precinctName | Tên phường/xã |  |
| streetBlockName | Tên cụm/tổ/thôn |  |

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
| lstOptionSetValue |                                              |               |
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

## 14. API_CM_012 {API kiểm tra số lượng thuê bao tối đa của số giấy tờ} – validateMaxSub

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

<a id="api-15"></a>

## 15. API_CM_013 {API kiểm tra thuê bao} – checkM2MSubscriber

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

<a id="api-16"></a>

## 16. API_CM_017 {API lấy phí dịch vụ} – getListFee

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| isdn | Số thuê bao | Số thuê bao |
| serial | Serial | Lấy serial ở màn hình Nhập số thuê bao |
| telecomServiceId | Id dịch vụ | Từ màn Chọn loại thuê bao<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| reasonId | Lý do chọn | Lý do (lấy reasonId tương ứng với Hình thức hòa mạng chọn trên giao diện từ API_PRODUCT_002 trả về) |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| actionCode | Loại tác động | Mặc định truyền 00 |
| payMethod | Hình thức thanh toán | Bỏ qua không truyền |
| areaCode | Địa chỉ hợp đồng | Bỏ qua không truyền |
| staffCode | Mã nhân viên thực hiện | Mã nhân viên thực hiện |
| prepaidValue | Mã cước đóng trước | Bỏ qua không truyền |
| prepaidId | Id cước đóng trước | Bỏ qua không truyền |
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

<a id="api-17"></a>

## 17. API_PRODUCT_048 {API lấy phí theo list số phục vụ đấu lô} - getListFeeV2

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| telecomServiceId | Mã dịch vụ | Mã dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023) |
| reasonId | Mã lý do | Lý do (lấy reasonId tương ứng với Lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| actionCode | Mã tác động | Mã tác động (Mặc định truyền 00) |
| payMethod | Hình thức thanh toán | Bỏ qua không truyền |
| areaCode | Địa chỉ hợp đồng | Bỏ qua không truyền |
| staffCode | Mã nhân viên thực hiện | Mã nhân viên thực hiện |
| prepaidValue | Mã cước đóng trước | Bỏ qua không truyền |
| prepaidId | Id cước đóng trước | Bỏ qua không truyền |
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
| feePrice | Giá trị phí | Phí dịch vụ hiển thị dạng đánh dấu chấm hàng nghìn:<br>{feePrice}+’đ’<br>Hiển thị trên bottom sheet Chi tiết đơn giá > Xem chi tiết phí của từng thuê bao (Hình 5.1.3.12) |

---

<a id="api-18"></a>

## 18. API_PRODUCT_023 {API lấy ra danh sách gói cước} – getProductCodeByMapActiveInfo

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
| message                                                                         | Chi tiết lỗi                                 |               |
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

<a id="api-19"></a>

## 19. API_PRODUCT_002 {API lấy danh sách Hình thức hòa mạng} – getReasonFull

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Mã user đăng nhập |  |
| payType | 1: Trả sau 2: Trả trước | Lấy từ màn hình chọn Loại thuê bao |
| actionCode | Mã tác động | Truyền = 00 |
| serviceType | Loại dịch vụ | Check thông tin từ màn hình Chọn loại thuê bao<br>1: là Mobile thì truyền M<br>2: là Home phone thì truyền H |
| mode |  | Truyền mode = 1 |
| getReasonCharUse |  | Truyền false |
| offerId | ID gói cước | Lấy offerId từ API_CM_023 |
| subType | Loại thuê bao | Bỏ qua không truyền |
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

<a id="api-20"></a>

## 20. API_PRODUCT_014 {API lấy danh sách Đối tượng} - getListObject

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| custType | Mã loại khách hàng | Loại khách hàng (lấy custType tương ứng Loại khách hàng chọn trên giao diện từ API_PRODUCT_010) |
| birthDate | Ngày sinh khách hàng<br>Định dạng ddMMyyyy | Ngày sinh khách hàng |

- ***Response:***

| **API**           | **Mô tả**                                    | **Giao diện** |
|-------------------|----------------------------------------------|---------------|
| Code              | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| lstOptionSetValue |                                              |               |
| name              | Tên đối tượng                                | Đối tượng     |
| value             | Mã đối tượng                                 |               |

---

<a id="api-21"></a>

## 21. API_PRODUCT_003 {API lấy thông tin địa bàn của User} – getStaffShopFullInfo

> *Ghi chú: API này xuất hiện lặp lại trong tài liệu (mục 8), phần response có sai khác nhỏ về tên field (`shopDTO.province` vs `staffDTO.shopDTO.province`) — giữ nguyên để đối chiếu.*

- *Request:*

| **API**   | **Mô tả**         | **Map dữ liệu truyền vào** |
|-----------|-------------------|----------------------------|
| staffCode | Mã user đăng nhập | Mã user đăng nhập          |

- *Response:*

| **API**                   | **Mô tả**                                    | **Giao diện**                                                           |
|---------------------------|----------------------------------------------|-------------------------------------------------------------------------|
| code                      | Mã lỗi (200: thành công, khác 200: Thất bại) |                                                                         |
| staffDTO                  |                                              |                                                                         |
| staffDTO.shopDTO.province | Mã Tỉnh/TP                                   | Tỉnh/TP (Map với API_PRODUCT_004 để trả ra Tên Tỉnh/TP tương ứng)       |
| staffDTO.shopDTO.district | Mã Quận/huyện                                | Quận/huyện (Map với API_PRODUCT_004 để trả ra Tên Quận/huyện tương ứng) |
| staffDTO.shopDTO.precinct | Mã Phường/xã                                 | Phường/xã (Map với API_PRODUCT_004 để trả ra Tên Phường/xã tương ứng)   |
| shopDTO.streetBlock       | Mã Tổ/thôn                                   | Tổ/thôn (Map với API_PRODUCT_004 để trả ra Tên Tổ/thôn tương ứng)       |

---

<a id="api-22"></a>

## 22. API_CM_029 { API lấy danh sách đối tượng đặc biệt} – getListObjectSpec

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

<span id="_Màn_hình_nhập_4" class="anchor"></span>***API_PRODUCT_031 {API lấy danh sách hàng hoá} - getListStockTypeWS***

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
| name | Tên loại mặ hàng |  |
| ParentId | Mã loại mặt hàng cha |  |
| **lstProductOfferType.productOfferings: Danh sách mặt hàng tương ứng thuộc loại mặt hàng** |  |  |
| productPackTypeId | Id loại mặt hàng của DVBH |  |
| productOfferTypeId | Id loại mặt hàng |  |
| productOfferTypeName | Tên loại mặt hàng |  |
| productOfferingId | Id mặt hàng |  |
| code | Mã mặt hàng |  |
| name | Tên mặt hàng | Tên mặt hàng |

---

<a id="api-23"></a>

## 23. API_CM_040 {API validate thông tin thuê bao} – validateSubscriberConnect

- *Request:*

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| staffCode | Nhân viên đấu nối | Nhân viên đấu nối |
| **subscriberDTO: Thông tin thuê bao** |  |  |
| isdn | Số thuê bao | Số thuê bao (màn Nhập thông tin thuê bao)<br>Nếu đấu nối nhiều thuê bao thì nhập Số thuê bao đầu tiên |
| serial | Số serial | Số serial (màn Nhập thông tin thuê bao)<br>Nếu là eSim thì lấy serial từ API_IM_009 trả về |
| productCode | Mã gói cước | Mã gói cước (lấy productOfferingDTOs.code từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| promotionCode | Mã khuyến mại | Bỏ qua không truyền |
| reasonId | Mã lý do đấu nối | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |
| telecomServiceId | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền 1<br>Nếu là Homephone thì truyền 2 |
| subType | Loại thuê bao | Bỏ qua không truyền |
| payType | 1: Trả sau 2: Trả trước | Lấy từ màn hình chọn Loại thuê bao |
| limitUsageBefore | Hạn mức trước xác minh | Bỏ qua không truyền |
| limitUsageAfter | Hạn mức sau xác minh | Bỏ qua không truyền |
| subObject | Đối tượng | Đối tượng (Lấy value tương ứng Đối tượng chọn trên giao diện từ API_PRODUCT_014 trả về) |
| **prepaidMonthBO: Thông tin cước đóng trước** |  |  |
| prepaidId | id cước đóng trước | Bỏ qua không truyền |
| prepaidValue | Mã cước đóng trước | Bỏ qua không truyền |

- *Response:*

| **API**     | **Mô tả**                                    | **Giao diện** |
|-------------|----------------------------------------------|---------------|
| Code        | Mã lỗi (200: thành công, khác 200: Thất bại) |               |
| Description | Mô tả lỗi                                    |               |

---

<a id="api-24"></a>

## 24. API_IM_004 {API lấy số tự động} – searchIsdnByReasonId

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

<span id="_Toc186201401" class="anchor"></span>***API_IM_009 {API khóa eSIM cho hệ thống mDealer} – lockEsimForMBccs***

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

<a id="api-25"></a>

## 25. API_CM_038 {API lấy cấu hình số lượng thuê bao tối đa và tối thiểu} – getNumSubMinMaxConnectPostPaid

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

<a id="api-26"></a>

## 26. API_PRODUCT_031 {API lấy danh sách hàng hoá} - getListStockTypeWS

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
| productOfferTypeId | Id loại mặt hàng | Chỉ hiển thị lên giao diện các mặt hàng có loại mặt hàng productOfferTypeId not in (1,2,3,4,5)<br>Mặt hàng không serial: hiển thị các list product_offer_type_id = 11<br>Mặt hàng: hiển thị các list product_offer_type_id != 11 |
| name | Tên loại mặ hàng |  |
| ParentId | Mã loại mặt hàng cha |  |
| **lstProductOfferType.productOfferings: Danh sách mặt hàng tương ứng thuộc loại mặt hàng** |  |  |
| productPackTypeId | Id loại mặt hàng của DVBH |  |
| productOfferTypeId | Id loại mặt hàng |  |
| productOfferTypeName | Tên loại mặt hàng |  |
| productOfferingId | Id mặt hàng |  |
| code | Mã mặt hàng |  |
| name | Tên mặt hàng | Tên mặt hàng |

---

<a id="api-27"></a>

## 27. API_QLHS_001 {API lấy danh sách chứng từ} - getListRecordConfig

- ***Request:***

| API | Mô tả | Map dữ liệu truyền vào |
| --- | --- | --- |
| idNo | Số giấy tờ | Số giấy tờ |
| idType | Mã loại giấy tờ | Mã loại giấy tờ |
| **objectGetListRecord** |  |  |
| actionCode | Loại tác động (ở đây là Nghiệp vụ Đấu nối trả trước) | Mặc định truyền “00” |
| reasonId | Id lý do | Lý do (lấy reasonId tương ứng lý do chọn trên giao diện từ API_PRODUCT_002 trả về) |
| cusType | Loại khách hàng |  |
| serviceType | Loại dịch vụ | Loại dịch vụ<br>Nếu là Mobile thì truyền M<br>Nếu là Homephone thì truyền H |
| subType | Loại thuê bao | Bỏ qua không truyền |
| prepaid | Prepaid (áp dụng cho thuê bao trả trước) | Truyền 1 |
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

<a id="api-28"></a>

## 28. API_CM_007 {API view thông tin hồ sơ điện tử} - printRequest

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
| subscriberDTO.subId | Id số thuê bao | Lấy từ IM (nếu có) |
| subscriberDTO.contractId | Số hợp đồng | Bỏ qua không truyền |
| subscriberDTO.custId | Id khách hàng | Bỏ qua không truyền |
| subscriberDTO.accountId | Id tài khoản | Bỏ qua không truyền |
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
| subscriberDTO.limitUsage |  | Bỏ qua không truyền |
| subscriberDTO.firstConnect |  | Bỏ qua không truyền |
| subscriberDTO.payType | Trả trước/Trả sau | Từ màn Chọn loại thuê bao<br>Nếu là Trả trước thì truyền 2<br>Nếu là Trả sau thì truyền 1 |
| subscriberDTO.descriptionProduct | Mô tả gói cước | Mô tả gói cước (lấy productOfferingDTOs.description từ API_PRODUCT_023 theo gói cước tương ứng trên giao diện) |
| subscriberDTO.actStatus |  | Bỏ qua không truyền |
| subscriberDTO.promotionCode | Mã khuyến mại | Bỏ qua không truyền |
| subscriberDTO.subObject | Mã loại đối tượng | Lấy value tương ứng với giá trị trên giao diện từ API_PRODUCT_014 |
| subscriberDTO.regTypeId | Id lý do | Lấy reasonId tương ứng với giá trị trên giao diện từ API_PRODUCT_002 |
| subscriberDTO.signDate | Ngày hiện tại (dạng yyyy-mm-dd) | Ngày hiện tại (dạng yyyy-mm-dd) |
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
| actionCode | Mã tác động | Truyền = 989 |
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

<a id="api-29"></a>

## 29. API_QLHS_002 {API upload file chứng từ} - upload-profile

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

<a id="api-30"></a>

## 30. API_PRODUCT_006 {API lấy danh sách chính sách bảo vệ và xử lý dữ liệu cá nhân} - getListApparam

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

<a id="api-31"></a>

## 31. API generate captcha - captcha

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

<a id="api-32"></a>

## 32. API validate captcha – validate-captcha

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
