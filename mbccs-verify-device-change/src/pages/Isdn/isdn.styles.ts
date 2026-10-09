import type { CSSProperties } from 'react'
import { color, radius, spacing, textStyle, zIndex } from '@mbccs/design-system/foundation'

export const ISDN_MAX_LENGTH = 15

export function formatIsdn(digits: string): string {
  return digits.replace(/(\d{3})(?=\d)/g, '$1 ').trim()
}

export function resolveIsdnFieldStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    gap: spacing.xxs,
    boxSizing: 'border-box',
    width: '100%',
    minHeight: 52,
    margin: 0,
    padding: `${spacing.md}px ${spacing.xl}px`,
    borderRadius: radius.xl2,
    border: `1px solid ${color.border.neutrals.divider}`,
    background: color.background.neutrals.primary,
    textAlign: 'left',
    cursor: 'pointer',
  }
}

export function resolveIsdnHintStyle(): CSSProperties {
  return {
    ...textStyle('textXs'),
    margin: `${spacing.xs}px ${spacing.xl}px 0`,
    color: color.text.neutrals.secondary,
  }
}

export function resolveKeypadScreenStyle(): CSSProperties {
  return {
    position: 'fixed',
    inset: 0,
    zIndex: zIndex.overlay,
    display: 'flex',
    flexDirection: 'column',
    boxSizing: 'border-box',
    background: color.background.neutrals.quaternary,
    paddingBottom: 'env(safe-area-inset-bottom, 0px)',
  }
}

export function resolveKeypadDisplayStyle(): CSSProperties {
  return {
    display: 'flex',
    flex: '1 1 auto',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: spacing.md,
    boxSizing: 'border-box',
    minHeight: 0,
    padding: `${spacing.xl5}px ${spacing.xl}px ${spacing.xl3}px`,
  }
}

export function resolveKeypadGridStyle(): CSSProperties {
  return {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    gap: spacing.md,
    boxSizing: 'border-box',
    width: '100%',
    maxWidth: 420,
    margin: '0 auto',
    padding: `0 ${spacing.md}px`,
  }
}

export function resolveKeyStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    boxSizing: 'border-box',
    minHeight: 64,
    margin: 0,
    padding: `${spacing.sm}px ${spacing.md}px`,
    border: 'none',
    borderRadius: radius.lg,
    background: color.background.neutrals.primary,
    color: color.text.neutrals.primary,
    cursor: 'pointer',
  }
}

export function resolveKeyLettersStyle(): CSSProperties {
  return {
    ...textStyle('textXs'),
    letterSpacing: '0.12em',
    color: color.text.neutrals.secondary,
  }
}

export function resolveKeypadFooterStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    width: '100%',
    maxWidth: 420,
    margin: '0 auto',
    padding: `${spacing.xl}px ${spacing.xl}px ${spacing.xl}px`,
  }
}
