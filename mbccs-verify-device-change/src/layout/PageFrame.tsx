import type { CSSProperties, ReactNode } from 'react'
import { color, spacing } from '@mbccs/design-system/foundation'

type PageFrameProps = {
  children: ReactNode
  /** Top padding in px. */
  paddingTop?: number
  /** Bottom padding in px. */
  paddingBottom?: number
  /**
   * Phone horizontal padding.
   * `compact` is 8px (Figma body). `regular` is the mobile screen padding.
   * iPad mini and wider use 24px; iPad 13" uses 32px.
   */
  pad?: 'compact' | 'regular'
  gap?: number
}

/**
 * Full-bleed page canvas. Phones stay full width; iPads use a centered column.
 */
export function PageFrame({
  children,
  paddingTop = 0,
  paddingBottom = spacing.xl,
  pad = 'compact',
  gap = 0,
}: PageFrameProps) {
  const frameStyle = {
    gap,
    '--page-pad-top': `${paddingTop}px`,
    '--page-pad-bottom': `${paddingBottom}px`,
  } as CSSProperties

  return (
    <div
      style={{
        boxSizing: 'border-box',
        minHeight: '100%',
        width: '100%',
        background: color.background.neutrals.tertiary,
      }}
    >
      <main className="page-frame" data-pad={pad} style={frameStyle}>
        {children}
      </main>
    </div>
  )
}
