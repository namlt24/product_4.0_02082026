import type { CSSProperties } from 'react'
import { color, radius, spacing } from '@mbccs/design-system/foundation'

export function resolveCustomerSummaryStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    gap: spacing.lg,
    width: '100%',
    padding: spacing.xl,
    borderRadius: radius.xl2,
    background: color.background.neutrals.primary,
    border: `1px solid ${color.border.neutrals.quaternary}`,
  }
}

export function resolveCustomerAvatarStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: spacing.xl6,
    height: spacing.xl6,
    borderRadius: radius.full,
    background: color.background.brands.primary,
    border: `1px solid ${color.border.neutrals.quaternary}`,
  }
}

export function resolveCustomerTextStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    minWidth: 0,
    flex: 1,
  }
}

export function resolveMethodCardStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.lg,
    width: '100%',
    padding: spacing.xl,
    borderRadius: radius.xl4,
    background: color.background.neutrals.primary,
  }
}

export function resolveMethodLabelStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.xxs,
  }
}

export function resolveMethodListStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.lg,
    margin: 0,
    padding: 0,
    listStyle: 'none',
  }
}

export function resolveMethodOptionStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    gap: spacing.xl,
    width: '100%',
    minHeight: spacing.xl7,
    padding: spacing.xl,
    borderRadius: radius.xl2,
    background: color.background.neutrals.primary,
    border: `1px solid ${color.border.neutrals.quaternary}`,
    color: color.icon.neutrals.primary,
  }
}

export function resolveMethodCopyStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    minWidth: 0,
    flex: 1,
  }
}

export function resolveMethodIconStyle(): CSSProperties {
  return {
    display: 'inline-flex',
    flexShrink: 0,
    width: spacing.xl3,
    height: spacing.xl3,
  }
}
