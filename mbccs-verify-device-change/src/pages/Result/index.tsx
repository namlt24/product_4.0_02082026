import { useEffect, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { spacing } from '@mbccs/design-system/foundation'
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import {
  RequestResultCard,
  RequestResultStatus,
  type RequestResultStatus as ResultStatus,
  type RequestResultTextPart,
} from '@mbccs/modules-bccs/result'
import { useTranslation } from '@/i18n'
import { lk } from '@/i18n/key'
import { PageFrame } from '@/layout/PageFrame'

/** Preview payload until the submit API returns an order. */
const PREVIEW_RESULT = {
  phone: '0983 123 452',
  orderCode: '123456789',
} as const

const STATUSES = new Set<string>(Object.values(RequestResultStatus))

function parseResultStatus(value: string | null): ResultStatus {
  if (value != null && STATUSES.has(value)) return value as ResultStatus
  return RequestResultStatus.Processing
}

function buildMessage(
  status: ResultStatus,
  phone: string,
  t: (key: string) => string,
): RequestResultTextPart[] {
  if (status === RequestResultStatus.Processing) {
    return [
      { text: t(lk.verifyDevice.processingBefore) },
      { text: phone, emphasized: true },
      { text: t(lk.verifyDevice.processingAfter) },
    ]
  }

  const after =
    status === RequestResultStatus.Success
      ? t(lk.verifyDevice.successAfter)
      : t(lk.verifyDevice.errorAfter)

  return [
    { text: t(lk.verifyDevice.subscriberPrefix) },
    { text: phone, emphasized: true },
    { text: after },
  ]
}

function titleKey(status: ResultStatus): string {
  if (status === RequestResultStatus.Success) return lk.verifyDevice.successTitle
  if (status === RequestResultStatus.Error) return lk.verifyDevice.errorTitle
  return lk.verifyDevice.processingTitle
}

/**
 * Result of "Xác thực đổi máy". `?status=processing|success|error` selects the variant.
 */
export default function ResultPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const status = parseResultStatus(searchParams.get('status'))
  const message = useMemo(
    () => buildMessage(status, PREVIEW_RESULT.phone, t),
    [status, t],
  )

  useEffect(() => {
    document.title = t(lk.verifyDevice.title)
    void wvNavigationBar.update({ title: t(lk.verifyDevice.title) })
  }, [t])

  return (
    <PageFrame paddingTop={spacing.xl} paddingBottom={spacing.xl}>
      <RequestResultCard
        status={status}
        title={t(titleKey(status))}
        message={message}
        orderLabel={t(lk.verifyDevice.orderLabel)}
        orderCode={PREVIEW_RESULT.orderCode}
        orderRequired
        copyLabel={t(lk.verifyDevice.copyOrder)}
      />
    </PageFrame>
  )
}
