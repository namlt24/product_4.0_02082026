import { BccsText } from '@mbccs/design-system/components'
import { color } from '@mbccs/design-system/foundation'
import {
  resolveCustomerAvatarStyle,
  resolveCustomerSummaryStyle,
  resolveCustomerTextStyle,
} from './identity.styles.js'
import type { CustomerSummaryCardProps } from './identity.types.js'

/**
 * Shared customer summary (Figma: Block Khách hàng).
 * Initials, primary line, and customer-type description.
 */
export function CustomerSummaryCard({
  initials,
  title,
  description,
}: CustomerSummaryCardProps) {
  return (
    <section style={resolveCustomerSummaryStyle()}>
      <div style={resolveCustomerAvatarStyle()} aria-hidden>
        <BccsText
          text={initials}
          role="textLg"
          weight="semibold"
          color={color.text.neutrals.onBrand}
        />
      </div>
      <div style={resolveCustomerTextStyle()}>
        <BccsText
          text={title}
          role="headingXs"
          color={color.text.neutrals.primary}
        />
        <BccsText
          text={description}
          role="textXs"
          color={color.text.neutrals.secondary}
        />
      </div>
    </section>
  )
}
