import { createParser, Safe, type InferParser } from '@mbccs/core/parse'
import type { CustomerType } from './ocr.js'

/** Thông tin nhân diện nfc */
export const identityDataSchema = {
  /** Giá trị trường mrz */
  mrz: Safe.string,
  /** Ảnh chân dung, truyền định dạng base64 */
  faceImage: Safe.string,
  /** Số giấy tờ */
  cardNumber: Safe.string,
  /** Ngày sinh */
  dateOfBirth: Safe.string,
  /** Ngày cấp */
  issueDate: Safe.string,
  /** previousNumber */
  previousNumber: Safe.string,
  /** Họ tên */
  name: Safe.string,
  /** Giới tính */
  sex: Safe.string,
  /** Quốc tịch */
  nationality: Safe.string,
  /** Tôn giáo */
  religion: Safe.string,
  /** Quê quán */
  hometown: Safe.string,
  /** Địa chỉ thường trú */
  address: Safe.string,
  /** Ngày hết hạn */
  expireDate: Safe.string,
  /** partnerName */
  partnerName: Safe.string,
  /** Họ tên bố */
  fatherName: Safe.string,
  /** Họ tên mẹ */
  motherName: Safe.string,
}

export const identityDataParser = createParser(identityDataSchema)

export type IdentityData = InferParser<typeof identityDataSchema>

export const nfcRequestSchema = {
  /** Chuỗi mã hóa nfc */
  dataVerifyObject: Safe.string,
  /** Chuỗi mã hóa nfc */
  dgCert: Safe.string,
  /** Ảnh chân dung nfc */
  nfcPortrait: Safe.string,
  identityData: (val: unknown) => identityDataParser.safe(val),
}

export const nfcRequestParser = createParser(nfcRequestSchema)

export type NfcRequest = InferParser<typeof nfcRequestSchema>

/** Request body may omit unused fields (`Partial` of schema-inferred model). */
export interface ValidateNfcArgs {
  body: Partial<NfcRequest>
  customerType?: CustomerType
}
