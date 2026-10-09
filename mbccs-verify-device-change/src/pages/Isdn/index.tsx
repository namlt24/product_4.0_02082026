import { useCallback, useEffect, useState } from 'react'
import { BccsText } from '@mbccs/design-system/components'
import { color, spacing } from '@mbccs/design-system/foundation'
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import { useLoading } from '@mbccs/runtime/feedback/loading'
import { useTranslation } from '@/i18n'
import { lk } from '@/i18n/key'
import { PageFrame } from '@/layout/PageFrame'
import { IphoneKeypad } from './IphoneKeypad'
import {
  ISDN_MAX_LENGTH,
  formatIsdn,
  resolveIsdnFieldStyle,
  resolveIsdnHintStyle,
} from './isdn.styles'

/**
 * Required ISDN entry. Tapping the field opens an iPhone-style number screen.
 * Continue closes that screen and shows the app loading overlay.
 */
export default function IsdnPage() {
  const { t } = useTranslation()
  const { showLoading } = useLoading()
  const [isdn, setIsdn] = useState('')
  const [padOpen, setPadOpen] = useState(false)

  useEffect(() => {
    document.title = t(lk.isdn.title)
    void wvNavigationBar.update({ title: t(lk.isdn.title) })
  }, [t])

  const appendDigit = useCallback((digit: string) => {
    setIsdn((current) => (current.length >= ISDN_MAX_LENGTH ? current : current + digit))
  }, [])

  const deleteDigit = useCallback(() => {
    setIsdn((current) => current.slice(0, -1))
  }, [])

  const continueFromPad = useCallback(() => {
    if (isdn.length === 0) return
    setPadOpen(false)
    showLoading({ message: t(lk.isdn.loading) })
  }, [isdn.length, showLoading, t])

  return (
    <PageFrame pad="regular" paddingTop={spacing.xl} paddingBottom={spacing.xl} gap={spacing.lg}>
      <button type="button" onClick={() => setPadOpen(true)} style={resolveIsdnFieldStyle()}>
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: spacing.xxs }}>
          <BccsText
            as="span"
            role="textXs"
            text={t(lk.isdn.fieldLabel)}
            color={color.text.neutrals.quaternary}
          />
          <span aria-hidden>
            <BccsText as="span" role="textXs" text="*" color={color.text.brands.primary} />
          </span>
        </span>
        <BccsText
          as="span"
          role="textSm"
          text={isdn.length > 0 ? formatIsdn(isdn) : t(lk.isdn.placeholder)}
          color={isdn.length > 0 ? color.text.neutrals.primary : color.text.neutrals.quaternary}
        />
      </button>
      <p style={resolveIsdnHintStyle()}>{t(lk.isdn.required)}</p>
      {padOpen ? (
        <IphoneKeypad
          title={t(lk.isdn.keypadTitle)}
          value={isdn}
          placeholder={t(lk.isdn.placeholder)}
          continueLabel={t(lk.isdn.continue)}
          deleteLabel={t(lk.isdn.delete)}
          onDigit={appendDigit}
          onDelete={deleteDigit}
          onContinue={continueFromPad}
          onDismiss={() => setPadOpen(false)}
        />
      ) : null}
    </PageFrame>
  )
}
