import { useEffect } from 'react'
import { BccsText, BccsButton, BccsButtonVariant } from '@mbccs/design-system/components'
import { color, textStyle } from '@mbccs/design-system/foundation'
import { BccsClickable } from '@mbccs/design-system/interaction'
import {
  formatIsdn,
  resolveKeyLettersStyle,
  resolveKeyStyle,
  resolveKeypadDisplayStyle,
  resolveKeypadFooterStyle,
  resolveKeypadGridStyle,
  resolveKeypadScreenStyle,
} from './isdn.styles'

const DIGIT_KEYS = [
  { digit: '1', letters: '' },
  { digit: '2', letters: 'ABC' },
  { digit: '3', letters: 'DEF' },
  { digit: '4', letters: 'GHI' },
  { digit: '5', letters: 'JKL' },
  { digit: '6', letters: 'MNO' },
  { digit: '7', letters: 'PQRS' },
  { digit: '8', letters: 'TUV' },
  { digit: '9', letters: 'WXYZ' },
] as const

type IphoneKeypadProps = {
  title: string
  value: string
  placeholder: string
  continueLabel: string
  deleteLabel: string
  onDigit: (digit: string) => void
  onDelete: () => void
  onContinue: () => void
  onDismiss: () => void
}

function DeleteIcon() {
  return (
    <svg width="28" height="20" viewBox="0 0 28 20" fill="none" aria-hidden>
      <path
        d="M9.2 1.5h13.3A3.5 3.5 0 0 1 26 5v10a3.5 3.5 0 0 1-3.5 3.5H9.2a3.5 3.5 0 0 1-2.6-1.16L1.8 11.4a2 2 0 0 1 0-2.8l4.8-5.94A3.5 3.5 0 0 1 9.2 1.5Z"
        stroke="currentColor"
        strokeWidth="1.6"
      />
      <path d="M12.5 7.2 19.5 13M19.5 7.2l-7 5.8" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  )
}

/** Full-screen number entry patterned on the iPhone keypad. */
export function IphoneKeypad({
  title,
  value,
  placeholder,
  continueLabel,
  deleteLabel,
  onDigit,
  onDelete,
  onContinue,
  onDismiss,
}: IphoneKeypadProps) {
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onDismiss()
      if (event.key === 'Backspace') onDelete()
      if (/^\d$/.test(event.key)) onDigit(event.key)
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [onDelete, onDigit, onDismiss])

  return (
    <div role="dialog" aria-modal="true" aria-label={title} style={resolveKeypadScreenStyle()}>
      <div style={resolveKeypadDisplayStyle()}>
        <BccsText
          as="p"
          role="headingLg"
          text={value.length > 0 ? formatIsdn(value) : placeholder}
          color={value.length > 0 ? color.text.neutrals.primary : color.text.neutrals.quaternary}
          textAlign="center"
        />
      </div>
      <div style={resolveKeypadGridStyle()}>
        {DIGIT_KEYS.map((key) => (
          <BccsClickable key={key.digit} aria-label={key.digit} onClick={() => onDigit(key.digit)} style={resolveKeyStyle()}>
            <span style={{ ...textStyle('headingMd'), color: color.text.neutrals.primary }}>{key.digit}</span>
            <span style={resolveKeyLettersStyle()}>{key.letters || '\u00a0'}</span>
          </BccsClickable>
        ))}
        <span />
        <BccsClickable aria-label="0" onClick={() => onDigit('0')} style={resolveKeyStyle()}>
          <span style={{ ...textStyle('headingMd'), color: color.text.neutrals.primary }}>0</span>
          <span style={resolveKeyLettersStyle()}>{'\u00a0'}</span>
        </BccsClickable>
        <BccsClickable aria-label={deleteLabel} onClick={onDelete} style={resolveKeyStyle()}>
          <DeleteIcon />
        </BccsClickable>
      </div>
      <div style={resolveKeypadFooterStyle()}>
        <BccsButton
          text={continueLabel}
          variant={BccsButtonVariant.SolidPrimary}
          enabled={value.length > 0}
          onClick={onContinue}
          style={{ width: '100%' }}
        />
      </div>
    </div>
  )
}
