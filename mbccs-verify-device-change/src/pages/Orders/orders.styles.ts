import type { CSSProperties } from 'react'
import { color, colorPrimitives, radius, spacing, textStyle } from '@mbccs/design-system/foundation'

export function resolveOrdersToolbarStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xl,
  }
}

export function resolveOrderSearchStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.md,
    width: '100%',
    boxSizing: 'border-box',
    padding: `${spacing.md}px ${spacing.xl}px`,
    borderRadius: radius.xl2,
    background: colorPrimitives.gray.shade50,
    border: `1px solid ${color.border.neutrals.divider}`,
    color: color.icon.neutrals.primary,
  }
}

export function resolveOrderSearchInputStyle(hasValue: boolean): CSSProperties {
  return {
    ...textStyle('textSm'),
    flex: 1,
    minWidth: 0,
    margin: 0,
    padding: 0,
    border: 'none',
    outline: 'none',
    background: 'transparent',
    color: hasValue ? color.text.neutrals.primary : color.text.neutrals.quaternary,
  }
}

export function resolveOrderChipRowStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.md,
  }
}

export function resolveFilterChipStyle(): CSSProperties {
  return {
    display: 'inline-flex',
    alignItems: 'center',
    flexShrink: 0,
    boxSizing: 'border-box',
    padding: `${spacing.md}px ${spacing.lg}px`,
    borderRadius: radius.full,
    background: color.background.neutrals.primary,
    border: `1px solid ${color.border.neutrals.quaternary}`,
    color: color.text.neutrals.primary,
  }
}

export function resolveFilterChipLabelStyle(): CSSProperties {
  return {
    ...textStyle('textSm'),
    display: 'inline-flex',
    alignItems: 'center',
    height: spacing.xl3,
    padding: `0 ${spacing.md}px`,
    whiteSpace: 'nowrap',
  }
}

export function resolveOrderListStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xl,
    margin: 0,
    padding: 0,
    listStyle: 'none',
  }
}

export function resolveOrderCardStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    boxSizing: 'border-box',
    width: '100%',
    borderRadius: radius.xl4,
    background: color.background.neutrals.primary,
    overflow: 'hidden',
  }
}

export function resolveOrderCardHeaderStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
    boxSizing: 'border-box',
    padding: `${spacing.lg}px ${spacing.xl}px`,
  }
}

export function resolveOrderCardBodyStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xl,
    boxSizing: 'border-box',
    padding: `${spacing.md}px ${spacing.xl}px`,
  }
}

export function resolveOrderCardRowStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
  }
}

export function resolveOrderCardFooterStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: spacing.md,
    boxSizing: 'border-box',
    minHeight: spacing.xl6,
    padding: `${spacing.md}px ${spacing.xl}px`,
    color: color.icon.neutrals.primary,
  }
}

export function resolveOrderMetaStyle(): CSSProperties {
  return {
    display: 'inline-flex',
    alignItems: 'center',
    gap: spacing.md,
    minWidth: 0,
  }
}

export function resolveOrderTagStyle(background: string): CSSProperties {
  return {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    boxSizing: 'border-box',
    height: spacing.xl2,
    padding: `0 ${spacing.lg}px`,
    borderRadius: radius.full,
    background,
    color: color.text.neutrals.white,
  }
}
