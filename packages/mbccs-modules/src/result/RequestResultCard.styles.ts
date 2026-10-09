import type { CSSProperties } from 'react'
import { color, radius, spacing, textStyle } from '@mbccs/design-system/foundation'
import {
  RequestResultStatus,
  type RequestResultStatus as ResultStatus,
} from './RequestResultCard.types.js'

export function resolveResultCardStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: spacing.xl,
    width: '100%',
    padding: spacing.xl,
    borderRadius: radius.xl,
    background: color.background.neutrals.primary,
  }
}

export function resolveResultCopyStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    gap: spacing.lg,
    width: '100%',
  }
}

export function resolveResultMessageStyle(): CSSProperties {
  return {
    ...textStyle('textSm'),
    margin: 0,
    textAlign: 'center',
    color: color.text.neutrals.secondary,
  }
}

export function resolveResultOrderStyle(): CSSProperties {
  return {
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    gap: spacing.md,
    width: '100%',
    padding: `${spacing.md}px ${spacing.xl}px`,
    borderRadius: radius.xl2,
    background: color.background.neutrals.secondary,
    border: `1px solid ${color.border.neutrals.divider}`,
  }
}

export function resolveResultOrderTextStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    minWidth: 0,
    flex: 1,
  }
}

export function resolveResultLabelRowStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.xxs,
  }
}

export function resolveResultTitleColor(status: ResultStatus): string {
  if (status === RequestResultStatus.Success) return color.text.semantics.success.primary
  if (status === RequestResultStatus.Error) return color.text.semantics.error.primary
  return color.text.neutrals.primary
}

export function resolveResultEmphasisColor(status: ResultStatus): string {
  if (status === RequestResultStatus.Processing) return color.text.brands.primary
  return color.text.semantics.error.primary
}
