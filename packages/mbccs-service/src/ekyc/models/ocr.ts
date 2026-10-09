import { createParser, Safe, Strict, type InferParser } from '@mbccs/core/parse'
import { businessLicenseResponseParser } from './businessLicense.js'

export type CustomerType =
  | 'REPRESENTATIVE'
  | 'PERSONAL'
  | 'BUSINESS'
  | 'GUARDIAN'
  | 'TRANSFER_PERSONAL'
  | 'TRANSFER_BUSINESS'
  | 'TRANSFER_REPRESENTATIVE'
  | 'TRANSFER_CUSTOMER'

export type ImageType = 'FACE' | 'FRONT' | 'BACK'
export type IdentityType = 'PASS' | 'CMT' | 'CCCD' | 'GPKD'
export type UploadType = 'GALLERY' | 'CAMERA'

export const ocrResponseSchema = {
  address: Safe.string,
  birthday: Safe.string,
  ethnicity: Safe.string,
  expiry: Safe.string,
  id: Safe.string,
  idType: Safe.string,
  idConf: Safe.string,
  issueBy: Safe.string,
  issueDate: Safe.string,
  name: Safe.string,
  religion: Safe.string,
  sex: Safe.string,
  document: Safe.string,
  province: Safe.string,
  district: Safe.string,
  precinct: Safe.string,
  street: Safe.string,
  streetName: Safe.string,
  country: Safe.string,
  national: Safe.string,
  type: Safe.string,
  date: Safe.string,
  idCheck: Safe.string,
  uploadType: Safe.enum(['GALLERY', 'CAMERA'] as const),
  nfcRequired: Safe.enum(['REQUIRED', 'NON_REQUIRED', 'OPTIONAL'] as const),
  nfcMaximumAttempts: Safe.integer,
  isChip: Safe.string,
}

export const ocrResponseParser = createParser(ocrResponseSchema)

export type OcrResponse = InferParser<typeof ocrResponseSchema>

export const recognizePeopleIdUrlSchema = {
  frontUrl: Safe.string,
  behindUrl: Safe.string,
  faceUrl: Safe.string,
  representativeFrontUrl: Safe.string,
  representativeBehindUrl: Safe.string,
  representativeFaceUrl: Safe.string,
  businessLicenseUrl: Safe.array(Strict.string),
}

export const recognizePeopleIdUrlParser = createParser(recognizePeopleIdUrlSchema)

export type RecognizePeopleIdUrl = InferParser<typeof recognizePeopleIdUrlSchema>

export const recognizeImageResponseSchema = {
  recognizePeopleIdFront: (val: unknown) => ocrResponseParser.safe(val),
  recognizePeopleIdBehind: (val: unknown) => ocrResponseParser.safe(val),
  recognizeBusinessLicense: (val: unknown) => businessLicenseResponseParser.safe(val),
  recognizePeopleId: (val: unknown) => ocrResponseParser.safe(val),
  recognizePeopleIdUrl: (val: unknown) => recognizePeopleIdUrlParser.safe(val),
}

export const recognizeImageResponseParser = createParser(recognizeImageResponseSchema)

export type RecognizeImageResponse = InferParser<typeof recognizeImageResponseSchema>

export interface OcrArgs {
  image: Blob | File | string
  imageType: ImageType
  identityType: IdentityType
  uploadType: UploadType
  customerType?: CustomerType
  actionCode?: string
}
