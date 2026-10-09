import type { ReactNode } from 'react'

/** Illustration size of the result mark in Figma (120×120). */
export const REQUEST_RESULT_MARK_SIZE = 120

export const RequestResultStatus = {
  Processing: 'processing',
  Success: 'success',
  Error: 'error',
} as const

export type RequestResultStatus =
  (typeof RequestResultStatus)[keyof typeof RequestResultStatus]

/** One run of the result message. `emphasized` styles the subscriber number. */
export type RequestResultTextPart = {
  text: string
  emphasized?: boolean
}

export type RequestResultCardProps = {
  status: RequestResultStatus
  title: string
  message: readonly RequestResultTextPart[]
  orderLabel: string
  orderCode: string
  orderRequired?: boolean
  copyLabel: string
  onCopyOrderCode?: () => void
  mark?: ReactNode
}
