import type { ReactNode } from 'react'

/** Value color on a detail row. */
export const DetailValueTone = {
  Default: 'default',
  Brand: 'brand',
  Muted: 'muted',
} as const

export type DetailValueTone = (typeof DetailValueTone)[keyof typeof DetailValueTone]

/** Status pill shared by order list and order detail. */
export const DetailStatus = {
  InProgress: 'inProgress',
  Completed: 'completed',
  Cancelled: 'cancelled',
} as const

export type DetailStatus = (typeof DetailStatus)[keyof typeof DetailStatus]

export type DetailField = {
  label: string
  value?: string
  tone?: DetailValueTone
  status?: DetailStatus
  statusLabel?: string
}

export type DetailSection = {
  id: string
  title: string
  icon: ReactNode
  fields: readonly DetailField[]
}

export type DetailMedia = {
  id: string
  title: string
  icon: ReactNode
  src: string
  alt: string
  height: number
}

export type DetailViewProps = {
  sections: readonly DetailSection[]
  media?: readonly DetailMedia[]
}

/** Portrait frame height from the detail screens. */
export const DETAIL_PORTRAIT_HEIGHT = 240

/** Passport frame height from the detail screens. */
export const DETAIL_DOCUMENT_HEIGHT = 428

/** Content width the photo heights were designed against. */
export const DETAIL_MEDIA_FRAME_WIDTH = 345
