import type { ReactNode } from 'react'
import { color } from '@mbccs/design-system/foundation'
import { BccsClickable } from '@mbccs/design-system/interaction'
import { resolveFilterChipLabelStyle, resolveFilterChipStyle } from './orders.styles'

type FilterChipProps = {
  label: string
  value?: string
  trailingIcon: ReactNode
  onClick: () => void
}

/** Pill filter. `value` is painted in the brand color after the label. */
export function FilterChip({ label, value, trailingIcon, onClick }: FilterChipProps) {
  return (
    <BccsClickable onClick={onClick} style={resolveFilterChipStyle()}>
      <span style={resolveFilterChipLabelStyle()}>
        <span style={{ color: value ? color.text.neutrals.primary : color.text.brands.primary }}>
          {label}
        </span>
        {value ? <span style={{ color: color.text.brands.primary }}>{value}</span> : null}
      </span>
      <span
        style={{
          display: 'inline-flex',
          color: value ? color.icon.neutrals.primary : color.icon.brands.primary,
        }}
      >
        {trailingIcon}
      </span>
    </BccsClickable>
  )
}
