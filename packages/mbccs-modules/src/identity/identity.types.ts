import type { ReactNode } from 'react'

export type CustomerSummaryCardProps = {
  initials: string
  title: string
  description: string
}

export type IdentityMethodOption = {
  id: string
  icon: ReactNode
  title: string
  description: string
}

export type IdentityMethodPickerProps = {
  label: string
  required?: boolean
  options: readonly IdentityMethodOption[]
  onSelect: (id: string) => void
}
