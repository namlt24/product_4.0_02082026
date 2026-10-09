# Xác thực đổi máy — mô tả FE và hướng dẫn gắn dữ liệu

Miniapp: `mbccs-verify-device-change`.  
Module dùng chung: `@mbccs/modules-bccs` (`identity`, `result`, `detail`).

Tài liệu này dành cho người gắn API. Component **không gọi HTTP**. Page nhận dữ liệu (hiện là hằng số mẫu), format chuỗi hiển thị, rồi truyền vào props. Nhãn trên UI nằm ở `src/i18n/vi.ts` và `en.ts`, không lấy từ backend.

Sau khi sửa module, chạy `pnpm --filter @mbccs/modules-bccs build` rồi mới chạy miniapp. Import đúng subpath, không import root package.

## Đã làm

Bốn màn, hash router, chạy trên browser với JSAPI mock (`adapterId: "mock"`).

| Màn | Route | Component |
|---|---|---|
| Chọn hình thức nhận diện | `#/` và `#/?segment=...` | `CustomerSummaryCard`, `IdentityMethodPicker` |
| Quản lý đơn hàng | `#/orders` | `OrderCard` (trong miniapp, chưa tách module) |
| Chi tiết đơn hàng | `#/orders/detail` | `DetailView` |
| Kết quả yêu cầu | `#/result` | `RequestResultCard` |

Cả bốn màn nằm trong `PageFrame`: điện thoại full ngang, iPad mini trở lên căn cột, iPad lớn tối đa 960px.

Chưa nối API. Số liệu trên màn là preview. Bấm phương thức nhận diện và chip lọc chưa gọi gì. Ô tìm kiếm chỉ lọc mảng đang có trên client theo tên dịch vụ, số thuê bao, tên khách hàng, mã đơn.

## Hợp đồng chung

- Trạng thái dùng chuỗi hằng, không dùng số magic.
- Chuỗi hiển thị (nhãn, câu thông báo, tên đã format) do FE ghép. Backend trả mã và giá trị nghiệp vụ.
- Ảnh truyền `src` (URL hoặc data URL) và `alt`. Component không tự tải theo id.
- Lỗi kỹ thuật (`BccsError`) do page gọi `showError`. Component kết quả chỉ vẽ trạng thái nghiệp vụ `processing | success | error`.
- Thời gian trên UI mẫu đang là chuỗi đã format `HH:mm | dd/MM/yyyy`. API nên trả ISO-8601, FE format trước khi đưa vào prop `value`.

---

## 1. CustomerSummaryCard

Khối khách hàng phía trên màn nhận diện. Import:

```ts
import { CustomerSummaryCard } from '@mbccs/modules-bccs/identity'
```

| Prop | Kiểu | Backend cung cấp |
|---|---|---|
| `initials` | `string` | 1–2 ký tự từ tên. FE cắt, không cần API riêng. |
| `title` | `string` | Số thuê bao đã format để hiện, ví dụ `0364 123 456`. |
| `description` | `string` | FE map từ `segment`, không gửi câu mô tả từ API. |

`segment` FE đang hiểu:

| Giá trị | Nhãn trên thẻ | Hình thức phía dưới |
|---|---|---|
| `domestic-individual` | Khách hàng cá nhân trong nước | NFC, VNeID |
| `foreign-individual` | Khách hàng cá nhân nước ngoài | Chụp trực tiếp (hộ chiếu) |
| `domestic-organization` | Doanh nghiệp/tổ chức - NSD là cá nhân trong nước | NFC, VNeID |
| `foreign-organization` | Doanh nghiệp/tổ chức - NSD là cá nhân nước ngoài | Chụp trực tiếp (GPKD) |

Thiếu hoặc sai `segment` thì FE coi là `domestic-individual`.

```tsx
<CustomerSummaryCard
  initials="TD"
  title="0364 123 456"
  description="Khách hàng cá nhân trong nước"
/>
```

Gợi ý payload:

```json
{
  "subscriberNo": "0364123456",
  "customerName": "Trần Đức",
  "segment": "domestic-individual"
}
```

## 2. IdentityMethodPicker

Danh sách hình thức nhận diện. Cùng import `@mbccs/modules-bccs/identity`.

| Prop | Kiểu | Ý nghĩa |
|---|---|---|
| `label` | `string` | Nhãn nhóm. FE dùng i18n, hiện «Hình thức nhận diện thông tin khách hàng». |
| `required` | `boolean` | Hiện dấu `*`. Không validate form. |
| `options` | `IdentityMethodOption[]` | FE dựng theo `segment`. |
| `onSelect` | `(id: string) => void` | Id phương thức user vừa bấm. |

Mỗi option:

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `string` | Khóa FE đang dùng: `nfc`, `vneid`, `direct-capture`. |
| `icon` | React node | FE gắn `NfcIcon`, `VneidIcon`, `UserScanIcon`. API không gửi icon. |
| `title` | `string` | «Quét NFC», «VNEID», «Chụp trực tiếp». |
| `description` | `string` | Dòng mô tả dưới title. |

```tsx
<IdentityMethodPicker
  label="Hình thức nhận diện thông tin khách hàng"
  required
  options={[
    {
      id: 'nfc',
      icon: <NfcIcon />,
      title: 'Quét NFC',
      description: 'Quét CCCD gắn chip NFC.',
    },
  ]}
  onSelect={(id) => {
    // id === 'nfc' | 'vneid' | 'direct-capture'
  }}
/>
```

Việc backend cần sẵn khi FE nối bước này: API bắt đầu phiên theo `subscriberNo` + `segment` + `methodId`. `direct-capture` dùng cho cả hộ chiếu (cá nhân nước ngoài) và giấy phép (tổ chức, NSD nước ngoài). FE phân biệt bằng `segment`, không bằng id riêng.

Hiện `onSelect` là hàm rỗng.

## 3. OrderCard

Thẻ một đơn trên `#/orders`. Nằm trong miniapp, chưa export từ modules.

File: `src/pages/Orders/OrderCard.tsx`.

| Field trên `order` | Kiểu | Việc của API |
|---|---|---|
| `id` | `string` | Id đơn, dùng làm key và để mở chi tiết. |
| `serviceName` | `string` | Tên dịch vụ hiện trên đầu thẻ. Mẫu: «Xác thực đổi máy». |
| `status` | `inProgress` \| `completed` \| `cancelled` | Màu tag. Sai giá trị thì tag không có màu. |
| `statusLabel` | `string` | Chữ trong tag. FE map: Đang thực hiện / Hoàn thành / Đã huỷ. |
| `phone` | `string` | Số thuê bao. |
| `customerName` | `string` | Tên khách hàng. |
| `orderCode` | `string` | Mã đơn, hiện ở footer. |
| `createdAt` | `string` | Chuỗi đã format `HH:mm \| dd/MM/yyyy`. |
| `phoneLabel`, `customerLabel` | `string` | Nhãn i18n, API không gửi. |

Bấm thẻ đi tới `#/orders/detail?status={status}`. Chưa truyền `id`. Khi có API, route nên thành `/orders/detail?id={id}` và trang chi tiết tải theo id.

Ô tìm kiếm lọc local theo `serviceName`, `phone`, `customerName`, `orderCode`. Chip «Lọc», «Thời gian», «Trạng thái», «Loại dịch vụ» chỉ là giao diện. «Xoá bộ lọc» chỉ xóa text search.

Danh sách rỗng thì FE hiện `BccsEmpty`.

Gợi ý một phần tử:

```json
{
  "id": "6196411852",
  "orderCode": "6196411852",
  "serviceCode": "VERIFY_DEVICE_CHANGE",
  "status": "inProgress",
  "subscriberNo": "0981234567",
  "customerName": "Nguyễn Văn Bê",
  "createdAt": "2026-01-01T18:00:00+07:00"
}
```

## 4. DetailView

Khối chi tiết dùng chung nhiều nghiệp vụ. Import:

```ts
import {
  DetailView,
  DetailStatus,
  DetailValueTone,
  DETAIL_PORTRAIT_HEIGHT,
  DETAIL_DOCUMENT_HEIGHT,
} from '@mbccs/modules-bccs/detail'
```

| Prop | Kiểu | Ý nghĩa |
|---|---|---|
| `sections` | `DetailSection[]` | Các thẻ nhãn–giá trị, theo thứ tự. |
| `media` | `DetailMedia[]` | Ảnh phía dưới. Bỏ qua hoặc `[]` nếu không có ảnh. |

`DetailSection`:

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `string` | Duy nhất trong một `DetailView`. FE đang dùng `customer`, `order`. |
| `title` | `string` | Tiêu đề thẻ. |
| `icon` | React node | FE gắn icon. API không gửi. |
| `fields` | `DetailField[]` | Các dòng. |

`DetailField` — một dòng là **giá trị chữ** hoặc **tag trạng thái**:

| Field | Khi nào gửi |
|---|---|
| `label` | Luôn có. |
| `value` | Dòng chữ. Bỏ khi dòng là tag. |
| `tone` | `default` (đen), `brand` (đỏ thương hiệu), `muted` (xám). Mặc định `default`. |
| `status` | `inProgress` \| `completed` \| `cancelled` khi dòng là tag màu. |
| `statusLabel` | Chữ trong tag. Cả `status` và `statusLabel` cùng có thì FE vẽ tag, bỏ `value`. |

Quy ước màn chi tiết đơn hiện tại:

- «Thời gian hết hạn» dùng `tone: "brand"`.
- «Thời gian huỷ» chỉ thêm khi `status === "cancelled"`, `tone: "muted"`.
- Ảnh chân dung luôn có. Ảnh hộ chiếu chỉ khi query `photos=passport`. Khi nối API, gửi ảnh nào có dữ liệu, đừng dựa vào query này.

`DetailMedia`:

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `string` | `portrait`, `passport` trên màn này. |
| `title` | `string` | «Ảnh khách hàng», «Ảnh hộ chiếu». |
| `icon` | React node | FE gắn. |
| `src` | `string` | URL ảnh backend trả về. |
| `alt` | `string` | Mô tả ảnh. |
| `height` | `number` | Chiều cao thiết kế: chân dung `240`, giấy tờ `428`. FE giữ tỉ lệ `345 / height`. |

`DetailStatus`: `inProgress`, `completed`, `cancelled`.  
`DetailValueTone`: `default`, `brand`, `muted`.

```tsx
<DetailView
  sections={[
    {
      id: 'customer',
      title: 'Thông tin khách hàng',
      icon: <DetailUserIcon />,
      fields: [
        { label: 'Họ và tên khách hàng', value: 'Nguyễn Văn Bê' },
        { label: 'Số giấy tờ', value: '001313030494' },
        { label: 'Số điện thoại', value: '0368888888' },
      ],
    },
    {
      id: 'order',
      title: 'Thông tin đơn hàng',
      icon: <DetailBagIcon />,
      fields: [
        { label: 'Loại đơn hàng', value: 'Xác thực đổi máy' },
        { label: 'Mã đơn hàng', value: '6196411852' },
        { label: 'Trạng thái', status: 'completed', statusLabel: 'Hoàn thành' },
        { label: 'Thời gian tạo', value: '10:00 | 25/12/2025' },
        { label: 'Thời gian hết hạn', value: '10:00 | 27/12/2025', tone: 'brand' },
      ],
    },
  ]}
  media={[
    {
      id: 'portrait',
      title: 'Ảnh khách hàng',
      icon: <DetailScanIcon />,
      src: portraitUrl,
      alt: 'Ảnh chân dung khách hàng',
      height: DETAIL_PORTRAIT_HEIGHT,
    },
  ]}
/>
```

Gợi ý payload chi tiết:

```json
{
  "id": "6196411852",
  "orderCode": "6196411852",
  "serviceCode": "VERIFY_DEVICE_CHANGE",
  "status": "cancelled",
  "customerName": "Nguyễn Văn Bê",
  "documentNo": "001313030494",
  "phone": "0368888888",
  "createdAt": "2025-12-25T10:00:00+07:00",
  "expiresAt": "2025-12-27T10:00:00+07:00",
  "cancelledAt": "2025-12-27T10:00:00+07:00",
  "portraitUrl": "https://.../portrait.jpg",
  "documentImageUrl": "https://.../passport.jpg"
}
```

`cancelledAt` chỉ có nghĩa khi `status` là `cancelled`. `documentImageUrl` rỗng thì FE không render thẻ hộ chiếu.

Query để xem năm biến thể UI lúc chưa có API:

- `#/orders/detail` — hoàn thành, chỉ chân dung
- `#/orders/detail?status=cancelled` — đã huỷ, có thời gian huỷ
- `#/orders/detail?status=inProgress&photos=passport` — đang thực hiện, đủ hai ảnh
- `#/orders/detail?status=completed&photos=passport`
- `#/orders/detail?status=cancelled&photos=passport`

## 5. RequestResultCard

Thẻ kết quả sau khi gửi yêu cầu. Import:

```ts
import {
  RequestResultCard,
  RequestResultStatus,
} from '@mbccs/modules-bccs/result'
```

| Prop | Kiểu | Việc của API |
|---|---|---|
| `status` | `processing` \| `success` \| `error` | Chọn ảnh, màu tiêu đề, màu số thuê bao. |
| `title` | `string` | FE map từ status. |
| `message` | `{ text: string, emphasized?: boolean }[]` | Các mảnh câu. Mảnh `emphasized: true` là số thuê bao, tô đậm. |
| `orderLabel` | `string` | Nhãn «Mã đơn hàng», từ i18n. |
| `orderCode` | `string` | Mã đơn để hiện và copy. |
| `orderRequired` | `boolean` | Hiện `*` cạnh nhãn. Không liên quan validate server. |
| `copyLabel` | `string` | Tên nút copy cho trình đọc màn hình. |
| `onCopyOrderCode` | `() => void` | Optional. FE đã copy clipboard trước, callback để báo toast. |
| `mark` | React node | Optional. Bỏ qua thì FE vẽ mark theo `status`. |

Màu theo status:

| `status` | Tiêu đề | Số thuê bao trong câu |
|---|---|---|
| `processing` | màu chữ thường | đỏ thương hiệu |
| `success` | xanh thành công | đỏ thương hiệu |
| `error` | đỏ lỗi, `role="alert"` | đỏ thương hiệu |

```tsx
<RequestResultCard
  status="success"
  title="Xác thực đổi máy thành công"
  message={[
    { text: 'Thuê bao ' },
    { text: '0983 123 452', emphasized: true },
    { text: ' xác thực đổi máy thành công.' },
  ]}
  orderLabel="Mã đơn hàng"
  orderCode="123456789"
  orderRequired
  copyLabel="Chia sẻ mã đơn hàng"
/>
```

Gợi ý payload sau submit:

```json
{
  "status": "processing",
  "orderCode": "123456789",
  "subscriberNo": "0983123452"
}
```

`error` ở đây là kết quả nghiệp vụ (xác thực không thành công), không phải HTTP 4xx/5xx. Lỗi mạng và lỗi hệ thống đi nhánh `BccsError` + `showError`, không nhét vào `status: "error"` của thẻ này.

Xem UI: `#/result`, `#/result?status=success`, `#/result?status=error`.

---

## Việc FE còn để trống khi backend có API

1. Thay ba bộ preview (màn nhận diện, danh sách đơn, chi tiết, kết quả) bằng một response theo đơn.
2. `onSelect(methodId)` gọi API tạo phiên, rồi điều hướng.
3. Danh sách đơn nhận phân trang và bộ lọc thật. Chip lọc hiện chưa gửi filter.
4. Chi tiết đơn tải theo `id`, không theo `?status=` và `?photos=`.
5. Copy mã đơn: nếu clipboard lỗi, page gọi toast. Component không tự hiện lỗi.
