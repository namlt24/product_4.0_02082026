import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { BccsChip, BccsChipSize, BccsEmpty } from '@mbccs/design-system/components'
import { spacing } from '@mbccs/design-system/foundation'
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import { useTranslation } from '@/i18n'
import { lk } from '@/i18n/key'
import { PageFrame } from '@/layout/PageFrame'
import { FilterChip } from './FilterChip'
import { OrderCard, OrderStatus, type OrderCardModel } from './OrderCard'
import { ChevronDownIcon, FilterIcon, SearchIcon } from './icons'
import {
  resolveOrderChipRowStyle,
  resolveOrderListStyle,
  resolveOrderSearchInputStyle,
  resolveOrderSearchStyle,
  resolveOrdersToolbarStyle,
} from './orders.styles'

/** Preview rows from the Quản lý đơn hàng frame until the order API exists. */
const PREVIEW_ORDERS = [
  { id: 'in-progress', status: OrderStatus.InProgress },
  { id: 'completed', status: OrderStatus.Completed },
  { id: 'cancelled', status: OrderStatus.Cancelled },
] as const

const PREVIEW_DETAIL = {
  phone: '0981234567',
  customerName: 'Nguyễn Văn Bê',
  orderCode: '6196411852',
  createdAt: '18:00 | 01/01/2026',
} as const

function statusLabelKey(status: (typeof PREVIEW_ORDERS)[number]['status']): string {
  if (status === OrderStatus.Completed) return lk.orders.completed
  if (status === OrderStatus.Cancelled) return lk.orders.cancelled
  return lk.orders.inProgress
}

function matchesQuery(order: OrderCardModel, query: string): boolean {
  const haystack = [order.serviceName, order.phone, order.customerName, order.orderCode]
    .join(' ')
    .toLowerCase()
  return haystack.includes(query.trim().toLowerCase())
}

/**
 * Order management. Search filters the preview list by order code, phone, or customer name.
 */
export default function OrdersPage() {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')

  useEffect(() => {
    document.title = t(lk.orders.title)
    void wvNavigationBar.update({ title: t(lk.orders.title) })
  }, [t])

  const orders = useMemo<OrderCardModel[]>(
    () =>
      PREVIEW_ORDERS.map((item) => ({
        id: item.id,
        status: item.status,
        statusLabel: t(statusLabelKey(item.status)),
        serviceName: t(lk.verifyDevice.title),
        phoneLabel: t(lk.orders.phone),
        customerLabel: t(lk.orders.customer),
        ...PREVIEW_DETAIL,
      })),
    [t],
  )

  const visibleOrders = orders.filter((order) => matchesQuery(order, query))

  return (
    <PageFrame paddingBottom={spacing.xl} gap={spacing.xl}>
      <div style={resolveOrdersToolbarStyle()}>
        <label style={resolveOrderSearchStyle()}>
          <SearchIcon />
          <input
            className="order-search-input"
            type="search"
            value={query}
            placeholder={t(lk.orders.searchPlaceholder)}
            aria-label={t(lk.orders.searchPlaceholder)}
            onChange={(event) => setQuery(event.target.value)}
            style={resolveOrderSearchInputStyle(query.length > 0)}
          />
        </label>
        <div className="order-chip-row" style={resolveOrderChipRowStyle()}>
          <FilterChip
            label={t(lk.orders.filter)}
            trailingIcon={<FilterIcon />}
            onClick={() => undefined}
          />
          <FilterChip
            label={t(lk.orders.timePrefix)}
            value={t(lk.orders.timeValue)}
            trailingIcon={<ChevronDownIcon />}
            onClick={() => undefined}
          />
          <FilterChip
            label={t(lk.orders.statusPrefix)}
            value={t(lk.orders.statusValue)}
            trailingIcon={<ChevronDownIcon />}
            onClick={() => undefined}
          />
          <FilterChip
            label={t(lk.orders.servicePrefix)}
            value={t(lk.orders.serviceValue)}
            trailingIcon={<ChevronDownIcon />}
            onClick={() => undefined}
          />
          <BccsChip
            text={t(lk.orders.clearFilters)}
            size={BccsChipSize.Sm}
            onClick={() => setQuery('')}
            style={{ flexShrink: 0, whiteSpace: 'nowrap' }}
          />
        </div>
      </div>
      {visibleOrders.length > 0 ? (
        <ul style={resolveOrderListStyle()}>
          {visibleOrders.map((order) => (
            <li key={order.id}>
              <Link
                to={`/orders/detail?status=${order.status}`}
                style={{ color: 'inherit', textDecoration: 'none' }}
              >
                <OrderCard order={order} />
              </Link>
            </li>
          ))}
        </ul>
      ) : (
        <BccsEmpty label={t(lk.orders.empty)} description={t(lk.orders.emptyDescription)} />
      )}
    </PageFrame>
  )
}
