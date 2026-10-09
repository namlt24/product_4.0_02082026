import { useEffect, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { spacing } from '@mbccs/design-system/foundation'
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import {
  DETAIL_DOCUMENT_HEIGHT,
  DETAIL_PORTRAIT_HEIGHT,
  DetailBagIcon,
  DetailIdCardIcon,
  DetailScanIcon,
  DetailStatus,
  DetailUserIcon,
  DetailValueTone,
  DetailView,
  type DetailField,
  type DetailMedia,
  type DetailSection,
  type DetailStatus as Status,
} from '@mbccs/modules-bccs/detail'
import portraitSrc from '@/assets/detail-portrait.png'
import passportSrc from '@/assets/detail-passport.png'
import { useTranslation } from '@/i18n'
import { lk } from '@/i18n/key'
import { PageFrame } from '@/layout/PageFrame'

const PREVIEW = {
  customerName: 'Nguyễn Văn Bê',
  documentNo: '001313030494',
  phone: '0368888888',
  orderCode: '6196411852',
  createdAt: '10:00 | 25/12/2025',
  expiresAt: '10:00 | 27/12/2025',
  cancelledAt: '10:00 | 27/12/2025',
} as const

const STATUSES = new Set<string>(Object.values(DetailStatus))

function parseStatus(value: string | null): Status {
  if (value != null && STATUSES.has(value)) return value as Status
  return DetailStatus.Completed
}

function statusLabelKey(status: Status): string {
  if (status === DetailStatus.Completed) return lk.orders.completed
  if (status === DetailStatus.Cancelled) return lk.orders.cancelled
  return lk.orders.inProgress
}

/**
 * Order detail. `?status=inProgress|completed|cancelled` and `?photos=passport`
 * select the five Figma variants.
 */
export default function OrderDetailPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const status = parseStatus(searchParams.get('status'))
  const showPassport = searchParams.get('photos') === 'passport'

  useEffect(() => {
    document.title = t(lk.orders.detailTitle)
    void wvNavigationBar.update({ title: t(lk.orders.detailTitle) })
  }, [t])

  const sections = useMemo<DetailSection[]>(() => {
    const orderFields: DetailField[] = [
      { label: t(lk.orders.orderType), value: t(lk.verifyDevice.title) },
      { label: t(lk.orders.orderCode), value: PREVIEW.orderCode },
      {
        label: t(lk.orders.status),
        status,
        statusLabel: t(statusLabelKey(status)),
      },
      { label: t(lk.orders.createdAt), value: PREVIEW.createdAt },
      {
        label: t(lk.orders.expiresAt),
        value: PREVIEW.expiresAt,
        tone: DetailValueTone.Brand,
      },
    ]

    if (status === DetailStatus.Cancelled) {
      orderFields.push({
        label: t(lk.orders.cancelledAt),
        value: PREVIEW.cancelledAt,
        tone: DetailValueTone.Muted,
      })
    }

    return [
      {
        id: 'customer',
        title: t(lk.orders.customerSection),
        icon: <DetailUserIcon />,
        fields: [
          { label: t(lk.orders.customerName), value: PREVIEW.customerName },
          { label: t(lk.orders.documentNo), value: PREVIEW.documentNo },
          { label: t(lk.orders.contactPhone), value: PREVIEW.phone },
        ],
      },
      {
        id: 'order',
        title: t(lk.orders.orderSection),
        icon: <DetailBagIcon />,
        fields: orderFields,
      },
    ]
  }, [status, t])

  const media = useMemo<DetailMedia[]>(() => {
    const items: DetailMedia[] = [
      {
        id: 'portrait',
        title: t(lk.orders.portraitTitle),
        icon: <DetailScanIcon />,
        src: portraitSrc,
        alt: t(lk.orders.portraitAlt),
        height: DETAIL_PORTRAIT_HEIGHT,
      },
    ]

    if (showPassport) {
      items.push({
        id: 'passport',
        title: t(lk.orders.passportTitle),
        icon: <DetailIdCardIcon />,
        src: passportSrc,
        alt: t(lk.orders.passportAlt),
        height: DETAIL_DOCUMENT_HEIGHT,
      })
    }

    return items
  }, [showPassport, t])

  return (
    <PageFrame paddingBottom={spacing.xl3}>
      <DetailView sections={sections} media={media} />
    </PageFrame>
  )
}
