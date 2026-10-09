import { useId } from 'react'
import { IcChevronRight } from '@mbccs/design-system/assets/icons'
import { BccsText } from '@mbccs/design-system/components'
import { color } from '@mbccs/design-system/foundation'
import { BccsClickable } from '@mbccs/design-system/interaction'
import {
  resolveMethodCardStyle,
  resolveMethodCopyStyle,
  resolveMethodIconStyle,
  resolveMethodLabelStyle,
  resolveMethodListStyle,
  resolveMethodOptionStyle,
} from './identity.styles.js'
import type {
  IdentityMethodOption,
  IdentityMethodPickerProps,
} from './identity.types.js'

const CHEVRON_SIZE = 20

function MethodOptionButton({
  option,
  onSelect,
}: {
  option: IdentityMethodOption
  onSelect: (id: string) => void
}) {
  return (
    <BccsClickable
      aria-label={option.title}
      style={resolveMethodOptionStyle()}
      onClick={() => onSelect(option.id)}
    >
      <span style={resolveMethodIconStyle()}>{option.icon}</span>
      <span style={resolveMethodCopyStyle()}>
        <BccsText
          text={option.title}
          role="headingXs"
          color={color.text.neutrals.primary}
        />
        <BccsText
          text={option.description}
          role="textXs"
          color={color.text.neutrals.secondary}
        />
      </span>
      <IcChevronRight width={CHEVRON_SIZE} height={CHEVRON_SIZE} aria-hidden />
    </BccsClickable>
  )
}

/**
 * Shared identity-method picker (Figma: Nghiệp vụ xác nhận thông tin khách hàng).
 * Callers supply the options for the current customer segment.
 */
export function IdentityMethodPicker({
  label,
  required = false,
  options,
  onSelect,
}: IdentityMethodPickerProps) {
  const labelId = useId()

  return (
    <section style={resolveMethodCardStyle()}>
      <div id={labelId} style={resolveMethodLabelStyle()}>
        <BccsText
          text={label}
          role="textXs"
          color={color.text.neutrals.quaternary}
        />
        {required ? (
          <span aria-hidden>
            <BccsText
              text="*"
              role="textXs"
              weight="semibold"
              color={color.text.brands.primary}
            />
          </span>
        ) : null}
      </div>
      <ul
        role="list"
        aria-labelledby={labelId}
        aria-required={required || undefined}
        style={resolveMethodListStyle()}
      >
        {options.map((option) => (
          <li key={option.id} style={{ display: 'flex' }}>
            <MethodOptionButton option={option} onSelect={onSelect} />
          </li>
        ))}
      </ul>
    </section>
  )
}
