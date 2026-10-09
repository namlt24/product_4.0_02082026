import { useEffect, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { spacing } from '@mbccs/design-system/foundation'
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import {
  CustomerSummaryCard,
  IdentityMethodPicker,
} from '@mbccs/modules-bccs/identity'
import { useTranslation } from '@/i18n'
import { lk } from '@/i18n/key'
import { PageFrame } from '@/layout/PageFrame'
import {
  PREVIEW_SUBSCRIBER,
  parseCustomerSegment,
  resolveIdentityContent,
} from './identityMethods'

/**
 * Verify-device identity step.
 * Customer segment selects which Figma variant to show:
 * domestic individual, foreign individual, or the two organization cases.
 */
export default function HomePage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const segment = parseCustomerSegment(searchParams.get('segment'))
  const content = useMemo(
    () => resolveIdentityContent(segment, t),
    [segment, t],
  )

  useEffect(() => {
    document.title = t(lk.verifyDevice.title)
    void wvNavigationBar.update({ title: t(lk.verifyDevice.title) })
  }, [t])

  return (
    <PageFrame pad="regular" paddingTop={spacing.xl} paddingBottom={spacing.xl} gap={spacing.lg}>
      <CustomerSummaryCard
        initials={PREVIEW_SUBSCRIBER.initials}
        title={PREVIEW_SUBSCRIBER.phone}
        description={content.description}
      />
      <IdentityMethodPicker
        label={t(lk.verifyDevice.methodLabel)}
        required
        options={content.methods}
        onSelect={() => undefined}
      />
    </PageFrame>
  )
}
