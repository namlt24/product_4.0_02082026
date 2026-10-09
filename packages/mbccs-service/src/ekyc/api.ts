import type { BccsResult } from '@mbccs/core/common'
import { bccsNetworkClient } from '@mbccs/core/network'
import { endpoints } from './endpoints.js'
import {
  generalResponseParser,
  recognizeImageResponseParser,
  type GeneralResponse,
  type OcrArgs,
  type RecognizeImageResponse,
  type ValidateNfcArgs,
} from './models/index.js'

/**
 * Validates NFC identity data (`POST /api/validate-nfc`).
 *
 * @param args - NFC payload plus required `uuid` header.
 * @returns A `BccsResult` with parsed `GeneralResponse` on success.
 */
export function validateNfc({
  body,
  customerType,
}: ValidateNfcArgs): Promise<BccsResult<GeneralResponse>> {
  return bccsNetworkClient.post(endpoints.validateNfc.path, body, {
    params: customerType ? { customerType } : undefined,
    parser: generalResponseParser,
  })
}

/**
 * Uploads an identity image for OCR (`POST /api/ocr`, multipart).
 *
 * @param args - Image blob/file and OCR query parameters.
 * @returns A `BccsResult` with parsed `RecognizeImageResponse` on success.
 */
export function ocr({
  image,
  imageType,
  identityType,
  uploadType,
  customerType,
  actionCode,
}: OcrArgs): Promise<BccsResult<RecognizeImageResponse>> {
  const form = new FormData()
  form.append('image', image)

  return bccsNetworkClient.upload(endpoints.ocr.path, form, {
    params: {
      imageType,
      identityType,
      uploadType,
      ...(customerType ? { customerType } : {}),
      ...(actionCode ? { actionCode } : {}),
    },
    parser: recognizeImageResponseParser,
  })
}
