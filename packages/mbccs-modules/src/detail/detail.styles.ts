import type { CSSProperties } from 'react'
import { color, radius, spacing, textStyle } from '@mbccs/design-system/foundation'
import {
  DETAIL_MEDIA_FRAME_WIDTH,
  DetailValueTone,
  type DetailValueTone as Tone,
} from './detail.types.js'

/** Photo matte behind a clipped capture. Matches the detail frames. */
const photoMatte = '#A0A8AB'

export function resolveDetailStackStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xl,
    boxSizing: 'border-box',
    width: '100%',
  }
}

export function resolveDetailCardStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xl,
    boxSizing: 'border-box',
    width: '100%',
    padding: spacing.xl,
    borderRadius: radius.xl4,
    background: color.background.neutrals.primary,
  }
}

export function resolveDetailHeadingStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.lg,
    margin: 0,
    color: color.icon.neutrals.primary,
  }
}

export function resolveDetailFieldsStyle(): CSSProperties {
  return {
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.xs,
    margin: 0,
  }
}

export function resolveDetailRowStyle(): CSSProperties {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
    minHeight: spacing.xl5,
    margin: 0,
  }
}

export function resolveDetailLabelStyle(): CSSProperties {
  return {
    flex: '1 1 40%',
    minWidth: 0,
    margin: 0,
  }
}

export function resolveDetailValueCellStyle(): CSSProperties {
  return {
    flex: '1 1 40%',
    minWidth: 0,
    margin: 0,
    display: 'flex',
    justifyContent: 'flex-end',
  }
}

export function resolveDetailDividerStyle(): CSSProperties {
  return {
    height: 1,
    margin: 0,
    border: 'none',
    background: color.border.neutrals.divider,
  }
}

export function resolveDetailValueStyle(tone: Tone = DetailValueTone.Default): CSSProperties {
  const valueColor =
    tone === DetailValueTone.Brand
      ? color.text.brands.primary
      : tone === DetailValueTone.Muted
        ? color.text.neutrals.disabled
        : color.text.neutrals.primary

  return {
    ...textStyle('textSm', { weight: 'semibold' }),
    margin: 0,
    maxWidth: '100%',
    textAlign: 'right',
    overflowWrap: 'anywhere',
    color: valueColor,
  }
}

export function resolveDetailTagStyle(background: string): CSSProperties {
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
  }
}

export function resolveDetailPhotoFrameStyle(height: number): CSSProperties {
  return {
    width: '100%',
    aspectRatio: `${DETAIL_MEDIA_FRAME_WIDTH} / ${height}`,
    borderRadius: radius.md,
    overflow: 'hidden',
    background: photoMatte,
  }
}

export function resolveDetailPhotoStyle(): CSSProperties {
  return {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    objectPosition: 'center top',
  }
}
